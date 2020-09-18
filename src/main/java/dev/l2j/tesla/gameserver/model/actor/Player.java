package dev.l2j.tesla.gameserver.model.actor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import dev.l2j.tesla.autobots.AutobotsManager;
import dev.l2j.tesla.Config;
import dev.l2j.tesla.L2DatabaseFactory;
import dev.l2j.tesla.gameserver.LoginServerThread;
import dev.l2j.tesla.gameserver.communitybbs.BB.Forum;
import dev.l2j.tesla.gameserver.communitybbs.Manager.ForumsBBSManager;
import dev.l2j.tesla.gameserver.geoengine.GeoEngine;
import dev.l2j.tesla.gameserver.handler.IItemHandler;
import dev.l2j.tesla.gameserver.handler.ItemHandler;
import dev.l2j.tesla.gameserver.handler.admincommandhandlers.AdminEditChar;
import dev.l2j.tesla.gameserver.model.actor.template.PlayerTemplate;
import dev.l2j.tesla.gameserver.model.craft.ManufactureList;
import dev.l2j.tesla.gameserver.model.entity.Castle;
import dev.l2j.tesla.gameserver.model.entity.Duel;
import dev.l2j.tesla.gameserver.model.entity.Siege;
import dev.l2j.tesla.gameserver.model.group.CommandChannel;
import dev.l2j.tesla.gameserver.model.group.Party;
import dev.l2j.tesla.gameserver.model.holder.IntIntHolder;
import dev.l2j.tesla.gameserver.model.holder.SkillUseHolder;
import dev.l2j.tesla.gameserver.model.holder.Timestamp;
import dev.l2j.tesla.gameserver.model.holder.skillnode.GeneralSkillNode;
import dev.l2j.tesla.gameserver.model.itemcontainer.listeners.ItemPassiveSkillsListener;
import dev.l2j.tesla.gameserver.model.location.Location;
import dev.l2j.tesla.gameserver.model.location.SpawnLocation;
import dev.l2j.tesla.gameserver.model.multisell.PreparedListContainer;
import dev.l2j.tesla.gameserver.model.olympiad.OlympiadGameManager;
import dev.l2j.tesla.gameserver.model.olympiad.OlympiadGameTask;
import dev.l2j.tesla.gameserver.model.olympiad.OlympiadManager;
import dev.l2j.tesla.gameserver.model.partymatching.PartyMatchRoom;
import dev.l2j.tesla.gameserver.model.partymatching.PartyMatchRoomList;
import dev.l2j.tesla.gameserver.model.partymatching.PartyMatchWaitingList;
import dev.l2j.tesla.gameserver.model.pledge.Clan;
import dev.l2j.tesla.gameserver.model.pledge.ClanMember;
import dev.l2j.tesla.gameserver.model.tradelist.TradeList;
import dev.l2j.tesla.gameserver.network.GameClient;
import dev.l2j.tesla.gameserver.network.SystemMessageId;
import dev.l2j.tesla.gameserver.scripting.Quest;
import dev.l2j.tesla.gameserver.scripting.QuestState;
import dev.l2j.tesla.gameserver.skills.Env;
import dev.l2j.tesla.gameserver.skills.Formulas;
import dev.l2j.tesla.gameserver.skills.effects.EffectTemplate;
import dev.l2j.tesla.gameserver.skills.l2skills.L2SkillSiegeFlag;
import dev.l2j.tesla.gameserver.skills.l2skills.L2SkillSummon;
import dev.l2j.tesla.commons.concurrent.ThreadPool;
import dev.l2j.tesla.commons.math.MathUtil;
import dev.l2j.tesla.commons.random.Rnd;

import dev.l2j.tesla.gameserver.data.ItemTable;
import dev.l2j.tesla.gameserver.data.SkillTable;
import dev.l2j.tesla.gameserver.data.SkillTable.FrequentSkill;
import dev.l2j.tesla.gameserver.data.manager.CastleManager;
import dev.l2j.tesla.gameserver.data.manager.CoupleManager;
import dev.l2j.tesla.gameserver.data.manager.CursedWeaponManager;
import dev.l2j.tesla.gameserver.data.manager.DimensionalRiftManager;
import dev.l2j.tesla.gameserver.data.manager.DuelManager;
import dev.l2j.tesla.gameserver.data.manager.FestivalOfDarknessManager;
import dev.l2j.tesla.gameserver.data.manager.HeroManager;
import dev.l2j.tesla.gameserver.data.manager.SevenSignsManager;
import dev.l2j.tesla.gameserver.data.manager.ZoneManager;
import dev.l2j.tesla.gameserver.data.sql.ClanTable;
import dev.l2j.tesla.gameserver.data.sql.PlayerInfoTable;
import dev.l2j.tesla.gameserver.data.xml.AdminData;
import dev.l2j.tesla.gameserver.data.xml.MapRegionData.TeleportType;
import dev.l2j.tesla.gameserver.data.xml.NpcData;
import dev.l2j.tesla.gameserver.data.xml.PlayerData;
import dev.l2j.tesla.gameserver.data.xml.RecipeData;
import dev.l2j.tesla.gameserver.data.xml.ScriptData;
import dev.l2j.tesla.gameserver.enums.AiEventType;
import dev.l2j.tesla.gameserver.enums.CabalType;
import dev.l2j.tesla.gameserver.enums.GaugeColor;
import dev.l2j.tesla.gameserver.enums.IntentionType;
import dev.l2j.tesla.gameserver.enums.LootRule;
import dev.l2j.tesla.gameserver.enums.MessageType;
import dev.l2j.tesla.gameserver.enums.PolyType;
import dev.l2j.tesla.gameserver.enums.PunishmentType;
import dev.l2j.tesla.gameserver.enums.ScriptEventType;
import dev.l2j.tesla.gameserver.enums.SealType;
import dev.l2j.tesla.gameserver.enums.ShortcutType;
import dev.l2j.tesla.gameserver.enums.SiegeSide;
import dev.l2j.tesla.gameserver.enums.TeamType;
import dev.l2j.tesla.gameserver.enums.ZoneId;
import dev.l2j.tesla.gameserver.enums.actors.ClassId;
import dev.l2j.tesla.gameserver.enums.actors.ClassRace;
import dev.l2j.tesla.gameserver.enums.actors.ClassType;
import dev.l2j.tesla.gameserver.enums.actors.Sex;
import dev.l2j.tesla.gameserver.enums.actors.StoreType;
import dev.l2j.tesla.gameserver.enums.items.ActionType;
import dev.l2j.tesla.gameserver.enums.items.ArmorType;
import dev.l2j.tesla.gameserver.enums.items.EtcItemType;
import dev.l2j.tesla.gameserver.enums.items.ShotType;
import dev.l2j.tesla.gameserver.enums.items.WeaponType;
import dev.l2j.tesla.gameserver.enums.skills.L2EffectFlag;
import dev.l2j.tesla.gameserver.enums.skills.L2EffectType;
import dev.l2j.tesla.gameserver.enums.skills.L2SkillType;
import dev.l2j.tesla.gameserver.enums.skills.Stats;
import dev.l2j.tesla.gameserver.model.AccessLevel;
import dev.l2j.tesla.gameserver.model.L2Effect;
import dev.l2j.tesla.gameserver.model.L2Skill;
import dev.l2j.tesla.gameserver.model.PetDataEntry;
import dev.l2j.tesla.gameserver.model.Shortcut;
import dev.l2j.tesla.gameserver.model.World;
import dev.l2j.tesla.gameserver.model.WorldObject;
import dev.l2j.tesla.gameserver.model.WorldRegion;
import dev.l2j.tesla.gameserver.model.actor.ai.NextAction;
import dev.l2j.tesla.gameserver.model.actor.ai.type.CreatureAI;
import dev.l2j.tesla.gameserver.model.actor.ai.type.PlayerAI;
import dev.l2j.tesla.gameserver.model.actor.ai.type.SummonAI;
import dev.l2j.tesla.gameserver.model.actor.instance.Cubic;
import dev.l2j.tesla.gameserver.model.actor.instance.Door;
import dev.l2j.tesla.gameserver.model.actor.instance.FestivalMonster;
import dev.l2j.tesla.gameserver.model.actor.instance.Folk;
import dev.l2j.tesla.gameserver.model.actor.instance.Monster;
import dev.l2j.tesla.gameserver.model.actor.instance.Pet;
import dev.l2j.tesla.gameserver.model.actor.instance.Servitor;
import dev.l2j.tesla.gameserver.model.actor.instance.SiegeGuard;
import dev.l2j.tesla.gameserver.model.actor.instance.StaticObject;
import dev.l2j.tesla.gameserver.model.actor.instance.TamedBeast;
import dev.l2j.tesla.gameserver.model.actor.instance.WeddingManagerNpc;
import dev.l2j.tesla.gameserver.model.actor.npc.RewardInfo;
import dev.l2j.tesla.gameserver.model.actor.player.Appearance;
import dev.l2j.tesla.gameserver.model.actor.player.BlockList;
import dev.l2j.tesla.gameserver.model.actor.player.Experience;
import dev.l2j.tesla.gameserver.model.actor.player.FishingStance;
import dev.l2j.tesla.gameserver.model.actor.player.HennaList;
import dev.l2j.tesla.gameserver.model.actor.player.MacroList;
import dev.l2j.tesla.gameserver.model.actor.player.Punishment;
import dev.l2j.tesla.gameserver.model.actor.player.RadarList;
import dev.l2j.tesla.gameserver.model.actor.player.Request;
import dev.l2j.tesla.gameserver.model.actor.player.ShortcutList;
import dev.l2j.tesla.gameserver.model.actor.player.SubClass;
import dev.l2j.tesla.gameserver.model.actor.stat.PlayerStat;
import dev.l2j.tesla.gameserver.model.actor.status.PlayerStatus;
import dev.l2j.tesla.gameserver.model.actor.template.PetTemplate;
import dev.l2j.tesla.gameserver.model.item.Recipe;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance.ItemLocation;
import dev.l2j.tesla.gameserver.model.item.instance.ItemInstance.ItemState;
import dev.l2j.tesla.gameserver.model.item.kind.Item;
import dev.l2j.tesla.gameserver.model.item.kind.Weapon;
import dev.l2j.tesla.gameserver.model.itemcontainer.Inventory;
import dev.l2j.tesla.gameserver.model.itemcontainer.ItemContainer;
import dev.l2j.tesla.gameserver.model.itemcontainer.PcFreight;
import dev.l2j.tesla.gameserver.model.itemcontainer.PcInventory;
import dev.l2j.tesla.gameserver.model.itemcontainer.PcWarehouse;
import dev.l2j.tesla.gameserver.model.itemcontainer.PetInventory;
import dev.l2j.tesla.gameserver.model.memo.PlayerMemo;
import dev.l2j.tesla.gameserver.model.zone.type.BossZone;
import dev.l2j.tesla.gameserver.network.serverpackets.AbstractNpcInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed;
import dev.l2j.tesla.gameserver.network.serverpackets.ChairSit;
import dev.l2j.tesla.gameserver.network.serverpackets.ChangeWaitType;
import dev.l2j.tesla.gameserver.network.serverpackets.CharInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.ConfirmDlg;
import dev.l2j.tesla.gameserver.network.serverpackets.DeleteObject;
import dev.l2j.tesla.gameserver.network.serverpackets.EtcStatusUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.ExAutoSoulShot;
import dev.l2j.tesla.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.ExOlympiadMode;
import dev.l2j.tesla.gameserver.network.serverpackets.ExSetCompassZoneCode;
import dev.l2j.tesla.gameserver.network.serverpackets.ExStorageMaxCount;
import dev.l2j.tesla.gameserver.network.serverpackets.FriendList;
import dev.l2j.tesla.gameserver.network.serverpackets.GetOnVehicle;
import dev.l2j.tesla.gameserver.network.serverpackets.HennaInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.InventoryUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.ItemList;
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket;
import dev.l2j.tesla.gameserver.network.serverpackets.LeaveWorld;
import dev.l2j.tesla.gameserver.network.serverpackets.MagicSkillUse;
import dev.l2j.tesla.gameserver.network.serverpackets.MoveToPawn;
import dev.l2j.tesla.gameserver.network.serverpackets.MyTargetSelected;
import dev.l2j.tesla.gameserver.network.serverpackets.ObservationMode;
import dev.l2j.tesla.gameserver.network.serverpackets.ObservationReturn;
import dev.l2j.tesla.gameserver.network.serverpackets.PartySmallWindowUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.PetInventoryUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import dev.l2j.tesla.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.PrivateStoreListBuy;
import dev.l2j.tesla.gameserver.network.serverpackets.PrivateStoreListSell;
import dev.l2j.tesla.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import dev.l2j.tesla.gameserver.network.serverpackets.PrivateStoreManageListSell;
import dev.l2j.tesla.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import dev.l2j.tesla.gameserver.network.serverpackets.PrivateStoreMsgSell;
import dev.l2j.tesla.gameserver.network.serverpackets.RecipeShopManageList;
import dev.l2j.tesla.gameserver.network.serverpackets.RecipeShopMsg;
import dev.l2j.tesla.gameserver.network.serverpackets.RecipeShopSellList;
import dev.l2j.tesla.gameserver.network.serverpackets.RelationChanged;
import dev.l2j.tesla.gameserver.network.serverpackets.Ride;
import dev.l2j.tesla.gameserver.network.serverpackets.SendTradeDone;
import dev.l2j.tesla.gameserver.network.serverpackets.ServerClose;
import dev.l2j.tesla.gameserver.network.serverpackets.SetupGauge;
import dev.l2j.tesla.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.ShortCutInit;
import dev.l2j.tesla.gameserver.network.serverpackets.SkillCoolTime;
import dev.l2j.tesla.gameserver.network.serverpackets.SkillList;
import dev.l2j.tesla.gameserver.network.serverpackets.SocialAction;
import dev.l2j.tesla.gameserver.network.serverpackets.SpawnItem;
import dev.l2j.tesla.gameserver.network.serverpackets.StaticObjectInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.StatusUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.StopMove;
import dev.l2j.tesla.gameserver.network.serverpackets.SystemMessage;
import dev.l2j.tesla.gameserver.network.serverpackets.TargetSelected;
import dev.l2j.tesla.gameserver.network.serverpackets.TargetUnselected;
import dev.l2j.tesla.gameserver.network.serverpackets.TitleUpdate;
import dev.l2j.tesla.gameserver.network.serverpackets.TradePressOtherOk;
import dev.l2j.tesla.gameserver.network.serverpackets.TradePressOwnOk;
import dev.l2j.tesla.gameserver.network.serverpackets.TradeStart;
import dev.l2j.tesla.gameserver.network.serverpackets.UserInfo;
import dev.l2j.tesla.gameserver.network.serverpackets.ValidateLocation;
import dev.l2j.tesla.gameserver.skills.funcs.FuncHennaCON;
import dev.l2j.tesla.gameserver.skills.funcs.FuncHennaDEX;
import dev.l2j.tesla.gameserver.skills.funcs.FuncHennaINT;
import dev.l2j.tesla.gameserver.skills.funcs.FuncHennaMEN;
import dev.l2j.tesla.gameserver.skills.funcs.FuncHennaSTR;
import dev.l2j.tesla.gameserver.skills.funcs.FuncHennaWIT;
import dev.l2j.tesla.gameserver.skills.funcs.FuncMaxCpMul;
import dev.l2j.tesla.gameserver.taskmanager.AttackStanceTaskManager;
import dev.l2j.tesla.gameserver.taskmanager.GameTimeTaskManager;
import dev.l2j.tesla.gameserver.taskmanager.ItemsOnGroundTaskManager;
import dev.l2j.tesla.gameserver.taskmanager.PvpFlagTaskManager;
import dev.l2j.tesla.gameserver.taskmanager.ShadowItemTaskManager;
import dev.l2j.tesla.gameserver.taskmanager.WaterTaskManager;

/**
 * This class represents a player in the world.<br>
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).
 */
public class Player extends Playable {
    private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
    private static final String ADD_OR_UPDATE_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,class_index) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE skill_level=VALUES(skill_level)";
    private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
    private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";

    private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
    private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE char_obj_id=? AND class_index=? ORDER BY buff_index ASC";
    private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";

    private static final String INSERT_CHARACTER = "INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,nobless,power_grade) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=? WHERE obj_id=?";
    private static final String RESTORE_CHARACTER = "SELECT * FROM characters WHERE obj_id=?";

    private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
    private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
    private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?";
    private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";

    private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
    private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";

    private static final String RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
    private static final String ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
    private static final String UPDATE_TARGET_RECOM_HAVE = "UPDATE characters SET rec_have=? WHERE obj_Id=?";
    private static final String UPDATE_CHAR_RECOM_LEFT = "UPDATE characters SET rec_left=? WHERE obj_Id=?";

    private static final String UPDATE_NOBLESS = "UPDATE characters SET nobless=? WHERE obj_Id=?";

    private static final String DELETE_RECIPEBOOK = "DELETE FROM character_recipebook WHERE charId=?";
    private static final String SAVE_RECIPEBOOK = "INSERT INTO character_recipebook (charId, recipeId) values(?,?)";

    public static final int REQUEST_TIMEOUT = 15;

    private static final Comparator<GeneralSkillNode> COMPARE_SKILLS_BY_MIN_LVL = Comparator.comparing(GeneralSkillNode::getMinLvl);
    private static final Comparator<GeneralSkillNode> COMPARE_SKILLS_BY_LVL = Comparator.comparing(GeneralSkillNode::getValue);

    private GameClient _client;
    private final Map<Integer, String> _chars = new HashMap<>();

    private String _accountName;
    private long _deleteTimer;

    private boolean _isOnline;
    private long _onlineTime;
    private long _onlineBeginTime;
    private long _lastAccess;
    private long _uptime;

    protected int _baseClass;
    protected int _activeClass;
    protected int _classIndex;

    private final Map<Integer, SubClass> _subClasses = new ConcurrentSkipListMap<>();
    private final ReentrantLock _subclassLock = new ReentrantLock();

    private Appearance _appearance;

    private long _expBeforeDeath;
    private int _karma;
    private int _pvpKills;
    private int _pkKills;
    private byte _pvpFlag;
    private byte _siegeState;
    private int _curWeightPenalty;

    private int _lastCompassZone; // the last compass zone update send to the client

    private boolean _isIn7sDungeon;

    private Punishment _punishment = new Punishment(this);

    private boolean _isInOlympiadMode;
    private boolean _isInOlympiadStart;
    private int _olympiadGameId = -1;
    private int _olympiadSide = -1;

    private Duel.DuelState _duelState = Duel.DuelState.NO_DUEL;
    private int _duelId;
    private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;

    private Boat _boat;
    private SpawnLocation _boatPosition = new SpawnLocation(0, 0, 0, 0);

    private boolean _canFeed;
    protected PetTemplate _petTemplate;
    private PetDataEntry _petData;
    private int _controlItemId;
    private int _curFeed;
    protected Future<?> _mountFeedTask;
    private ScheduledFuture<?> _dismountTask;

    private int _mountType;
    private int _mountNpcId;
    private int _mountLevel;
    private int _mountObjectId;

    protected int _throneId;

    private int _teleMode;
    private boolean _isCrystallizing;
    private boolean _isCrafting;

    private final Map<Integer, Recipe> _dwarvenRecipeBook = new HashMap<>();
    private final Map<Integer, Recipe> _commonRecipeBook = new HashMap<>();

    private boolean _isSitting;

    private final Location _savedLocation = new Location(0, 0, 0);

    private int _recomHave;
    private int _recomLeft;
    private final List<Integer> _recomChars = new ArrayList<>();

    private final PcInventory _inventory = new PcInventory(this);
    private PcWarehouse _warehouse;
    private PcFreight _freight;
    private final List<PcFreight> _depositedFreight = new ArrayList<>();

    private StoreType _storeType = StoreType.NONE;

    private TradeList _activeTradeList;
    private ItemContainer _activeWarehouse;
    private ManufactureList _createList;
    private TradeList _sellList;
    private TradeList _buyList;

    private PreparedListContainer _currentMultiSell;

    private boolean _isNoble;
    private boolean _isHero;

    private Folk _currentFolk;

    private int _questNpcObject;

    private final List<QuestState> _quests = new ArrayList<>();
    private final List<QuestState> _notifyQuestOfDeathList = new ArrayList<>();

    private final PlayerMemo _vars = new PlayerMemo(getObjectId());

    private final ShortcutList _shortcutList = new ShortcutList(this);

    private final MacroList _macroList = new MacroList(this);

    private final HennaList _hennaList = new HennaList(this);

    private Summon _summon;
    private TamedBeast _tamedBeast;

    private final RadarList _radarList = new RadarList(this);

    private int _partyroom;

    private int _clanId;
    private Clan _clan;
    private int _apprentice;
    private int _sponsor;
    private long _clanJoinExpiryTime;
    private long _clanCreateExpiryTime;
    private int _powerGrade;
    private int _clanPrivileges;
    private int _pledgeClass;
    private int _pledgeType;
    private int _lvlJoinedAcademy;

    private boolean _wantsPeace;

    private int _deathPenaltyBuffLevel;

    private final AtomicInteger _charges = new AtomicInteger();
    private ScheduledFuture<?> _chargeTask;

    private Location _currentSkillWorldPosition;

    private AccessLevel _accessLevel;

    private boolean _messageRefusal; // message refusal mode
    private boolean _tradeRefusal; // Trade refusal
    private boolean _exchangeRefusal; // Exchange refusal

    private Party _party;
    private LootRule _lootRule;

    private Player _activeRequester;
    private long _requestExpireTime;
    private final Request _request = new Request(this);

    private ScheduledFuture<?> _protectTask;

    private long _recentFakeDeathEndTime;
    private boolean _isFakeDeath;

    private int _expertiseArmorPenalty;
    private boolean _expertiseWeaponPenalty;

    private ItemInstance _activeEnchantItem;

    protected boolean _inventoryDisable;

    protected Map<Integer, Cubic> _cubics = new ConcurrentSkipListMap<>();

    protected Set<Integer> _activeSoulShots = ConcurrentHashMap.newKeySet(1);

    private final int _loto[] = new int[5];
    private final int _race[] = new int[2];

    private final BlockList _blockList = new BlockList(this);

    private TeamType _team = TeamType.NONE;

    private int _alliedVarkaKetra; // lvl of alliance with ketra orcs or varka silenos, used in quests and aggro checks [-5,-1] varka, 0 neutral, [1,5] ketra

    private FishingStance _fishingStance = new FishingStance(this);

    private final List<String> _validBypass = new ArrayList<>();
    private final List<String> _validBypass2 = new ArrayList<>();

    private Forum _forumMemo;

    private boolean _isInSiege;

    private final Map<Integer, L2Skill> _skills = new ConcurrentSkipListMap<>();

    private final SkillUseHolder _currentSkill = new SkillUseHolder();
    private final SkillUseHolder _currentPetSkill = new SkillUseHolder();
    private final SkillUseHolder _queuedSkill = new SkillUseHolder();

    private int _cursedWeaponEquippedId;

    private int _reviveRequested;
    private double _revivePower = .0;
    private boolean _revivePet;

    private double _cpUpdateIncCheck = .0;
    private double _cpUpdateDecCheck = .0;
    private double _cpUpdateInterval = .0;
    private double _mpUpdateIncCheck = .0;
    private double _mpUpdateDecCheck = .0;
    private double _mpUpdateInterval = .0;

    private int _clientX;
    private int _clientY;
    private int _clientZ;

    private int _mailPosition;

    private static final int FALLING_VALIDATION_DELAY = 10000;
    private volatile long _fallingTimestamp;

    private ScheduledFuture<?> _shortBuffTask;
    private int _shortBuffTaskSkillId;

    private int _coupleId;
    private boolean _isUnderMarryRequest;
    private int _requesterId;

    private final List<Integer> _friendList = new ArrayList<>(); // Related to CB.
    private final List<Integer> _selectedFriendList = new ArrayList<>(); // Related to CB.
    private final List<Integer> _selectedBlocksList = new ArrayList<>(); // Related to CB.

    private Player _summonTargetRequest;
    private L2Skill _summonSkillRequest;

    private Door _requestedGate;

    private Location _enterWorldLocation;

    public Location getEnterWorldLocation() {
        return _enterWorldLocation;
    }

    public void setEnterWorldLocation(Location location) {
        _enterWorldLocation = location;
    }

    private Location _lastDeathLocation;

    public Location getLastDeathLocation() {
        return _lastDeathLocation;
    }

    public void setLastDeathLocation(Location location) {
        _lastDeathLocation = location;
    }

    /**
     * Constructor of Player (use Creature constructor).
     * <ul>
     * <li>Call the Creature constructor to create an empty _skills slot and copy basic Calculator set to this Player</li>
     * <li>Set the name of the Player</li>
     * </ul>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the Player to 1</B></FONT>
     *
     * @param objectId    Identifier of the object to initialized
     * @param template    The L2PcTemplate to apply to the Player
     * @param accountName The name of the account including this Player
     * @param app         The PcAppearance of the Player
     */
    protected Player(int objectId, PlayerTemplate template, String accountName, Appearance app) {
        super(objectId, template);

        initCharStatusUpdateValues();

        _accountName = accountName;
        _appearance = app;

        // Create an AI
        _ai = new PlayerAI(this);

        // Retrieve from the database all items of this Player and add them to _inventory
        getInventory().restore();
        getWarehouse();
        getFreight();
    }

    /**
     * Create a new Player and add it in the characters table of the database.
     * <ul>
     * <li>Create a new Player with an account name</li>
     * <li>Set the name, the Hair Style, the Hair Color and the Face type of the Player</li>
     * <li>Add the player in the characters table of the database</li>
     * </ul>
     *
     * @param objectId    Identifier of the object to initialized
     * @param template    The L2PcTemplate to apply to the Player
     * @param accountName The name of the Player
     * @param name        The name of the Player
     * @param hairStyle   The hair style Identifier of the Player
     * @param hairColor   The hair color Identifier of the Player
     * @param face        The face type Identifier of the Player
     * @param sex         The sex type Identifier of the Player
     * @return The Player added to the database or null
     */
    public static Player create(int objectId, PlayerTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, Sex sex) {
        // Create a new Player with an account name
        Appearance app = new Appearance(face, hairColor, hairStyle, sex);
        Player player = new Player(objectId, template, accountName, app);

        // Set the name of the Player
        player.setName(name);

        // Set access level
        player.setAccessLevel(Config.DEFAULT_ACCESS_LEVEL);

        // Cache few informations into CharNameTable.
        PlayerInfoTable.getInstance().addPlayer(objectId, accountName, name, player.getAccessLevel().getLevel());

        // Set the base class ID to that of the actual class ID.
        player.setBaseClass(player.getClassId());

        // Add the player in the characters table of the database
        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_CHARACTER)) {
            ps.setString(1, accountName);
            ps.setInt(2, player.getObjectId());
            ps.setString(3, player.getName());
            ps.setInt(4, player.getLevel());
            ps.setInt(5, player.getMaxHp());
            ps.setDouble(6, player.getCurrentHp());
            ps.setInt(7, player.getMaxCp());
            ps.setDouble(8, player.getCurrentCp());
            ps.setInt(9, player.getMaxMp());
            ps.setDouble(10, player.getCurrentMp());
            ps.setInt(11, player.getAppearance().getFace());
            ps.setInt(12, player.getAppearance().getHairStyle());
            ps.setInt(13, player.getAppearance().getHairColor());
            ps.setInt(14, player.getAppearance().getSex().ordinal());
            ps.setLong(15, player.getExp());
            ps.setInt(16, player.getSp());
            ps.setInt(17, player.getKarma());
            ps.setInt(18, player.getPvpKills());
            ps.setInt(19, player.getPkKills());
            ps.setInt(20, player.getClanId());
            ps.setInt(21, player.getRace().ordinal());
            ps.setInt(22, player.getClassId().getId());
            ps.setLong(23, player.getDeleteTimer());
            ps.setInt(24, player.hasDwarvenCraft() ? 1 : 0);
            ps.setString(25, player.getTitle());
            ps.setInt(26, player.getAccessLevel().getLevel());
            ps.setInt(27, player.isOnlineInt());
            ps.setInt(28, player.isIn7sDungeon() ? 1 : 0);
            ps.setInt(29, player.getClanPrivileges());
            ps.setInt(30, player.wantsPeace() ? 1 : 0);
            ps.setInt(31, player.getBaseClass());
            ps.setInt(32, player.isNoble() ? 1 : 0);
            ps.setLong(33, 0);
            ps.executeUpdate();
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't create player {} for {} account.", e, name, accountName);
            return null;
        }

        return player;
    }

    @Override
    public void addFuncsToNewCharacter() {
        // Add Creature functionalities.
        super.addFuncsToNewCharacter();

        addStatFunc(FuncMaxCpMul.getInstance());

        addStatFunc(FuncHennaSTR.getInstance());
        addStatFunc(FuncHennaDEX.getInstance());
        addStatFunc(FuncHennaINT.getInstance());
        addStatFunc(FuncHennaMEN.getInstance());
        addStatFunc(FuncHennaCON.getInstance());
        addStatFunc(FuncHennaWIT.getInstance());
    }

    @Override
    protected void initCharStatusUpdateValues() {
        super.initCharStatusUpdateValues();

        _cpUpdateInterval = getMaxCp() / 352.0;
        _cpUpdateIncCheck = getMaxCp();
        _cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
        _mpUpdateInterval = getMaxMp() / 352.0;
        _mpUpdateIncCheck = getMaxMp();
        _mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
    }

    @Override
    public void initCharStat() {
        setStat(new PlayerStat(this));
    }

    @Override
    public final PlayerStat getStat() {
        return (PlayerStat) super.getStat();
    }

    @Override
    public void initCharStatus() {
        setStatus(new PlayerStatus(this));
    }

    @Override
    public final PlayerStatus getStatus() {
        return (PlayerStatus) super.getStatus();
    }

    public final Appearance getAppearance() {
        return _appearance;
    }

    /**
     * @return the base L2PcTemplate link to the Player.
     */
    public final PlayerTemplate getBaseTemplate() {
        return PlayerData.getInstance().getTemplate(_baseClass);
    }

    /**
     * Return the L2PcTemplate link to the Player.
     */
    @Override
    public final PlayerTemplate getTemplate() {
        return (PlayerTemplate) super.getTemplate();
    }

    public void setTemplate(ClassId newclass) {
        super.setTemplate(PlayerData.getInstance().getTemplate(newclass));
    }

    /**
     * Return the AI of the Player (create it if necessary).
     */
    @Override
    public CreatureAI getAI() {
        CreatureAI ai = _ai;
        if (ai == null) {
            synchronized (this) {
                if (_ai == null)
                    _ai = new PlayerAI(this);

                return _ai;
            }
        }
        return ai;
    }

    /**
     * Return the Level of the Player.
     */
    @Override
    public final int getLevel() {
        return getStat().getLevel();
    }

    /**
     * A newbie is a player reaching level 6. He isn't considered newbie at lvl 25.<br>
     * Since IL newbie isn't anymore the first character of an account reaching that state, but any.
     *
     * @return True if newbie.
     */
    public boolean isNewbie() {
        return getClassId().level() <= 1 && getLevel() >= 6 && getLevel() <= 25;
    }

    public void setBaseClass(int baseClass) {
        _baseClass = baseClass;
    }

    public void setBaseClass(ClassId classId) {
        _baseClass = classId.ordinal();
    }

    public boolean isInStoreMode() {
        return _storeType != StoreType.NONE;
    }

    public boolean isCrafting() {
        return _isCrafting;
    }

    public void setCrafting(boolean state) {
        _isCrafting = state;
    }

    /**
     * @return a table containing all Common RecipeList of the Player.
     */
    public Collection<Recipe> getCommonRecipeBook() {
        return _commonRecipeBook.values();
    }

    /**
     * @return a table containing all Dwarf RecipeList of the Player.
     */
    public Collection<Recipe> getDwarvenRecipeBook() {
        return _dwarvenRecipeBook.values();
    }

    /**
     * Add a new L2RecipList to the table _commonrecipebook containing all RecipeList of the Player.
     *
     * @param recipe The RecipeList to add to the _recipebook
     */
    public void registerCommonRecipeList(Recipe recipe) {
        _commonRecipeBook.put(recipe.getId(), recipe);
    }

    /**
     * Add a new L2RecipList to the table _recipebook containing all RecipeList of the Player.
     *
     * @param recipe The RecipeList to add to the _recipebook
     */
    public void registerDwarvenRecipeList(Recipe recipe) {
        _dwarvenRecipeBook.put(recipe.getId(), recipe);
    }

    /**
     * @param recipeId The Identifier of the RecipeList to check in the player's recipe books
     * @return <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b>
     */
    public boolean hasRecipeList(int recipeId) {
        return _dwarvenRecipeBook.containsKey(recipeId) || _commonRecipeBook.containsKey(recipeId);
    }

    /**
     * Tries to remove a {@link Recipe} from this {@link Player}. Delete the associated {@link Shortcut}, if existing.
     *
     * @param recipeId : The id of the Recipe to remove.
     */
    public void unregisterRecipeList(int recipeId) {
        if (_dwarvenRecipeBook.containsKey(recipeId))
            _dwarvenRecipeBook.remove(recipeId);
        else if (_commonRecipeBook.containsKey(recipeId))
            _commonRecipeBook.remove(recipeId);

        // Delete all existing shortcuts refering to this recipe id.
        getShortcutList().deleteShortcuts(recipeId, ShortcutType.RECIPE);
    }

    /**
     * @return the Id for the last talked quest NPC.
     */
    public int getLastQuestNpcObject() {
        return _questNpcObject;
    }

    public void setLastQuestNpcObject(int npcId) {
        _questNpcObject = npcId;
    }

    /**
     * @param name The name of the quest.
     * @return The QuestState object corresponding to the quest name.
     */
    public QuestState getQuestState(String name) {
        for (QuestState qs : _quests) {
            if (name.equals(qs.getQuest().getName()))
                return qs;
        }
        return null;
    }

    /**
     * Add a QuestState to the table _quest containing all quests began by the Player.
     *
     * @param qs The QuestState to add to _quest.
     */
    public void setQuestState(QuestState qs) {
        _quests.add(qs);
    }

    /**
     * Remove a QuestState from the table _quest containing all quests began by the Player.
     *
     * @param qs : The QuestState to be removed from _quest.
     */
    public void delQuestState(QuestState qs) {
        _quests.remove(qs);
    }

    /**
     * @param completed : If true, include completed quests to the list.
     * @return list of started and eventually completed quests of the player.
     */
    public List<Quest> getAllQuests(boolean completed) {
        List<Quest> quests = new ArrayList<>();

        for (QuestState qs : _quests) {
            if (qs == null || completed && qs.isCreated() || !completed && !qs.isStarted())
                continue;

            Quest quest = qs.getQuest();
            if (quest == null || !quest.isRealQuest())
                continue;

            quests.add(quest);
        }

        return quests;
    }

    public void processQuestEvent(String questName, String event) {
        Quest quest = ScriptData.getInstance().getQuest(questName);
        if (quest == null)
            return;

        QuestState qs = getQuestState(questName);
        if (qs == null)
            return;

        WorldObject object = World.getInstance().getObject(getLastQuestNpcObject());
        if (!(object instanceof Npc) || !isInsideRadius(object, Npc.INTERACTION_DISTANCE, false, false))
            return;

        final Npc npc = (Npc) object;

        final List<Quest> scripts = npc.getTemplate().getEventQuests(ScriptEventType.ON_TALK);
        if (scripts != null) {
            for (Quest script : scripts) {
                if (script == null || !script.equals(quest))
                    continue;

                quest.notifyEvent(event, npc, this);
                break;
            }
        }
    }

    /**
     * Add QuestState instance that is to be notified of Player's death.
     *
     * @param qs The QuestState that subscribe to this event
     */
    public void addNotifyQuestOfDeath(QuestState qs) {
        if (qs == null)
            return;

        if (!_notifyQuestOfDeathList.contains(qs))
            _notifyQuestOfDeathList.add(qs);
    }

    /**
     * Remove QuestState instance that is to be notified of Player's death.
     *
     * @param qs The QuestState that subscribe to this event
     */
    public void removeNotifyQuestOfDeath(QuestState qs) {
        if (qs == null)
            return;

        _notifyQuestOfDeathList.remove(qs);
    }

    /**
     * @return A list of QuestStates which registered for notify of death of this Player.
     */
    public final List<QuestState> getNotifyQuestOfDeath() {
        return _notifyQuestOfDeathList;
    }

    /**
     * @return player memos.
     */
    public PlayerMemo getMemos() {
        return _vars;
    }

    /**
     * @return the {@link ShortcutList} of the current {@link Player}.
     */
    public ShortcutList getShortcutList() {
        return _shortcutList;
    }

    /**
     * @return the {@link MacroList} of the current {@link Player}.
     */
    public MacroList getMacroList() {
        return _macroList;
    }

    /**
     * Set the siege state of the Player.
     *
     * @param siegeState 1 = attacker, 2 = defender, 0 = not involved
     */
    public void setSiegeState(byte siegeState) {
        _siegeState = siegeState;
    }

    /**
     * @return the siege state of the Player.
     */
    public byte getSiegeState() {
        return _siegeState;
    }

    /**
     * Set the PvP Flag of the Player.
     *
     * @param pvpFlag 0 or 1.
     */
    public void setPvpFlag(int pvpFlag) {
        _pvpFlag = (byte) pvpFlag;
    }

    @Override
    public byte getPvpFlag() {
        return _pvpFlag;
    }

    public void updatePvPFlag(int value) {
        if (getPvpFlag() == value)
            return;

        setPvpFlag(value);
        sendPacket(new UserInfo(this));

        if (_summon != null)
            sendPacket(new RelationChanged(_summon, getRelation(this), false));

        broadcastRelationsChanges();
    }

    public int getRelation(Player target) {
        int result = 0;

        // karma and pvp may not be required
        if (getPvpFlag() != 0)
            result |= RelationChanged.RELATION_PVP_FLAG;
        if (getKarma() > 0)
            result |= RelationChanged.RELATION_HAS_KARMA;

        if (isClanLeader())
            result |= RelationChanged.RELATION_LEADER;

        if (getSiegeState() != 0) {
            result |= RelationChanged.RELATION_INSIEGE;
            if (getSiegeState() != target.getSiegeState())
                result |= RelationChanged.RELATION_ENEMY;
            else
                result |= RelationChanged.RELATION_ALLY;
            if (getSiegeState() == 1)
                result |= RelationChanged.RELATION_ATTACKER;
        }

        if (getClan() != null && target.getClan() != null) {
            if (target.getPledgeType() != Clan.SUBUNIT_ACADEMY && getPledgeType() != Clan.SUBUNIT_ACADEMY && target.getClan().isAtWarWith(getClan().getClanId())) {
                result |= RelationChanged.RELATION_1SIDED_WAR;
                if (getClan().isAtWarWith(target.getClan().getClanId()))
                    result |= RelationChanged.RELATION_MUTUAL_WAR;
            }
        }
        return result;
    }

    @Override
    public void revalidateZone(boolean force) {
        super.revalidateZone(force);

        if (Config.ALLOW_WATER) {
            if (isInsideZone(ZoneId.WATER))
                WaterTaskManager.getInstance().add(this);
            else
                WaterTaskManager.getInstance().remove(this);
        }

        if (isInsideZone(ZoneId.SIEGE)) {
            if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
                return;

            _lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
            sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2));
        } else if (isInsideZone(ZoneId.PVP)) {
            if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
                return;

            _lastCompassZone = ExSetCompassZoneCode.PVPZONE;
            sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE));
        } else if (isIn7sDungeon()) {
            if (_lastCompassZone == ExSetCompassZoneCode.SEVENSIGNSZONE)
                return;

            _lastCompassZone = ExSetCompassZoneCode.SEVENSIGNSZONE;
            sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SEVENSIGNSZONE));
        } else if (isInsideZone(ZoneId.PEACE)) {
            if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
                return;

            _lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
            sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE));
        } else {
            if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
                return;

            if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
                updatePvPStatus();

            _lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
            sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE));
        }
    }

    /**
     * @return the PK counter of the Player.
     */
    public int getPkKills() {
        return _pkKills;
    }

    /**
     * Set the PK counter of the Player.
     *
     * @param pkKills A number.
     */
    public void setPkKills(int pkKills) {
        _pkKills = pkKills;
    }

    /**
     * @return The _deleteTimer of the Player.
     */
    public long getDeleteTimer() {
        return _deleteTimer;
    }

    /**
     * Set the _deleteTimer of the Player.
     *
     * @param deleteTimer Time in ms.
     */
    public void setDeleteTimer(long deleteTimer) {
        _deleteTimer = deleteTimer;
    }

    /**
     * @return The current weight of the Player.
     */
    public int getCurrentLoad() {
        return _inventory.getTotalWeight();
    }

    /**
     * @return The number of recommandation obtained by the Player.
     */
    public int getRecomHave() {
        return _recomHave;
    }

    /**
     * Set the number of recommandations obtained by the Player (Max : 255).
     *
     * @param value Number of recommandations obtained.
     */
    public void setRecomHave(int value) {
        _recomHave = MathUtil.limit(value, 0, 255);
    }

    /**
     * Edit the number of recommandation obtained by the Player (Max : 255).
     *
     * @param value : The value to add or remove.
     */
    public void editRecomHave(int value) {
        _recomHave = MathUtil.limit(_recomHave + value, 0, 255);
    }

    /**
     * @return The number of recommandation that the Player can give.
     */
    public int getRecomLeft() {
        return _recomLeft;
    }

    /**
     * Set the number of givable recommandations by the {@link Player} (Max : 9).
     *
     * @param value : The number of recommendations a player can give.
     */
    public void setRecomLeft(int value) {
        _recomLeft = MathUtil.limit(value, 0, 9);
    }

    /**
     * Increment the number of recommandation that the Player can give.
     */
    protected void decRecomLeft() {
        if (_recomLeft > 0)
            _recomLeft--;
    }

    public List<Integer> getRecomChars() {
        return _recomChars;
    }

    public void giveRecom(Player target) {
        target.editRecomHave(1);
        decRecomLeft();

        _recomChars.add(target.getObjectId());

        try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(ADD_CHAR_RECOM)) {
                ps.setInt(1, getObjectId());
                ps.setInt(2, target.getObjectId());
                ps.execute();
            }

            try (PreparedStatement ps = con.prepareStatement(UPDATE_TARGET_RECOM_HAVE)) {
                ps.setInt(1, target.getRecomHave());
                ps.setInt(2, target.getObjectId());
                ps.execute();
            }

            try (PreparedStatement ps = con.prepareStatement(UPDATE_CHAR_RECOM_LEFT)) {
                ps.setInt(1, getRecomLeft());
                ps.setInt(2, getObjectId());
                ps.execute();
            }
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't update player recommendations.", e);
        }
    }

    public boolean canRecom(Player target) {
        return !_recomChars.contains(target.getObjectId());
    }

    /**
     * Set the exp of the Player before a death
     *
     * @param exp
     */
    public void setExpBeforeDeath(long exp) {
        _expBeforeDeath = exp;
    }

    public long getExpBeforeDeath() {
        return _expBeforeDeath;
    }

    /**
     * Return the Karma of the Player.
     */
    @Override
    public int getKarma() {
        return _karma;
    }

    /**
     * Set the Karma of the Player and send StatusUpdate (broadcast).
     *
     * @param karma A value.
     */
    public void setKarma(int karma) {
        if (karma < 0)
            karma = 0;

        if (_karma > 0 && karma == 0) {
            sendPacket(new UserInfo(this));
            broadcastRelationsChanges();
        }

        // send message with new karma value
        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1).addNumber(karma));

        _karma = karma;
        broadcastKarma();
    }

    /**
     * Weight Limit = (CON Modifier*69000)*Skills
     *
     * @return The max weight that the Player can load.
     */
    public int getMaxLoad() {
        int con = getCON();
        if (con < 1)
            return 31000;

        if (con > 59)
            return 176000;

        double baseLoad = Math.pow(1.029993928, con) * 30495.627366;
        return (int) calcStat(Stats.MAX_LOAD, baseLoad * Config.ALT_WEIGHT_LIMIT, this, null);
    }

    public int getExpertiseArmorPenalty() {
        return _expertiseArmorPenalty;
    }

    public boolean getExpertiseWeaponPenalty() {
        return _expertiseWeaponPenalty;
    }

    public int getWeightPenalty() {
        return _curWeightPenalty;
    }

    /**
     * Update the overloaded status of the Player.
     */
    public void refreshOverloaded() {
        int maxLoad = getMaxLoad();
        if (maxLoad > 0) {
            int weightproc = getCurrentLoad() * 1000 / maxLoad;
            int newWeightPenalty;

            if (weightproc < 500)
                newWeightPenalty = 0;
            else if (weightproc < 666)
                newWeightPenalty = 1;
            else if (weightproc < 800)
                newWeightPenalty = 2;
            else if (weightproc < 1000)
                newWeightPenalty = 3;
            else
                newWeightPenalty = 4;

            if (_curWeightPenalty != newWeightPenalty) {
                _curWeightPenalty = newWeightPenalty;

                if (newWeightPenalty > 0) {
                    addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty), false);
                    setIsOverloaded(getCurrentLoad() > maxLoad);
                } else {
                    removeSkill(4270, false);
                    setIsOverloaded(false);
                }

                sendPacket(new UserInfo(this));
                sendPacket(new EtcStatusUpdate(this));
                broadcastCharInfo();
            }
        }
    }

    /**
     * Refresh expertise level ; weapon got one rank, when armor got 4 ranks.<br>
     */
    public void refreshExpertisePenalty() {
        final int expertiseLevel = getSkillLevel(L2Skill.SKILL_EXPERTISE);

        int armorPenalty = 0;
        boolean weaponPenalty = false;

        for (ItemInstance item : getInventory().getPaperdollItems()) {
            if (item.getItemType() != EtcItemType.ARROW && item.getItem().getCrystalType().getId() > expertiseLevel) {
                if (item.isWeapon())
                    weaponPenalty = true;
                else
                    armorPenalty += (item.getItem().getBodyPart() == Item.SLOT_FULL_ARMOR) ? 2 : 1;
            }
        }

        armorPenalty = Math.min(armorPenalty, 4);

        // Found a different state than previous ; update it.
        if (_expertiseWeaponPenalty != weaponPenalty || _expertiseArmorPenalty != armorPenalty) {
            _expertiseWeaponPenalty = weaponPenalty;
            _expertiseArmorPenalty = armorPenalty;

            // Passive skill "Grade Penalty" is either granted or dropped.
            if (_expertiseWeaponPenalty || _expertiseArmorPenalty > 0)
                addSkill(SkillTable.getInstance().getInfo(4267, 1), false);
            else
                removeSkill(4267, false);

            sendSkillList();
            sendPacket(new EtcStatusUpdate(this));

            final ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
            if (weapon != null) {
                if (_expertiseWeaponPenalty)
                    ItemPassiveSkillsListener.getInstance().onUnequip(0, weapon, this);
                else
                    ItemPassiveSkillsListener.getInstance().onEquip(0, weapon, this);
            }
        }
    }

    /**
     * Equip or unequip the item.
     * <UL>
     * <LI>If item is equipped, shots are applied if automation is on.</LI>
     * <LI>If item is unequipped, shots are discharged.</LI>
     * </UL>
     *
     * @param item        The item to charge/discharge.
     * @param abortAttack If true, the current attack will be aborted in order to equip the item.
     */
    public void useEquippableItem(ItemInstance item, boolean abortAttack) {
        ItemInstance[] items = null;
        final boolean isEquipped = item.isEquipped();
        final int oldInvLimit = getInventoryLimit();

        if (item.getItem() instanceof Weapon)
            item.unChargeAllShots();

        if (isEquipped) {
            if (item.getEnchantLevel() > 0)
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item));
            else
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item));

            items = getInventory().unEquipItemInBodySlotAndRecord(item);
        } else {
            items = getInventory().equipItemAndRecord(item);

            if (item.isEquipped()) {
                if (item.getEnchantLevel() > 0)
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED).addNumber(item.getEnchantLevel()).addItemName(item));
                else
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED).addItemName(item));

                if ((item.getItem().getBodyPart() & Item.SLOT_ALLWEAPON) != 0)
                    rechargeShots(true, true);
            } else
                sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
        }
        refreshExpertisePenalty();
        broadcastUserInfo();

        InventoryUpdate iu = new InventoryUpdate();
        iu.addItems(Arrays.asList(items));
        sendPacket(iu);

        if (abortAttack)
            abortAttack();

        if (getInventoryLimit() != oldInvLimit)
            sendPacket(new ExStorageMaxCount(this));
    }

    /**
     * @return PvP Kills of the Player (number of player killed during a PvP).
     */
    public int getPvpKills() {
        return _pvpKills;
    }

    /**
     * Set PvP Kills of the Player (number of player killed during a PvP).
     *
     * @param pvpKills A value.
     */
    public void setPvpKills(int pvpKills) {
        _pvpKills = pvpKills;
    }

    /**
     * @return The ClassId object of the Player contained in L2PcTemplate.
     */
    public ClassId getClassId() {
        return getTemplate().getClassId();
    }

    /**
     * Set the template of the Player.
     *
     * @param Id The Identifier of the L2PcTemplate to set to the Player
     */
    public void setClassId(int Id) {
        if (!_subclassLock.tryLock())
            return;

        try {
            if (getLvlJoinedAcademy() != 0 && _clan != null && ClassId.VALUES[Id].level() == 2) {
                if (getLvlJoinedAcademy() <= 16)
                    _clan.addReputationScore(400);
                else if (getLvlJoinedAcademy() >= 39)
                    _clan.addReputationScore(170);
                else
                    _clan.addReputationScore((400 - (getLvlJoinedAcademy() - 16) * 10));

                setLvlJoinedAcademy(0);

                // Oust pledge member from the academy, because he has finished his 2nd class transfer.
                _clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()), SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(getName()));
                _clan.removeClanMember(getObjectId(), 0);
                sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);

                // receive graduation gift : academy circlet
                addItem("Gift", 8181, 1, this, true);
            }

            if (isSubClassActive())
                _subClasses.get(_classIndex).setClassId(Id);

            broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1000, 0));
            setClassTemplate(Id);

            if (getClassId().level() == 3)
                sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
            else
                sendPacket(SystemMessageId.CLASS_TRANSFER);

            // Update class icon in party and clan
            if (_party != null)
                _party.broadcastPacket(new PartySmallWindowUpdate(this));

            if (_clan != null)
                _clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));

            if (Config.AUTO_LEARN_SKILLS)
                rewardSkills();
        } finally {
            _subclassLock.unlock();
        }
    }

    /**
     * @return the Experience of the Player.
     */
    public long getExp() {
        return getStat().getExp();
    }

    public void setActiveEnchantItem(ItemInstance scroll) {
        _activeEnchantItem = scroll;
    }

    public ItemInstance getActiveEnchantItem() {
        return _activeEnchantItem;
    }

    /**
     * @return The Race object of the Player.
     */
    public ClassRace getRace() {
        return (isSubClassActive()) ? getBaseTemplate().getRace() : getTemplate().getRace();
    }

    public RadarList getRadarList() {
        return _radarList;
    }

    /**
     * @return the SP amount of the Player.
     */
    public int getSp() {
        return getStat().getSp();
    }

    /**
     * @param castleId The castle to check.
     * @return True if this Player is a clan leader in ownership of the passed castle.
     */
    public boolean isCastleLord(int castleId) {
        final Clan clan = getClan();
        if (clan != null && clan.getLeader().getPlayerInstance() == this) {
            final Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
            return castle != null && castle.getCastleId() == castleId;
        }
        return false;
    }

    /**
     * @return The Clan Identifier of the Player.
     */
    public int getClanId() {
        return _clanId;
    }

    /**
     * @return The Clan Crest Identifier of the Player or 0.
     */
    public int getClanCrestId() {
        return (_clan != null) ? _clan.getCrestId() : 0;
    }

    /**
     * @return The Clan CrestLarge Identifier or 0
     */
    public int getClanCrestLargeId() {
        return (_clan != null) ? _clan.getCrestLargeId() : 0;
    }

    public long getClanJoinExpiryTime() {
        return _clanJoinExpiryTime;
    }

    public void setClanJoinExpiryTime(long time) {
        _clanJoinExpiryTime = time;
    }

    public long getClanCreateExpiryTime() {
        return _clanCreateExpiryTime;
    }

    public void setClanCreateExpiryTime(long time) {
        _clanCreateExpiryTime = time;
    }

    public void setOnlineTime(long time) {
        _onlineTime = time;
        _onlineBeginTime = System.currentTimeMillis();
    }

    /**
     * Return the PcInventory Inventory of the Player contained in _inventory.
     */
    @Override
    public PcInventory getInventory() {
        return _inventory;
    }

    /**
     * @return True if the Player is sitting.
     */
    public boolean isSitting() {
        return _isSitting;
    }

    /**
     * Set _isSitting to given value.
     *
     * @param state A boolean.
     */
    public void setSitting(boolean state) {
        _isSitting = state;
    }

    /**
     * Sit down the Player, set the AI Intention to REST and send ChangeWaitType packet (broadcast)
     */
    public void sitDown() {
        sitDown(true);
    }

    public void sitDown(boolean checkCast) {
        if (checkCast && isCastingNow())
            return;

        if (!_isSitting && !isAttackingDisabled() && !isOutOfControl() && !isImmobilized()) {
            breakAttack();
            setSitting(true);
            broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));

            // Schedule a sit down task to wait for the animation to finish
            getAI().setIntention(IntentionType.REST);

            ThreadPool.schedule(() -> setIsParalyzed(false), 2500);
            setIsParalyzed(true);
        }
    }

    /**
     * Stand up the Player, set the AI Intention to IDLE and send ChangeWaitType packet (broadcast)
     */
    public void standUp() {
        if (_isSitting && !isInStoreMode() && !isAlikeDead() && !isParalyzed()) {
            if (_effects.isAffected(L2EffectFlag.RELAXING))
                stopEffects(L2EffectType.RELAXING);

            broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));

            // Schedule a stand up task to wait for the animation to finish
            ThreadPool.schedule(() ->
            {
                setSitting(false);
                setIsParalyzed(false);
                getAI().setIntention(IntentionType.IDLE);
            }, 2500);
            setIsParalyzed(true);
        }
    }

    /**
     * Stands up and close any opened shop window, if any.
     */
    public void forceStandUp() {
        // Cancels any shop types.
        if (isInStoreMode()) {
            setStoreType(StoreType.NONE);
            broadcastUserInfo();
        }

        // Stand up.
        standUp();
    }

    /**
     * Used to sit or stand. If not possible, queue the action.
     *
     * @param target       The target, used for thrones types.
     * @param sittingState The sitting state, inheritated from packet or player status.
     */
    public void tryToSitOrStand(final WorldObject target, final boolean sittingState) {
        if (isFakeDeath()) {
            stopFakeDeath(true);
            return;
        }

        final boolean isThrone = target instanceof StaticObject && ((StaticObject) target).getType() == 1;

        // Player wants to sit on a throne but is out of radius, move to the throne delaying the sit action.
        if (isThrone && !sittingState && !isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false)) {
            getAI().setIntention(IntentionType.MOVE_TO, new Location(target.getX(), target.getY(), target.getZ()));

            NextAction nextAction = new NextAction(AiEventType.ARRIVED, IntentionType.MOVE_TO, () ->
            {
                if (getMountType() != 0)
                    return;

                sitDown();

                if (!((StaticObject) target).isBusy()) {
                    _throneId = target.getObjectId();

                    ((StaticObject) target).setBusy(true);
                    broadcastPacket(new ChairSit(getObjectId(), ((StaticObject) target).getStaticObjectId()));
                }
            });

            // Binding next action to AI.
            getAI().setNextAction(nextAction);
            return;
        }

        // Player isn't moving, sit directly.
        if (!isMoving()) {
            if (getMountType() != 0)
                return;

            if (sittingState) {
                if (_throneId != 0) {
                    final WorldObject object = World.getInstance().getObject(_throneId);
                    if (object instanceof StaticObject)
                        ((StaticObject) object).setBusy(false);

                    _throneId = 0;
                }

                standUp();
            } else {
                sitDown();

                if (isThrone && !((StaticObject) target).isBusy() && isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false)) {
                    _throneId = target.getObjectId();

                    ((StaticObject) target).setBusy(true);
                    broadcastPacket(new ChairSit(getObjectId(), ((StaticObject) target).getStaticObjectId()));
                }
            }
        }
        // Player is moving, wait the current action is done, then sit.
        else {
            NextAction nextAction = new NextAction(AiEventType.ARRIVED, IntentionType.MOVE_TO, () ->
            {
                if (getMountType() != 0)
                    return;

                if (sittingState) {
                    if (_throneId != 0) {
                        final WorldObject object = World.getInstance().getObject(_throneId);
                        if (object instanceof StaticObject)
                            ((StaticObject) object).setBusy(false);

                        _throneId = 0;
                    }

                    standUp();
                } else {
                    sitDown();

                    if (isThrone && !((StaticObject) target).isBusy() && isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false)) {
                        _throneId = target.getObjectId();

                        ((StaticObject) target).setBusy(true);
                        broadcastPacket(new ChairSit(getObjectId(), ((StaticObject) target).getStaticObjectId()));
                    }
                }
            });

            // Binding next action to AI.
            getAI().setNextAction(nextAction);
        }
    }

    /**
     * @return The PcWarehouse object of the Player.
     */
    public PcWarehouse getWarehouse() {
        if (_warehouse == null) {
            _warehouse = new PcWarehouse(this);
            _warehouse.restore();
        }
        return _warehouse;
    }

    /**
     * Free memory used by Warehouse
     */
    public void clearWarehouse() {
        if (_warehouse != null)
            _warehouse.deleteMe();

        _warehouse = null;
    }

    /**
     * @return The PcFreight object of the Player.
     */
    public PcFreight getFreight() {
        if (_freight == null) {
            _freight = new PcFreight(this);
            _freight.restore();
        }
        return _freight;
    }

    /**
     * Free memory used by Freight
     */
    public void clearFreight() {
        if (_freight != null)
            _freight.deleteMe();

        _freight = null;
    }

    /**
     * @param objectId The id of the owner.
     * @return deposited PcFreight object for the objectId or create new if not existing.
     */
    public PcFreight getDepositedFreight(int objectId) {
        for (PcFreight freight : _depositedFreight) {
            if (freight != null && freight.getOwnerId() == objectId)
                return freight;
        }

        PcFreight freight = new PcFreight(null);
        freight.doQuickRestore(objectId);
        _depositedFreight.add(freight);
        return freight;
    }

    /**
     * Clear memory used by deposited freight
     */
    public void clearDepositedFreight() {
        for (PcFreight freight : _depositedFreight) {
            if (freight != null)
                freight.deleteMe();
        }
        _depositedFreight.clear();
    }

    /**
     * @return The Adena amount of the Player.
     */
    public int getAdena() {
        return _inventory.getAdena();
    }

    /**
     * @return The Ancient Adena amount of the Player.
     */
    public int getAncientAdena() {
        return _inventory.getAncientAdena();
    }

    /**
     * Add adena to Inventory of the Player and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param count       int Quantity of adena to be added
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     */
    public void addAdena(String process, int count, WorldObject reference, boolean sendMessage) {
        if (sendMessage)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addNumber(count));

        if (count > 0) {
            _inventory.addAdena(process, count, this, reference);

            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(_inventory.getAdenaInstance());
            sendPacket(iu);
        }
    }

    /**
     * Reduce adena in Inventory of the Player and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param count       int Quantity of adena to be reduced
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean reduceAdena(String process, int count, WorldObject reference, boolean sendMessage) {
        if (count > getAdena()) {
            if (sendMessage)
                sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);

            return false;
        }

        if (count > 0) {
            ItemInstance adenaItem = _inventory.getAdenaInstance();
            if (!_inventory.reduceAdena(process, count, this, reference))
                return false;

            // Send update packet
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(adenaItem);
            sendPacket(iu);

            if (sendMessage)
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA).addNumber(count));
        }
        return true;
    }

    /**
     * Add ancient adena to Inventory of the Player and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param count       int Quantity of ancient adena to be added
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     */
    public void addAncientAdena(String process, int count, WorldObject reference, boolean sendMessage) {
        if (sendMessage)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(PcInventory.ANCIENT_ADENA_ID).addNumber(count));

        if (count > 0) {
            _inventory.addAncientAdena(process, count, this, reference);

            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(_inventory.getAncientAdenaInstance());
            sendPacket(iu);
        }
    }

    /**
     * Reduce ancient adena in Inventory of the Player and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param count       int Quantity of ancient adena to be reduced
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean reduceAncientAdena(String process, int count, WorldObject reference, boolean sendMessage) {
        if (count > getAncientAdena()) {
            if (sendMessage)
                sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);

            return false;
        }

        if (count > 0) {
            ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
            if (!_inventory.reduceAncientAdena(process, count, this, reference))
                return false;

            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(ancientAdenaItem);
            sendPacket(iu);

            if (sendMessage) {
                if (count > 1)
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID).addItemNumber(count));
                else
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID));
            }
        }
        return true;
    }

    /**
     * Adds item to inventory and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param item        ItemInstance to be added
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     */
    public void addItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage) {
        if (item.getCount() > 0) {
            // Sends message to client if requested
            if (sendMessage) {
                if (item.getCount() > 1)
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(item).addNumber(item.getCount()));
                else if (item.getEnchantLevel() > 0)
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2).addNumber(item.getEnchantLevel()).addItemName(item));
                else
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item));
            }

            // Add the item to inventory
            ItemInstance newitem = _inventory.addItem(process, item, this, reference);

            // Send inventory update packet
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(newitem);
            sendPacket(playerIU);

            // Update current load as well
            StatusUpdate su = new StatusUpdate(this);
            su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
            sendPacket(su);

            // Cursed Weapon
            if (CursedWeaponManager.getInstance().isCursed(newitem.getItemId()))
                CursedWeaponManager.getInstance().activate(this, newitem);
                // If you pickup arrows and a bow is equipped, try to equip them if no arrows is currently equipped.
            else if (item.getItem().getItemType() == EtcItemType.ARROW && getAttackType() == WeaponType.BOW && getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
                checkAndEquipArrows();
        }
    }

    /**
     * Adds item to Inventory and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param itemId      int Item Identifier of the item to be added
     * @param count       int Quantity of items to be added
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return The created ItemInstance.
     */
    public ItemInstance addItem(String process, int itemId, int count, WorldObject reference, boolean sendMessage) {
        if (count > 0) {
            // Retrieve the template of the item.
            final Item item = ItemTable.getInstance().getTemplate(itemId);
            if (item == null)
                return null;

            // Sends message to client if requested.
            if (sendMessage && ((!isCastingNow() && item.getItemType() == EtcItemType.HERB) || item.getItemType() != EtcItemType.HERB)) {
                if (count > 1) {
                    if (process.equalsIgnoreCase("Sweep") || process.equalsIgnoreCase("Quest"))
                        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addItemNumber(count));
                    else
                        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(itemId).addItemNumber(count));
                } else {
                    if (process.equalsIgnoreCase("Sweep") || process.equalsIgnoreCase("Quest"))
                        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId));
                    else
                        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(itemId));
                }
            }

            // If the item is herb type, dont add it to inventory.
            if (item.getItemType() == EtcItemType.HERB) {
                final ItemInstance herb = new ItemInstance(0, itemId);

                final IItemHandler handler = ItemHandler.getInstance().getHandler(herb.getEtcItem());
                if (handler != null)
                    handler.useItem(this, herb, false);
            } else {
                // Add the item to inventory
                final ItemInstance createdItem = _inventory.addItem(process, itemId, count, this, reference);

                // Cursed Weapon
                if (CursedWeaponManager.getInstance().isCursed(createdItem.getItemId()))
                    CursedWeaponManager.getInstance().activate(this, createdItem);
                    // If you pickup arrows and a bow is equipped, try to equip them if no arrows is currently equipped.
                else if (item.getItemType() == EtcItemType.ARROW && getAttackType() == WeaponType.BOW && getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
                    checkAndEquipArrows();

                return createdItem;
            }
        }
        return null;
    }

    /**
     * Destroy item from inventory and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param item        ItemInstance to be destroyed
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean destroyItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage) {
        return destroyItem(process, item, item.getCount(), reference, sendMessage);
    }

    /**
     * Destroy item from inventory and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param item        ItemInstance to be destroyed
     * @param count       int Quantity of ancient adena to be reduced
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean destroyItem(String process, ItemInstance item, int count, WorldObject reference, boolean sendMessage) {
        item = _inventory.destroyItem(process, item, count, this, reference);
        if (item == null) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

            return false;
        }

        // Send inventory update packet
        final InventoryUpdate iu = new InventoryUpdate();
        if (item.getCount() == 0)
            iu.addRemovedItem(item);
        else
            iu.addModifiedItem(item);
        sendPacket(iu);

        // Update current load as well
        final StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
        sendPacket(su);

        // Sends message to client if requested
        if (sendMessage) {
            if (count > 1)
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item).addItemNumber(count));
            else
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item));
        }
        return true;
    }

    /**
     * Destroys item from inventory and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param objectId    int Item Instance identifier of the item to be destroyed
     * @param count       int Quantity of items to be destroyed
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    @Override
    public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage) {
        final ItemInstance item = _inventory.getItemByObjectId(objectId);
        if (item == null) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

            return false;
        }

        return destroyItem(process, item, count, reference, sendMessage);
    }

    /**
     * Destroys shots from inventory without logging and only occasional saving to database. Sends InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param objectId    int Item Instance identifier of the item to be destroyed
     * @param count       int Quantity of items to be destroyed
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean destroyItemWithoutTrace(String process, int objectId, int count, WorldObject reference, boolean sendMessage) {
        ItemInstance item = _inventory.getItemByObjectId(objectId);

        if (item == null || item.getCount() < count) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

            return false;
        }

        return destroyItem(null, item, count, reference, sendMessage);
    }

    /**
     * Destroy item from inventory by using its <B>itemId</B> and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param itemId      int Item identifier of the item to be destroyed
     * @param count       int Quantity of items to be destroyed
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    @Override
    public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage) {
        if (itemId == 57)
            return reduceAdena(process, count, reference, sendMessage);

        ItemInstance item = _inventory.getItemByItemId(itemId);

        if (item == null || item.getCount() < count || _inventory.destroyItemByItemId(process, itemId, count, this, reference) == null) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

            return false;
        }

        // Send inventory update packet
        InventoryUpdate playerIU = new InventoryUpdate();
        playerIU.addItem(item);
        sendPacket(playerIU);

        // Update current load as well
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
        sendPacket(su);

        // Sends message to client if requested
        if (sendMessage) {
            if (count > 1)
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(count));
            else
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(itemId));
        }
        return true;
    }

    /**
     * Transfers item to another ItemContainer and send InventoryUpdate packet to the Player.
     *
     * @param process   String Identifier of process triggering this action
     * @param objectId  int Item Identifier of the item to be transfered
     * @param count     int Quantity of items to be transfered
     * @param target    Inventory the Inventory target.
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the new item or the updated item in inventory
     */
    public ItemInstance transferItem(String process, int objectId, int count, Inventory target, WorldObject reference) {
        final ItemInstance oldItem = checkItemManipulation(objectId, count);
        if (oldItem == null)
            return null;

        final ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
        if (newItem == null)
            return null;

        // Send inventory update packet
        InventoryUpdate playerIU = new InventoryUpdate();

        if (oldItem.getCount() > 0 && oldItem != newItem)
            playerIU.addModifiedItem(oldItem);
        else
            playerIU.addRemovedItem(oldItem);

        sendPacket(playerIU);

        // Update current load as well
        StatusUpdate playerSU = new StatusUpdate(this);
        playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
        sendPacket(playerSU);

        // Send target update packet
        if (target instanceof PcInventory) {
            final Player targetPlayer = ((PcInventory) target).getOwner();

            InventoryUpdate playerIU2 = new InventoryUpdate();
            if (newItem.getCount() > count)
                playerIU2.addModifiedItem(newItem);
            else
                playerIU2.addNewItem(newItem);
            targetPlayer.sendPacket(playerIU2);

            // Update current load as well
            playerSU = new StatusUpdate(targetPlayer);
            playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
            targetPlayer.sendPacket(playerSU);
        } else if (target instanceof PetInventory) {
            PetInventoryUpdate petIU = new PetInventoryUpdate();
            if (newItem.getCount() > count)
                petIU.addModifiedItem(newItem);
            else
                petIU.addNewItem(newItem);
            ((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
        }
        return newItem;
    }

    /**
     * Drop item from inventory and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param item        ItemInstance to be dropped
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    public boolean dropItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage) {
        item = _inventory.dropItem(process, item, this, reference);

        if (item == null) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

            return false;
        }

        item.dropMe(this, getX() + Rnd.get(-25, 25), getY() + Rnd.get(-25, 25), getZ() + 20);

        // Send inventory update packet
        InventoryUpdate playerIU = new InventoryUpdate();
        playerIU.addItem(item);
        sendPacket(playerIU);

        // Update current load as well
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
        sendPacket(su);

        // Sends message to client if requested
        if (sendMessage)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item));

        return true;
    }

    /**
     * Drop item from inventory by using its <B>objectID</B> and send InventoryUpdate packet to the Player.
     *
     * @param process     String Identifier of process triggering this action
     * @param objectId    int Item Instance identifier of the item to be dropped
     * @param count       int Quantity of items to be dropped
     * @param x           int coordinate for drop X
     * @param y           int coordinate for drop Y
     * @param z           int coordinate for drop Z
     * @param reference   WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return ItemInstance corresponding to the new item or the updated item in inventory
     */
    public ItemInstance dropItem(String process, int objectId, int count, int x, int y, int z, WorldObject reference, boolean sendMessage) {
        ItemInstance invItem = _inventory.getItemByObjectId(objectId);
        ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);

        if (item == null) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

            return null;
        }

        item.dropMe(this, x, y, z);

        // Send inventory update packet
        InventoryUpdate playerIU = new InventoryUpdate();
        playerIU.addItem(invItem);
        sendPacket(playerIU);

        // Update current load as well
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
        sendPacket(su);

        // Sends message to client if requested
        if (sendMessage)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item));

        return item;
    }

    public ItemInstance checkItemManipulation(int objectId, int count) {
        if (World.getInstance().getObject(objectId) == null)
            return null;

        final ItemInstance item = getInventory().getItemByObjectId(objectId);

        if (item == null || item.getOwnerId() != getObjectId())
            return null;

        if (count < 1 || (count > 1 && !item.isStackable()))
            return null;

        if (count > item.getCount())
            return null;

        // Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
        if (_summon != null && _summon.getControlItemId() == objectId || _mountObjectId == objectId)
            return null;

        if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
            return null;

        // We cannot put a Weapon with Augmention in WH while casting (Possible Exploit)
        if (item.isAugmented() && (isCastingNow() || isCastingSimultaneouslyNow()))
            return null;

        return item;
    }

    /**
     * Launch a task corresponding to Config time.
     *
     * @param protect boolean Drop timer or activate it.
     */
    public void setSpawnProtection(boolean protect) {
        if (protect) {
            if (_protectTask == null)
                _protectTask = ThreadPool.schedule(() ->
                {
                    setSpawnProtection(false);
                    sendMessage("The spawn protection has ended.");
                }, Config.PLAYER_SPAWN_PROTECTION * 1000);
        } else {
            _protectTask.cancel(true);
            _protectTask = null;
        }
        broadcastUserInfo();
    }

    public boolean isSpawnProtected() {
        return _protectTask != null;
    }

    /**
     * Set protection from agro mobs when getting up from fake death, according settings.
     */
    public void setRecentFakeDeath() {
        _recentFakeDeathEndTime = System.currentTimeMillis() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * 1000;
    }

    public void clearRecentFakeDeath() {
        _recentFakeDeathEndTime = 0;
    }

    public boolean isRecentFakeDeath() {
        return _recentFakeDeathEndTime > System.currentTimeMillis();
    }

    public final boolean isFakeDeath() {
        return _isFakeDeath;
    }

    public final void setIsFakeDeath(boolean value) {
        _isFakeDeath = value;
    }

    @Override
    public final boolean isAlikeDead() {
        if (super.isAlikeDead())
            return true;

        return isFakeDeath();
    }

    /**
     * @return The client owner of this char.
     */
    public GameClient getClient() {
        return _client;
    }

    public void setClient(GameClient client) {
        _client = client;
    }

    public String getAccountName() {
        return _accountName;
    }

    public Map<Integer, String> getAccountChars() {
        return _chars;
    }

    /**
     * Close the active connection with the {@link GameClient} linked to this {@link Player}.
     *
     * @param closeClient : If true, the client is entirely closed. Otherwise, the client is sent back to login.
     */
    public void logout(boolean closeClient) {
        final GameClient client = _client;
        if (client == null)
            return;

        if (client.isDetached())
            client.cleanMe(true);
        else if (!client.getConnection().isClosed())
            client.close((closeClient) ? LeaveWorld.STATIC_PACKET : ServerClose.STATIC_PACKET);
    }

    public Location getCurrentSkillWorldPosition() {
        return _currentSkillWorldPosition;
    }

    public void setCurrentSkillWorldPosition(Location worldPosition) {
        _currentSkillWorldPosition = worldPosition;
    }

    /**
     * @see Creature#enableSkill(L2Skill)
     */
    @Override
    public void enableSkill(L2Skill skill) {
        super.enableSkill(skill);
        _reuseTimeStamps.remove(skill.getReuseHashCode());
    }

    @Override
    public boolean checkDoCastConditions(L2Skill skill) {
        if (!super.checkDoCastConditions(skill))
            return false;

        // Can't summon multiple servitors.
        if (skill.getSkillType() == L2SkillType.SUMMON) {
            if (!((L2SkillSummon) skill).isCubic() && (_summon != null || isMounted())) {
                sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
                return false;
            }
        }
        // Can't use ressurect skills on siege if you are defender and control towers is not alive, if you are attacker and flag isn't spawned or if you aren't part of that siege.
        else if (skill.getSkillType() == L2SkillType.RESURRECT) {
            final Siege siege = CastleManager.getInstance().getActiveSiege(this);
            if (siege != null) {
                if (getClan() == null) {
                    sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
                    return false;
                }

                final SiegeSide side = siege.getSide(getClan());
                if (side == SiegeSide.DEFENDER || side == SiegeSide.OWNER) {
                    if (siege.getControlTowerCount() == 0) {
                        sendPacket(SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION);
                        return false;
                    }
                } else if (side == SiegeSide.ATTACKER) {
                    if (getClan().getFlag() == null) {
                        sendPacket(SystemMessageId.NO_RESURRECTION_WITHOUT_BASE_CAMP);
                        return false;
                    }
                } else {
                    sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
                    return false;
                }
            }
        }
        // Can't casting signets on peace zone.
        else if (skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME) {
            final WorldRegion region = getRegion();
            if (region == null)
                return false;

            if (!region.checkEffectRangeInsidePeaceZone(skill, (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND) ? getCurrentSkillWorldPosition() : getPosition())) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
                return false;
            }
        }

        // Can't use Hero and resurrect skills during Olympiad
        if (isInOlympiadMode() && (skill.isHeroSkill() || skill.getSkillType() == L2SkillType.RESURRECT)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
            return false;
        }

        // Check if the spell uses charges
        if (skill.getMaxCharges() == 0 && getCharges() < skill.getNumCharges()) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
            return false;
        }

        return true;
    }

    @Override
    public void onAction(Player player) {
        // Set the target of the player
        if (player.getTarget() != this)
            player.setTarget(this);
        else {
            // Check if this Player has a Private Store
            if (isInStoreMode()) {
                player.getAI().setIntention(IntentionType.INTERACT, this);
                return;
            }

            // Check if this Player is autoAttackable
            if (isAutoAttackable(player)) {
                // Player with lvl < 21 can't attack a cursed weapon holder and a cursed weapon holder can't attack players with lvl < 21
                if ((isCursedWeaponEquipped() && player.getLevel() < 21) || (player.isCursedWeaponEquipped() && getLevel() < 21)) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    return;
                }

                if (GeoEngine.getInstance().canSeeTarget(player, this)) {
                    player.getAI().setIntention(IntentionType.ATTACK, this);
                    player.onActionRequest();
                }
            } else {
                // avoids to stuck when clicking two or more times
                player.sendPacket(ActionFailed.STATIC_PACKET);

                if (player != this && GeoEngine.getInstance().canSeeTarget(player, this))
                    player.getAI().setIntention(IntentionType.FOLLOW, this);
            }
        }
    }

    @Override
    public void onActionShift(Player player) {
        if (player.isGM())
            AdminEditChar.showCharacterInfo(player, this);

        super.onActionShift(player);
    }

    /**
     * @param barPixels
     * @return true if cp update should be done, false if not
     */
    private boolean needCpUpdate(int barPixels) {
        double currentCp = getCurrentCp();

        if (currentCp <= 1.0 || getMaxCp() < barPixels)
            return true;

        if (currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck) {
            if (currentCp == getMaxCp()) {
                _cpUpdateIncCheck = currentCp + 1;
                _cpUpdateDecCheck = currentCp - _cpUpdateInterval;
            } else {
                double doubleMulti = currentCp / _cpUpdateInterval;
                int intMulti = (int) doubleMulti;

                _cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
                _cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
            }

            return true;
        }

        return false;
    }

    /**
     * @param barPixels
     * @return true if mp update should be done, false if not
     */
    private boolean needMpUpdate(int barPixels) {
        double currentMp = getCurrentMp();

        if (currentMp <= 1.0 || getMaxMp() < barPixels)
            return true;

        if (currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck) {
            if (currentMp == getMaxMp()) {
                _mpUpdateIncCheck = currentMp + 1;
                _mpUpdateDecCheck = currentMp - _mpUpdateInterval;
            } else {
                double doubleMulti = currentMp / _mpUpdateInterval;
                int intMulti = (int) doubleMulti;

                _mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
                _mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
            }

            return true;
        }

        return false;
    }

    /**
     * Send packet StatusUpdate with current HP,MP and CP to the Player and only current HP, MP and Level to all other Player of the Party.
     * <ul>
     * <li>Send StatusUpdate with current HP, MP and CP to this Player</li>
     * <li>Send PartySmallWindowUpdate with current HP, MP and Level to all other Player of the Party</li>
     * </ul>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all Player of the _statusListener</B></FONT>
     */
    @Override
    public void broadcastStatusUpdate() {
        // Send StatusUpdate with current HP, MP and CP to this Player
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
        su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
        su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
        su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
        sendPacket(su);

        final boolean needCpUpdate = needCpUpdate(352);
        final boolean needHpUpdate = needHpUpdate(352);

        // Check if a party is in progress and party window update is needed.
        if (_party != null && (needCpUpdate || needHpUpdate || needMpUpdate(352)))
            _party.broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));

        if (isInOlympiadMode() && isOlympiadStart() && (needCpUpdate || needHpUpdate)) {
            final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(getOlympiadGameId());
            if (game != null && game.isBattleStarted())
                game.getZone().broadcastStatusUpdate(this);
        }

        // In duel, MP updated only with CP or HP
        if (isInDuel() && (needCpUpdate || needHpUpdate)) {
            ExDuelUpdateUserInfo update = new ExDuelUpdateUserInfo(this);
            DuelManager.getInstance().broadcastToOppositeTeam(this, update);
        }
    }

    @Override
    public void broadcastPacket(L2GameServerPacket packet, boolean selfToo) {
        if (selfToo)
            sendPacket(packet);

        super.broadcastPacket(packet, selfToo);
    }

    @Override
    public void broadcastPacketInRadius(L2GameServerPacket packet, int radius) {
        sendPacket(packet);

        super.broadcastPacketInRadius(packet, radius);
    }

    /**
     * Broadcast informations from a user to himself and his knownlist.<BR>
     * If player is morphed, it sends informations from the template the player is using.
     * <ul>
     * <li>Send a UserInfo packet (public and private data) to this Player.</li>
     * <li>Send a CharInfo packet (public data only) to Player's knownlist.</li>
     * </ul>
     */
    public final void broadcastUserInfo() {
        sendPacket(new UserInfo(this));

        if (getPolyType() == PolyType.NPC)
            broadcastPacket(new AbstractNpcInfo.PcMorphInfo(this, getPolyTemplate()), false);
        else
            broadcastCharInfo();
    }

    public final void broadcastCharInfo() {
        for (Player player : getKnownType(Player.class)) {
            player.sendPacket(new CharInfo(this));

            final int relation = getRelation(player);
            final boolean isAutoAttackable = isAutoAttackable(player);

            player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
            if (_summon != null)
                player.sendPacket(new RelationChanged(_summon, relation, isAutoAttackable));
        }
    }

    /**
     * Broadcast player title information.
     */
    public final void broadcastTitleInfo() {
        sendPacket(new UserInfo(this));
        broadcastPacket(new TitleUpdate(this));
    }

    /**
     * @return the Alliance Identifier of the Player.
     */
    public int getAllyId() {
        if (_clan == null)
            return 0;

        return _clan.getAllyId();
    }

    public int getAllyCrestId() {
        if (getClanId() == 0)
            return 0;

        if (getClan().getAllyId() == 0)
            return 0;

        return getClan().getAllyCrestId();
    }

    /**
     * Send a packet to the Player.
     */
    @Override
    public void sendPacket(L2GameServerPacket packet) {
        if (_client != null)
            _client.sendPacket(packet);
    }

    /**
     * Send SystemMessage packet.
     *
     * @param id SystemMessageId
     */
    @Override
    public void sendPacket(SystemMessageId id) {
        sendPacket(SystemMessage.getSystemMessage(id));
    }

    /**
     * Manage Interact Task with another Player.<BR>
     * Turn the character in front of the target.<BR>
     * In case of private stores, send the related packet.
     *
     * @param target The Creature targeted
     */
    public void doInteract(Creature target) {
        if (target instanceof Player) {
            Player temp = (Player) target;
            sendPacket(new MoveToPawn(this, temp, Npc.INTERACTION_DISTANCE));

            switch (temp.getStoreType()) {
                case SELL:
                case PACKAGE_SELL:
                    sendPacket(new PrivateStoreListSell(this, temp));
                    break;

                case BUY:
                    sendPacket(new PrivateStoreListBuy(this, temp));
                    break;

                case MANUFACTURE:
                    sendPacket(new RecipeShopSellList(this, temp));
                    break;
            }
        } else {
            // _interactTarget=null should never happen but one never knows ^^;
            if (target != null)
                target.onAction(this);
        }
    }

    /**
     * Manage AutoLoot Task.
     * <ul>
     * <li>Send a System Message to the Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
     * <li>Add the Item to the Player inventory</li>
     * <li>Send InventoryUpdate to this Player with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
     * <li>Send StatusUpdate to this Player with current weight</li>
     * </ul>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT>
     *
     * @param target The reference Object.
     * @param item   The dropped ItemHolder.
     */
    public void doAutoLoot(Attackable target, IntIntHolder item) {
        if (isInParty())
            getParty().distributeItem(this, item, false, target);
        else if (item.getId() == 57)
            addAdena("Loot", item.getValue(), target, true);
        else
            addItem("Loot", item.getId(), item.getValue(), target, true);
    }

    /**
     * Manage Pickup Task.
     * <ul>
     * <li>Send StopMove to this Player</li>
     * <li>Remove the ItemInstance from the world and send GetItem packets</li>
     * <li>Send a System Message to the Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
     * <li>Add the Item to the Player inventory</li>
     * <li>Send InventoryUpdate to this Player with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
     * <li>Send StatusUpdate to this Player with current weight</li>
     * </ul>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT>
     *
     * @param object The ItemInstance to pick up
     */
    @Override
    public void doPickupItem(WorldObject object) {
        if (isAlikeDead() || isFakeDeath())
            return;

        // Set the AI Intention to IDLE
        getAI().setIntention(IntentionType.IDLE);

        // Check if the WorldObject to pick up is a ItemInstance
        if (!(object instanceof ItemInstance))
            return;

        ItemInstance item = (ItemInstance) object;

        // Send ActionFailed to this Player
        sendPacket(ActionFailed.STATIC_PACKET);
        sendPacket(new StopMove(this));

        synchronized (item) {
            if (!item.isVisible())
                return;

            if (isInStoreMode())
                return;

            if (!_inventory.validateWeight(item.getCount() * item.getItem().getWeight())) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
                return;
            }

            if (((isInParty() && getParty().getLootRule() == LootRule.ITEM_LOOTER) || !isInParty()) && !_inventory.validateCapacity(item)) {
                sendPacket(SystemMessageId.SLOTS_FULL);
                return;
            }

            if (getActiveTradeList() != null) {
                sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
                return;
            }

            if (item.getOwnerId() != 0 && !isLooterOrInLooterParty(item.getOwnerId())) {
                if (item.getItemId() == 57)
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(item.getCount()));
                else if (item.getCount() > 1)
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(item).addNumber(item.getCount()));
                else
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(item));

                return;
            }

            if (item.hasDropProtection())
                item.removeDropProtection();

            // Remove the ItemInstance from the world and send GetItem packets
            item.pickupMe(this);

            // item must be removed from ItemsOnGroundManager if is active
            ItemsOnGroundTaskManager.getInstance().remove(item);
        }

        // Auto use herbs - pick up
        if (item.getItemType() == EtcItemType.HERB) {
            IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
            if (handler != null)
                handler.useItem(this, item, false);

            item.destroyMe("Consume", this, null);
        }
        // Cursed Weapons are not distributed
        else if (CursedWeaponManager.getInstance().isCursed(item.getItemId())) {
            addItem("Pickup", item, null, true);
        } else {
            // if item is instance of L2ArmorType or WeaponType broadcast an "Attention" system message
            if (item.getItemType() instanceof ArmorType || item.getItemType() instanceof WeaponType) {
                SystemMessage msg;
                if (item.getEnchantLevel() > 0)
                    msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3).addString(getName()).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
                else
                    msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2).addString(getName()).addItemName(item.getItemId());

                broadcastPacketInRadius(msg, 1400);
            }

            // Check if a Party is in progress
            if (isInParty())
                getParty().distributeItem(this, item);
                // Target is adena
            else if (item.getItemId() == 57 && getInventory().getAdenaInstance() != null) {
                addAdena("Pickup", item.getCount(), null, true);
                item.destroyMe("Pickup", this, null);
            }
            // Target is regular item
            else
                addItem("Pickup", item, null, true);
        }

        // Schedule a paralyzed task to wait for the animation to finish
        ThreadPool.schedule(() -> setIsParalyzed(false), (int) (700 / getStat().getMovementSpeedMultiplier()));
        setIsParalyzed(true);
    }

    @Override
    public void doAttack(Creature target) {
        super.doAttack(target);
        clearRecentFakeDeath();
    }

    @Override
    public void doCast(L2Skill skill) {
        super.doCast(skill);
        clearRecentFakeDeath();
    }

    public boolean canOpenPrivateStore() {
        if (getActiveTradeList() != null)
            cancelActiveTrade();

        return !isAlikeDead() && !isInOlympiadMode() && !isMounted() && !isInsideZone(ZoneId.NO_STORE) && !isCastingNow();
    }

    public void tryOpenPrivateBuyStore() {
        if (canOpenPrivateStore()) {
            if (getStoreType() == StoreType.BUY || getStoreType() == StoreType.BUY_MANAGE)
                setStoreType(StoreType.NONE);

            if (getStoreType() == StoreType.NONE) {
                standUp();

                setStoreType(StoreType.BUY_MANAGE);
                sendPacket(new PrivateStoreManageListBuy(this));
            }
        } else {
            if (isInsideZone(ZoneId.NO_STORE))
                sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);

            sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public void tryOpenPrivateSellStore(boolean isPackageSale) {
        if (canOpenPrivateStore()) {
            if (getStoreType() == StoreType.SELL || getStoreType() == StoreType.SELL_MANAGE || getStoreType() == StoreType.PACKAGE_SELL)
                setStoreType(StoreType.NONE);

            if (getStoreType() == StoreType.NONE) {
                standUp();

                setStoreType(StoreType.SELL_MANAGE);
                sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
            }
        } else {
            if (isInsideZone(ZoneId.NO_STORE))
                sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);

            sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public void tryOpenWorkshop(boolean isDwarven) {
        if (canOpenPrivateStore()) {
            if (isInStoreMode())
                setStoreType(StoreType.NONE);

            if (getStoreType() == StoreType.NONE) {
                standUp();

                if (getCreateList() == null)
                    setCreateList(new ManufactureList());

                sendPacket(new RecipeShopManageList(this, isDwarven));
            }
        } else {
            if (isInsideZone(ZoneId.NO_STORE))
                sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);

            sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public final PreparedListContainer getMultiSell() {
        return _currentMultiSell;
    }

    public final void setMultiSell(PreparedListContainer list) {
        _currentMultiSell = list;
    }

    @Override
    public void setTarget(WorldObject newTarget) {
        // Check if the new target is visible.
        if (newTarget != null && !newTarget.isVisible() && !(newTarget instanceof Player && isInParty() && _party.containsPlayer(newTarget)))
            newTarget = null;

        // Can't target and attack festival monsters if not participant
        if ((newTarget instanceof FestivalMonster) && !isFestivalParticipant())
            newTarget = null;
            // Can't target and attack rift invaders if not in the same room
        else if (newTarget != null && isInParty() && getParty().isInDimensionalRift() && !getParty().getDimensionalRift().isInCurrentRoomZone(newTarget))
            newTarget = null;

        // Get the current target
        WorldObject oldTarget = getTarget();

        if (oldTarget != null) {
            if (oldTarget.equals(newTarget))
                return; // no target change

            // Remove the Player from the _statusListener of the old target if it was a Creature
            if (oldTarget instanceof Creature)
                ((Creature) oldTarget).removeStatusListener(this);
        }

        // Verify if it's a static object.
        if (newTarget instanceof StaticObject) {
            sendPacket(new MyTargetSelected(newTarget.getObjectId(), 0));
            sendPacket(new StaticObjectInfo((StaticObject) newTarget));
        }
        // Add the Player to the _statusListener of the new target if it's a Creature
        else if (newTarget instanceof Creature) {
            final Creature target = (Creature) newTarget;

            // Validate location of the new target.
            if (newTarget.getObjectId() != getObjectId())
                sendPacket(new ValidateLocation(target));

            // Show the client his new target.
            sendPacket(new MyTargetSelected(target.getObjectId(), (target.isAutoAttackable(this) || target instanceof Summon) ? getLevel() - target.getLevel() : 0));

            target.addStatusListener(this);

            // Send max/current hp.
            final StatusUpdate su = new StatusUpdate(target);
            su.addAttribute(StatusUpdate.MAX_HP, target.getMaxHp());
            su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
            sendPacket(su);

            broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ()), false);
        }

        if (newTarget instanceof Folk)
            setCurrentFolk((Folk) newTarget);
        else if (newTarget == null) {
            sendPacket(ActionFailed.STATIC_PACKET);

            if (getTarget() != null) {
                broadcastPacket(new TargetUnselected(this));
                setCurrentFolk(null);
            }
        }

        // Target the new WorldObject
        super.setTarget(newTarget);
    }

    /**
     * Return the active weapon instance (always equipped in the right hand).
     */
    @Override
    public ItemInstance getActiveWeaponInstance() {
        return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
    }

    /**
     * Return the active weapon item (always equipped in the right hand).
     */
    @Override
    public Weapon getActiveWeaponItem() {
        final ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
        return (weapon == null) ? getTemplate().getFists() : (Weapon) weapon.getItem();
    }

    /**
     * @return the type of attack, depending of the worn weapon.
     */
    @Override
    public WeaponType getAttackType() {
        return getActiveWeaponItem().getItemType();
    }

    /**
     * @param type : The ArmorType to check. It supports NONE, SHIELD, MAGIC, LIGHT and HEAVY.
     * @return true if the given ArmorType is used by the chest of the player, false otherwise.
     */
    public boolean isWearingArmorType(ArmorType type) {
        // Retrieve either the shield or the chest, following ArmorType to check.
        final ItemInstance armor = getInventory().getPaperdollItem((type == ArmorType.SHIELD) ? Inventory.PAPERDOLL_LHAND : Inventory.PAPERDOLL_CHEST);
        if (armor == null)
            return type == ArmorType.NONE; // Return true if not equipped and the check was based on NONE ArmorType.

        // Test if the equipped item is an armor, then finally compare both ArmorType.
        return armor.getItemType() instanceof ArmorType && armor.getItemType() == type;
    }

    public boolean isUnderMarryRequest() {
        return _isUnderMarryRequest;
    }

    public void setUnderMarryRequest(boolean state) {
        _isUnderMarryRequest = state;
    }

    public int getCoupleId() {
        return _coupleId;
    }

    public void setCoupleId(int coupleId) {
        _coupleId = coupleId;
    }

    public void setRequesterId(int requesterId) {
        _requesterId = requesterId;
    }

    public void engageAnswer(int answer) {
        if (!_isUnderMarryRequest || _requesterId == 0)
            return;

        final Player requester = World.getInstance().getPlayer(_requesterId);
        if (requester != null) {
            if (answer == 1) {
                // Create the couple
                CoupleManager.getInstance().addCouple(requester, this);

                // Then "finish the job"
                WeddingManagerNpc.justMarried(requester, this);
            } else {
                setUnderMarryRequest(false);
                sendMessage("You declined your partner's marriage request.");

                requester.setUnderMarryRequest(false);
                requester.sendMessage("Your partner declined your marriage request.");
            }
        }
    }

    /**
     * Return the secondary weapon instance (always equipped in the left hand).
     */
    @Override
    public ItemInstance getSecondaryWeaponInstance() {
        return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
    }

    /**
     * Return the secondary L2Item item (always equiped in the left hand).
     */
    @Override
    public Item getSecondaryWeaponItem() {
        ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
        if (item != null)
            return item.getItem();

        return null;
    }

    /**
     * Kill the Creature, Apply Death Penalty, Manage gain/loss Karma and Item Drop.
     * <ul>
     * <li>Reduce the Experience of the Player in function of the calculated Death Penalty</li>
     * <li>If necessary, unsummon the Pet of the killed Player</li>
     * <li>Manage Karma gain for attacker and Karam loss for the killed Player</li>
     * <li>If the killed Player has Karma, manage Drop Item</li>
     * <li>Kill the Player</li>
     * </ul>
     *
     * @param killer The Creature who attacks
     */
    @Override
    public boolean doDie(Creature killer) {
        // Kill the Player
        if (!super.doDie(killer))
            return false;

        if (isMounted())
            stopFeed();

        synchronized (this) {
            if (isFakeDeath())
                stopFakeDeath(true);
        }

        if (killer != null) {
            Player pk = killer.getActingPlayer();

            // Clear resurrect xp calculation
            setExpBeforeDeath(0);

            if (isCursedWeaponEquipped())
                CursedWeaponManager.getInstance().drop(_cursedWeaponEquippedId, killer);
            else {
                if (pk == null || !pk.isCursedWeaponEquipped()) {
                    onDieDropItem(killer); // Check if any item should be dropped

                    // if the area isn't an arena
                    if (!isInArena()) {
                        // if both victim and attacker got clans & aren't academicians
                        if (pk != null && pk.getClan() != null && getClan() != null && !isAcademyMember() && !pk.isAcademyMember()) {
                            // if clans got mutual war, then use the reputation calcul
                            if (_clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(_clan.getClanId())) {
                                // when your reputation score is 0 or below, the other clan cannot acquire any reputation points
                                if (getClan().getReputationScore() > 0)
                                    pk.getClan().addReputationScore(1);
                                // when the opposing sides reputation score is 0 or below, your clans reputation score doesn't decrease
                                if (pk.getClan().getReputationScore() > 0)
                                    _clan.takeReputationScore(1);
                            }
                        }
                    }

                    // Reduce player's xp and karma.
                    if (Config.ALT_GAME_DELEVEL && (!hasSkill(L2Skill.SKILL_LUCKY) || getStat().getLevel() > 9))
                        deathPenalty(pk != null && getClan() != null && pk.getClan() != null && (getClan().isAtWarWith(pk.getClanId()) || pk.getClan().isAtWarWith(getClanId())), pk != null, killer instanceof SiegeGuard);
                }
            }
        }

        // Unsummon Cubics
        if (!_cubics.isEmpty()) {
            for (Cubic cubic : _cubics.values()) {
                cubic.stopAction();
                cubic.cancelDisappear();
            }

            _cubics.clear();
        }

        if (_fusionSkill != null)
            abortCast();

        for (Creature character : getKnownType(Creature.class))
            if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
                character.abortCast();

        // calculate death penalty buff
        calculateDeathPenaltyBuffLevel(killer);

        WaterTaskManager.getInstance().remove(this);

        if (isPhoenixBlessed() || (isAffected(L2EffectFlag.CHARM_OF_COURAGE) && isInSiege()))
            reviveRequest(this, null, false);

        // Icons update in order to get retained buffs list
        updateEffectIcons();

        return true;
    }

    private void onDieDropItem(Creature killer) {
        if (killer == null)
            return;

        Player pk = killer.getActingPlayer();
        if (getKarma() <= 0 && pk != null && pk.getClan() != null && getClan() != null && pk.getClan().isAtWarWith(getClanId()))
            return;

        if ((!isInsideZone(ZoneId.PVP) || pk == null) && (!isGM() || Config.KARMA_DROP_GM)) {
            boolean isKillerNpc = (killer instanceof Npc);
            int pkLimit = Config.KARMA_PK_LIMIT;

            int dropEquip = 0;
            int dropEquipWeapon = 0;
            int dropItem = 0;
            int dropLimit = 0;
            int dropPercent = 0;

            if (getKarma() > 0 && getPkKills() >= pkLimit) {
                dropPercent = Config.KARMA_RATE_DROP;
                dropEquip = Config.KARMA_RATE_DROP_EQUIP;
                dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
                dropItem = Config.KARMA_RATE_DROP_ITEM;
                dropLimit = Config.KARMA_DROP_LIMIT;
            } else if (isKillerNpc && getLevel() > 4 && !isFestivalParticipant()) {
                dropPercent = Config.PLAYER_RATE_DROP;
                dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
                dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
                dropItem = Config.PLAYER_RATE_DROP_ITEM;
                dropLimit = Config.PLAYER_DROP_LIMIT;
            }

            if (dropPercent > 0 && Rnd.get(100) < dropPercent) {
                int dropCount = 0;
                int itemDropPercent = 0;

                for (ItemInstance itemDrop : getInventory().getItems()) {
                    // Don't drop those following things
                    if (!itemDrop.isDropable() || itemDrop.isShadowItem() || itemDrop.getItemId() == 57 || itemDrop.getItem().getType2() == Item.TYPE2_QUEST || _summon != null && _summon.getControlItemId() == itemDrop.getItemId() || Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_ITEMS, itemDrop.getItemId()) >= 0 || Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS, itemDrop.getItemId()) >= 0)
                        continue;

                    if (itemDrop.isEquipped()) {
                        // Set proper chance according to Item type of equipped Item
                        itemDropPercent = itemDrop.getItem().getType2() == Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
                        getInventory().unEquipItemInSlot(itemDrop.getLocationSlot());
                    } else
                        itemDropPercent = dropItem; // Item in inventory

                    // NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
                    if (Rnd.get(100) < itemDropPercent) {
                        dropItem("DieDrop", itemDrop, killer, true);

                        if (++dropCount >= dropLimit)
                            break;
                    }
                }
            }
        }
    }

    public void updateKarmaLoss(long exp) {
        if (!isCursedWeaponEquipped() && getKarma() > 0) {
            int karmaLost = Formulas.calculateKarmaLost(getLevel(), exp);
            if (karmaLost > 0)
                setKarma(getKarma() - karmaLost);
        }
    }

    /**
     * This method is used to update PvP counter, or PK counter / add Karma if necessary.<br>
     * It also updates clan kills/deaths counters on siege.
     *
     * @param target The L2Playable victim.
     */
    public void onKillUpdatePvPKarma(Playable target) {
        if (target == null)
            return;

        final Player targetPlayer = target.getActingPlayer();
        if (targetPlayer == null || targetPlayer == this)
            return;

        // Don't rank up the CW if it was a summon.
        if (isCursedWeaponEquipped() && target instanceof Player) {
            CursedWeaponManager.getInstance().increaseKills(_cursedWeaponEquippedId);
            return;
        }

        // If in duel and you kill (only can kill l2summon), do nothing
        if (isInDuel() && targetPlayer.isInDuel())
            return;

        // If in pvp zone, do nothing.
        if (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP)) {
            // Until the zone was a siege zone. Check also if victim was a player. Randomers aren't counted.
            if (target instanceof Player && getSiegeState() > 0 && targetPlayer.getSiegeState() > 0 && getSiegeState() != targetPlayer.getSiegeState()) {
                // Now check clan relations.
                final Clan killerClan = getClan();
                if (killerClan != null)
                    killerClan.setSiegeKills(killerClan.getSiegeKills() + 1);

                final Clan targetClan = targetPlayer.getClan();
                if (targetClan != null)
                    targetClan.setSiegeDeaths(targetClan.getSiegeDeaths() + 1);
            }
            return;
        }

        // Check if it's pvp (cases : regular, wars, victim is PKer)
        if (checkIfPvP(target) || (targetPlayer.getClan() != null && getClan() != null && getClan().isAtWarWith(targetPlayer.getClanId()) && targetPlayer.getClan().isAtWarWith(getClanId()) && targetPlayer.getPledgeType() != Clan.SUBUNIT_ACADEMY && getPledgeType() != Clan.SUBUNIT_ACADEMY) || (targetPlayer.getKarma() > 0 && Config.KARMA_AWARD_PK_KILL)) {
            if (target instanceof Player) {
                // Add PvP point to attacker.
                setPvpKills(getPvpKills() + 1);

                // Send UserInfo packet to attacker with its Karma and PK Counter
                sendPacket(new UserInfo(this));
            }
        }
        // Otherwise, killer is considered as a PKer.
        else if (targetPlayer.getKarma() == 0 && targetPlayer.getPvpFlag() == 0) {
            // PK Points are increased only if you kill a player.
            if (target instanceof Player)
                setPkKills(getPkKills() + 1);

            // Calculate new karma.
            setKarma(getKarma() + Formulas.calculateKarmaGain(getPkKills(), target instanceof Summon));

            // Send UserInfo packet to attacker with its Karma and PK Counter
            sendPacket(new UserInfo(this));
        }
    }

    public void updatePvPStatus() {
        if (isInsideZone(ZoneId.PVP))
            return;

        PvpFlagTaskManager.getInstance().add(this, Config.PVP_NORMAL_TIME);

        if (getPvpFlag() == 0)
            updatePvPFlag(1);
    }

    public void updatePvPStatus(Creature target) {
        final Player player = target.getActingPlayer();
        if (player == null)
            return;

        if (isInDuel() && player.getDuelId() == getDuelId())
            return;

        if ((!isInsideZone(ZoneId.PVP) || !target.isInsideZone(ZoneId.PVP)) && player.getKarma() == 0) {
            PvpFlagTaskManager.getInstance().add(this, checkIfPvP(player) ? Config.PVP_PVP_TIME : Config.PVP_NORMAL_TIME);

            if (getPvpFlag() == 0)
                updatePvPFlag(1);
        }
    }

    /**
     * Restore the experience this Player has lost and sends StatusUpdate packet.
     *
     * @param restorePercent The specified % of restored experience.
     */
    public void restoreExp(double restorePercent) {
        if (getExpBeforeDeath() > 0) {
            getStat().addExp((int) Math.round((getExpBeforeDeath() - getExp()) * restorePercent / 100));
            setExpBeforeDeath(0);
        }
    }

    /**
     * Reduce the Experience (and level if necessary) of the Player in function of the calculated Death Penalty.
     * <ul>
     * <li>Calculate the Experience loss</li>
     * <li>Set the value of _expBeforeDeath</li>
     * <li>Set the new Experience value of the Player and Decrease its level if necessary</li>
     * <li>Send StatusUpdate packet with its new Experience</li>
     * </ul>
     *
     * @param atWar            If true, use clan war penalty system instead of regular system.
     * @param killedByPlayable Used to see if victim loses XP or not.
     * @param killedBySiegeNpc Used to see if victim loses XP or not.
     */
    public void deathPenalty(boolean atWar, boolean killedByPlayable, boolean killedBySiegeNpc) {
        // No xp loss inside pvp zone unless
        // - it's a siege zone and you're NOT participating
        // - you're killed by a non-pc whose not belong to the siege
        if (isInsideZone(ZoneId.PVP)) {
            // No xp loss for siege participants inside siege zone.
            if (isInsideZone(ZoneId.SIEGE)) {
                if (isInSiege() && (killedByPlayable || killedBySiegeNpc))
                    return;
            }
            // No xp loss for arenas participants killed by playable.
            else if (killedByPlayable)
                return;
        }

        // Get the level of the Player
        final int lvl = getLevel();

        // The death steal you some Exp
        double percentLost = 7.0;
        if (getLevel() >= 76)
            percentLost = 2.0;
        else if (getLevel() >= 40)
            percentLost = 4.0;

        if (getKarma() > 0)
            percentLost *= Config.RATE_KARMA_EXP_LOST;

        if (isFestivalParticipant() || atWar || isInsideZone(ZoneId.SIEGE))
            percentLost /= 4.0;

        // Calculate the Experience loss
        long lostExp = 0;

        if (lvl < Experience.MAX_LEVEL)
            lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
        else
            lostExp = Math.round((getStat().getExpForLevel(Experience.MAX_LEVEL) - getStat().getExpForLevel(Experience.MAX_LEVEL - 1)) * percentLost / 100);

        // Get the Experience before applying penalty
        setExpBeforeDeath(getExp());

        // Set new karma
        updateKarmaLoss(lostExp);

        // Set the new Experience value of the Player
        getStat().addExp(-lostExp);
    }

    public int getPartyRoom() {
        return _partyroom;
    }

    public boolean isInPartyMatchRoom() {
        return _partyroom > 0;
    }

    public void setPartyRoom(int id) {
        _partyroom = id;
    }

    /**
     * Remove the player from both waiting list and any potential room.
     */
    public void removeMeFromPartyMatch() {
        PartyMatchWaitingList.getInstance().removePlayer(this);
        if (_partyroom != 0) {
            PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyroom);
            if (room != null)
                room.deleteMember(this);
        }
    }

    @Override
    public Summon getSummon() {
        return _summon;
    }

    /**
     * Set the {@link Summon} of this {@link Player}.
     *
     * @param summon : The Summon to set.
     */
    public void setSummon(Summon summon) {
        _summon = summon;
    }

    /**
     * @return true if this {@link Player} has a {@link Pet}, false otherwise.
     */
    public boolean hasPet() {
        return _summon instanceof Pet;
    }

    /**
     * @return true if this {@link Player} has a {@link Servitor}, false otherwise.
     */
    public boolean hasServitor() {
        return _summon instanceof Servitor;
    }

    /**
     * @return the {@link TamedBeast} of this {@link Player}, null otherwise.
     */
    public TamedBeast getTrainedBeast() {
        return _tamedBeast;
    }

    /**
     * Set the {@link TamedBeast} of this {@link Player}.
     *
     * @param tamedBeast : The TamedBeast to set.
     */
    public void setTrainedBeast(TamedBeast tamedBeast) {
        _tamedBeast = tamedBeast;
    }

    /**
     * @return the current {@link Request}.
     */
    public Request getRequest() {
        return _request;
    }

    /**
     * Set the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
     *
     * @param requester
     */
    public void setActiveRequester(Player requester) {
        _activeRequester = requester;
    }

    /**
     * @return the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
     */
    public Player getActiveRequester() {
        if (_activeRequester != null && _activeRequester.isRequestExpired() && _activeTradeList == null)
            _activeRequester = null;

        return _activeRequester;
    }

    /**
     * @return True if a request is in progress.
     */
    public boolean isProcessingRequest() {
        return getActiveRequester() != null || _requestExpireTime > System.currentTimeMillis();
    }

    /**
     * @return True if a transaction <B>(trade OR request)</B> is in progress.
     */
    public boolean isProcessingTransaction() {
        return getActiveRequester() != null || _activeTradeList != null || _requestExpireTime > System.currentTimeMillis();
    }

    /**
     * Set the _requestExpireTime of that Player, and set his partner as the active requester.
     *
     * @param partner The partner to make checks on.
     */
    public void onTransactionRequest(Player partner) {
        _requestExpireTime = System.currentTimeMillis() + REQUEST_TIMEOUT * 1000;
        partner.setActiveRequester(this);
    }

    /**
     * @return true if last request is expired.
     */
    public boolean isRequestExpired() {
        return _requestExpireTime <= System.currentTimeMillis();
    }

    /**
     * Select the Warehouse to be used in next activity.
     */
    public void onTransactionResponse() {
        _requestExpireTime = 0;
    }

    /**
     * Select the Warehouse to be used in next activity.
     *
     * @param warehouse An active warehouse.
     */
    public void setActiveWarehouse(ItemContainer warehouse) {
        _activeWarehouse = warehouse;
    }

    /**
     * @return The active Warehouse.
     */
    public ItemContainer getActiveWarehouse() {
        return _activeWarehouse;
    }

    /**
     * Set the TradeList to be used in next activity.
     *
     * @param tradeList The TradeList to be used.
     */
    public void setActiveTradeList(TradeList tradeList) {
        _activeTradeList = tradeList;
    }

    /**
     * @return The active TradeList.
     */
    public TradeList getActiveTradeList() {
        return _activeTradeList;
    }

    public void onTradeStart(Player partner) {
        _activeTradeList = new TradeList(this);
        _activeTradeList.setPartner(partner);

        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1).addString(partner.getName()));
        sendPacket(new TradeStart(this));
    }

    public void onTradeConfirm(Player partner) {
        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CONFIRMED_TRADE).addString(partner.getName()));

        partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
        sendPacket(TradePressOtherOk.STATIC_PACKET);
    }

    public void onTradeCancel(Player partner) {
        if (_activeTradeList == null)
            return;

        _activeTradeList.lock();
        _activeTradeList = null;

        sendPacket(new SendTradeDone(0));
        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELED_TRADE).addString(partner.getName()));
    }

    public void onTradeFinish(boolean successfull) {
        _activeTradeList = null;
        sendPacket(new SendTradeDone(1));
        if (successfull)
            sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
    }

    public void startTrade(Player partner) {
        onTradeStart(partner);
        partner.onTradeStart(this);
    }

    public void cancelActiveTrade() {
        if (_activeTradeList == null)
            return;

        Player partner = _activeTradeList.getPartner();
        if (partner != null)
            partner.onTradeCancel(this);

        onTradeCancel(this);
    }

    /**
     * @return The _createList object of the Player.
     */
    public ManufactureList getCreateList() {
        return _createList;
    }

    /**
     * Set the _createList object of the Player.
     *
     * @param list
     */
    public void setCreateList(ManufactureList list) {
        _createList = list;
    }

    /**
     * @return The _sellList object of the Player.
     */
    public TradeList getSellList() {
        if (_sellList == null)
            _sellList = new TradeList(this);

        return _sellList;
    }

    /**
     * @return the _buyList object of the Player.
     */
    public TradeList getBuyList() {
        if (_buyList == null)
            _buyList = new TradeList(this);

        return _buyList;
    }

    /**
     * Set the Store type of the Player.
     *
     * @param type : 0 = none, 1 = sell, 2 = sellmanage, 3 = buy, 4 = buymanage, 5 = manufacture.
     */
    public void setStoreType(StoreType type) {
        _storeType = type;
    }

    /**
     * @return The Store type of the Player.
     */
    public StoreType getStoreType() {
        return _storeType;
    }

    /**
     * @return true if the {@link Player} can use dwarven recipes.
     */
    public boolean hasDwarvenCraft() {
        return hasSkill(L2Skill.SKILL_CREATE_DWARVEN);
    }

    /**
     * @return true if the {@link Player} can use common recipes.
     */
    public boolean hasCommonCraft() {
        return hasSkill(L2Skill.SKILL_CREATE_COMMON);
    }

    /**
     * Method used by regular leveling system.<br>
     * Reward the {@link Player} with autoGet skills only, or if Config.AUTO_LEARN_SKILLS is activated, with all available skills.
     */
    public void giveSkills() {
        if (Config.AUTO_LEARN_SKILLS)
            rewardSkills();
        else {
            // We reward all autoGet skills to this player, but don't store any on database.
            for (GeneralSkillNode skill : getAvailableAutoGetSkills())
                addSkill(skill.getSkill(), false);

            // Remove the Lucky skill if level superior to 10.
            if (getLevel() >= 10 && hasSkill(L2Skill.SKILL_LUCKY))
                removeSkill(L2Skill.SKILL_LUCKY, false);

            // Remove invalid skills.
            removeInvalidSkills();

            sendSkillList();
        }
    }

    /**
     * Method used by admin commands, Config.AUTO_LEARN_SKILLS or class master.<br>
     * Reward the {@link Player} with all available skills, being autoGet or general skills.
     */
    public void rewardSkills() {
        // We reward all skills to the players, but don't store autoGet skills on the database.
        for (GeneralSkillNode skill : getAllAvailableSkills())
            addSkill(skill.getSkill(), skill.getCost() != 0);

        // Remove the Lucky skill if level superior to 10.
        if (getLevel() >= 10 && hasSkill(L2Skill.SKILL_LUCKY))
            removeSkill(L2Skill.SKILL_LUCKY, false);

        // Remove invalid skills.
        removeInvalidSkills();

        sendSkillList();
    }

    /**
     * Delete all invalid {@link L2Skill}s for this {@link Player}.<br>
     * <br>
     * A skill is considered invalid when the level of obtention of the skill is superior to 9 compared to player level (expertise skill obtention level is compared to player level without any penalty).<br>
     * <br>
     * It is then either deleted, or level is refreshed.
     */
    private void removeInvalidSkills() {
        if (getSkills().isEmpty())
            return;

        // Retrieve the player template skills, based on actual level (+9 for regular skills, unchanged for expertise).
        final Map<Integer, Optional<GeneralSkillNode>> availableSkills = getTemplate().getSkills().stream().filter(s -> s.getMinLvl() <= getLevel() + ((s.getId() == L2Skill.SKILL_EXPERTISE) ? 0 : 9)).collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL)));

        for (L2Skill skill : getSkills().values()) {
            // Bother only with skills existing on template (spare temporary skills, items skills, etc).
            if (getTemplate().getSkills().stream().filter(s -> s.getId() == skill.getId()).count() == 0)
                continue;

            // The known skill doesn't exist on available skills ; we drop existing skill.
            final Optional<GeneralSkillNode> tempSkill = availableSkills.get(skill.getId());
            if (tempSkill == null) {
                removeSkill(skill.getId(), true);
                continue;
            }

            // Retrieve the skill and max level for enchant scenario.
            final GeneralSkillNode availableSkill = tempSkill.get();
            final int maxLevel = SkillTable.getInstance().getMaxLevel(skill.getId());

            // Case of enchanted skills.
            if (skill.getLevel() > maxLevel) {
                // Player level is inferior to 76, or available skill is a good candidate.
                if ((getLevel() < 76 || availableSkill.getValue() < maxLevel) && skill.getLevel() > availableSkill.getValue())
                    addSkill(availableSkill.getSkill(), true);
            }
            // We check if current known skill level is bigger than available skill level. If it's true, we override current skill with available skill.
            else if (skill.getLevel() > availableSkill.getValue())
                addSkill(availableSkill.getSkill(), true);
        }
    }

    /**
     * Regive all skills which aren't saved to database, like Noble, Hero, Clan Skills.<br>
     * <b>Do not call this on enterworld or char load.</b>.
     */
    private void regiveTemporarySkills() {
        // Add noble skills if noble.
        if (isNoble())
            setNoble(true, false);

        // Add Hero skills if hero.
        if (isHero())
            setHero(true);

        // Add clan skills.
        if (getClan() != null) {
            getClan().addSkillEffects(this);

            if (getClan().getLevel() >= Config.MINIMUM_CLAN_LEVEL && isClanLeader())
                addSiegeSkills();
        }

        // Reload passive skills from armors / jewels / weapons
        getInventory().reloadEquippedItems();

        // Add Death Penalty Buff Level
        restoreDeathPenaltyBuffLevel();
    }

    public void addSiegeSkills() {
        for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(isNoble()))
            addSkill(sk, false);
    }

    public void removeSiegeSkills() {
        for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(isNoble()))
            removeSkill(sk.getId(), false);
    }

    /**
     * @return a {@link List} of all available autoGet {@link GeneralSkillNode}s <b>of maximal level</b> for this {@link Player}.
     */
    public List<GeneralSkillNode> getAvailableAutoGetSkills() {
        final List<GeneralSkillNode> result = new ArrayList<>();

        getTemplate().getSkills().stream().filter(s -> s.getMinLvl() <= getLevel() && s.getCost() == 0).collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL))).forEach((i, s) ->
        {
            if (getSkillLevel(i) < s.get().getValue())
                result.add(s.get());
        });
        return result;
    }

    /**
     * @return a {@link List} of available {@link GeneralSkillNode}s (only general) for this {@link Player}.
     */
    public List<GeneralSkillNode> getAvailableSkills() {
        final List<GeneralSkillNode> result = new ArrayList<>();

        getTemplate().getSkills().stream().filter(s -> s.getMinLvl() <= getLevel() && s.getCost() != 0).forEach(s ->
        {
            if (getSkillLevel(s.getId()) == s.getValue() - 1)
                result.add(s);
        });
        return result;
    }

    /**
     * @return a {@link List} of all available {@link GeneralSkillNode}s (being general or autoGet) <b>of maximal level</b> for this {@link Player}.
     */
    public List<GeneralSkillNode> getAllAvailableSkills() {
        final List<GeneralSkillNode> result = new ArrayList<>();

        getTemplate().getSkills().stream().filter(s -> s.getMinLvl() <= getLevel()).collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL))).forEach((i, s) ->
        {
            if (getSkillLevel(i) < s.get().getValue())
                result.add(s.get());
        });
        return result;
    }

    /**
     * Retrieve next lowest level skill to learn, based on current player level and skill sp cost.
     *
     * @return the required level for next {@link GeneralSkillNode} to learn for this {@link Player}.
     */
    public int getRequiredLevelForNextSkill() {
        return getTemplate().getSkills().stream().filter(s -> s.getMinLvl() > getLevel() && s.getCost() != 0).min(COMPARE_SKILLS_BY_MIN_LVL).map(s -> s.getMinLvl()).orElse(0);
    }

    /**
     * Set the _clan object, _clanId, _clanLeader Flag and title of the Player.
     *
     * @param clan The Clan object which is used to feed Player values.
     */
    public void setClan(Clan clan) {
        _clan = clan;
        setTitle("");

        if (clan == null) {
            _clanId = 0;
            _clanPrivileges = 0;
            _pledgeType = 0;
            _powerGrade = 0;
            _lvlJoinedAcademy = 0;
            _apprentice = 0;
            _sponsor = 0;
            return;
        }

        if (!clan.isMember(getObjectId())) {
            // char has been kicked from clan
            setClan(null);
            return;
        }

        _clanId = clan.getClanId();
    }

    /**
     * @return The _clan object of the Player.
     */
    public Clan getClan() {
        return _clan;
    }

    /**
     * @return True if the Player is the leader of its clan.
     */
    public boolean isClanLeader() {
        return _clan != null && getObjectId() == _clan.getLeaderId();
    }

    /**
     * Reduce the number of arrows owned by the Player and send InventoryUpdate or ItemList (to unequip if the last arrow was consummed).
     */
    @Override
    protected void reduceArrowCount() // TODO: replace with a simple player.destroyItem...
    {
        final ItemInstance arrows = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
        if (arrows == null)
            return;

        final InventoryUpdate iu = new InventoryUpdate();

        if (arrows.getCount() > 1) {
            synchronized (arrows) {
                arrows.changeCount(null, -1, this, null);
                arrows.setLastChange(ItemState.MODIFIED);

                iu.addModifiedItem(arrows);

                // could do also without saving, but let's save approx 1 of 10
                if (Rnd.get(10) < 1)
                    arrows.updateDatabase();

                _inventory.refreshWeight();
            }
        } else {
            iu.addRemovedItem(arrows);

            // Destroy entire item and save to database
            _inventory.destroyItem("Consume", arrows, this, null);
        }
        sendPacket(iu);
    }

    /**
     * Check if the arrow item exists on inventory and is already slotted ; if not, equip it.
     */
    @Override
    protected boolean checkAndEquipArrows() {
        // Retrieve arrows instance on player inventory.
        final ItemInstance arrows = getInventory().findArrowForBow(getActiveWeaponItem());
        if (arrows == null)
            return false;

        // Arrows are already equiped, don't bother.
        if (arrows.getLocation() == ItemLocation.PAPERDOLL)
            return true;

        // Equip arrows in left hand.
        getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, arrows);

        // Send ItemList to this player to update left hand equipement
        sendPacket(new ItemList(this, false));

        return true;
    }

    /**
     * Disarm the player's weapon and shield.
     *
     * @return true if successful, false otherwise.
     */
    public boolean disarmWeapons() {
        // Don't allow disarming a cursed weapon
        if (isCursedWeaponEquipped())
            return false;

        // Unequip the weapon
        ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
        if (wpn != null) {
            ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(wpn);
            InventoryUpdate iu = new InventoryUpdate();
            for (ItemInstance itm : unequipped)
                iu.addModifiedItem(itm);
            sendPacket(iu);

            abortAttack();
            broadcastUserInfo();

            // this can be 0 if the user pressed the right mousebutton twice very fast
            if (unequipped.length > 0) {
                SystemMessage sm;
                if (unequipped[0].getEnchantLevel() > 0)
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequipped[0].getEnchantLevel()).addItemName(unequipped[0]);
                else
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0]);

                sendPacket(sm);
            }
        }

        // Unequip the shield
        ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
        if (sld != null) {
            ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(sld);
            InventoryUpdate iu = new InventoryUpdate();
            for (ItemInstance itm : unequipped)
                iu.addModifiedItem(itm);
            sendPacket(iu);

            abortAttack();
            broadcastUserInfo();

            // this can be 0 if the user pressed the right mousebutton twice very fast
            if (unequipped.length > 0) {
                SystemMessage sm;
                if (unequipped[0].getEnchantLevel() > 0)
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequipped[0].getEnchantLevel()).addItemName(unequipped[0]);
                else
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0]);

                sendPacket(sm);
            }
        }
        return true;
    }

    public boolean mount(Summon pet) {
        if (!disarmWeapons())
            return false;

        setRunning();
        stopAllToggles();

        Ride mount = new Ride(getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().getNpcId());
        setMount(pet.getNpcId(), pet.getLevel(), mount.getMountType());

        _petTemplate = (PetTemplate) pet.getTemplate();
        _petData = _petTemplate.getPetDataEntry(pet.getLevel());
        _mountObjectId = pet.getControlItemId();

        startFeed(pet.getNpcId());
        broadcastPacket(mount);

        // Notify self and others about speed change
        broadcastUserInfo();

        pet.unSummon(this);
        return true;
    }

    public boolean mount(int npcId, int controlItemId) {
        if (!disarmWeapons())
            return false;

        setRunning();
        stopAllToggles();

        Ride mount = new Ride(getObjectId(), Ride.ACTION_MOUNT, npcId);
        if (setMount(npcId, getLevel(), mount.getMountType())) {
            _petTemplate = (PetTemplate) NpcData.getInstance().getTemplate(npcId);
            _petData = _petTemplate.getPetDataEntry(getLevel());
            _mountObjectId = controlItemId;

            broadcastPacket(mount);

            // Notify self and others about speed change
            broadcastUserInfo();

            startFeed(npcId);

            return true;
        }
        return false;
    }

    public boolean mountPlayer(Summon summon) {
        if (summon instanceof Pet && summon.isMountable() && !isMounted() && !isBetrayed()) {
            if (isDead()) // A strider cannot be ridden when dead.
            {
                sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
                return false;
            }

            if (summon.isDead()) // A dead strider cannot be ridden.
            {
                sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
                return false;
            }

            if (summon.isInCombat() || summon.isRooted()) // A strider in battle cannot be ridden.
            {
                sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
                return false;
            }

            if (isInCombat()) // A strider cannot be ridden while in battle
            {
                sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
                return false;
            }

            if (isSitting()) // A strider can be ridden only when standing
            {
                sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
                return false;
            }

            if (isFishing()) // You can't mount, dismount, break and drop items while fishing
            {
                sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
                return false;
            }

            if (isCursedWeaponEquipped()) // You can't mount, dismount, break and drop items while weilding a cursed weapon
            {
                sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
                return false;
            }

            if (!MathUtil.checkIfInRange(200, this, summon, true)) {
                sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_STRIDER_TO_MOUNT);
                return false;
            }

            if (((Pet) summon).checkHungryState()) {
                sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
                return false;
            }

            if (!summon.isDead() && !isMounted())
                mount(summon);
        } else if (isMounted()) {
            if (getMountType() == 2 && isInsideZone(ZoneId.NO_LANDING)) {
                sendPacket(SystemMessageId.NO_DISMOUNT_HERE);
                return false;
            }

            if (checkFoodState(_petTemplate.getHungryLimit())) {
                sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
                return false;
            }

            dismount();
        }
        return true;
    }

    public boolean dismount() {
        sendPacket(new SetupGauge(GaugeColor.GREEN, 0));

        int petId = _mountNpcId;
        if (setMount(0, 0, 0)) {
            stopFeed();

            broadcastPacket(new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0));

            _petTemplate = null;
            _petData = null;
            _mountObjectId = 0;

            storePetFood(petId);

            // Notify self and others about speed change
            broadcastUserInfo();
            return true;
        }
        return false;
    }

    public void storePetFood(int petId) {
        if (_controlItemId != 0 && petId != 0) {
            try (Connection con = L2DatabaseFactory.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE pets SET fed=? WHERE item_obj_id = ?")) {
                ps.setInt(1, getCurrentFeed());
                ps.setInt(2, _controlItemId);
                ps.executeUpdate();

                _controlItemId = 0;
            } catch (Exception e) {
                WorldObject.LOGGER.error("Couldn't store pet food data for {}.", e, _controlItemId);
            }
        }
    }

    protected class FeedTask implements Runnable {
        @Override
        public void run() {
            if (!isMounted()) {
                stopFeed();
                return;
            }

            // Eat or return to pet control item.
            if (getCurrentFeed() > getFeedConsume())
                setCurrentFeed(getCurrentFeed() - getFeedConsume());
            else {
                setCurrentFeed(0);
                stopFeed();
                dismount();
                sendPacket(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED);
                return;
            }

            ItemInstance food = getInventory().getItemByItemId(_petTemplate.getFood1());
            if (food == null)
                food = getInventory().getItemByItemId(_petTemplate.getFood2());

            if (food != null && checkFoodState(_petTemplate.getAutoFeedLimit())) {
                IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
                if (handler != null) {
                    handler.useItem(Player.this, food, false);
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(food));
                }
            }
        }
    }

    protected synchronized void startFeed(int npcId) {
        _canFeed = npcId > 0;
        if (!isMounted())
            return;

        if (_summon != null) {
            setCurrentFeed(((Pet) _summon).getCurrentFed());
            _controlItemId = _summon.getControlItemId();
            sendPacket(new SetupGauge(GaugeColor.GREEN, getCurrentFeed() * 10000 / getFeedConsume(), _petData.getMaxMeal() * 10000 / getFeedConsume()));
            if (!isDead())
                _mountFeedTask = ThreadPool.scheduleAtFixedRate(new FeedTask(), 10000, 10000);
        } else if (_canFeed) {
            setCurrentFeed(_petData.getMaxMeal());
            sendPacket(new SetupGauge(GaugeColor.GREEN, getCurrentFeed() * 10000 / getFeedConsume(), _petData.getMaxMeal() * 10000 / getFeedConsume()));
            if (!isDead())
                _mountFeedTask = ThreadPool.scheduleAtFixedRate(new FeedTask(), 10000, 10000);
        }
    }

    protected synchronized void stopFeed() {
        if (_mountFeedTask != null) {
            _mountFeedTask.cancel(false);
            _mountFeedTask = null;
        }
    }

    public PetTemplate getPetTemplate() {
        return _petTemplate;
    }

    public PetDataEntry getPetDataEntry() {
        return _petData;
    }

    public int getCurrentFeed() {
        return _curFeed;
    }

    protected int getFeedConsume() {
        return (isAttackingNow()) ? _petData.getMountMealInBattle() : _petData.getMountMealInNormal();
    }

    public void setCurrentFeed(int num) {
        _curFeed = Math.min(num, _petData.getMaxMeal());

        sendPacket(new SetupGauge(GaugeColor.GREEN, getCurrentFeed() * 10000 / getFeedConsume(), _petData.getMaxMeal() * 10000 / getFeedConsume()));
    }

    /**
     * @param state : The state to check (can be autofeed, hungry or unsummon).
     * @return true if the limit is reached, false otherwise or if there is no need to feed.
     */
    public boolean checkFoodState(double state) {
        return (_canFeed) ? getCurrentFeed() < (_petData.getMaxMeal() * state) : false;
    }

    public void setUptime(long time) {
        _uptime = time;
    }

    public long getUptime() {
        return System.currentTimeMillis() - _uptime;
    }

    /**
     * Return True if the Player is invulnerable.
     */
    @Override
    public boolean isInvul() {
        return super.isInvul() || isSpawnProtected();
    }

    /**
     * Return True if the Player has a Party in progress.
     */
    @Override
    public boolean isInParty() {
        return _party != null;
    }

    /**
     * Set the _party object of the Player (without joining it).
     *
     * @param party The object.
     */
    public void setParty(Party party) {
        _party = party;
    }

    /**
     * Return the _party object of the Player.
     */
    @Override
    public Party getParty() {
        return _party;
    }

    public LootRule getLootRule() {
        return _lootRule;
    }

    public void setLootRule(LootRule lootRule) {
        _lootRule = lootRule;
    }

    /**
     * Return True if the Player is a GM.
     */
    @Override
    public boolean isGM() {
        return getAccessLevel().isGm();
    }

    /**
     * Set the {@link AccessLevel} of this {@link Player}.
     * <ul>
     * <li>If invalid, set the default user access level 0.</li>
     * <li>If superior to 0, it means it's a special access.</li>
     * </ul>
     *
     * @param level : The level to set.
     */
    public void setAccessLevel(int level) {
        // Retrieve the AccessLevel. Even if not existing, it returns user level.
        AccessLevel accessLevel = AdminData.getInstance().getAccessLevel(level);
        if (accessLevel == null) {
            WorldObject.LOGGER.warn("An invalid access level {} has been granted for {}, therefore it has been reset.", level, toString());
            accessLevel = AdminData.getInstance().getAccessLevel(0);
        }

        _accessLevel = accessLevel;

        if (level > 0) {
            // For level lower or equal to user, we don't apply AccessLevel name as title.
            setTitle(accessLevel.getName());

            // We log master access.
            if (level == AdminData.getInstance().getMasterAccessLevel())
                WorldObject.LOGGER.info("{} has logged in with Master access level.", getName());
        }

        // We refresh GMList if the access level is GM.
        if (accessLevel.isGm()) {
            // A little hack to avoid Enterworld config to be replaced.
            if (!AdminData.getInstance().isRegisteredAsGM(this))
                AdminData.getInstance().addGm(this, false);
        } else
            AdminData.getInstance().deleteGm(this);

        getAppearance().setNameColor(accessLevel.getNameColor());
        getAppearance().setTitleColor(accessLevel.getTitleColor());
        broadcastUserInfo();

        PlayerInfoTable.getInstance().updatePlayerData(this, true);
    }

    public void setAccountAccesslevel(int level) {
        LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
    }

    /**
     * @return the _accessLevel of the Player.
     */
    public AccessLevel getAccessLevel() {
        return _accessLevel;
    }

    /**
     * Update Stats of the Player client side by sending UserInfo/StatusUpdate to this Player and CharInfo/StatusUpdate to all Player in its _KnownPlayers (broadcast).
     *
     * @param broadcastType
     */
    public void updateAndBroadcastStatus(int broadcastType) {
        refreshOverloaded();
        refreshExpertisePenalty();

        if (broadcastType == 1)
            sendPacket(new UserInfo(this));
        else if (broadcastType == 2)
            broadcastUserInfo();
    }

    /**
     * Send StatusUpdate packet with Karma to the Player and all Player to inform (broadcast).
     */
    public void broadcastKarma() {
        StatusUpdate su = new StatusUpdate(this);
        su.addAttribute(StatusUpdate.KARMA, getKarma());
        sendPacket(su);

        if (_summon != null)
            sendPacket(new RelationChanged(_summon, getRelation(this), false));

        broadcastRelationsChanges();
    }

    /**
     * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).
     *
     * @param isOnline
     * @param updateInDb
     */
    public void setOnlineStatus(boolean isOnline, boolean updateInDb) {
        if (_isOnline != isOnline)
            _isOnline = isOnline;

        // Update the characters table of the database with online status and lastAccess (called when login and logout)
        if (updateInDb)
            updateOnlineStatus();
    }

    public void setIsIn7sDungeon(boolean isIn7sDungeon) {
        _isIn7sDungeon = isIn7sDungeon;
    }

    /**
     * Update the characters table of the database with online status and lastAccess of this Player (called when login and logout).
     */
    public void updateOnlineStatus() {
        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?")) {
            ps.setInt(1, isOnlineInt());
            ps.setLong(2, System.currentTimeMillis());
            ps.setInt(3, getObjectId());
            ps.execute();
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't set player online status.", e);
        }
    }

    /**
     * Retrieve a Player from the characters table of the database.
     * <ul>
     * <li>Retrieve the Player from the characters table of the database</li>
     * <li>Set the x,y,z position of the Player and make it invisible</li>
     * <li>Update the overloaded status of the Player</li>
     * </ul>
     *
     * @param objectId Identifier of the object to initialized
     * @return The Player loaded from the database
     */
    public static Player restore(int objectId) {
        Player player = null;

        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(RESTORE_CHARACTER)) {
            ps.setInt(1, objectId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final int activeClassId = rs.getInt("classid");
                    final PlayerTemplate template = PlayerData.getInstance().getTemplate(activeClassId);
                    final Appearance app = new Appearance(rs.getByte("face"), rs.getByte("hairColor"), rs.getByte("hairStyle"), Sex.values()[rs.getInt("sex")]);

                    player = new Player(objectId, template, rs.getString("account_name"), app);
                    player.setName(rs.getString("char_name"));
                    player._lastAccess = rs.getLong("lastAccess");

                    player.getStat().setExp(rs.getLong("exp"));
                    player.getStat().setLevel(rs.getByte("level"));
                    player.getStat().setSp(rs.getInt("sp"));

                    player.setExpBeforeDeath(rs.getLong("expBeforeDeath"));
                    player.setWantsPeace(rs.getInt("wantspeace") == 1);
                    player.setKarma(rs.getInt("karma"));
                    player.setPvpKills(rs.getInt("pvpkills"));
                    player.setPkKills(rs.getInt("pkkills"));
                    player.setOnlineTime(rs.getLong("onlinetime"));
                    player.setNoble(rs.getInt("nobless") == 1, false);

                    player.setClanJoinExpiryTime(rs.getLong("clan_join_expiry_time"));
                    if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
                        player.setClanJoinExpiryTime(0);

                    player.setClanCreateExpiryTime(rs.getLong("clan_create_expiry_time"));
                    if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
                        player.setClanCreateExpiryTime(0);

                    player.setPowerGrade(rs.getInt("power_grade"));
                    player.setPledgeType(rs.getInt("subpledge"));

                    int clanId = rs.getInt("clanid");
                    if (clanId > 0)
                        player.setClan(ClanTable.getInstance().getClan(clanId));

                    if (player.getClan() != null) {
                        if (player.getClan().getLeaderId() != player.getObjectId()) {
                            if (player.getPowerGrade() == 0)
                                player.setPowerGrade(5);

                            player.setClanPrivileges(player.getClan().getPriviledgesByRank(player.getPowerGrade()));
                        } else {
                            player.setClanPrivileges(Clan.CP_ALL);
                            player.setPowerGrade(1);
                        }
                    } else
                        player.setClanPrivileges(Clan.CP_NOTHING);

                    player.setDeleteTimer(rs.getLong("deletetime"));
                    player.setTitle(rs.getString("title"));
                    player.setAccessLevel(rs.getInt("accesslevel"));
                    player.setUptime(System.currentTimeMillis());
                    player.setRecomHave(rs.getInt("rec_have"));
                    player.setRecomLeft(rs.getInt("rec_left"));

                    player._classIndex = 0;
                    try {
                        player.setBaseClass(rs.getInt("base_class"));
                    } catch (Exception e) {
                        player.setBaseClass(activeClassId);
                    }

                    // Restore Subclass Data (cannot be done earlier in function)
                    if (restoreSubClassData(player) && activeClassId != player.getBaseClass()) {
                        for (SubClass subClass : player.getSubClasses().values())
                            if (subClass.getClassId() == activeClassId)
                                player._classIndex = subClass.getClassIndex();
                    }

                    // Subclass in use but doesn't exist in DB - a possible subclass cheat has been attempted. Switching to base class.
                    if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass())
                        player.setClassId(player.getBaseClass());
                    else
                        player._activeClass = activeClassId;

                    player.setApprentice(rs.getInt("apprentice"));
                    player.setSponsor(rs.getInt("sponsor"));
                    player.setLvlJoinedAcademy(rs.getInt("lvl_joined_academy"));
                    player.setIsIn7sDungeon(rs.getInt("isin7sdungeon") == 1);

                    player.getPunishment().load(rs.getInt("punish_level"), rs.getLong("punish_timer"));

                    CursedWeaponManager.getInstance().checkPlayer(player);

                    player.setAllianceWithVarkaKetra(rs.getInt("varka_ketra_ally"));

                    player.setDeathPenaltyBuffLevel(rs.getInt("death_penalty_level"));

                    // Set the position of the Player.
                    player.getPosition().set(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("heading"));

                    // Set Hero status if it applies
                    if (HeroManager.getInstance().isActiveHero(objectId))
                        player.setHero(true);

                    // Set pledge class rank.
                    player.setPledgeClass(ClanMember.calculatePledgeClass(player));

                    // Retrieve from the database all secondary data of this Player and reward expertise/lucky skills if necessary.
                    // Note that Clan, Noblesse and Hero skills are given separately and not here.
                    player.restoreCharData();
                    player.giveSkills();

                    // buff and status icons
                    if (Config.STORE_SKILL_COOLTIME)
                        player.restoreEffects();

                    // Restore current CP, HP and MP values
                    final double currentHp = rs.getDouble("curHp");

                    player.setCurrentCp(rs.getDouble("curCp"));
                    player.setCurrentHp(currentHp);
                    player.setCurrentMp(rs.getDouble("curMp"));

                    if (currentHp < 0.5) {
                        player.setIsDead(true);
                        player.stopHpMpRegeneration();
                    }

                    // Restore pet if it exists in the world.
                    final Pet pet = World.getInstance().getPet(player.getObjectId());
                    if (pet != null) {
                        player.setSummon(pet);
                        pet.setOwner(player);
                    }

                    player.refreshOverloaded();
                    player.refreshExpertisePenalty();

                    player.restoreFriendList();

                    // Retrieve the name and ID of the other characters assigned to this account.
                    try (PreparedStatement ps2 = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?")) {
                        ps2.setString(1, player._accountName);
                        ps2.setInt(2, objectId);

                        try (ResultSet rs2 = ps2.executeQuery()) {
                            while (rs2.next())
                                player.getAccountChars().put(rs2.getInt("obj_Id"), rs2.getString("char_name"));
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't restore player data.", e);
        }

        return player;
    }

    public Forum getMemo() {
        if (_forumMemo == null) {
            final Forum forum = ForumsBBSManager.getInstance().getForumByName("MemoRoot");
            if (forum != null) {
                _forumMemo = forum.getChildByName(_accountName);
                if (_forumMemo == null)
                    _forumMemo = ForumsBBSManager.getInstance().createNewForum(_accountName, forum, Forum.MEMO, Forum.OWNERONLY, getObjectId());
            }
        }
        return _forumMemo;
    }

    /**
     * Restores sub-class data for the Player, used to check the current class index for the character.
     *
     * @param player The player to make checks on.
     * @return true if successful.
     */
    private static boolean restoreSubClassData(Player player) {
        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(RESTORE_CHAR_SUBCLASSES)) {
            ps.setInt(1, player.getObjectId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final SubClass subClass = new SubClass(rs.getInt("class_id"), rs.getInt("class_index"), rs.getLong("exp"), rs.getInt("sp"), rs.getByte("level"));

                    // Enforce the correct indexing of _subClasses against their class indexes.
                    player.getSubClasses().put(subClass.getClassIndex(), subClass);
                }
            }
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't restore subclasses for {}.", e, player.getName());
            return false;
        }

        return true;
    }

    /**
     * Restores secondary data for the Player, based on the current class index.
     */
    private void restoreCharData() {
        // Retrieve from the database all skills of this Player and add them to _skills.
        restoreSkills();

        // Retrieve from the database all macroses of this Player and add them to _macroses.
        _macroList.restore();

        // Retrieve from the database the recipe book of this Player.
        if (!isSubClassActive())
            restoreRecipeBook();

        // Retrieve from the database all shortCuts of this Player and add them to _shortCuts.
        _shortcutList.restore();

        // Retrieve from the database all henna of this Player and add them to _henna.
        _hennaList.restore();

        // Retrieve from the database all recom data of this Player and add to _recomChars.
        restoreRecom();
    }

    /**
     * Store {@link Recipe} book data for this {@link Player}, if he isn't on an active subclass.
     */
    private void storeRecipeBook() {
        // If the player is on a sub-class don't even attempt to store a recipe book.
        if (isSubClassActive())
            return;

        try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(DELETE_RECIPEBOOK)) {
                ps.setInt(1, getObjectId());
                ps.execute();
            }

            try (PreparedStatement ps = con.prepareStatement(SAVE_RECIPEBOOK)) {
                for (Recipe recipe : getCommonRecipeBook()) {
                    ps.setInt(1, getObjectId());
                    ps.setInt(2, recipe.getId());
                    ps.addBatch();
                }

                for (Recipe recipe : getDwarvenRecipeBook()) {
                    ps.setInt(1, getObjectId());
                    ps.setInt(2, recipe.getId());
                    ps.addBatch();
                }

                ps.executeBatch();
            }
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't store recipe book data.", e);
        }
    }

    /**
     * Restore {@link Recipe} book data for this {@link Player}.
     */
    private void restoreRecipeBook() {
        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT recipeId FROM character_recipebook WHERE charId=?")) {
            ps.setInt(1, getObjectId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Recipe recipe = RecipeData.getInstance().getRecipeList(rs.getInt("recipeId"));
                    if (recipe.isDwarven())
                        registerDwarvenRecipeList(recipe);
                    else
                        registerCommonRecipeList(recipe);
                }
            }
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't restore recipe book data.", e);
        }
    }

    /**
     * Update Player stats in the characters table of the database.
     *
     * @param storeActiveEffects
     */
    public synchronized void store(boolean storeActiveEffects) {
        storeCharBase();
        storeCharSub();
        storeEffect(storeActiveEffects);
        storeRecipeBook();

        _vars.storeMe();
    }

    public void store() {
        store(true);
    }

    public void storeCharBase() {
        // Get the exp, level, and sp of base class to store in base table
        final int currentClassIndex = getClassIndex();

        _classIndex = 0;

        final long exp = getStat().getExp();
        final int level = getStat().getLevel();
        final int sp = getStat().getSp();

        _classIndex = currentClassIndex;

        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_CHARACTER)) {
            ps.setInt(1, level);
            ps.setInt(2, getMaxHp());
            ps.setDouble(3, getCurrentHp());
            ps.setInt(4, getMaxCp());
            ps.setDouble(5, getCurrentCp());
            ps.setInt(6, getMaxMp());
            ps.setDouble(7, getCurrentMp());
            ps.setInt(8, getAppearance().getFace());
            ps.setInt(9, getAppearance().getHairStyle());
            ps.setInt(10, getAppearance().getHairColor());
            ps.setInt(11, getAppearance().getSex().ordinal());
            ps.setInt(12, getHeading());

            if (!isInObserverMode()) {
                ps.setInt(13, getX());
                ps.setInt(14, getY());
                ps.setInt(15, getZ());
            } else {
                ps.setInt(13, _savedLocation.getX());
                ps.setInt(14, _savedLocation.getY());
                ps.setInt(15, _savedLocation.getZ());
            }

            ps.setLong(16, exp);
            ps.setLong(17, getExpBeforeDeath());
            ps.setInt(18, sp);
            ps.setInt(19, getKarma());
            ps.setInt(20, getPvpKills());
            ps.setInt(21, getPkKills());
            ps.setInt(22, getClanId());
            ps.setInt(23, getRace().ordinal());
            ps.setInt(24, getClassId().getId());
            ps.setLong(25, getDeleteTimer());
            ps.setString(26, getTitle());
            ps.setInt(27, getAccessLevel().getLevel());
            ps.setInt(28, isOnlineInt());
            ps.setInt(29, isIn7sDungeon() ? 1 : 0);
            ps.setInt(30, getClanPrivileges());
            ps.setInt(31, wantsPeace() ? 1 : 0);
            ps.setInt(32, getBaseClass());

            long totalOnlineTime = _onlineTime;
            if (_onlineBeginTime > 0)
                totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;

            ps.setLong(33, totalOnlineTime);
            ps.setInt(34, _punishment.getType().ordinal());
            ps.setLong(35, _punishment.getTimer());
            ps.setInt(36, isNoble() ? 1 : 0);
            ps.setLong(37, getPowerGrade());
            ps.setInt(38, getPledgeType());
            ps.setInt(39, getLvlJoinedAcademy());
            ps.setLong(40, getApprentice());
            ps.setLong(41, getSponsor());
            ps.setInt(42, getAllianceWithVarkaKetra());
            ps.setLong(43, getClanJoinExpiryTime());
            ps.setLong(44, getClanCreateExpiryTime());
            ps.setString(45, getName());
            ps.setLong(46, getDeathPenaltyBuffLevel());
            ps.setInt(47, getObjectId());

            ps.execute();
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't store player base data.", e);
        }
    }

    private void storeCharSub() {
        if (_subClasses.isEmpty())
            return;

        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_CHAR_SUBCLASS)) {
            for (SubClass subClass : _subClasses.values()) {
                ps.setLong(1, subClass.getExp());
                ps.setInt(2, subClass.getSp());
                ps.setInt(3, subClass.getLevel());
                ps.setInt(4, subClass.getClassId());
                ps.setInt(5, getObjectId());
                ps.setInt(6, subClass.getClassIndex());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't store subclass data.", e);
        }
    }

    private void storeEffect(boolean storeEffects) {
        if (!Config.STORE_SKILL_COOLTIME)
            return;

        try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
            // Delete all current stored effects for char to avoid dupe
            try (PreparedStatement ps = con.prepareStatement(DELETE_SKILL_SAVE)) {
                ps.setInt(1, getObjectId());
                ps.setInt(2, getClassIndex());
                ps.executeUpdate();
            }

            int buff_index = 0;
            final List<Integer> storedSkills = new ArrayList<>();

            try (PreparedStatement ps = con.prepareStatement(ADD_SKILL_SAVE)) {
                // Store all effect data along with calulated remaining reuse delays for matching skills. 'restore_type'= 0.
                if (storeEffects) {
                    for (L2Effect effect : getAllEffects()) {
                        if (effect == null)
                            continue;

                        switch (effect.getEffectType()) {
                            case HEAL_OVER_TIME:
                            case COMBAT_POINT_HEAL_OVER_TIME:
                                continue;
                        }

                        final L2Skill skill = effect.getSkill();
                        if (storedSkills.contains(skill.getReuseHashCode()))
                            continue;

                        storedSkills.add(skill.getReuseHashCode());
                        if (!effect.isHerbEffect() && effect.getInUse() && !skill.isToggle()) {
                            ps.setInt(1, getObjectId());
                            ps.setInt(2, skill.getId());
                            ps.setInt(3, skill.getLevel());
                            ps.setInt(4, effect.getCount());
                            ps.setInt(5, effect.getTime());

                            final Timestamp t = _reuseTimeStamps.get(skill.getReuseHashCode());
                            if (t != null && t.hasNotPassed()) {
                                ps.setLong(6, t.getReuse());
                                ps.setDouble(7, t.getStamp());
                            } else {
                                ps.setLong(6, 0);
                                ps.setDouble(7, 0);
                            }

                            ps.setInt(8, 0);
                            ps.setInt(9, getClassIndex());
                            ps.setInt(10, ++buff_index);
                            ps.addBatch(); // Add SQL
                        }
                    }
                }

                // Store the reuse delays of remaining skills which lost effect but still under reuse delay. 'restore_type' 1.
                for (Map.Entry<Integer, Timestamp> entry : _reuseTimeStamps.entrySet()) {
                    final int hash = entry.getKey();
                    if (storedSkills.contains(hash))
                        continue;

                    final Timestamp t = entry.getValue();
                    if (t != null && t.hasNotPassed()) {
                        storedSkills.add(hash);

                        ps.setInt(1, getObjectId());
                        ps.setInt(2, t.getId());
                        ps.setInt(3, t.getValue());
                        ps.setInt(4, -1);
                        ps.setInt(5, -1);
                        ps.setLong(6, t.getReuse());
                        ps.setDouble(7, t.getStamp());
                        ps.setInt(8, 1);
                        ps.setInt(9, getClassIndex());
                        ps.setInt(10, ++buff_index);
                        ps.addBatch(); // Add SQL
                    }
                }

                ps.executeBatch(); // Execute SQLs
            }
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't store player effects.", e);
        }
    }

    /**
     * @return True if the Player is online.
     */
    public boolean isOnline() {
        return _isOnline;
    }

    /**
     * @return an int interpretation of online status.
     */
    public int isOnlineInt() {
        if (_isOnline && getClient() != null)
            return getClient().isDetached() ? 2 : 1;

        return 0;
    }

    public boolean isIn7sDungeon() {
        return _isIn7sDungeon;
    }

    /**
     * Add a {@link L2Skill} and its Func objects to the calculator set of the {@link Player}. Don't refresh shortcuts.
     *
     * @param newSkill : The skill to add.
     * @param store    : If true, we save the skill on database.
     * @return true if the skill has been successfully added.
     * @see Player#addSkill(L2Skill, boolean, boolean)
     */
    public boolean addSkill(L2Skill newSkill, boolean store) {
        return addSkill(newSkill, store, false);
    }

    /**
     * Add a {@link L2Skill} and its Func objects to the calculator set of the {@link Player}.<BR>
     * <ul>
     * <li>Replace or add oldSkill by newSkill (only if oldSkill is different than newSkill)</li>
     * <li>If an old skill has been replaced, remove all its Func objects of Creature calculator set</li>
     * <li>Add Func objects of newSkill to the calculator set of the Creature</li>
     * </ul>
     *
     * @param newSkill        : The skill to add.
     * @param store           : If true, we save the skill on database.
     * @param updateShortcuts : If true, we refresh all shortcuts associated to that skill (should be only called when skill upgrades, either manually or by trainer).
     * @return true if the skill has been successfully added.
     */
    public boolean addSkill(L2Skill newSkill, boolean store, boolean updateShortcuts) {
        // New skill is null, abort.
        if (newSkill == null)
            return false;

        // Search the old skill. We test if it's different than the new one. If yes, we abort the operation.
        final L2Skill oldSkill = getSkills().get(newSkill.getId());
        if (oldSkill != null && oldSkill.equals(newSkill))
            return false;

        // The 2 skills were different (or old wasn't existing). We can refresh the map.
        getSkills().put(newSkill.getId(), newSkill);

        // If an old skill has been replaced, remove all its Func objects
        if (oldSkill != null) {
            // if skill came with another one, we should delete the other one too.
            if (oldSkill.triggerAnotherSkill())
                removeSkill(oldSkill.getTriggeredId(), false);

            removeStatsByOwner(oldSkill);
        }

        // Add Func objects of newSkill to the calculator set of the Creature
        addStatFuncs(newSkill.getStatFuncs(this));

        // Test and delete chance skill if found.
        if (oldSkill != null && getChanceSkills() != null)
            removeChanceSkill(oldSkill.getId());

        // If new skill got a chance, trigger it.
        if (newSkill.isChance())
            addChanceTrigger(newSkill);

        // Add or update the skill in the database.
        if (store)
            storeSkill(newSkill, -1);

        // Update shortcuts.
        if (updateShortcuts)
            getShortcutList().refreshShortcuts(newSkill.getId(), newSkill.getLevel(), ShortcutType.SKILL);

        return true;
    }

    /**
     * Remove a {@link L2Skill} from this {@link Player}. If parameter store is true, we also remove it from database and update shortcuts.
     *
     * @param skillId : The skill identifier to remove.
     * @param store   : If true, we delete the skill from database.
     * @return the L2Skill removed or null if it couldn't be removed.
     */
    public L2Skill removeSkill(int skillId, boolean store) {
        return removeSkill(skillId, store, true);
    }

    /**
     * Remove a {@link L2Skill} from this {@link Player}. If parameter store is true, we also remove it from database and update shortcuts.
     *
     * @param skillId      : The skill identifier to remove.
     * @param store        : If true, we delete the skill from database.
     * @param removeEffect : If true, we remove the associated effect if existing.
     * @return the L2Skill removed or null if it couldn't be removed.
     */
    public L2Skill removeSkill(int skillId, boolean store, boolean removeEffect) {
        // Remove the skill from the Creature _skills
        final L2Skill oldSkill = getSkills().remove(skillId);
        if (oldSkill == null)
            return null;

        // this is just a fail-safe againts buggers and gm dummies...
        if (oldSkill.triggerAnotherSkill() && oldSkill.getTriggeredId() > 0)
            removeSkill(oldSkill.getTriggeredId(), false);

        // Stop casting if this skill is used right now
        if (getLastSkillCast() != null && isCastingNow() && skillId == getLastSkillCast().getId())
            abortCast();

        if (getLastSimultaneousSkillCast() != null && isCastingSimultaneouslyNow() && skillId == getLastSimultaneousSkillCast().getId())
            abortCast();

        // Remove all its Func objects from the Creature calculator set
        if (removeEffect) {
            removeStatsByOwner(oldSkill);
            stopSkillEffects(skillId);
        }

        if (oldSkill.isChance() && getChanceSkills() != null)
            removeChanceSkill(skillId);

        if (store) {
            try (Connection con = L2DatabaseFactory.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(DELETE_SKILL_FROM_CHAR)) {
                ps.setInt(1, skillId);
                ps.setInt(2, getObjectId());
                ps.setInt(3, getClassIndex());
                ps.execute();
            } catch (Exception e) {
                WorldObject.LOGGER.error("Couldn't delete player skill.", e);
            }

            // Don't busy testing shortcuts if skill was a passive skill.
            if (!oldSkill.isPassive())
                getShortcutList().deleteShortcuts(skillId, ShortcutType.SKILL);
        }
        return oldSkill;
    }

    /**
     * Insert or update a {@link Player} skill in the database.<br>
     * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
     *
     * @param skill      : The skill to add or update (if updated, only the level is refreshed).
     * @param classIndex : The current class index to set, or current if none is found.
     */
    private void storeSkill(L2Skill skill, int classIndex) {
        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(ADD_OR_UPDATE_SKILL)) {
            ps.setInt(1, getObjectId());
            ps.setInt(2, skill.getId());
            ps.setInt(3, skill.getLevel());
            ps.setInt(4, (classIndex > -1) ? classIndex : _classIndex);
            ps.executeUpdate();
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't store player skill.", e);
        }
    }

    /**
     * Restore all skills from database for this {@link Player} and feed getSkills().
     */
    private void restoreSkills() {
        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR)) {
            ps.setInt(1, getObjectId());
            ps.setInt(2, getClassIndex());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    addSkill(SkillTable.getInstance().getInfo(rs.getInt("skill_id"), rs.getInt("skill_level")), false);
            }
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't restore player skills.", e);
        }
    }

    /**
     * Retrieve from the database all skill effects of this Player and add them to the player.
     */
    public void restoreEffects() {
        try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(RESTORE_SKILL_SAVE)) {
                ps.setInt(1, getObjectId());
                ps.setInt(2, getClassIndex());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        final int effectCount = rs.getInt("effect_count");
                        final int effectCurTime = rs.getInt("effect_cur_time");
                        final long reuseDelay = rs.getLong("reuse_delay");
                        final long systime = rs.getLong("systime");
                        final int restoreType = rs.getInt("restore_type");

                        final L2Skill skill = SkillTable.getInstance().getInfo(rs.getInt("skill_id"), rs.getInt("skill_level"));
                        if (skill == null)
                            continue;

                        final long remainingTime = systime - System.currentTimeMillis();
                        if (remainingTime > 10) {
                            disableSkill(skill, remainingTime);
                            addTimeStamp(skill, reuseDelay, systime);
                        }

                        /**
                         * Restore Type 1 The remaning skills lost effect upon logout but were still under a high reuse delay.
                         */
                        if (restoreType > 0)
                            continue;

                        /**
                         * Restore Type 0 These skills were still in effect on the character upon logout. Some of which were self casted and might still have a long reuse delay which also is restored.
                         */
                        if (skill.hasEffects()) {
                            final Env env = new Env();
                            env.setCharacter(this);
                            env.setTarget(this);
                            env.setSkill(skill);

                            for (EffectTemplate et : skill.getEffectTemplates()) {
                                final L2Effect ef = et.getEffect(env);
                                if (ef != null) {
                                    ef.setCount(effectCount);
                                    ef.setFirstTime(effectCurTime);
                                    ef.scheduleEffect();
                                }
                            }
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(DELETE_SKILL_SAVE)) {
                ps.setInt(1, getObjectId());
                ps.setInt(2, getClassIndex());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't restore effects.", e);
        }
    }

    /**
     * Retrieve from the database all Recommendation data of this Player, add to _recomChars and calculate stats of the Player.
     */
    private void restoreRecom() {
        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(RESTORE_CHAR_RECOMS)) {
            ps.setInt(1, getObjectId());

            try (ResultSet rset = ps.executeQuery()) {
                while (rset.next())
                    _recomChars.add(rset.getInt("target_id"));
            }
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't restore recommendations.", e);
        }
    }

    /**
     * @return the {@link HennaList} of this {@link Player}.
     */
    public HennaList getHennaList() {
        return _hennaList;
    }

    /**
     * Return True if the Player is autoAttackable.
     * <ul>
     * <li>Check if the attacker isn't the Player Pet</li>
     * <li>Check if the attacker is Monster</li>
     * <li>If the attacker is a Player, check if it is not in the same party</li>
     * <li>Check if the Player has Karma</li>
     * <li>If the attacker is a Player, check if it is not in the same siege clan (Attacker, Defender)</li>
     * </ul>
     */
    @Override
    public boolean isAutoAttackable(Creature attacker) {
        // Check if the attacker isn't the Player Pet
        if (attacker == this || attacker == _summon)
            return false;

        // Check if the attacker is a Monster
        if (attacker instanceof Monster)
            return true;

        // Check if the attacker is not in the same party
        if (_party != null && _party.containsPlayer(attacker))
            return false;

        // Check if the attacker is a L2Playable
        if (attacker instanceof Playable) {
            if (isInsideZone(ZoneId.PEACE))
                return false;

            // Get Player
            final Player cha = attacker.getActingPlayer();

            // Check if the attacker is in olympiad and olympiad start
            if (attacker instanceof Player && cha.isInOlympiadMode()) {
                if (isInOlympiadMode() && isOlympiadStart() && cha.getOlympiadGameId() == getOlympiadGameId())
                    return true;

                return false;
            }

            // is AutoAttackable if both players are in the same duel and the duel is still going on
            if (getDuelState() == Duel.DuelState.DUELLING && getDuelId() == cha.getDuelId())
                return true;

            if (getClan() != null) {
                final Siege siege = CastleManager.getInstance().getActiveSiege(this);
                if (siege != null) {
                    // Check if a siege is in progress and if attacker and the Player aren't in the Defender clan
                    if (siege.checkSides(cha.getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER) && siege.checkSides(getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER))
                        return false;

                    // Check if a siege is in progress and if attacker and the Player aren't in the Attacker clan
                    if (siege.checkSide(cha.getClan(), SiegeSide.ATTACKER) && siege.checkSide(getClan(), SiegeSide.ATTACKER))
                        return false;
                }

                // Check if clan is at war
                if (getClan().isAtWarWith(cha.getClanId()) && !wantsPeace() && !cha.wantsPeace() && !isAcademyMember())
                    return true;
            }

            // Check if the Player is in an arena.
            if (isInArena() && attacker.isInArena())
                return true;

            // Check if the attacker is not in the same ally.
            if (getAllyId() != 0 && getAllyId() == cha.getAllyId())
                return false;

            // Check if the attacker is not in the same clan.
            if (getClan() != null && getClan().isMember(cha.getObjectId()))
                return false;

            // Now check again if the Player is in pvp zone (as arenas check was made before, it ends with sieges).
            if (isInsideZone(ZoneId.PVP) && attacker.isInsideZone(ZoneId.PVP))
                return true;
        } else if (attacker instanceof SiegeGuard) {
            if (getClan() != null) {
                final Siege siege = CastleManager.getInstance().getActiveSiege(this);
                return (siege != null && siege.checkSide(getClan(), SiegeSide.ATTACKER));
            }
        }

        // Check if the Player has Karma
        if (getKarma() > 0 || getPvpFlag() > 0)
            return true;

        return false;
    }

    /**
     * Check if the active L2Skill can be casted.
     * <ul>
     * <li>Check if the skill isn't toggle and is offensive</li>
     * <li>Check if the target is in the skill cast range</li>
     * <li>Check if the skill is Spoil type and if the target isn't already spoiled</li>
     * <li>Check if the caster owns enought consummed Item, enough HP and MP to cast the skill</li>
     * <li>Check if the caster isn't sitting</li>
     * <li>Check if all skills are enabled and this skill is enabled</li>
     * <li>Check if the caster own the weapon needed</li>
     * <li>Check if the skill is active</li>
     * <li>Check if all casting conditions are completed</li>
     * <li>Notify the AI with CAST and target</li>
     * </ul>
     *
     * @param skill    The L2Skill to use
     * @param forceUse used to force ATTACK on players
     * @param dontMove used to prevent movement, if not in range
     */
    @Override
    public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove) {
        // Check if the skill is active
        if (skill.isPassive()) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // Check if this skill is enabled (ex : reuse time)
        if (isSkillDisabled(skill)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill));
            return false;
        }

        // Players wearing Formal Wear cannot use skills.
        if (isWearingFormalWear()) {
            sendPacket(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR);
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // ************************************* Check Casting in Progress *******************************************

        // If a skill is currently being used, queue this one if this is not the same
        if (isCastingNow()) {
            // Check if new skill different from current skill in progress ; queue it in the player _queuedSkill
            if (_currentSkill.getSkill() != null && skill.getId() != _currentSkill.getSkillId())
                setQueuedSkill(skill, forceUse, dontMove);

            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        setIsCastingNow(true);

        // Set the player _currentSkill.
        setCurrentSkill(skill, forceUse, dontMove);

        // Wipe queued skill.
        if (_queuedSkill.getSkill() != null)
            setQueuedSkill(null, false, false);

        if (!checkUseMagicConditions(skill, forceUse, dontMove)) {
            setIsCastingNow(false);
            return false;
        }

        // Check if the target is correct and Notify the AI with CAST and target
        WorldObject target = null;

        switch (skill.getTargetType()) {
            case TARGET_AURA:
            case TARGET_FRONT_AURA:
            case TARGET_BEHIND_AURA:
            case TARGET_GROUND:
            case TARGET_SELF:
            case TARGET_CORPSE_ALLY:
            case TARGET_AURA_UNDEAD:
                target = this;
                break;

            default: // Get the first target of the list
                target = skill.getFirstOfTargetList(this);
                break;
        }

        // Notify the AI with CAST and target
        getAI().setIntention(IntentionType.CAST, skill, target);
        return true;
    }

    private boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove) {
        // ************************************* Check Player State *******************************************

        // Check if the player is dead or out of control.
        if (isDead() || isOutOfControl()) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        L2SkillType sklType = skill.getSkillType();

        if (isFishing() && (sklType != L2SkillType.PUMPING && sklType != L2SkillType.REELING && sklType != L2SkillType.FISHING)) {
            // Only fishing skills are available
            sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
            return false;
        }

        if (isInObserverMode()) {
            sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
            abortCast();
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // Check if the caster is sitted.
        if (isSitting()) {
            // Send a System Message to the caster
            sendPacket(SystemMessageId.CANT_MOVE_SITTING);

            // Send ActionFailed to the Player
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // Check if the skill type is TOGGLE
        if (skill.isToggle()) {
            // Get effects of the skill
            L2Effect effect = getFirstEffect(skill.getId());

            if (effect != null) {
                // If the toggle is different of FakeDeath, you can de-activate it clicking on it.
                if (skill.getId() != 60)
                    effect.exit();

                // Send ActionFailed to the Player
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        }

        // Check if the player uses "Fake Death" skill
        if (isFakeDeath()) {
            // Send ActionFailed to the Player
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // ************************************* Check Target *******************************************
        // Create and set a WorldObject containing the target of the skill
        WorldObject target = null;
        L2Skill.SkillTargetType sklTargetType = skill.getTargetType();
        Location worldPosition = getCurrentSkillWorldPosition();

        if (sklTargetType == L2Skill.SkillTargetType.TARGET_GROUND && worldPosition == null) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        switch (sklTargetType) {
            // Target the player if skill type is AURA, PARTY, CLAN or SELF
            case TARGET_AURA:
            case TARGET_FRONT_AURA:
            case TARGET_BEHIND_AURA:
            case TARGET_AURA_UNDEAD:
            case TARGET_PARTY:
            case TARGET_ALLY:
            case TARGET_CLAN:
            case TARGET_GROUND:
            case TARGET_SELF:
            case TARGET_CORPSE_ALLY:
            case TARGET_AREA_SUMMON:
                target = this;
                break;
            case TARGET_PET:
            case TARGET_SUMMON:
                target = _summon;
                break;
            default:
                target = getTarget();
                break;
        }

        // Check the validity of the target
        if (target == null) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        if (target instanceof Door) {
            if (!((Door) target).isAutoAttackable(this) // Siege doors only hittable during siege
                    || (((Door) target).isUnlockable() && skill.getSkillType() != L2SkillType.UNLOCK)) // unlockable doors
            {
                sendPacket(SystemMessageId.INCORRECT_TARGET);
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        }

        // Are the target and the player in the same duel?
        if (isInDuel()) {
            if (target instanceof Playable) {
                // Get Player
                Player cha = target.getActingPlayer();
                if (cha.getDuelId() != getDuelId()) {
                    sendPacket(SystemMessageId.INCORRECT_TARGET);
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
            }
        }

        // ************************************* Check skill availability *******************************************

        // Siege summon checks. Both checks send a message to the player if it return false.
        if (skill.isSiegeSummonSkill()) {
            final Siege siege = CastleManager.getInstance().getActiveSiege(this);
            if (siege == null || !siege.checkSide(getClan(), SiegeSide.ATTACKER) || (isInSiege() && isInsideZone(ZoneId.CASTLE))) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_CALL_PET_FROM_THIS_LOCATION));
                return false;
            }

            if (SevenSignsManager.getInstance().isSealValidationPeriod() && SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE) == CabalType.DAWN && SevenSignsManager.getInstance().getPlayerCabal(getObjectId()) == CabalType.DUSK) {
                sendPacket(SystemMessageId.SEAL_OF_STRIFE_FORBIDS_SUMMONING);
                return false;
            }
        }

        // ************************************* Check casting conditions *******************************************

        // Check if all casting conditions are completed
        if (!skill.checkCondition(this, target, false)) {
            // Send ActionFailed to the Player
            sendPacket(ActionFailed.STATIC_PACKET);
            return false;
        }

        // ************************************* Check Skill Type *******************************************

        // Check if this is offensive magic skill
        if (skill.isOffensive()) {
            if (isInsidePeaceZone(this, target)) {
                // If Creature or target is in a peace zone, send a system message TARGET_IN_PEACEZONE ActionFailed
                sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }

            if (isInOlympiadMode() && !isOlympiadStart()) {
                // if Player is in Olympia and the match isn't already start, send ActionFailed
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }

            // Check if the target is attackable
            if (!target.isAttackable() && !getAccessLevel().allowPeaceAttack()) {
                // If target is not attackable, send ActionFailed
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }

            // Check if a Forced ATTACK is in progress on non-attackable target
            if (!target.isAutoAttackable(this) && !forceUse) {
                switch (sklTargetType) {
                    case TARGET_AURA:
                    case TARGET_FRONT_AURA:
                    case TARGET_BEHIND_AURA:
                    case TARGET_AURA_UNDEAD:
                    case TARGET_CLAN:
                    case TARGET_ALLY:
                    case TARGET_PARTY:
                    case TARGET_SELF:
                    case TARGET_GROUND:
                    case TARGET_CORPSE_ALLY:
                    case TARGET_AREA_SUMMON:
                        break;
                    default: // Send ActionFailed to the Player
                        sendPacket(ActionFailed.STATIC_PACKET);
                        return false;
                }
            }

            // Check if the target is in the skill cast range
            if (dontMove) {
                // Calculate the distance between the Player and the target
                if (sklTargetType == L2Skill.SkillTargetType.TARGET_GROUND) {
                    if (!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), (int) (skill.getCastRange() + getCollisionRadius()), false, false)) {
                        // Send a System Message to the caster
                        sendPacket(SystemMessageId.TARGET_TOO_FAR);

                        // Send ActionFailed to the Player
                        sendPacket(ActionFailed.STATIC_PACKET);
                        return false;
                    }
                } else if (skill.getCastRange() > 0 && !isInsideRadius(target, (int) (skill.getCastRange() + getCollisionRadius()), false, false)) {
                    // Send a System Message to the caster
                    sendPacket(SystemMessageId.TARGET_TOO_FAR);

                    // Send ActionFailed to the Player
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
            }
        }

        // Check if the skill is defensive
        if (!skill.isOffensive() && target instanceof Monster && !forceUse) {
            // check if the target is a monster and if force attack is set.. if not then we don't want to cast.
            switch (sklTargetType) {
                case TARGET_PET:
                case TARGET_SUMMON:
                case TARGET_AURA:
                case TARGET_FRONT_AURA:
                case TARGET_BEHIND_AURA:
                case TARGET_AURA_UNDEAD:
                case TARGET_CLAN:
                case TARGET_SELF:
                case TARGET_CORPSE_ALLY:
                case TARGET_PARTY:
                case TARGET_ALLY:
                case TARGET_CORPSE_MOB:
                case TARGET_AREA_CORPSE_MOB:
                case TARGET_GROUND:
                    break;
                default: {
                    switch (sklType) {
                        case BEAST_FEED:
                        case DELUXE_KEY_UNLOCK:
                        case UNLOCK:
                            break;
                        default:
                            sendPacket(ActionFailed.STATIC_PACKET);
                            return false;
                    }
                    break;
                }
            }
        }

        // Check if the skill is Spoil type and if the target isn't already spoiled
        if (sklType == L2SkillType.SPOIL) {
            if (!(target instanceof Monster)) {
                // Send a System Message to the Player
                sendPacket(SystemMessageId.INCORRECT_TARGET);

                // Send ActionFailed to the Player
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        }

        // Check if the skill is Sweep type and if conditions not apply
        if (sklType == L2SkillType.SWEEP && target instanceof Monster) {
            if (((Monster) target).isDead()) {
                final int spoilerId = ((Monster) target).getSpoilerId();
                if (spoilerId == 0) {
                    // Send a System Message to the Player
                    sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);

                    // Send ActionFailed to the Player
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }

                if (!isLooterOrInLooterParty(spoilerId)) {
                    // Send a System Message to the Player
                    sendPacket(SystemMessageId.SWEEP_NOT_ALLOWED);

                    // Send ActionFailed to the Player
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
            }
        }

        // Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
        if (sklType == L2SkillType.DRAIN_SOUL) {
            if (!(target instanceof Monster)) {
                // Send a System Message to the Player
                sendPacket(SystemMessageId.INCORRECT_TARGET);

                // Send ActionFailed to the Player
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        }

        // Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
        switch (sklTargetType) {
            case TARGET_PARTY:
            case TARGET_ALLY: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
            case TARGET_CLAN: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
            case TARGET_AURA:
            case TARGET_FRONT_AURA:
            case TARGET_BEHIND_AURA:
            case TARGET_AURA_UNDEAD:
            case TARGET_GROUND:
            case TARGET_SELF:
            case TARGET_CORPSE_ALLY:
            case TARGET_AREA_SUMMON:
                break;
            default:
                if (!checkPvpSkill(target, skill) && !getAccessLevel().allowPeaceAttack()) {
                    // Send a System Message to the Player
                    sendPacket(SystemMessageId.TARGET_IS_INCORRECT);

                    // Send ActionFailed to the Player
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
        }

        if ((sklTargetType == L2Skill.SkillTargetType.TARGET_HOLY && !checkIfOkToCastSealOfRule(CastleManager.getInstance().getCastle(this), false, skill, target)) || (sklType == L2SkillType.SIEGEFLAG && !L2SkillSiegeFlag.checkIfOkToPlaceFlag(this, false)) || (sklType == L2SkillType.STRSIEGEASSAULT && !checkIfOkToUseStriderSiegeAssault(skill)) || (sklType == L2SkillType.SUMMON_FRIEND && !(checkSummonerStatus() && checkSummonTargetStatus(target)))) {
            sendPacket(ActionFailed.STATIC_PACKET);
            abortCast();
            return false;
        }

        // GeoData Los Check here
        if (skill.getCastRange() > 0) {
            if (sklTargetType == L2Skill.SkillTargetType.TARGET_GROUND) {
                if (!GeoEngine.getInstance().canSeeTarget(this, worldPosition)) {
                    sendPacket(SystemMessageId.CANT_SEE_TARGET);
                    sendPacket(ActionFailed.STATIC_PACKET);
                    return false;
                }
            } else if (!GeoEngine.getInstance().canSeeTarget(this, target)) {
                sendPacket(SystemMessageId.CANT_SEE_TARGET);
                sendPacket(ActionFailed.STATIC_PACKET);
                return false;
            }
        }
        // finally, after passing all conditions
        return true;
    }

    public boolean checkIfOkToUseStriderSiegeAssault(L2Skill skill) {
        final Siege siege = CastleManager.getInstance().getActiveSiege(this);

        SystemMessage sm;

        if (!isRiding())
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
        else if (!(getTarget() instanceof Door))
            sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
        else if (siege == null || !siege.checkSide(getClan(), SiegeSide.ATTACKER))
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
        else
            return true;

        sendPacket(sm);
        return false;
    }

    public boolean checkIfOkToCastSealOfRule(Castle castle, boolean isCheckOnly, L2Skill skill, WorldObject target) {
        SystemMessage sm;

        if (castle == null || castle.getCastleId() <= 0)
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
        else if (!castle.isGoodArtifact(target))
            sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
        else if (!castle.getSiege().isInProgress())
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
        else if (!MathUtil.checkIfInRange(200, this, target, true))
            sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
        else if (!isInsideZone(ZoneId.CAST_ON_ARTIFACT))
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
        else if (!castle.getSiege().checkSide(getClan(), SiegeSide.ATTACKER))
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);
        else {
            if (!isCheckOnly) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING);
                castle.getSiege().announceToPlayers(sm, false);
            }
            return true;
        }
        sendPacket(sm);
        return false;
    }

    /**
     * @param objectId : The looter object to make checks on.
     * @return true if the active player is the looter or in the same party or command channel than looter objectId.
     */
    public boolean isLooterOrInLooterParty(int objectId) {
        if (objectId == getObjectId())
            return true;

        final Player looter = World.getInstance().getPlayer(objectId);
        if (looter == null)
            return false;

        if (_party == null)
            return false;

        final CommandChannel channel = _party.getCommandChannel();
        return (channel != null) ? channel.containsPlayer(looter) : _party.containsPlayer(looter);
    }

    /**
     * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
     *
     * @param target WorldObject instance containing the target
     * @param skill  L2Skill instance with the skill being casted
     * @return {@code false} if the skill is a pvpSkill and target is not a valid pvp target, {@code true} otherwise.
     */
    public boolean checkPvpSkill(WorldObject target, L2Skill skill) {
        if (skill == null || target == null)
            return false;

        if (!(target instanceof Playable))
            return true;

        if (skill.isDebuff() || skill.isOffensive()) {
            final Player targetPlayer = target.getActingPlayer();
            if (targetPlayer == null || this == target)
                return false;

            // Peace Zone
            if (target.isInsideZone(ZoneId.PEACE))
                return false;

            // Duel
            if (isInDuel() && targetPlayer.isInDuel() && getDuelId() == targetPlayer.getDuelId())
                return true;

            final boolean isCtrlPressed = (getCurrentSkill() != null && getCurrentSkill().isCtrlPressed()) || (getCurrentPetSkill() != null && getCurrentPetSkill().isCtrlPressed());

            // Party
            if (isInParty() && targetPlayer.isInParty()) {
                // Same Party
                if (getParty().getLeader() == targetPlayer.getParty().getLeader()) {
                    if (skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target && skill.isDamage())
                        return true;

                    return false;
                } else if (getParty().getCommandChannel() != null && getParty().getCommandChannel().containsPlayer(targetPlayer)) {
                    if (skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target && skill.isDamage())
                        return true;

                    return false;
                }
            }

            // You can debuff anyone except party members while in an arena...
            if (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP))
                return true;

            // Olympiad
            if (isInOlympiadMode() && targetPlayer.isInOlympiadMode() && getOlympiadGameId() == targetPlayer.getOlympiadGameId())
                return true;

            final Clan aClan = getClan();
            final Clan tClan = targetPlayer.getClan();

            if (aClan != null && tClan != null) {
                if (aClan.isAtWarWith(tClan.getClanId()) && tClan.isAtWarWith(aClan.getClanId())) {
                    // Check if skill can do dmg
                    if (skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target && skill.isAOE())
                        return true;

                    return isCtrlPressed;
                } else if (getClanId() == targetPlayer.getClanId() || (getAllyId() > 0 && getAllyId() == targetPlayer.getAllyId())) {
                    // Check if skill can do dmg
                    if (skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target && skill.isDamage())
                        return true;

                    return false;
                }
            }

            // On retail, it is impossible to debuff a "peaceful" player.
            if (targetPlayer.getPvpFlag() == 0 && targetPlayer.getKarma() == 0) {
                // Check if skill can do dmg
                if (skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target && skill.isDamage())
                    return true;

                return false;
            }

            if (targetPlayer.getPvpFlag() > 0 || targetPlayer.getKarma() > 0)
                return true;

            return false;
        }
        return true;
    }

    /**
     * @return True if the Player is a Mage (based on class templates).
     */
    public boolean isMageClass() {
        return getClassId().getType() != ClassType.FIGHTER;
    }

    public boolean isMounted() {
        return _mountType > 0;
    }

    /**
     * This method allows to :
     * <ul>
     * <li>change isRiding/isFlying flags</li>
     * <li>gift player with Wyvern Breath skill if mount is a wyvern</li>
     * <li>send the skillList (faded icons update)</li>
     * </ul>
     *
     * @param npcId     the npcId of the mount
     * @param npcLevel  The level of the mount
     * @param mountType 0, 1 or 2 (dismount, strider or wyvern).
     * @return always true.
     */
    public boolean setMount(int npcId, int npcLevel, int mountType) {
        switch (mountType) {
            case 0: // Dismounted
                if (isFlying())
                    removeSkill(FrequentSkill.WYVERN_BREATH.getSkill().getId(), false);
                break;

            case 2: // Flying Wyvern
                addSkill(FrequentSkill.WYVERN_BREATH.getSkill(), false);
                break;
        }

        _mountNpcId = npcId;
        _mountType = mountType;
        _mountLevel = npcLevel;

        sendSkillList(); // Update faded icons && eventual added skills.
        return true;
    }

    @Override
    public boolean isSeated() {
        return _throneId > 0;
    }

    @Override
    public boolean isRiding() {
        return _mountType == 1;
    }

    @Override
    public boolean isFlying() {
        return _mountType == 2;
    }

    /**
     * @return the type of Pet mounted (0 : none, 1 : Strider, 2 : Wyvern).
     */
    public int getMountType() {
        return _mountType;
    }

    @Override
    public final void stopAllEffects() {
        super.stopAllEffects();
        updateAndBroadcastStatus(2);
    }

    @Override
    public final void stopAllEffectsExceptThoseThatLastThroughDeath() {
        super.stopAllEffectsExceptThoseThatLastThroughDeath();
        updateAndBroadcastStatus(2);
    }

    /**
     * Stop all toggle-type effects
     */
    public final void stopAllToggles() {
        _effects.stopAllToggles();
    }

    public final void stopCubics() {
        if (getCubics() != null) {
            boolean removed = false;
            for (Cubic cubic : getCubics().values()) {
                cubic.stopAction();
                delCubic(cubic.getId());
                removed = true;
            }
            if (removed)
                broadcastUserInfo();
        }
    }

    public final void stopCubicsByOthers() {
        if (getCubics() != null) {
            boolean removed = false;
            for (Cubic cubic : getCubics().values()) {
                if (cubic.givenByOther()) {
                    cubic.stopAction();
                    delCubic(cubic.getId());
                    removed = true;
                }
            }
            if (removed)
                broadcastUserInfo();
        }
    }

    /**
     * Send UserInfo to this Player and CharInfo to all Player in its _KnownPlayers.<BR>
     * <ul>
     * <li>Send UserInfo to this Player (Public and Private Data)</li>
     * <li>Send CharInfo to all Player in _KnownPlayers of the Player (Public data only)</li>
     * </ul>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR>
     * <BR>
     */
    @Override
    public void updateAbnormalEffect() {
        broadcastUserInfo();
    }

    /**
     * Disable the Inventory and create a new task to enable it after 1.5s.
     */
    public void tempInventoryDisable() {
        _inventoryDisable = true;

        ThreadPool.schedule(() -> _inventoryDisable = false, 1500);
    }

    /**
     * @return True if the Inventory is disabled.
     */
    public boolean isInventoryDisabled() {
        return _inventoryDisable;
    }

    public Map<Integer, Cubic> getCubics() {
        return _cubics;
    }

    /**
     * Add a L2CubicInstance to the Player _cubics.
     *
     * @param id
     * @param level
     * @param matk
     * @param activationtime
     * @param activationchance
     * @param totalLifetime
     * @param givenByOther
     */
    public void addCubic(int id, int level, double matk, int activationtime, int activationchance, int totalLifetime, boolean givenByOther) {
        _cubics.put(id, new Cubic(this, id, level, (int) matk, activationtime, activationchance, totalLifetime, givenByOther));
    }

    /**
     * Remove a L2CubicInstance from the Player _cubics.
     *
     * @param id
     */
    public void delCubic(int id) {
        _cubics.remove(id);
    }

    /**
     * @param id
     * @return the L2CubicInstance corresponding to the Identifier of the Player _cubics.
     */
    public Cubic getCubic(int id) {
        return _cubics.get(id);
    }

    @Override
    public String toString() {
        return getName() + " (" + getObjectId() + ")";
    }

    /**
     * @return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).
     */
    public int getEnchantEffect() {
        final ItemInstance wpn = getActiveWeaponInstance();
        return (wpn == null) ? 0 : Math.min(127, wpn.getEnchantLevel());
    }

    /**
     * Remember the current {@link Folk} of the {@link Player}, used notably for integrity check.
     *
     * @param folk : The Folk to remember.
     */
    public void setCurrentFolk(Folk folk) {
        _currentFolk = folk;
    }

    /**
     * @return the current {@link Folk} of the {@link Player}.
     */
    public Folk getCurrentFolk() {
        return _currentFolk;
    }

    /**
     * @return True if Player is a participant in the Festival of Darkness.
     */
    public boolean isFestivalParticipant() {
        return FestivalOfDarknessManager.getInstance().isParticipant(this);
    }

    public void addAutoSoulShot(int itemId) {
        _activeSoulShots.add(itemId);
    }

    public boolean removeAutoSoulShot(int itemId) {
        return _activeSoulShots.remove(itemId);
    }

    public Set<Integer> getAutoSoulShot() {
        return _activeSoulShots;
    }

    @Override
    public boolean isChargedShot(ShotType type) {
        ItemInstance weapon = getActiveWeaponInstance();
        return weapon != null && weapon.isChargedShot(type);
    }

    @Override
    public void setChargedShot(ShotType type, boolean charged) {
        ItemInstance weapon = getActiveWeaponInstance();
        if (weapon != null)
            weapon.setChargedShot(type, charged);
    }

    @Override
    public void rechargeShots(boolean physical, boolean magic) {
        if (_activeSoulShots.isEmpty())
            return;

        for (int itemId : _activeSoulShots) {
            ItemInstance item = getInventory().getItemByItemId(itemId);
            if (item != null) {
                if (magic && item.getItem().getDefaultAction() == ActionType.spiritshot) {
                    IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                    if (handler != null)
                        handler.useItem(this, item, false);
                }

                if (physical && item.getItem().getDefaultAction() == ActionType.soulshot) {
                    IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
                    if (handler != null)
                        handler.useItem(this, item, false);
                }
            } else
                removeAutoSoulShot(itemId);
        }
    }

    /**
     * Cancel autoshot use for shot itemId
     *
     * @param itemId int id to disable
     * @return true if canceled.
     */
    public boolean disableAutoShot(int itemId) {
        if (_activeSoulShots.contains(itemId)) {
            removeAutoSoulShot(itemId);
            sendPacket(new ExAutoSoulShot(itemId, 0));
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
            return true;
        }

        return false;
    }

    /**
     * Cancel all autoshots for player
     */
    public void disableAutoShotsAll() {
        for (int itemId : _activeSoulShots) {
            sendPacket(new ExAutoSoulShot(itemId, 0));
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
        }
        _activeSoulShots.clear();
    }

    public int getClanPrivileges() {
        return _clanPrivileges;
    }

    public void setClanPrivileges(int n) {
        _clanPrivileges = n;
    }

    public int getPledgeClass() {
        return _pledgeClass;
    }

    public void setPledgeClass(int classId) {
        _pledgeClass = classId;
    }

    public int getPledgeType() {
        return _pledgeType;
    }

    public void setPledgeType(int typeId) {
        _pledgeType = typeId;
    }

    public int getApprentice() {
        return _apprentice;
    }

    public void setApprentice(int id) {
        _apprentice = id;
    }

    public int getSponsor() {
        return _sponsor;
    }

    public void setSponsor(int id) {
        _sponsor = id;
    }

    @Override
    public void sendMessage(String message) {
        sendPacket(SystemMessage.sendString(message));
    }

    /**
     * Unsummon all types of summons : pets, cubics, normal summons and trained beasts.
     */
    public void dropAllSummons() {
        // Delete summons and pets
        if (_summon != null)
            _summon.unSummon(this);

        // Delete trained beasts
        if (_tamedBeast != null)
            _tamedBeast.deleteMe();

        // Delete any form of cubics
        stopCubics();
    }

    public void enterObserverMode(int x, int y, int z) {
        dropAllSummons();

        if (getParty() != null)
            getParty().removePartyMember(this, MessageType.EXPELLED);

        standUp();

        _savedLocation.set(getPosition());

        setTarget(null);
        setIsInvul(true);
        getAppearance().setInvisible();
        setIsParalyzed(true);
        startParalyze();

        teleportTo(x, y, z, 0);
        sendPacket(new ObservationMode(x, y, z));
    }

    public void enterOlympiadObserverMode(int id) {
        final OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(id);
        if (task == null)
            return;

        dropAllSummons();

        if (getParty() != null)
            getParty().removePartyMember(this, MessageType.EXPELLED);

        _olympiadGameId = id;

        standUp();

        // Don't override saved location if we jump from stadium to stadium.
        if (!isInObserverMode())
            _savedLocation.set(getPosition());

        setTarget(null);
        setIsInvul(true);
        getAppearance().setInvisible();

        teleportTo(task.getZone().getLocs().get(2), 0);
        sendPacket(new ExOlympiadMode(3));
    }

    public void leaveObserverMode() {
        if (hasAI())
            getAI().setIntention(IntentionType.IDLE);

        setTarget(null);
        getAppearance().setVisible();
        setIsInvul(false);
        setIsParalyzed(false);
        stopParalyze();

        sendPacket(new ObservationReturn(_savedLocation));
        teleportTo(_savedLocation, 0);

        // Clear the location.
        _savedLocation.clean();
    }

    public void leaveOlympiadObserverMode() {
        if (_olympiadGameId == -1)
            return;

        _olympiadGameId = -1;

        if (hasAI())
            getAI().setIntention(IntentionType.IDLE);

        setTarget(null);
        getAppearance().setVisible();
        setIsInvul(false);

        sendPacket(new ExOlympiadMode(0));
        teleportTo(_savedLocation, 0);

        // Clear the location.
        _savedLocation.clean();
    }

    public int getOlympiadSide() {
        return _olympiadSide;
    }

    public void setOlympiadSide(int i) {
        _olympiadSide = i;
    }

    public int getOlympiadGameId() {
        return _olympiadGameId;
    }

    public void setOlympiadGameId(int id) {
        _olympiadGameId = id;
    }

    public Location getSavedLocation() {
        return _savedLocation;
    }

    public boolean isInObserverMode() {
        return !_isInOlympiadMode && !_savedLocation.equals(Location.DUMMY_LOC);
    }

    public int getTeleMode() {
        return _teleMode;
    }

    public void setTeleMode(int mode) {
        _teleMode = mode;
    }

    public int getLoto(int i) {
        return _loto[i];
    }

    public void setLoto(int i, int val) {
        _loto[i] = val;
    }

    public int getRace(int i) {
        return _race[i];
    }

    public void setRace(int i, int val) {
        _race[i] = val;
    }

    public boolean isInRefusalMode() {
        return _messageRefusal;
    }

    public void setInRefusalMode(boolean mode) {
        _messageRefusal = mode;
        sendPacket(new EtcStatusUpdate(this));
    }

    public void setTradeRefusal(boolean mode) {
        _tradeRefusal = mode;
    }

    public boolean getTradeRefusal() {
        return _tradeRefusal;
    }

    public void setExchangeRefusal(boolean mode) {
        _exchangeRefusal = mode;
    }

    public boolean getExchangeRefusal() {
        return _exchangeRefusal;
    }

    public BlockList getBlockList() {
        return _blockList;
    }

    public boolean isHero() {
        return _isHero;
    }

    public void setHero(boolean hero) {
        if (hero && _baseClass == _activeClass) {
            for (L2Skill skill : SkillTable.getHeroSkills())
                addSkill(skill, false);
        } else {
            for (L2Skill skill : SkillTable.getHeroSkills())
                removeSkill(skill.getId(), false);
        }
        _isHero = hero;

        sendSkillList();
    }

    public boolean isOlympiadStart() {
        return _isInOlympiadStart;
    }

    public void setOlympiadStart(boolean b) {
        _isInOlympiadStart = b;
    }

    public boolean isInOlympiadMode() {
        return _isInOlympiadMode;
    }

    public void setOlympiadMode(boolean b) {
        _isInOlympiadMode = b;
    }

    public boolean isInDuel() {
        return _duelId > 0;
    }

    public int getDuelId() {
        return _duelId;
    }

    public void setDuelState(Duel.DuelState state) {
        _duelState = state;
    }

    public Duel.DuelState getDuelState() {
        return _duelState;
    }

    /**
     * Sets up the duel state using a non 0 duelId.
     *
     * @param duelId 0=not in a duel
     */
    public void setInDuel(int duelId) {
        if (duelId > 0) {
            _duelState = Duel.DuelState.ON_COUNTDOWN;
            _duelId = duelId;
        } else {
            if (_duelState == Duel.DuelState.DEAD) {
                enableAllSkills();
                getStatus().startHpMpRegeneration();
            }
            _duelState = Duel.DuelState.NO_DUEL;
            _duelId = 0;
        }
    }

    /**
     * This returns a SystemMessage stating why the player is not available for duelling.
     *
     * @return S1_CANNOT_DUEL... message
     */
    public SystemMessage getNoDuelReason() {
        // Prepare the message with the good reason.
        final SystemMessage sm = SystemMessage.getSystemMessage(_noDuelReason).addCharName(this);

        // Reinitialize the reason.
        _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;

        // Send stored reason.
        return sm;
    }

    /**
     * Checks if this player might join / start a duel. To get the reason use getNoDuelReason() after calling this function.
     *
     * @return true if the player might join/start a duel.
     */
    public boolean canDuel() {
        if (isInCombat() || _punishment.getType() == PunishmentType.JAIL)
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
        else if (isDead() || isAlikeDead() || (getCurrentHp() < getMaxHp() / 2 || getCurrentMp() < getMaxMp() / 2))
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_HP_OR_MP_IS_BELOW_50_PERCENT;
        else if (isInDuel())
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
        else if (isInOlympiadMode())
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
        else if (isCursedWeaponEquipped() || getKarma() != 0)
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE;
        else if (isInStoreMode())
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
        else if (isMounted() || isInBoat())
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
        else if (isFishing())
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING;
        else if (isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.PEACE) || isInsideZone(ZoneId.SIEGE))
            _noDuelReason = SystemMessageId.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
        else
            return true;

        return false;
    }

    public boolean isNoble() {
        return _isNoble;
    }

    /**
     * Set Noblesse Status, and reward with nobles' skills.
     *
     * @param val   Add skills if setted to true, else remove skills.
     * @param store Store the status directly in the db if setted to true.
     */
    public void setNoble(boolean val, boolean store) {
        if (val)
            for (L2Skill skill : SkillTable.getNobleSkills())
                addSkill(skill, false);
        else
            for (L2Skill skill : SkillTable.getNobleSkills())
                removeSkill(skill.getId(), false);

        _isNoble = val;

        sendSkillList();

        if (store) {
            try (Connection con = L2DatabaseFactory.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(UPDATE_NOBLESS)) {
                ps.setBoolean(1, val);
                ps.setInt(2, getObjectId());
                ps.executeUpdate();
            } catch (Exception e) {
                WorldObject.LOGGER.error("Couldn't update nobless status for {}.", e, getName());
            }
        }
    }

    public void setLvlJoinedAcademy(int lvl) {
        _lvlJoinedAcademy = lvl;
    }

    public int getLvlJoinedAcademy() {
        return _lvlJoinedAcademy;
    }

    public boolean isAcademyMember() {
        return _lvlJoinedAcademy > 0;
    }

    public void setTeam(TeamType team) {
        _team = team;
    }

    public TeamType getTeam() {
        return _team;
    }

    public void setWantsPeace(boolean wantsPeace) {
        _wantsPeace = wantsPeace;
    }

    public boolean wantsPeace() {
        return _wantsPeace;
    }

    public boolean isFishing() {
        return _fishingStance.isUnderFishCombat() || _fishingStance.isLookingForFish();
    }

    public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance) {
        _alliedVarkaKetra = sideAndLvlOfAlliance;
    }

    /**
     * [-5,-1] varka, 0 neutral, [1,5] ketra
     *
     * @return the side faction.
     */
    public int getAllianceWithVarkaKetra() {
        return _alliedVarkaKetra;
    }

    public boolean isAlliedWithVarka() {
        return _alliedVarkaKetra < 0;
    }

    public boolean isAlliedWithKetra() {
        return _alliedVarkaKetra > 0;
    }

    public void sendSkillList() {
        final boolean isWearingFormalWear = isWearingFormalWear();
        final boolean isClanDisabled = getClan() != null && getClan().getReputationScore() < 0;

        final SkillList sl = new SkillList();
        for (L2Skill skill : getSkills().values())
            sl.addSkill(skill.getId(), skill.getLevel(), skill.isPassive(), isWearingFormalWear || (skill.isClanSkill() && isClanDisabled));

        sendPacket(sl);
    }

    /**
     * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>) for this character.<BR>
     * 2. This method no longer changes the active _classIndex of the player. This is only done by the calling of setActiveClass() method as that should be the only way to do so.
     *
     * @param classId
     * @param classIndex
     * @return boolean subclassAdded
     */
    public boolean addSubClass(int classId, int classIndex) {
        if (!_subclassLock.tryLock())
            return false;

        try {
            if (_subClasses.size() == 3 || classIndex == 0 || _subClasses.containsKey(classIndex))
                return false;

            final SubClass subclass = new SubClass(classId, classIndex);

            try (Connection con = L2DatabaseFactory.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(ADD_CHAR_SUBCLASS)) {
                ps.setInt(1, getObjectId());
                ps.setInt(2, subclass.getClassId());
                ps.setLong(3, subclass.getExp());
                ps.setInt(4, subclass.getSp());
                ps.setInt(5, subclass.getLevel());
                ps.setInt(6, subclass.getClassIndex());
                ps.execute();
            } catch (Exception e) {
                WorldObject.LOGGER.error("Couldn't add subclass for {}.", e, getName());
                return false;
            }

            _subClasses.put(subclass.getClassIndex(), subclass);

            PlayerData.getInstance().getTemplate(classId).getSkills().stream().filter(s -> s.getMinLvl() <= 40).collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL))).forEach((i, s) ->
            {
                storeSkill(s.get().getSkill(), classIndex);
            });

            return true;
        } finally {
            _subclassLock.unlock();
        }
    }

    /**
     * 1. Completely erase all existance of the subClass linked to the classIndex.<BR>
     * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR>
     * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR>
     *
     * @param classIndex
     * @param newClassId
     * @return boolean subclassAdded
     */
    public boolean modifySubClass(int classIndex, int newClassId) {
        if (!_subclassLock.tryLock())
            return false;

        try {
            try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
                // Remove all henna info stored for this sub-class.
                try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_HENNAS)) {
                    ps.setInt(1, getObjectId());
                    ps.setInt(2, classIndex);
                    ps.execute();
                }

                // Remove all shortcuts info stored for this sub-class.
                try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SHORTCUTS)) {
                    ps.setInt(1, getObjectId());
                    ps.setInt(2, classIndex);
                    ps.execute();
                }

                // Remove all effects info stored for this sub-class.
                try (PreparedStatement ps = con.prepareStatement(DELETE_SKILL_SAVE)) {
                    ps.setInt(1, getObjectId());
                    ps.setInt(2, classIndex);
                    ps.execute();
                }

                // Remove all skill info stored for this sub-class.
                try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SKILLS)) {
                    ps.setInt(1, getObjectId());
                    ps.setInt(2, classIndex);
                    ps.execute();
                }

                // Remove all basic info stored about this sub-class.
                try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SUBCLASS)) {
                    ps.setInt(1, getObjectId());
                    ps.setInt(2, classIndex);
                    ps.execute();
                }
            } catch (Exception e) {
                WorldObject.LOGGER.error("Couldn't modify subclass for {} to class index {}.", e, getName(), classIndex);

                // This must be done in order to maintain data consistency.
                _subClasses.remove(classIndex);
                return false;
            }

            _subClasses.remove(classIndex);
        } finally {
            _subclassLock.unlock();
        }

        return addSubClass(newClassId, classIndex);
    }

    public boolean isSubClassActive() {
        return _classIndex > 0;
    }

    public Map<Integer, SubClass> getSubClasses() {
        return _subClasses;
    }

    public int getBaseClass() {
        return _baseClass;
    }

    public int getActiveClass() {
        return _activeClass;
    }

    public int getClassIndex() {
        return _classIndex;
    }

    private void setClassTemplate(int classId) {
        _activeClass = classId;

        // Set the template of the Player
        setTemplate(PlayerData.getInstance().getTemplate(classId));
    }

    /**
     * Changes the character's class based on the given class index. <BR>
     * <BR>
     * An index of zero specifies the character's original (base) class, while indexes 1-3 specifies the character's sub-classes respectively.
     *
     * @param classIndex
     * @return true if successful.
     */
    public boolean setActiveClass(int classIndex) {
        if (!_subclassLock.tryLock())
            return false;

        try {
            // Remove active item skills before saving char to database because next time when choosing this class, worn items can be different
            for (ItemInstance item : getInventory().getAugmentedItems()) {
                if (item != null && item.isEquipped())
                    item.getAugmentation().removeBonus(this);
            }

            // abort any kind of cast.
            abortCast();

            // Stop casting for any player that may be casting a force buff on this l2pcinstance.
            for (Creature character : getKnownType(Creature.class))
                if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
                    character.abortCast();

            store();
            _reuseTimeStamps.clear();

            // clear charges
            _charges.set(0);
            stopChargeTask();

            if (classIndex == 0)
                setClassTemplate(getBaseClass());
            else {
                try {
                    setClassTemplate(_subClasses.get(classIndex).getClassId());
                } catch (Exception e) {
                    WorldObject.LOGGER.error("Could not switch {}'s subclass to class index {}.", e, getName(), classIndex);
                    return false;
                }
            }
            _classIndex = classIndex;

            if (_party != null)
                _party.recalculateLevel();

            if (_summon instanceof Servitor)
                _summon.unSummon(this);

            for (L2Skill skill : getSkills().values())
                removeSkill(skill.getId(), false);

            stopAllEffectsExceptThoseThatLastThroughDeath();
            stopCubics();

            if (isSubClassActive()) {
                _dwarvenRecipeBook.clear();
                _commonRecipeBook.clear();
            } else
                restoreRecipeBook();

            restoreSkills();
            giveSkills();
            regiveTemporarySkills();

            // Prevents some issues when changing between subclases that shares skills
            getDisabledSkills().clear();

            restoreEffects();
            updateEffectIcons();
            sendPacket(new EtcStatusUpdate(this));

            // If player has quest "Repent Your Sins", remove it
            QuestState st = getQuestState("Q422_RepentYourSins");
            if (st != null)
                st.exitQuest(true);

            _hennaList.restore();
            sendPacket(new HennaInfo(this));

            if (getCurrentHp() > getMaxHp())
                setCurrentHp(getMaxHp());
            if (getCurrentMp() > getMaxMp())
                setCurrentMp(getMaxMp());
            if (getCurrentCp() > getMaxCp())
                setCurrentCp(getMaxCp());

            refreshOverloaded();
            refreshExpertisePenalty();
            broadcastUserInfo();

            // Clear resurrect xp calculation
            setExpBeforeDeath(0);

            // Remove shot automation
            disableAutoShotsAll();

            // Discharge any active shots
            ItemInstance item = getActiveWeaponInstance();
            if (item != null)
                item.unChargeAllShots();

            _shortcutList.restore();
            sendPacket(new ShortCutInit(this));

            broadcastPacket(new SocialAction(this, 15));
            sendPacket(new SkillCoolTime(this));
            return true;
        } finally {
            _subclassLock.unlock();
        }
    }

    public boolean isLocked() {
        return _subclassLock.isLocked();
    }

    public void onPlayerEnter() {
        if (isCursedWeaponEquipped())
            CursedWeaponManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).cursedOnLogin();

        // Add to the GameTimeTask to keep inform about activity time.
        GameTimeTaskManager.getInstance().add(this);

        // Teleport player if the Seven Signs period isn't the good one, or if the player isn't in a cabal.
        if (isIn7sDungeon() && !isGM()) {
            if (SevenSignsManager.getInstance().isSealValidationPeriod() || SevenSignsManager.getInstance().isCompResultsPeriod()) {
                if (SevenSignsManager.getInstance().getPlayerCabal(getObjectId()) != SevenSignsManager.getInstance().getCabalHighestScore()) {
                    teleportTo(TeleportType.TOWN);
                    setIsIn7sDungeon(false);
                }
            } else if (SevenSignsManager.getInstance().getPlayerCabal(getObjectId()) == CabalType.NORMAL) {
                teleportTo(TeleportType.TOWN);
                setIsIn7sDungeon(false);
            }
        }

        // Jail task
        _punishment.handle();

        if (isGM()) {
            if (isInvul())
                sendMessage("Entering world in Invulnerable mode.");
            if (getAppearance().getInvisible())
                sendMessage("Entering world in Invisible mode.");
            if (isInRefusalMode())
                sendMessage("Entering world in Message Refusal mode.");
        }

        revalidateZone(true);
        notifyFriends(true);
    }

    public long getLastAccess() {
        return _lastAccess;
    }

    @Override
    public void doRevive() {
        super.doRevive();

        stopEffects(L2EffectType.CHARMOFCOURAGE);
        sendPacket(new EtcStatusUpdate(this));

        _reviveRequested = 0;
        _revivePower = 0;

        if (isMounted())
            startFeed(_mountNpcId);

        // Schedule a paralyzed task to wait for the animation to finish
        ThreadPool.schedule(() -> setIsParalyzed(false), (int) (2000 / getStat().getMovementSpeedMultiplier()));
        setIsParalyzed(true);
    }

    @Override
    public void doRevive(double revivePower) {
        // Restore the player's lost experience, depending on the % return of the skill used (based on its power).
        restoreExp(revivePower);
        doRevive();
    }

    public void reviveRequest(Player Reviver, L2Skill skill, boolean Pet) {
        if (_reviveRequested == 1) {
            // Resurrection has already been proposed.
            if (_revivePet == Pet)
                Reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
            else {
                if (Pet)
                    // A pet cannot be resurrected while it's owner is in the process of resurrecting.
                    Reviver.sendPacket(SystemMessageId.CANNOT_RES_PET2);
                else
                    // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
                    Reviver.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
            }
            return;
        }

        if ((Pet && _summon != null && _summon.isDead()) || (!Pet && isDead())) {
            _reviveRequested = 1;

            if (isPhoenixBlessed())
                _revivePower = 100;
            else if (isAffected(L2EffectFlag.CHARM_OF_COURAGE))
                _revivePower = 0;
            else
                _revivePower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), Reviver);

            _revivePet = Pet;

            if (isAffected(L2EffectFlag.CHARM_OF_COURAGE)) {
                sendPacket(new ConfirmDlg(SystemMessageId.DO_YOU_WANT_TO_BE_RESTORED).addTime(60000));
                return;
            }

            sendPacket(new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_S1).addCharName(Reviver));
        }
    }

    public void reviveAnswer(int answer) {
        if (_reviveRequested != 1 || (!isDead() && !_revivePet) || (_revivePet && _summon != null && !_summon.isDead()))
            return;

        if (answer == 0 && isPhoenixBlessed())
            stopPhoenixBlessing(null);
        else if (answer == 1) {
            if (!_revivePet) {
                if (_revivePower != 0)
                    doRevive(_revivePower);
                else
                    doRevive();
            } else if (_summon != null) {
                if (_revivePower != 0)
                    _summon.doRevive(_revivePower);
                else
                    _summon.doRevive();
            }
        }
        _reviveRequested = 0;
        _revivePower = 0;
    }

    public boolean isReviveRequested() {
        return (_reviveRequested == 1);
    }

    public boolean isRevivingPet() {
        return _revivePet;
    }

    public void removeReviving() {
        _reviveRequested = 0;
        _revivePower = 0;
    }

    public void onActionRequest() {
        if (isSpawnProtected()) {
            sendMessage("As you acted, you are no longer under spawn protection.");
            setSpawnProtection(false);
        }
    }

    @Override
    public final void onTeleported() {
        super.onTeleported();

        if (Config.PLAYER_SPAWN_PROTECTION > 0)
            setSpawnProtection(true);

        // Stop toggles upon teleport.
        if (!isGM())
            stopAllToggles();

        // Modify the position of the tamed beast if necessary
        if (_tamedBeast != null) {
            _tamedBeast.getAI().stopFollow();
            _tamedBeast.teleportTo(getPosition(), 0);
            _tamedBeast.getAI().startFollow(this);
        }

        // Modify the position of the pet if necessary
        if (_summon != null) {
            _summon.setFollowStatus(false);
            _summon.teleportTo(getPosition(), 0);
            ((SummonAI) _summon.getAI()).setStartFollowController(true);
            _summon.setFollowStatus(true);
        }
    }

    @Override
    public void addExpAndSp(long addToExp, int addToSp) {
        getStat().addExpAndSp(addToExp, addToSp);
    }

    public void addExpAndSp(long addToExp, int addToSp, Map<Creature, RewardInfo> rewards) {
        getStat().addExpAndSp(addToExp, addToSp, rewards);
    }

    public void removeExpAndSp(long removeExp, int removeSp) {
        getStat().removeExpAndSp(removeExp, removeSp);
    }

    @Override
    public void reduceCurrentHp(double value, Creature attacker, boolean awake, boolean isDOT, L2Skill skill) {
        if (skill != null)
            getStatus().reduceHp(value, attacker, awake, isDOT, skill.isToggle(), skill.getDmgDirectlyToHP());
        else
            getStatus().reduceHp(value, attacker, awake, isDOT, false, false);

        // notify the tamed beast of attacks
        if (_tamedBeast != null)
            _tamedBeast.onOwnerGotAttacked(attacker);
    }

    public synchronized void addBypass(String bypass) {
        if (bypass == null)
            return;

        _validBypass.add(bypass);
    }

    public synchronized void addBypass2(String bypass) {
        if (bypass == null)
            return;

        _validBypass2.add(bypass);
    }

    public synchronized boolean validateBypass(String cmd) {
        for (String bp : _validBypass) {
            if (bp == null)
                continue;

            if (bp.equals(cmd))
                return true;
        }

        for (String bp : _validBypass2) {
            if (bp == null)
                continue;

            if (cmd.startsWith(bp))
                return true;
        }

        return false;
    }

    /**
     * Test cases (player drop, trade item) where the item shouldn't be able to manipulate.
     *
     * @param objectId : The item objectId.
     * @return true if it the item can be manipulated, false ovtherwise.
     */
    public ItemInstance validateItemManipulation(int objectId) {
        final ItemInstance item = getInventory().getItemByObjectId(objectId);

        // You don't own the item, or item is null.
        if (item == null || item.getOwnerId() != getObjectId())
            return null;

        // Pet whom item you try to manipulate is summoned/mounted.
        if (_summon != null && _summon.getControlItemId() == objectId || _mountObjectId == objectId)
            return null;

        // Item is under enchant process.
        if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
            return null;

        // Can't trade a cursed weapon.
        if (CursedWeaponManager.getInstance().isCursed(item.getItemId()))
            return null;

        return item;
    }

    public synchronized void clearBypass() {
        _validBypass.clear();
        _validBypass2.clear();
    }

    /**
     * @return Returns the inBoat.
     */
    public boolean isInBoat() {
        return _boat != null;
    }

    public Boat getBoat() {
        return _boat;
    }

    public void setBoat(Boat v) {
        if (v == null && _boat != null)
            _boat.removePassenger(this);

        _boat = v;
    }

    public void setCrystallizing(boolean mode) {
        _isCrystallizing = mode;
    }

    public boolean isCrystallizing() {
        return _isCrystallizing;
    }

    public SpawnLocation getBoatPosition() {
        return _boatPosition;
    }

    /**
     * Manage the delete task of a Player (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).
     * <ul>
     * <li>If the Player is in observer mode, set its position to its position before entering in observer mode</li>
     * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess</li>
     * <li>Stop the HP/MP/CP Regeneration task</li>
     * <li>Cancel Crafting, Attak or Cast</li>
     * <li>Remove the Player from the world</li>
     * <li>Stop Party and Unsummon Pet</li>
     * <li>Update database with items in its inventory and remove them from the world</li>
     * <li>Remove the object from region</li>
     * <li>Close the connection with the client</li>
     * </ul>
     */
    @Override
    public void deleteMe() {
        cleanup();
        store();
        super.deleteMe();
    }

    private synchronized void cleanup() {
        try {
            // Put the online status to false
            setOnlineStatus(false, true);

            // abort cast & attack and remove the target. Cancels movement aswell.
            abortAttack();
            abortCast();
            stopMove(null);
            setTarget(null);

            removeMeFromPartyMatch();

            if (isFlying())
                removeSkill(FrequentSkill.WYVERN_BREATH.getSkill().getId(), false);

            // Dismount the player.
            if (isMounted())
                dismount();
                // If the Player has a summon, unsummon it.
            else if (_summon != null)
                _summon.unSummon(this);

            // Stop all scheduled tasks.
            stopHpMpRegeneration();
            stopChargeTask();

            _punishment.stopTask(true);

            // Stop all timers associated to that Player.
            WaterTaskManager.getInstance().remove(this);
            AttackStanceTaskManager.getInstance().remove(this);
            PvpFlagTaskManager.getInstance().remove(this);
            GameTimeTaskManager.getInstance().remove(this);
            ShadowItemTaskManager.getInstance().remove(this);

            // Cancel the cast of eventual fusion skill users on this target.
            for (Creature character : getKnownType(Creature.class))
                if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
                    character.abortCast();

            // Stop signets & toggles effects.
            for (L2Effect effect : getAllEffects()) {
                if (effect.getSkill().isToggle()) {
                    effect.exit();
                    continue;
                }

                switch (effect.getEffectType()) {
                    case SIGNET_GROUND:
                    case SIGNET_EFFECT:
                        effect.exit();
                        break;
                }
            }

            // Remove the Player from the world
            decayMe();

            // If a party is in progress, leave it
            if (_party != null)
                _party.removePartyMember(this, MessageType.DISCONNECTED);

            // Handle removal from olympiad game
            if (OlympiadManager.getInstance().isRegistered(this) || getOlympiadGameId() != -1)
                OlympiadManager.getInstance().removeDisconnectedCompetitor(this);

            // set the status for pledge member list to OFFLINE
            if (getClan() != null) {
                ClanMember clanMember = getClan().getClanMember(getObjectId());
                if (clanMember != null)
                    clanMember.setPlayerInstance(null);
            }

            // deals with sudden exit in the middle of transaction
            if (getActiveRequester() != null) {
                setActiveRequester(null);
                cancelActiveTrade();
            }

            // If the Player is a GM, remove it from the GM List
            if (isGM())
                AdminData.getInstance().deleteGm(this);

            // Check if the Player is in observer mode to set its position to its position before entering in observer mode
            if (isInObserverMode())
                setXYZInvisible(_savedLocation);

            // Oust player from boat
            if (getBoat() != null)
                getBoat().oustPlayer(this, true, Location.DUMMY_LOC);

            // Update inventory and remove them from the world
            getInventory().deleteMe();

            // Update warehouse and remove them from the world
            clearWarehouse();

            // Update freight and remove them from the world
            clearFreight();
            clearDepositedFreight();

            if (isCursedWeaponEquipped())
                CursedWeaponManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId).setPlayer(null);

            if (getClanId() > 0)
                getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);

            if (isSeated()) {
                final WorldObject object = World.getInstance().getObject(_throneId);
                if (object instanceof StaticObject)
                    ((StaticObject) object).setBusy(false);
            }

            World.getInstance().removePlayer(this); // force remove in case of crash during teleport

            // friends & blocklist update
            notifyFriends(false);
            getBlockList().playerLogout();
            AutobotsManager.INSTANCE.onLogout(this);
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't disconnect correctly the player.", e);
        }
    }

    public FishingStance getFishingStance() {
        return _fishingStance;
    }

    public int getInventoryLimit() {
        return ((getRace() == ClassRace.DWARF) ? Config.INVENTORY_MAXIMUM_DWARF : Config.INVENTORY_MAXIMUM_NO_DWARF) + (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);
    }

    public static int getQuestInventoryLimit() {
        return Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
    }

    public int getWareHouseLimit() {
        return ((getRace() == ClassRace.DWARF) ? Config.WAREHOUSE_SLOTS_DWARF : Config.WAREHOUSE_SLOTS_NO_DWARF) + (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);
    }

    public int getPrivateSellStoreLimit() {
        return ((getRace() == ClassRace.DWARF) ? Config.MAX_PVTSTORE_SLOTS_DWARF : Config.MAX_PVTSTORE_SLOTS_OTHER) + (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);
    }

    public int getPrivateBuyStoreLimit() {
        return ((getRace() == ClassRace.DWARF) ? Config.MAX_PVTSTORE_SLOTS_DWARF : Config.MAX_PVTSTORE_SLOTS_OTHER) + (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);
    }

    public int getFreightLimit() {
        return Config.FREIGHT_SLOTS + (int) getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
    }

    public int getDwarfRecipeLimit() {
        return Config.DWARF_RECIPE_LIMIT + (int) getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
    }

    public int getCommonRecipeLimit() {
        return Config.COMMON_RECIPE_LIMIT + (int) getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
    }

    public int getMountNpcId() {
        return _mountNpcId;
    }

    public int getMountLevel() {
        return _mountLevel;
    }

    public void setMountObjectId(int id) {
        _mountObjectId = id;
    }

    public int getMountObjectId() {
        return _mountObjectId;
    }

    @Override
    public Map<Integer, L2Skill> getSkills() {
        return _skills;
    }

    /**
     * @return the current player skill in use.
     */
    public SkillUseHolder getCurrentSkill() {
        return _currentSkill;
    }

    /**
     * Update the _currentSkill holder.
     *
     * @param skill        : The skill to update for (or null)
     * @param ctrlPressed  : The boolean information regarding ctrl key.
     * @param shiftPressed : The boolean information regarding shift key.
     */
    public void setCurrentSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed) {
        _currentSkill.setSkill(skill);
        _currentSkill.setCtrlPressed(ctrlPressed);
        _currentSkill.setShiftPressed(shiftPressed);
    }

    /**
     * @return the current pet skill in use.
     */
    public SkillUseHolder getCurrentPetSkill() {
        return _currentPetSkill;
    }

    /**
     * Update the _currentPetSkill holder.
     *
     * @param skill        : The skill to update for (or null)
     * @param ctrlPressed  : The boolean information regarding ctrl key.
     * @param shiftPressed : The boolean information regarding shift key.
     */
    public void setCurrentPetSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed) {
        _currentPetSkill.setSkill(skill);
        _currentPetSkill.setCtrlPressed(ctrlPressed);
        _currentPetSkill.setShiftPressed(shiftPressed);
    }

    /**
     * @return the current queued skill in use.
     */
    public SkillUseHolder getQueuedSkill() {
        return _queuedSkill;
    }

    /**
     * Update the _queuedSkill holder.
     *
     * @param skill        : The skill to update for (or null)
     * @param ctrlPressed  : The boolean information regarding ctrl key.
     * @param shiftPressed : The boolean information regarding shift key.
     */
    public void setQueuedSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed) {
        _queuedSkill.setSkill(skill);
        _queuedSkill.setCtrlPressed(ctrlPressed);
        _queuedSkill.setShiftPressed(shiftPressed);
    }

    public Punishment getPunishment() {
        return _punishment;
    }

    /**
     * @return true if the {@link Player} is jailed, false otherwise.
     */
    public boolean isInJail() {
        return _punishment.getType() == PunishmentType.JAIL;
    }

    /**
     * @return true if the {@link Player} is chat banned, false otherwise.
     */
    public boolean isChatBanned() {
        return _punishment.getType() == PunishmentType.CHAT;
    }

    public int getPowerGrade() {
        return _powerGrade;
    }

    public void setPowerGrade(int power) {
        _powerGrade = power;
    }

    public boolean isCursedWeaponEquipped() {
        return _cursedWeaponEquippedId != 0;
    }

    public void setCursedWeaponEquippedId(int value) {
        _cursedWeaponEquippedId = value;
    }

    public int getCursedWeaponEquippedId() {
        return _cursedWeaponEquippedId;
    }

    public void shortBuffStatusUpdate(int magicId, int level, int time) {
        if (_shortBuffTask != null) {
            _shortBuffTask.cancel(false);
            _shortBuffTask = null;
        }

        _shortBuffTask = ThreadPool.schedule(() ->
        {
            sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
            setShortBuffTaskSkillId(0);
        }, time * 1000);
        setShortBuffTaskSkillId(magicId);

        sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
    }

    public int getShortBuffTaskSkillId() {
        return _shortBuffTaskSkillId;
    }

    public void setShortBuffTaskSkillId(int id) {
        _shortBuffTaskSkillId = id;
    }

    public int getDeathPenaltyBuffLevel() {
        return _deathPenaltyBuffLevel;
    }

    public void setDeathPenaltyBuffLevel(int level) {
        _deathPenaltyBuffLevel = level;
    }

    public void calculateDeathPenaltyBuffLevel(Creature killer) {
        if (_deathPenaltyBuffLevel >= 15) // maximum level reached
            return;

        if ((getKarma() > 0 || Rnd.get(1, 100) <= Config.DEATH_PENALTY_CHANCE) && !(killer instanceof Player) && !isGM() && !(getCharmOfLuck() && (killer == null || killer.isRaidRelated())) && !isPhoenixBlessed() && !(isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.SIEGE))) {
            if (_deathPenaltyBuffLevel != 0)
                removeSkill(5076, false);

            _deathPenaltyBuffLevel++;

            addSkill(SkillTable.getInstance().getInfo(5076, _deathPenaltyBuffLevel), false);
            sendPacket(new EtcStatusUpdate(this));
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(_deathPenaltyBuffLevel));
        }
    }

    public void reduceDeathPenaltyBuffLevel() {
        if (_deathPenaltyBuffLevel <= 0)
            return;

        removeSkill(5076, false);

        _deathPenaltyBuffLevel--;

        if (_deathPenaltyBuffLevel > 0) {
            addSkill(SkillTable.getInstance().getInfo(5076, _deathPenaltyBuffLevel), false);
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(_deathPenaltyBuffLevel));
        } else
            sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);

        sendPacket(new EtcStatusUpdate(this));
    }

    public void restoreDeathPenaltyBuffLevel() {
        if (_deathPenaltyBuffLevel > 0)
            addSkill(SkillTable.getInstance().getInfo(5076, _deathPenaltyBuffLevel), false);
    }

    private final Map<Integer, Timestamp> _reuseTimeStamps = new ConcurrentHashMap<>();

    public Collection<Timestamp> getReuseTimeStamps() {
        return _reuseTimeStamps.values();
    }

    public Map<Integer, Timestamp> getReuseTimeStamp() {
        return _reuseTimeStamps;
    }

    /**
     * Index according to skill id the current timestamp of use.
     *
     * @param skill
     * @param reuse delay
     */
    @Override
    public void addTimeStamp(L2Skill skill, long reuse) {
        _reuseTimeStamps.put(skill.getReuseHashCode(), new Timestamp(skill, reuse));
    }

    /**
     * Index according to skill this TimeStamp instance for restoration purposes only.
     *
     * @param skill
     * @param reuse
     * @param systime
     */
    public void addTimeStamp(L2Skill skill, long reuse, long systime) {
        _reuseTimeStamps.put(skill.getReuseHashCode(), new Timestamp(skill, reuse, systime));
    }

    @Override
    public Player getActingPlayer() {
        return this;
    }

    @Override
    public final void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss) {
        // Check if hit is missed
        if (miss) {
            sendPacket(SystemMessageId.MISSED_TARGET);
            return;
        }

        // Check if hit is critical
        if (pcrit)
            sendPacket(SystemMessageId.CRITICAL_HIT);
        if (mcrit)
            sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);

        if (target.isInvul()) {
            if (target.isParalyzed())
                sendPacket(SystemMessageId.OPPONENT_PETRIFIED);
            else
                sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
        } else
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber(damage));

        if (isInOlympiadMode() && target instanceof Player && ((Player) target).isInOlympiadMode() && ((Player) target).getOlympiadGameId() == getOlympiadGameId())
            OlympiadGameManager.getInstance().notifyCompetitorDamage(this, damage);
    }

    public void checkItemRestriction() {
        for (ItemInstance equippedItem : getInventory().getPaperdollItems()) {
            if (equippedItem.getItem().checkCondition(this, this, false))
                continue;

            useEquippableItem(equippedItem, equippedItem.isWeapon());
        }
    }

    /**
     * A method used to test player entrance on no landing zone.<br>
     * <br>
     * If a player is mounted on a Wyvern, it launches a dismount task after 5 seconds, and a warning message.
     */
    public void enterOnNoLandingZone() {
        if (getMountType() == 2) {
            if (_dismountTask == null)
                _dismountTask = ThreadPool.schedule(() -> dismount(), 5000);

            sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
        }
    }

    /**
     * A method used to test player leave on no landing zone.<br>
     * <br>
     * If a player is mounted on a Wyvern, it cancels the dismount task, if existing.
     */
    public void exitOnNoLandingZone() {
        if (getMountType() == 2 && _dismountTask != null) {
            _dismountTask.cancel(true);
            _dismountTask = null;
        }
    }

    public void setIsInSiege(boolean b) {
        _isInSiege = b;
    }

    public boolean isInSiege() {
        return _isInSiege;
    }

    /**
     * Remove player from BossZones (used on char logout/exit)
     */
    public void removeFromBossZone() {
        for (BossZone zone : ZoneManager.getInstance().getAllZones(BossZone.class))
            zone.removePlayer(this);
    }

    /**
     * @return the number of charges this Player got.
     */
    public int getCharges() {
        return _charges.get();
    }

    public void increaseCharges(int count, int max) {
        if (_charges.get() >= max) {
            sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
            return;
        }

        restartChargeTask();

        if (_charges.addAndGet(count) >= max) {
            _charges.set(max);
            sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
        } else
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(_charges.get()));

        sendPacket(new EtcStatusUpdate(this));
    }

    public boolean decreaseCharges(int count) {
        if (_charges.get() < count)
            return false;

        if (_charges.addAndGet(-count) == 0)
            stopChargeTask();
        else
            restartChargeTask();

        sendPacket(new EtcStatusUpdate(this));
        return true;
    }

    public void clearCharges() {
        _charges.set(0);
        sendPacket(new EtcStatusUpdate(this));
    }

    /**
     * Starts/Restarts the ChargeTask to Clear Charges after 10 Mins.
     */
    private void restartChargeTask() {
        if (_chargeTask != null) {
            _chargeTask.cancel(false);
            _chargeTask = null;
        }

        _chargeTask = ThreadPool.schedule(() -> clearCharges(), 600000);
    }

    /**
     * Stops the Charges Clearing Task.
     */
    public void stopChargeTask() {
        if (_chargeTask != null) {
            _chargeTask.cancel(false);
            _chargeTask = null;
        }
    }

    /**
     * Signets check used to valid who is affected when he entered in the aoe effect.
     *
     * @param cha The target to make checks on.
     * @return true if player can attack the target.
     */
    public boolean canAttackCharacter(Creature cha) {
        if (cha instanceof Attackable)
            return true;

        if (cha instanceof Playable) {
            if (cha.isInArena())
                return true;

            final Player target = cha.getActingPlayer();

            if (isInDuel() && target.isInDuel() && target.getDuelId() == getDuelId())
                return true;

            if (isInParty() && target.isInParty()) {
                if (getParty() == target.getParty())
                    return false;

                if ((getParty().getCommandChannel() != null || target.getParty().getCommandChannel() != null) && (getParty().getCommandChannel() == target.getParty().getCommandChannel()))
                    return false;
            }

            if (getClan() != null && target.getClan() != null) {
                if (getClanId() == target.getClanId())
                    return false;

                if ((getAllyId() > 0 || target.getAllyId() > 0) && getAllyId() == target.getAllyId())
                    return false;

                if (getClan().isAtWarWith(target.getClanId()))
                    return true;
            } else {
                if (target.getPvpFlag() == 0 && target.getKarma() == 0)
                    return false;
            }
        }
        return true;
    }

    /**
     * Teleport the current {@link Player} to the destination of another player.<br>
     * <br>
     * Check if summoning is allowed, and consume items if {@link L2Skill} got such constraints.
     *
     * @param player : The player to teleport on.
     * @param skill  : The skill used to find item consumption informations.
     */
    public void teleportToFriend(Player player, L2Skill skill) {
        if (player == null || skill == null)
            return;

        if (!player.checkSummonerStatus() || !player.checkSummonTargetStatus(this))
            return;

        final int itemConsumeId = skill.getTargetConsumeId();
        final int itemConsumeCount = skill.getTargetConsume();

        if (itemConsumeId != 0 && itemConsumeCount != 0) {
            if (getInventory().getInventoryItemCount(itemConsumeId, -1) < itemConsumeCount) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING).addItemName(skill.getTargetConsumeId()));
                return;
            }

            destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, this, true);
        }
        teleportTo(player.getX(), player.getY(), player.getZ(), 20);
    }

    /**
     * Test if the current {@link Player} can summon. Send back messages if he can't.
     *
     * @return true if the player can summon, false otherwise.
     */
    public boolean checkSummonerStatus() {
        if (isMounted())
            return false;

        if (isInOlympiadMode() || isInObserverMode() || isInsideZone(ZoneId.NO_SUMMON_FRIEND)) {
            sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
            return false;
        }
        return true;
    }

    /**
     * Test if the {@link WorldObject} can be summoned. Send back messages if he can't.
     *
     * @param target : The target to test.
     * @return true if the given target can be summoned, false otherwise.
     */
    public boolean checkSummonTargetStatus(WorldObject target) {
        if (!(target instanceof Player))
            return false;

        final Player player = (Player) target;

        if (player.isAlikeDead()) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addCharName(player));
            return false;
        }

        if (player.isInStoreMode()) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addCharName(player));
            return false;
        }

        if (player.isRooted() || player.isInCombat()) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addCharName(player));
            return false;
        }

        if (player.isInOlympiadMode()) {
            sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
            return false;
        }

        if (player.isFestivalParticipant() || player.isMounted()) {
            sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
            return false;
        }

        if (player.isInObserverMode() || player.isInsideZone(ZoneId.NO_SUMMON_FRIEND)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IN_SUMMON_BLOCKING_AREA).addCharName(player));
            return false;
        }

        return true;
    }

    public final int getClientX() {
        return _clientX;
    }

    public final int getClientY() {
        return _clientY;
    }

    public final int getClientZ() {
        return _clientZ;
    }

    public final void setClientX(int val) {
        _clientX = val;
    }

    public final void setClientY(int val) {
        _clientY = val;
    }

    public final void setClientZ(int val) {
        _clientZ = val;
    }

    /**
     * @return the mailPosition.
     */
    public int getMailPosition() {
        return _mailPosition;
    }

    /**
     * @param mailPosition The mailPosition to set.
     */
    public void setMailPosition(int mailPosition) {
        _mailPosition = mailPosition;
    }

    /**
     * @param z
     * @return true if character falling now On the start of fall return false for correct coord sync !
     */
    public final boolean isFalling(int z) {
        if (isDead() || isFlying() || isInsideZone(ZoneId.WATER))
            return false;

        if (System.currentTimeMillis() < _fallingTimestamp)
            return true;

        final int deltaZ = getZ() - z;
        if (deltaZ <= getBaseTemplate().getFallHeight())
            return false;

        final int damage = (int) Formulas.calcFallDam(this, deltaZ);
        if (damage > 0) {
            reduceCurrentHp(Math.min(damage, getCurrentHp() - 1), null, false, true, null);
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage));
        }

        setFalling();

        return false;
    }

    /**
     * Set falling timestamp
     */
    public final void setFalling() {
        _fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
    }

    public boolean isAllowedToEnchantSkills() {
        if (isLocked())
            return false;

        if (AttackStanceTaskManager.getInstance().isInAttackStance(this))
            return false;

        if (isCastingNow() || isCastingSimultaneouslyNow())
            return false;

        if (isInBoat())
            return false;

        return true;
    }

    public List<Integer> getFriendList() {
        return _friendList;
    }

    public void selectFriend(Integer friendId) {
        if (!_selectedFriendList.contains(friendId))
            _selectedFriendList.add(friendId);
    }

    public void deselectFriend(Integer friendId) {
        if (_selectedFriendList.contains(friendId))
            _selectedFriendList.remove(friendId);
    }

    public List<Integer> getSelectedFriendList() {
        return _selectedFriendList;
    }

    private void restoreFriendList() {
        _friendList.clear();

        try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 0")) {
            ps.setInt(1, getObjectId());

            try (ResultSet rset = ps.executeQuery()) {
                while (rset.next()) {
                    final int friendId = rset.getInt("friend_id");
                    if (friendId == getObjectId())
                        continue;

                    _friendList.add(friendId);
                }
            }
        } catch (Exception e) {
            WorldObject.LOGGER.error("Couldn't restore {}'s friendlist.", e, getName());
        }
    }

    private void notifyFriends(boolean login) {
        for (int id : _friendList) {
            Player friend = World.getInstance().getPlayer(id);
            if (friend != null) {
                friend.sendPacket(new FriendList(friend));

                if (login)
                    friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addCharName(this));
            }
        }
    }

    public void selectBlock(Integer friendId) {
        if (!_selectedBlocksList.contains(friendId))
            _selectedBlocksList.add(friendId);
    }

    public void deselectBlock(Integer friendId) {
        if (_selectedBlocksList.contains(friendId))
            _selectedBlocksList.remove(friendId);
    }

    public List<Integer> getSelectedBlocksList() {
        return _selectedBlocksList;
    }

    @Override
    public void broadcastRelationsChanges() {
        for (Player player : getKnownType(Player.class)) {
            final int relation = getRelation(player);
            final boolean isAutoAttackable = isAutoAttackable(player);

            player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
            if (_summon != null)
                player.sendPacket(new RelationChanged(_summon, relation, isAutoAttackable));
        }
    }

    @Override
    public void sendInfo(Player player) {
        if (isInBoat())
            getPosition().set(getBoat().getPosition());

        if (getPolyType() == PolyType.NPC)
            player.sendPacket(new AbstractNpcInfo.PcMorphInfo(this, getPolyTemplate()));
        else {
            player.sendPacket(new CharInfo(this));

            if (isSeated()) {
                final WorldObject object = World.getInstance().getObject(_throneId);
                if (object instanceof StaticObject)
                    player.sendPacket(new ChairSit(getObjectId(), ((StaticObject) object).getStaticObjectId()));
            }
        }

        int relation = getRelation(player);
        boolean isAutoAttackable = isAutoAttackable(player);

        player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
        if (_summon != null)
            player.sendPacket(new RelationChanged(_summon, relation, isAutoAttackable));

        relation = player.getRelation(this);
        isAutoAttackable = player.isAutoAttackable(this);

        sendPacket(new RelationChanged(player, relation, isAutoAttackable));
        if (player.getSummon() != null)
            sendPacket(new RelationChanged(player.getSummon(), relation, isAutoAttackable));

        if (isInBoat())
            player.sendPacket(new GetOnVehicle(getObjectId(), getBoat().getObjectId(), getBoatPosition()));

        switch (getStoreType()) {
            case SELL:
            case PACKAGE_SELL:
                player.sendPacket(new PrivateStoreMsgSell(this));
                break;

            case BUY:
                player.sendPacket(new PrivateStoreMsgBuy(this));
                break;

            case MANUFACTURE:
                player.sendPacket(new RecipeShopMsg(this));
                break;
        }
    }

    @Override
    public double getCollisionRadius() {
        return getBaseTemplate().getCollisionRadiusBySex(getAppearance().getSex());
    }

    @Override
    public double getCollisionHeight() {
        return getBaseTemplate().getCollisionHeightBySex(getAppearance().getSex());
    }

    public boolean teleportRequest(Player requester, L2Skill skill) {
        if (_summonTargetRequest != null && requester != null)
            return false;

        _summonTargetRequest = requester;
        _summonSkillRequest = skill;
        return true;
    }

    public void teleportAnswer(int answer, int requesterId) {
        if (_summonTargetRequest == null)
            return;

        if (answer == 1 && _summonTargetRequest.getObjectId() == requesterId)
            teleportToFriend(_summonTargetRequest, _summonSkillRequest);

        _summonTargetRequest = null;
        _summonSkillRequest = null;
    }

    public void activateGate(int answer, int type) {
        if (_requestedGate == null)
            return;

        if (answer == 1 && getTarget() == _requestedGate) {
            if (type == 1)
                _requestedGate.openMe();
            else if (type == 0)
                _requestedGate.closeMe();
        }

        _requestedGate = null;
    }

    public void setRequestedGate(Door door) {
        _requestedGate = door;
    }

    @Override
    public boolean polymorph(PolyType type, int npcId) {
        if (super.polymorph(type, npcId)) {
            sendPacket(new UserInfo(this));
            return true;
        }
        return false;
    }

    @Override
    public void unpolymorph() {
        super.unpolymorph();
        sendPacket(new UserInfo(this));
    }

    @Override
    public void addKnownObject(WorldObject object) {
        sendInfoFrom(object);
    }

    @Override
    public void removeKnownObject(WorldObject object) {
        super.removeKnownObject(object);

        // send Server-Client Packet DeleteObject to the Player
        sendPacket(new DeleteObject(object, (object instanceof Player) && ((Player) object).isSeated()));
    }

    public final void refreshInfos() {
        for (WorldObject object : getKnownType(WorldObject.class)) {
            if (object instanceof Player && ((Player) object).isInObserverMode())
                continue;

            sendInfoFrom(object);
        }
    }

    /**
     * teleToLocation method without Dimensional Rift check.
     *
     * @param loc : The Location to teleport.
     */
    public final void teleToLocation(Location loc) {
        super.teleportTo(loc, 0);
    }

    @Override
    public final void teleportTo(Location loc, int randomOffset) {
        if (DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), true)) {
            sendMessage("You have been sent to the waiting room.");

            if (isInParty() && getParty().isInDimensionalRift())
                getParty().getDimensionalRift().usedTeleport(this);

            loc = DimensionalRiftManager.getInstance().getRoom((byte) 0, (byte) 0).getTeleportLoc();
        }
        super.teleportTo(loc, randomOffset);
    }

    private final void sendInfoFrom(WorldObject object) {
        if (object.getPolyType() == PolyType.ITEM)
            sendPacket(new SpawnItem(object));
        else {
            // send object info to player
            object.sendInfo(this);

            if (object instanceof Creature) {
                // Send the state of the Creature to the Player.
                Creature obj = (Creature) object;
                if (obj.hasAI())
                    obj.getAI().describeStateToPlayer(this);
            }
        }
    }

    /**
     * @return true if this {@link Player} is currently wearing a Formal Wear.
     */
    public boolean isWearingFormalWear() {
        final ItemInstance formal = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
        return formal != null && formal.getItem().getBodyPart() == Item.SLOT_ALLDRESS;
    }
}