package dev.l2j.tesla.autobots

import dev.l2j.tesla.commons.mmocore.ReceivablePacket
import dev.l2j.tesla.gameserver.model.actor.Player
import dev.l2j.tesla.gameserver.network.GameClient
import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket
import java.nio.ByteBuffer

internal class AutobotClient(val autobot: Autobot) : GameClient(null) {
        
    override fun toString(): String {
        return "Bot Client toString()"
    }

    override fun decrypt(buf: ByteBuffer?, size: Int): Boolean {
        return true
    }

    override fun encrypt(buf: ByteBuffer?, size: Int): Boolean {
        return true
    }

    override fun enableCrypt(): ByteArray {
        return byteArrayOf()
    }

    override fun setState(pState: GameClientState?) {
        
    }

    override fun sendPacket(gsp: L2GameServerPacket?) {
        
    }

    override fun markToDeleteChar(slot: Int): Byte {
        return 0
    }

    override fun markRestoredChar(slot: Int) {
        
    }

    override fun loadCharFromDisk(slot: Int): Player {
        return autobot
    }

    override fun close(gsp: L2GameServerPacket?) {
        
    }

    override fun closeNow() {
        
    }

    override fun cleanMe(fast: Boolean) {
        
    }

    override fun dropPacket(): Boolean {
        return false
    }

    override fun onBufferUnderflow() {
        
    }

    override fun onUnknownPacket() {
        
    }

    override fun execute(packet: ReceivablePacket<GameClient>?) {
        
    }
    
    override fun isDetached(): Boolean {
        return false
    }
    
}