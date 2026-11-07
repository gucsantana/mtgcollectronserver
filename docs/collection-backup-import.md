# Collection backup import

Start an authenticated import with `POST /mtg-collectron/importbackup` as
`multipart/form-data` with:

* `file`: the collection backup CSV file
* `userId`: the ID of the account being verified
* `password`: that account's password
* `additiveImport`: optional boolean; defaults to `false`

The CSV must contain a `card_info` section followed by a `set_info` section. Each section starts with its marker row and a header row. All fields below are required in their respective headers, and each data row must have a value for every field except that percentage values in `set_info` may be blank.

Card fields:

```text
card_name,set_code,card_count,collector_number,is_considered_extra,is_foil,is_tagged_circle,is_tagged_cross,is_tagged_square,is_tagged_triangle
```

Set fields:

```text
set_code,base_set_owned,commons_count,extra_set_owned,foils_owned,full_set_base_only,full_set_every_single,full_set_one_each,mythics_count,rares_count,uncommons_count
```

When `additiveImport` is `false`, the user's existing cards and set info are removed and replaced with the CSV data. When it is `true`, matching cards (same set code and collector number) have their counts added, while the imported card name, foil status, extra status, and tags replace the existing values. Set info is recalculated from the merged collection rather than copied from the CSV:

* Rarity counts are summed from owned cards matched to the database card catalog.
* `base_set_owned`, `extra_set_owned`, and `foils_owned` are summed from the merged card counts.
* `full_set_base_only` is `base_set_owned / sets.card_count * 100`.
* `full_set_every_single` is `(base_set_owned + extra_set_owned) / sets.card_count * 100`.
* `full_set_one_each` is the sum of owned common, uncommon, rare, and mythic counts divided by the total database cards of those rarities, times 100.

The endpoint verifies the supplied user ID and password, queues the import, and
returns `202 Accepted` promptly:

```json
{
  "jobId": "a-random-uuid",
  "state": "queued",
  "startedAt": null,
  "completedAt": null,
  "setsImported": null,
  "cardsImported": null,
  "error": null
}
```

Poll `GET /mtg-collectron/importbackup/jobs/{jobId}` every few seconds using the
same authenticated username that submitted the import. The response has the
same shape and a state of `queued`, `running`, `completed`, or `failed`.
Completed jobs include `setsImported` and `cardsImported`; failed jobs include
an `error` message. Stop polling on either terminal state, then refresh the
collection after `completed`.

The import itself runs in a database transaction. Job status is held in
application memory and retained for up to 24 hours, so a process restart loses
active job status. As with the deployment's other in-memory job, run a single
ECS task unless shared job storage and coordination are added.

The `user_cards.card_name` column must support 255 characters:

```sql
ALTER TABLE user_cards MODIFY card_name VARCHAR(255) NOT NULL;
```
