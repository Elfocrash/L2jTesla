package dev.l2j.tesla.gameserver.model.boat;

import dev.l2j.tesla.gameserver.model.actor.Boat;
import dev.l2j.tesla.gameserver.model.location.BoatLocation;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.network.clientpackets.Say2;
import dev.l2j.tesla.gameserver.network.serverpackets.CreatureSay;
import dev.l2j.tesla.gameserver.network.serverpackets.PlaySound;
import dev.l2j.tesla.commons.concurrent.ThreadPool;

import dev.l2j.tesla.gameserver.data.manager.BoatManager;

public class BoatGiranTalking implements Runnable
{
	private static final Location OUST_LOC_1 = new Location(46763, 187041, -3451);
	private static final Location OUST_LOC_2 = new Location(-96777, 258970, -3623);
	
	// Time: 868s
	private static final BoatLocation[] GIRAN_TO_TALKING =
	{
		new BoatLocation(51914, 189023, -3610, 150, 800),
		new BoatLocation(60567, 189789, -3610, 150, 800),
		new BoatLocation(63732, 197457, -3610, 200, 800),
		new BoatLocation(63732, 219946, -3610, 250, 800),
		new BoatLocation(62008, 222240, -3610, 250, 1200),
		new BoatLocation(56115, 226791, -3610, 250, 1200),
		new BoatLocation(40384, 226432, -3610, 300, 800),
		new BoatLocation(37760, 226432, -3610, 300, 800),
		new BoatLocation(27153, 226791, -3610, 300, 800),
		new BoatLocation(12672, 227535, -3610, 300, 800),
		new BoatLocation(-1808, 228280, -3610, 300, 800),
		new BoatLocation(-22165, 230542, -3610, 300, 800),
		new BoatLocation(-42523, 235205, -3610, 300, 800),
		new BoatLocation(-68451, 259560, -3610, 250, 800),
		new BoatLocation(-70848, 261696, -3610, 200, 800),
		new BoatLocation(-83344, 261610, -3610, 200, 800),
		new BoatLocation(-88344, 261660, -3610, 180, 800),
		new BoatLocation(-92344, 261660, -3610, 180, 800),
		new BoatLocation(-94242, 261659, -3610, 150, 800)
	};
	
	private static final BoatLocation[] TALKING_DOCK =
	{
		new BoatLocation(-96622, 261660, -3610, 150, 800)
	};
	
	// Time: 1398s
	private static final BoatLocation[] TALKING_TO_GIRAN =
	{
		new BoatLocation(-113925, 261660, -3610, 150, 800),
		new BoatLocation(-126107, 249116, -3610, 180, 800),
		new BoatLocation(-126107, 234499, -3610, 180, 800),
		new BoatLocation(-126107, 219882, -3610, 180, 800),
		new BoatLocation(-109414, 204914, -3610, 180, 800),
		new BoatLocation(-92807, 204914, -3610, 180, 800),
		new BoatLocation(-80425, 216450, -3610, 250, 800),
		new BoatLocation(-68043, 227987, -3610, 250, 800),
		new BoatLocation(-63744, 231168, -3610, 250, 800),
		new BoatLocation(-60844, 231369, -3610, 250, 1800),
		new BoatLocation(-44915, 231369, -3610, 200, 800),
		new BoatLocation(-28986, 231369, -3610, 200, 800),
		new BoatLocation(8233, 207624, -3610, 200, 800),
		new BoatLocation(21470, 201503, -3610, 180, 800),
		new BoatLocation(40058, 195383, -3610, 180, 800),
		new BoatLocation(43022, 193793, -3610, 150, 800),
		new BoatLocation(45986, 192203, -3610, 150, 800),
		new BoatLocation(48950, 190613, -3610, 150, 800)
	};
	
	private static final BoatLocation GIRAN_DOCK = TALKING_TO_GIRAN[TALKING_TO_GIRAN.length - 1];
	
	private final Boat _boat;
	private int _cycle = 0;
	private int _shoutCount = 0;
	
	private final CreatureSay ARRIVED_AT_GIRAN;
	private final CreatureSay ARRIVED_AT_GIRAN_2;
	private final CreatureSay LEAVE_GIRAN5;
	private final CreatureSay LEAVE_GIRAN1;
	private final CreatureSay LEAVE_GIRAN0;
	private final CreatureSay LEAVING_GIRAN;
	private final CreatureSay ARRIVED_AT_TALKING;
	private final CreatureSay ARRIVED_AT_TALKING_2;
	private final CreatureSay LEAVE_TALKING5;
	private final CreatureSay LEAVE_TALKING1;
	private final CreatureSay LEAVE_TALKING0;
	private final CreatureSay LEAVING_TALKING;
	private final CreatureSay BUSY_TALKING;
	
	private final CreatureSay ARRIVAL_TALKING15;
	private final CreatureSay ARRIVAL_TALKING10;
	private final CreatureSay ARRIVAL_TALKING5;
	private final CreatureSay ARRIVAL_TALKING1;
	private final CreatureSay ARRIVAL_GIRAN20;
	private final CreatureSay ARRIVAL_GIRAN15;
	private final CreatureSay ARRIVAL_GIRAN10;
	private final CreatureSay ARRIVAL_GIRAN5;
	private final CreatureSay ARRIVAL_GIRAN1;
	
	private final PlaySound GIRAN_SOUND;
	private final PlaySound TALKING_SOUND;
	
	private final PlaySound GIRAN_SOUND_LEAVE_5MIN;
	private final PlaySound GIRAN_SOUND_LEAVE_1MIN;
	
	private final PlaySound TALKING_SOUND_LEAVE_5MIN;
	private final PlaySound TALKING_SOUND_LEAVE_1MIN;
	
	public BoatGiranTalking(Boat boat)
	{
		_boat = boat;
		
		ARRIVED_AT_GIRAN = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_GIRAN);
		ARRIVED_AT_GIRAN_2 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES);
		LEAVE_GIRAN5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES);
		LEAVE_GIRAN1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE);
		LEAVE_GIRAN0 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_TALKING);
		LEAVING_GIRAN = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_TALKING);
		ARRIVED_AT_TALKING = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_TALKING);
		ARRIVED_AT_TALKING_2 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_AFTER_10_MINUTES);
		LEAVE_TALKING5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_IN_5_MINUTES);
		LEAVE_TALKING1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_IN_1_MINUTE);
		LEAVE_TALKING0 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_GIRAN);
		LEAVING_TALKING = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_GIRAN);
		BUSY_TALKING = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_GIRAN_TALKING_DELAYED);
		
		ARRIVAL_TALKING15 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_15_MINUTES);
		ARRIVAL_TALKING10 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_10_MINUTES);
		ARRIVAL_TALKING5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_5_MINUTES);
		ARRIVAL_TALKING1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_1_MINUTE);
		ARRIVAL_GIRAN20 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_20_MINUTES);
		ARRIVAL_GIRAN15 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_15_MINUTES);
		ARRIVAL_GIRAN10 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_10_MINUTES);
		ARRIVAL_GIRAN5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_5_MINUTES);
		ARRIVAL_GIRAN1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_1_MINUTE);
		
		GIRAN_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		TALKING_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		
		GIRAN_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", _boat);
		GIRAN_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", _boat);
		
		TALKING_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", _boat);
		TALKING_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", _boat);
	}
	
	@Override
	public void run()
	{
		switch (_cycle)
		{
			case 0:
				BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], LEAVE_GIRAN5);
				_boat.broadcastPacket(GIRAN_SOUND_LEAVE_5MIN);
				ThreadPool.schedule(this, 240000);
				break;
			case 1:
				BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], LEAVE_GIRAN1);
				_boat.broadcastPacket(GIRAN_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 40000);
				break;
			case 2:
				BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], LEAVE_GIRAN0);
				_boat.broadcastPacket(GIRAN_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 20000);
				break;
			case 3:
				BoatManager.getInstance().broadcastPackets(GIRAN_DOCK, TALKING_DOCK[0], LEAVING_GIRAN, ARRIVAL_TALKING15);
				_boat.broadcastPacket(GIRAN_SOUND);
				_boat.payForRide(3946, 1, OUST_LOC_1);
				_boat.executePath(GIRAN_TO_TALKING);
				ThreadPool.schedule(this, 250000);
				break;
			case 4:
				BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, ARRIVAL_TALKING10);
				ThreadPool.schedule(this, 300000);
				break;
			case 5:
				BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, ARRIVAL_TALKING5);
				ThreadPool.schedule(this, 240000);
				break;
			case 6:
				BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, ARRIVAL_TALKING1);
				break;
			case 7:
				if (BoatManager.getInstance().isBusyDock(BoatManager.TALKING_ISLAND))
				{
					if (_shoutCount == 0)
						BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, BUSY_TALKING);
					
					_shoutCount++;
					if (_shoutCount > 35)
						_shoutCount = 0;
					
					ThreadPool.schedule(this, 5000);
					return;
				}
				BoatManager.getInstance().dockBoat(BoatManager.TALKING_ISLAND, true);
				_boat.executePath(TALKING_DOCK);
				break;
			case 8:
				BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GIRAN_DOCK, ARRIVED_AT_TALKING, ARRIVED_AT_TALKING_2);
				_boat.broadcastPacket(TALKING_SOUND);
				ThreadPool.schedule(this, 300000);
				break;
			case 9:
				BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, LEAVE_TALKING5);
				_boat.broadcastPacket(TALKING_SOUND_LEAVE_5MIN);
				ThreadPool.schedule(this, 240000);
				break;
			case 10:
				BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, LEAVE_TALKING1);
				_boat.broadcastPacket(TALKING_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 40000);
				break;
			case 11:
				BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, LEAVE_TALKING0);
				_boat.broadcastPacket(TALKING_SOUND_LEAVE_1MIN);
				ThreadPool.schedule(this, 20000);
				break;
			case 12:
				BoatManager.getInstance().dockBoat(BoatManager.TALKING_ISLAND, false);
				BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GIRAN_DOCK, LEAVING_TALKING);
				_boat.broadcastPacket(TALKING_SOUND);
				_boat.payForRide(3945, 1, OUST_LOC_2);
				_boat.executePath(TALKING_TO_GIRAN);
				ThreadPool.schedule(this, 200000);
				break;
			case 13:
				BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN20);
				ThreadPool.schedule(this, 300000);
				break;
			case 14:
				BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN15);
				ThreadPool.schedule(this, 300000);
				break;
			case 15:
				BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN10);
				ThreadPool.schedule(this, 300000);
				break;
			case 16:
				BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN5);
				ThreadPool.schedule(this, 240000);
				break;
			case 17:
				BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN1);
				break;
			case 18:
				BoatManager.getInstance().broadcastPackets(GIRAN_DOCK, TALKING_DOCK[0], ARRIVED_AT_GIRAN, ARRIVED_AT_GIRAN_2);
				_boat.broadcastPacket(GIRAN_SOUND);
				ThreadPool.schedule(this, 300000);
				break;
		}
		_shoutCount = 0;
		
		_cycle++;
		if (_cycle > 18)
			_cycle = 0;
	}
	
	public static void load()
	{
		final Boat boat = BoatManager.getInstance().getNewBoat(2, 48950, 190613, -3610, 60800);
		if (boat != null)
		{
			boat.registerEngine(new BoatGiranTalking(boat));
			boat.runEngine(180000);
		}
	}
}
