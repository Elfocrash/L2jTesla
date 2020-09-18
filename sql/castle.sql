CREATE TABLE IF NOT EXISTS `castle` (
  `id` INT NOT NULL default 0,
  `name` varchar(25) NOT NULL,
  `taxPercent` INT NOT NULL default 15,
  `treasury` BIGINT NOT NULL default 0,
  `siegeDate` DECIMAL(20,0) NOT NULL default 0,
  `regTimeOver` enum('true','false') DEFAULT 'true' NOT NULL,
  `certificates` SMALLINT NOT NULL DEFAULT 300,
  PRIMARY KEY (`name`),
  KEY `id` (`id`)
);

INSERT IGNORE INTO `castle` VALUES
(1,'Gludio',0,0,0,'true',300),
(2,'Dion',0,0,0,'true',300),
(3,'Giran',0,0,0,'true',300),
(4,'Oren',0,0,0,'true',300),
(5,'Aden',0,0,0,'true',300),
(6,'Innadril',0,0,0,'true',300),
(7,'Goddard',0,0,0,'true',300),
(8,'Rune',0,0,0,'true',300),
(9,'Schuttgart',0,0,0,'true',300);