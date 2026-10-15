package com.gucsantana.mtgcollectronserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

@Service
public class ScryfallImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScryfallImportService.class);
    private static final String BULK_DATA_URL = "https://api.scryfall.com/bulk-data";
    private static final String SETS_URL = "https://api.scryfall.com/sets";
    private static final String CARD_UPSERT_SQL = """
            INSERT INTO cards
                (id, set_code, name, collector_number, rarity, prices, image_uris, card_faces, promo_types, finishes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                set_code = VALUES(set_code),
                name = VALUES(name),
                collector_number = VALUES(collector_number),
                rarity = VALUES(rarity),
                prices = VALUES(prices),
                image_uris = VALUES(image_uris),
                card_faces = VALUES(card_faces),
                promo_types = VALUES(promo_types),
                finishes = VALUES(finishes)
            """;
    private static final String SET_UPSERT_SQL = """
            INSERT INTO sets
                (code, name, released_at, set_type, card_count, digital, nonfoil_only, foil_only, icon_svg_uri)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                released_at = VALUES(released_at),
                set_type = VALUES(set_type),
                card_count = VALUES(card_count),
                digital = VALUES(digital),
                nonfoil_only = VALUES(nonfoil_only),
                foil_only = VALUES(foil_only),
                icon_svg_uri = VALUES(icon_svg_uri)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;
    private final Executor executor;
    private final HttpClient httpClient;
    private final String userAgent;
    private final int batchSize;
    private final AtomicReference<ImportStatus> status = new AtomicReference<>(
            new ImportStatus("IDLE", null, null, 0, 0, null));

    public ScryfallImportService(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            CacheManager cacheManager,
            @Value("${scryfall.import.batch-size:500}") int batchSize,
            @Value("${scryfall.import.user-agent:mtgcollectronserver}") String userAgent,
            @Qualifier("scryfallImportExecutor") Executor executor) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("Scryfall import batch size must be greater than zero");
        }
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.cacheManager = cacheManager;
        this.batchSize = batchSize;
        this.userAgent = userAgent;
        this.executor = executor;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public ImportStatus getStatus() {
        return status.get();
    }

    public synchronized boolean startImport() {
        if ("RUNNING".equals(status.get().state())) {
            return false;
        }

        status.set(new ImportStatus("RUNNING", Instant.now(), null, 0, 0, null));
        try {
            executor.execute(this::runImport);
        } catch (RuntimeException exception) {
            status.set(new ImportStatus("FAILED", status.get().startedAt(), Instant.now(), 0, 0,
                    exception.getMessage()));
            throw exception;
        }
        return true;
    }

    private void runImport() {
        ImportStatus runningStatus = status.get();
        int importedSets = 0;
        int importedCards = 0;
        try {
            importedSets = importSets();
            status.set(new ImportStatus("RUNNING", runningStatus.startedAt(), null, 0, importedSets, null));
            importedCards = importCards(importedSets, runningStatus.startedAt());
            evictDatabaseCaches();
            status.set(new ImportStatus("SUCCEEDED", runningStatus.startedAt(), Instant.now(),
                    importedCards, importedSets, null));
            LOGGER.info("Scryfall import completed: {} paper cards and {} sets", importedCards, importedSets);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            markFailed(runningStatus.startedAt(), importedCards, importedSets, exception);
        } catch (IOException | RuntimeException exception) {
            markFailed(runningStatus.startedAt(), importedCards, importedSets, exception);
        }
    }

    private int importSets() throws IOException, InterruptedException {
        JsonNode response = getJson(SETS_URL);
        JsonNode data = requireArray(response, "data");
        List<SetRow> batch = new ArrayList<>(batchSize);
        int importedCount = 0;
        for (JsonNode set : data) {
            batch.add(new SetRow(
                    requiredText(set, "code"),
                    requiredText(set, "name"),
                    LocalDate.parse(requiredText(set, "released_at")),
                    requiredText(set, "set_type"),
                    requiredInt(set, "card_count"),
                    requiredBoolean(set, "digital"),
                    requiredBoolean(set, "nonfoil_only"),
                    requiredBoolean(set, "foil_only"),
                    optionalText(set, "icon_svg_uri")));
            if (batch.size() == batchSize) {
                importedCount += saveSetBatch(batch);
                batch.clear();
                updateProgress(0, importedCount);
            }
        }
        if (!batch.isEmpty()) {
            importedCount += saveSetBatch(batch);
            updateProgress(0, importedCount);
        }
        return importedCount;
    }

    private int saveSetBatch(List<SetRow> batch) {
        jdbcTemplate.batchUpdate(SET_UPSERT_SQL, batch, batchSize, (statement, set) -> {
            statement.setString(1, set.code());
            statement.setString(2, set.name());
            statement.setDate(3, Date.valueOf(set.releasedAt()));
            statement.setString(4, set.setType());
            statement.setInt(5, set.cardCount());
            statement.setBoolean(6, set.digital());
            statement.setBoolean(7, set.nonfoilOnly());
            statement.setBoolean(8, set.foilOnly());
            statement.setString(9, set.iconSvgUri());
        });
        return batch.size();
    }

    private int importCards(int importedSets, Instant startedAt) throws IOException, InterruptedException {
        String downloadUri = findDefaultCardsDownloadUri();
        try (InputStream responseBody = getStream(downloadUri);
                GZIPInputStream gzipInput = new GZIPInputStream(responseBody);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(gzipInput, StandardCharsets.UTF_8), 64 * 1024)) {
            List<CardRow> batch = new ArrayList<>(batchSize);
            int importedCount = 0;
            String line;
            long lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                CardRow cardRow;
                try {
                    JsonNode card = objectMapper.readTree(line);
                    if (!hasPaperGame(card) || isMeldResult(card)) {
                        continue;
                    }
                    cardRow = toCardRow(card);
                } catch (IOException exception) {
                    throw new IOException("Invalid Scryfall card data on bulk-data line " + lineNumber, exception);
                }
                batch.add(cardRow);
                if (batch.size() == batchSize) {
                    importedCount += saveCardBatch(batch);
                    batch.clear();
                    updateProgress(importedCount, importedSets);
                }
            }
            if (!batch.isEmpty()) {
                importedCount += saveCardBatch(batch);
                updateProgress(importedCount, importedSets);
            }
            return importedCount;
        }
    }

    private String findDefaultCardsDownloadUri() throws IOException, InterruptedException {
        JsonNode bulkData = requireArray(getJson(BULK_DATA_URL), "data");
        for (JsonNode descriptor : bulkData) {
            if ("default_cards".equals(optionalText(descriptor, "type"))) {
                String jsonlDownloadUri = optionalText(descriptor, "jsonl_download_uri");
                return jsonlDownloadUri != null ? jsonlDownloadUri : requiredText(descriptor, "download_uri");
            }
        }
        throw new IOException("Scryfall bulk-data response did not contain default_cards");
    }

    private int saveCardBatch(List<CardRow> batch) {
        jdbcTemplate.batchUpdate(CARD_UPSERT_SQL, batch, batchSize, (statement, card) -> {
            statement.setString(1, card.id());
            statement.setString(2, card.setCode());
            statement.setString(3, card.name());
            statement.setString(4, card.collectorNumber());
            statement.setString(5, card.rarity());
            statement.setString(6, card.prices());
            statement.setString(7, card.imageUris());
            statement.setString(8, card.cardFaces());
            statement.setString(9, card.promoTypes());
            statement.setString(10, card.finishes());
        });
        return batch.size();
    }

    private CardRow toCardRow(JsonNode card) throws IOException {
        return new CardRow(
                requiredText(card, "id"),
                requiredText(card, "set"),
                requiredText(card, "name"),
                requiredText(card, "collector_number"),
                requiredText(card, "rarity"),
                optionalJson(card, "prices"),
                optionalJson(card, "image_uris"),
                optionalJson(card, "card_faces"),
                optionalJson(card, "promo_types"),
                optionalJson(card, "finishes"));
    }

    private JsonNode getJson(String url) throws IOException, InterruptedException {
        try (InputStream body = getStream(url)) {
            return objectMapper.readTree(body);
        }
    }

    private InputStream getStream(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            try (InputStream body = response.body()) {
                String responseBody = new String(body.readNBytes(4096), StandardCharsets.UTF_8);
                throw new IOException("Scryfall request failed with HTTP " + response.statusCode()
                        + (responseBody.isBlank() ? "" : ": " + responseBody));
            }
        }
        return response.body();
    }

    private void evictDatabaseCaches() {
        evictCache("db-card-cache");
        evictCache("db-set-cache");
    }

    private void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    private void markFailed(Instant startedAt, int importedCards, int importedSets, Exception exception) {
        try {
            evictDatabaseCaches();
        } catch (RuntimeException cacheException) {
            exception.addSuppressed(cacheException);
        }
        LOGGER.error("Scryfall import failed", exception);
        ImportStatus progress = status.get();
        status.set(new ImportStatus("FAILED", startedAt, Instant.now(),
                Math.max(importedCards, progress.cardsImported()),
                Math.max(importedSets, progress.setsImported()), exception.getMessage()));
    }

    private void updateProgress(int importedCards, int importedSets) {
        status.set(new ImportStatus("RUNNING", status.get().startedAt(), null, importedCards, importedSets, null));
    }

    private static JsonNode requireArray(JsonNode object, String field) throws IOException {
        JsonNode value = object.path(field);
        if (!value.isArray()) {
            throw new IOException("Scryfall response is missing array field: " + field);
        }
        return value;
    }

    private static String requiredText(JsonNode object, String field) throws IOException {
        JsonNode value = object.path(field);
        if (!value.isString() || value.stringValue().isBlank()) {
            throw new IOException("Scryfall data is missing text field: " + field);
        }
        return value.stringValue();
    }

    private static String optionalText(JsonNode object, String field) {
        JsonNode value = object.path(field);
        return value.isString() ? value.stringValue() : null;
    }

    private static int requiredInt(JsonNode object, String field) throws IOException {
        JsonNode value = object.path(field);
        if (!value.canConvertToInt()) {
            throw new IOException("Scryfall data is missing integer field: " + field);
        }
        return value.intValue();
    }

    private static boolean requiredBoolean(JsonNode object, String field) throws IOException {
        JsonNode value = object.path(field);
        if (!value.isBoolean()) {
            throw new IOException("Scryfall data is missing boolean field: " + field);
        }
        return value.booleanValue();
    }

    private static String optionalJson(JsonNode object, String field) {
        JsonNode value = object.get(field);
        return value == null || value.isNull() ? null : value.toString();
    }

    private static boolean hasPaperGame(JsonNode card) throws IOException {
        JsonNode games = card.path("games");
        if (!games.isArray()) {
            throw new IOException("Scryfall card data is missing games array");
        }
        for (JsonNode game : games) {
            if ("paper".equals(game.stringValue())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMeldResult(JsonNode card) {
        JsonNode allParts = card.path("all_parts");
        if (!allParts.isArray()) {
            return false;
        }
        String cardName = optionalText(card, "name");
        for (JsonNode part : allParts) {
            if ("meld_result".equals(optionalText(part, "component"))
                    && cardName != null
                    && cardName.equals(optionalText(part, "name"))) {
                return true;
            }
        }
        return false;
    }

    private record CardRow(
            String id,
            String setCode,
            String name,
            String collectorNumber,
            String rarity,
            String prices,
            String imageUris,
            String cardFaces,
            String promoTypes,
            String finishes) {
    }

    private record SetRow(
            String code,
            String name,
            LocalDate releasedAt,
            String setType,
            int cardCount,
            boolean digital,
            boolean nonfoilOnly,
            boolean foilOnly,
            String iconSvgUri) {
    }

    public record ImportStatus(
            String state,
            Instant startedAt,
            Instant completedAt,
            int cardsImported,
            int setsImported,
            String error) {
    }
}
