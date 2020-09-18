CREATE TABLE IF NOT EXISTS `accounts` (
  `login` VARCHAR(45) NOT NULL DEFAULT '',
  `password` VARCHAR(128),
  `lastactive` DECIMAL(20),
  `access_level` INT(3) NOT NULL DEFAULT 0,
  `lastServer` INT(4) DEFAULT 1,
  PRIMARY KEY (`login`)
);
