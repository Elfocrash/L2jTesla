CREATE TABLE IF NOT EXISTS `augmentations` (
  `item_id` int(11) NOT NULL default 0,
  `attributes` int(11) NOT NULL default -1,
  `skill_id` int(11) NOT NULL default -1,
  `skill_level` int(11) NOT NULL default -1,
  PRIMARY KEY (`item_id`)
);