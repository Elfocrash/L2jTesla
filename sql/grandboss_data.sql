CREATE TABLE IF NOT EXISTS `grandboss_data` (
  `boss_id` INTEGER NOT NULL DEFAULT 0,
  `loc_x` INTEGER NOT NULL DEFAULT 0,
  `loc_y` INTEGER NOT NULL DEFAULT 0,
  `loc_z` INTEGER NOT NULL DEFAULT 0,
  `heading` INTEGER NOT NULL DEFAULT 0,
  `respawn_time` BIGINT NOT NULL DEFAULT 0,
  `currentHP` DECIMAL(8,0) DEFAULT NULL,
  `currentMP` DECIMAL(8,0) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`boss_id`)
);

INSERT IGNORE INTO `grandboss_data` VALUES
(25512, 96080, -110822, -3343, 0, 0, 0, 0, 0),		-- Dr Chaos
(29001, -21610, 181594, -5734, 0, 0, 0, 0, 0),		-- Queen Ant
(29006, 17726, 108915, -6480, 0, 0, 0, 0, 0),		-- Core
(29014, 55024, 17368, -5412, 10126, 0, 0, 0, 0),	-- Orfen
(29019, 185708, 114298, -8221, 32768, 0, 0, 0, 0),	-- Antharas
(29020, 115762, 17116, 10077, 8250, 0, 0, 0, 0),	-- Baium
(29022, 55312, 219168, -3223, 0, 0, 0, 0, 0),		-- Zaken
(29028, 212852, -114842, -1632, 0, 0, 0, 0, 0),		-- Valakas
(29045, 0, 0, 0, 0, 0, 0, 0, 0),					-- Frintezza
(29046, 174231, -88006, -5115, 0, 0, 0, 0, 0),		-- Scarlet Van Halisha (85)
(29047, 174231, -88006, -5115, 0, 0, 0, 0, 0),		-- Scarlet Van Halisha (85)
(29065, 27549, -6638, -2008, 0, 0, 0, 0, 0);		-- Sailren