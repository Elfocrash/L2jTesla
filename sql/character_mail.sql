CREATE TABLE IF NOT EXISTS `character_mail` (
  `charId` int(10) NOT NULL,
  `letterId` int(10),
  `senderId` int(10) NOT NULL,
  `location` varchar(45) NOT NULL,
  `recipientNames` varchar(200) default NULL,
  `subject` varchar(128) default NULL,
  `message` varchar(3000) default NULL,
  `sentDate` timestamp NULL default NULL,
  `unread` smallint(1) default 1,
  PRIMARY KEY  (`letterId`)
);