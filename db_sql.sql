CREATE DATABASE `mtg_collectron` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `mtg_collectron`;

CREATE TABLE `sets` (
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `released_at` datetime NOT NULL,
  `set_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `card_count` int NOT NULL,
  `digital` tinyint(1) NOT NULL,
  `nonfoil_only` tinyint(1) NOT NULL,
  `foil_only` tinyint(1) NOT NULL,
  `icon_svg_uri` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `cards` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `set_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `collector_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `rarity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `prices` json DEFAULT NULL,
  `image_uris` json DEFAULT NULL,
  `card_faces` json DEFAULT NULL,
  `promo_types` json DEFAULT NULL,
  `finishes` json DEFAULT NULL,
  `artist` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `set_number_pair` (`set_code`,`collector_number`),
  CONSTRAINT `fk_setCode` FOREIGN KEY (`set_code`) REFERENCES `sets` (`code`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `users` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `email` varchar(254) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email_verified_at` datetime DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `last_login_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_set_info` (
  `card_owner_id` bigint unsigned NOT NULL,
  `set_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `commons_count` int unsigned DEFAULT NULL,
  `uncommons_count` int unsigned DEFAULT NULL,
  `rares_count` int unsigned DEFAULT NULL,
  `mythics_count` int unsigned DEFAULT NULL,
  `base_set_owned` int unsigned DEFAULT NULL,
  `extra_set_owned` int unsigned DEFAULT NULL,
  `foils_owned` int unsigned DEFAULT NULL,
  `full_set_every_single` float unsigned DEFAULT NULL,
  `full_set_base_only` float unsigned DEFAULT NULL,
  `full_set_one_each` float unsigned DEFAULT NULL,
  KEY `fk_setOwnerId` (`card_owner_id`),
  KEY `fk_setCodeUserSets` (`set_code`),
  CONSTRAINT `fk_setCodeUserSets` FOREIGN KEY (`set_code`) REFERENCES `sets` (`code`) ON DELETE CASCADE,
  CONSTRAINT `fk_setOwnerId` FOREIGN KEY (`card_owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_cards` (
  `card_owner_id` bigint unsigned NOT NULL,
  `set_code` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `card_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `collector_number` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `card_count` int unsigned DEFAULT NULL,
  `is_foil` tinyint(1) NOT NULL,
  `is_considered_extra` tinyint(1) DEFAULT NULL,
  `is_tagged_square` tinyint(1) DEFAULT NULL,
  `is_tagged_circle` tinyint(1) DEFAULT NULL,
  `is_tagged_triangle` tinyint(1) DEFAULT NULL,
  `is_tagged_cross` tinyint(1) DEFAULT NULL,
  KEY `fk_cardOwnerId` (`card_owner_id`),
  KEY `fk_setCodeUserCards` (`set_code`),
  CONSTRAINT `fk_cardOwnerId` FOREIGN KEY (`card_owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_setCodeUserCards` FOREIGN KEY (`set_code`) REFERENCES `sets` (`code`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_preferences` (
  `user_id` bigint unsigned NOT NULL,
  `full_set_option_selected` int NOT NULL,
  `card_per_page_option_selected` int NOT NULL,
  `dark_mode` tinyint(1) DEFAULT NULL,
  `show_prices` tinyint(1) DEFAULT NULL,
  `show_sidebar_percent` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_preferencesUserId` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
