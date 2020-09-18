package dev.l2j.tesla.autobots

import dev.l2j.tesla.autobots.autofarm.AutofarmCommandHandler
import dev.l2j.tesla.autobots.autofarm.AutofarmManager
import dev.l2j.tesla.autobots.behaviors.preferences.ActivityType
import dev.l2j.tesla.autobots.dao.AutobotsDao
import dev.l2j.tesla.autobots.extensions.getControllingBot
import dev.l2j.tesla.autobots.extensions.isControllingBot
import dev.l2j.tesla.autobots.extensions.moveTo
import dev.l2j.tesla.autobots.extensions.useMagicSkill
import dev.l2j.tesla.autobots.models.AutobotInfo
import dev.l2j.tesla.autobots.models.BotChat
import dev.l2j.tesla.autobots.models.ChatType
import dev.l2j.tesla.autobots.ui.AutobotsUi
import dev.l2j.tesla.autobots.utils.*
import kotlinx.coroutines.launch
import dev.l2j.tesla.Config
import dev.l2j.tesla.commons.math.MathUtil
import dev.l2j.tesla.commons.random.Rnd
import dev.l2j.tesla.gameserver.data.sql.PlayerInfoTable
import dev.l2j.tesla.gameserver.data.xml.HennaData
import dev.l2j.tesla.gameserver.data.xml.MapRegionData
import dev.l2j.tesla.gameserver.data.xml.PlayerData
import dev.l2j.tesla.gameserver.enums.ZoneId
import dev.l2j.tesla.gameserver.enums.actors.ClassId
import dev.l2j.tesla.gameserver.handler.IChatHandler
import dev.l2j.tesla.gameserver.idfactory.IdFactory
import dev.l2j.tesla.gameserver.model.World
import dev.l2j.tesla.gameserver.model.WorldObject
import dev.l2j.tesla.gameserver.model.actor.Player
import dev.l2j.tesla.gameserver.model.actor.player.Appearance
import dev.l2j.tesla.gameserver.model.actor.player.BlockList
import dev.l2j.tesla.gameserver.network.serverpackets.ActionFailed
import dev.l2j.tesla.gameserver.network.serverpackets.CreatureSay
import java.util.concurrent.ConcurrentSkipListMap

object AutobotsManager {

    internal val activeBots = ConcurrentSkipListMap<String, Autobot>(String.CASE_INSENSITIVE_ORDER)
    
    fun start(){
        AutobotData

        AutobotsNameService
        IconsTable
        AutobotsDao.updateAllBotOnlineStatus(false)
        AutobotScheduler
        AutofarmManager

        Klock.start("onUpdate", 1000, AutobotData.settings.iterationDelay,
            CoScopes.onUpdateScope, action = {
            AutobotsManager.activeBots.values.forEach {
                CoScopes.onUpdateScope.launch {
                    try {
                        it.onUpdate()
                    } catch (e: Exception){
                        it.isBusyThinking.set(false)
                        println(e)
                    }
                }
            }
        })
    }

    fun onServerShutdown(){
        AutobotsDao.updateAllBotOnlineStatus(false)
    }

    fun onChat(type: Int, chatter: Player, target: String?, text: String, handler : IChatHandler) : Boolean{
        if(chatter.isGM) {
            if(chatter.isControllingBot()) {
                handler.handleChat(type, chatter.getControllingBot(), target, text)
                return true
            }
        }
        
        when(type){
            0 -> { // chat all
                AutobotsManager.activeBots.forEach {
                    if(MathUtil.checkIfInRange(1250, it.value, chatter, true) && !BlockList.isBlocked(it.value, chatter)) {
                        it.value.addChat(BotChat(ChatType.All, chatter.name, text))
                    }
                }
            }
            1 -> { // shout !
                AutobotsManager.activeBots.forEach {
                    val region = MapRegionData.getInstance().getMapRegion(it.value.x, it.value.y)
                    if (!BlockList.isBlocked(it.value, chatter) && region == MapRegionData.getInstance().getMapRegion(it.value.x, it.value.y)) {
                        it.value.addChat(BotChat(ChatType.Shout, chatter.name, text))
                    }
                }
            }
            2 -> { // pm "
                if(target == null) return false
                val bot = AutobotsManager.activeBots.getOrElse(target) {null} ?: return false

                World.getInstance().players.filter { it.isGM }.forEach {
                    it.sendPacket(
                        CreatureSay(
                            0,
                            15,
                            "${chatter.name} -> ${bot.name}",
                            text
                        )
                    )
                }
                bot.addChat(BotChat(ChatType.PmReceived, chatter.name, text))
            }
            17 -> { // hero chat %
                if(!chatter.isHero) return false
                AutobotsManager.activeBots.forEach {
                    it.value.addChat(BotChat(ChatType.Hero, chatter.name, text))
                }
            }
        }

        return false
    }

    fun loadAutobotDashboard(player: Player, autobot: Autobot) {
        AutobotsUi.loadBotDetails(player, autobot)
    }

    fun onMove(player: Player, targetX: Int, targetY: Int, targetZ: Int): Boolean {
        if (!player.isGM) return false
        
        if(BotZoneService.player == player) {
            BotZoneService.graph.points.add(BotZonePoint(targetX, targetY, targetZ+5))
            BotZoneService.sendZone(player)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return true
        }
        
        
        if(player.isControllingBot()) {
            val bot = player.getControllingBot()!!
            bot.moveTo(targetX, targetY, targetZ)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return true
        }
        return false
    }

    fun onUseMagic(player: Player, skillId: Int, ctrlPressed: Boolean) : Boolean {
        if (!player.isGM) return false
        
        if(player.isControllingBot()) {
            val bot = player.getControllingBot()!!
            val skill = bot.getSkill(skillId) ?: return true
            bot.useMagicSkill(skill, ctrlPressed)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return true
        }
        return false
    }

    fun onAction(player: Player, target: WorldObject, isShiftPressed: Boolean) : Boolean {
        if (!player.isGM) return false
        
        if(isShiftPressed) {
            return false
        }
        
        if(player.isControllingBot()) {
            val bot = player.getControllingBot()!!
            target.onAction(bot)
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return true
        }
        return false
    }
    
    fun onEnterWorld(player: Player){
        AutofarmManager.onEnterWorld(player)
    }
    
    fun onLogout(player: Player){
        AutofarmManager.onLogout(player)
    }

    fun onBypass(player: Player, command: String) {
        AutofarmCommandHandler.onBypass(player, command)
    }
    
    fun onAttackRequest(player: Player, target: WorldObject, isShiftPressed: Boolean) : Boolean {
        if (!player.isGM) return false
        
        if(isShiftPressed) {
            return false
        }

        if(player.isControllingBot()) {
            val bot = player.getControllingBot()!!
            target.onAction(bot)
            if (bot.target !== target) target.onAction(bot) else {
                if (target.objectId != bot.objectId && !bot.isInStoreMode && bot.activeRequester == null) target.onForcedAttack(bot) else player.sendPacket(
                    ActionFailed.STATIC_PACKET)
            }
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return true
        }        
        return false
    }
    
    internal suspend fun spawnAutobot(autobot: Autobot){
        if(autobot.isIngame()) {
            return
        }
        
        if(autobot.combatBehavior.activityPreferences.activityType == ActivityType.Schedule && autobot.combatBehavior.activityPreferences.logoutTimeIsInThePast()) {
            autobot.combatBehavior.activityPreferences.activityType = ActivityType.None
        }
        
        autobot.client = AutobotClient(autobot)
        PlayerInfoTable.getInstance().addPlayer(autobot.objectId, "Autobot", autobot.name, 0)
        autobot.rewardSkills()

        giveItemsByClassAndLevel(autobot)

        val symbols = AutobotData.symbols.firstOrNull { 
            it.classId == autobot.classId || it.classId == autobot.classId.parent || (autobot.classId.parent?.parent == it.classId )
        }

        if(symbols != null && symbols.symbolIds.any()) {
            symbols.symbolIds.forEach {
                val henna = HennaData.getInstance().getHenna(it) ?: return@forEach
                autobot.hennaList.addDontStore(henna)
            }
        }
        
        World.getInstance().addPlayer(autobot)
        AutobotsManager.activeBots[autobot.name] = autobot
        autobot.handlePlayerClanOnSpawn()

        autobot.setOnlineStatus(true, true)
        if (Config.PLAYER_SPAWN_PROTECTION > 0)
            autobot.setSpawnProtection(true)
        
        autobot.spawnMe(autobot.x, autobot.y, autobot.z)
        autobot.onPlayerEnter()
        
        if (!autobot.isGM && (!autobot.isInSiege || autobot.siegeState < 2) && autobot.isInsideZone(ZoneId.SIEGE))
            autobot.teleportTo(MapRegionData.TeleportType.TOWN)
        
        autobot.heal()
    }

    internal suspend fun createAndSpawnAutobot(x: Int, y: Int, z: Int, saveInDb: Boolean = true): Autobot {
        val autobot = AutobotsManager.createRandomFakePlayer(
            x,
            y,
            z,
            Rnd.get(1, 81),
            saveInDb
        )
        AutobotsManager.spawnAutobot(autobot)
        return autobot
    }

    internal suspend fun createRandomFakePlayer(x: Int, y: Int, z: Int, level: Int = 81, saveInDb: Boolean = true) : Autobot {
        val objectId = IdFactory.getInstance().nextId
        val playerName = AutobotsNameService.getRandomAvailableName()

        val classId = getSupportedClassesForLevel(level).random()

        val template = PlayerData.getInstance().getTemplate(classId)
        val app = getRandomAppearance(template.race)
        val player = Autobot(objectId, template, app)
        player.name = playerName
        player.setAccessLevel(Config.DEFAULT_ACCESS_LEVEL)
        player.setXYZ(x, y, z)        
        
        player.setBaseClass(player.classId)
        player.level = level
        giveItemsByClassAndLevel(player)
        player.heal()
        
        if(saveInDb) AutobotsDao.createAutobot(player)
        PlayerInfoTable.getInstance().addPlayer(player.objectId, "Autobots", player.name, player.accessLevel.level)
        return player
    }
    
    internal suspend fun createAutobot(creator: Player, name: String, level: Int, classId: ClassId, appearance: Appearance, x: Int, y: Int, z: Int, weaponEnchant: Int = 0, armorEnchant: Int = 0, jewelEnchant: Int = 0) : Autobot?{
        if(!AutobotsNameService.nameIsValid(name)) {
            creator.sendMessage("Bot name is not available")
            return null
        }
        
        val objectId = IdFactory.getInstance().nextId
        val template = PlayerData.getInstance().getTemplate(classId)
        val player = Autobot(objectId, template, appearance)
        player.name = name
        player.title = AutobotData.settings.defaultTitle
        player.setAccessLevel(Config.DEFAULT_ACCESS_LEVEL)
        player.setXYZ(x, y, z)
        
        player.setBaseClass(player.classId)
        
        player.level = level
        giveItemsByClassAndLevel(player, weaponEnchant, armorEnchant, jewelEnchant)
        player.heal()
        AutobotsDao.createAutobot(player)
        PlayerInfoTable.getInstance().addPlayer(player.objectId, "Autobots", player.name, player.accessLevel.level)
        return player 
    }
    
    internal fun deleteAutobot(autobot: Autobot, onBotIsLeader: () -> Unit = {}) : Boolean{
        if(autobot.isClanLeader) {
            onBotIsLeader()
            return false
        }
        
        if(autobot.clan != null) {
            AutobotsDao.removeClanMember(autobot, autobot.clan)
        }
        
        if(autobot.isIngame()) {
            autobot.despawn()
        }
        
        AutobotsDao.deleteBot(autobot.objectId)
        return true
    }

    internal fun getBotInfoFromOnlineOrDb(name: String) : AutobotInfo? {
        if(AutobotsManager.activeBots.containsKey(name)) {
            val bot = AutobotsManager.activeBots[name]
            return AutobotInfo(bot!!.name, bot.level, bot.isOnline, bot.classId, bot.objectId, bot.clanId)
        }

        return AutobotsDao.getInfoByName(name) ?: return null
    }

    internal fun getBotFromOnlineOrDb(name: String) : Autobot? {
        if(AutobotsManager.activeBots.containsKey(name))
            return AutobotsManager.activeBots[name]

        return AutobotsDao.loadByName(name) ?: return null
    }
}