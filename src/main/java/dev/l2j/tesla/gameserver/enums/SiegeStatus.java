package dev.l2j.tesla.gameserver.enums;

public enum SiegeStatus
{
	REGISTRATION_OPENED, // Equals canceled or end siege event.
	REGISTRATION_OVER,
	IN_PROGRESS // Equals siege start event.
}