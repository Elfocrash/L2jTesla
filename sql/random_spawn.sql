DROP TABLE IF EXISTS random_spawn;

CREATE TABLE random_spawn (
  `groupId` tinyint(3) unsigned NOT NULL,
  `npcId` smallint(5) unsigned NOT NULL,
  `count` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `initialDelay` int(8) NOT NULL DEFAULT '-1',
  `respawnDelay` int(8) NOT NULL DEFAULT '-1',
  `despawnDelay` int(8) NOT NULL DEFAULT '-1',
  `broadcastSpawn` enum('true','false') NOT NULL DEFAULT 'false',
  `randomSpawn` enum('true','false') NOT NULL DEFAULT 'true',
  PRIMARY KEY (`groupId`)
);

INSERT INTO `random_spawn` VALUES 
(1,30556,1,-1,1800000,1800000,'false','true'), -- Master Toma
(11,31113,1,-1,-1,-1,'true','true'), -- Merchant of Mammon
(12,31126,1,-1,-1,-1,'true','true'), -- Blacksmith of Mammon
(13,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(14,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(15,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(16,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(17,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(18,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(19,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(20,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(21,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(22,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(23,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(24,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(25,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(26,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(27,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(28,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(29,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(30,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(31,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(32,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(33,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(34,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(35,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(36,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(37,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(38,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(39,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(40,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(41,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(42,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(43,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(44,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(45,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(46,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(47,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(48,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(49,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(50,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(51,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(52,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(53,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(54,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(55,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(56,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(57,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(58,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(59,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(60,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(61,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(62,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(63,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(64,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(65,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(66,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(67,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(68,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(69,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(70,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(71,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(72,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(73,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(74,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(75,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(76,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(77,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(78,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(79,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(80,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(81,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(82,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(83,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(84,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(85,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(86,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(87,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(88,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(89,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(90,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(91,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(92,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(93,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(94,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(95,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(96,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(97,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(98,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(99,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(100,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(101,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(102,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(103,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(104,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(105,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(106,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(107,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(108,31170,1,-1,60,0,'false','false'), -- Crest of Dusk
(109,31171,1,-1,60,0,'false','false'), -- Crest of Dawn
(110,25283,1,-1,86400,0,'false','false'), -- Lilith (80)
(111,25286,1,-1,86400,0,'false','false'), -- Anakim (80)
(112,27316,1,1800000,14400000,1800000,'false','false'), -- Fallen Chieftain Vegus
(113,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(114,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(115,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(116,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(117,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(118,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(119,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(120,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(121,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(122,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(123,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(124,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(125,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(126,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(127,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(128,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(129,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(130,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(131,31093,1,-1,60,0,'false','false'), -- Preacher of Doom
(132,31094,1,-1,60,0,'false','false'), -- Orator of Revelations
(135,32049,1,-1,1200000,1200000,'false','true'); -- Rooney (Blacksmith of wind Rooney)