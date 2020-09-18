package dev.l2j.tesla.autobots.utils.packets

import dev.l2j.tesla.gameserver.network.serverpackets.L2GameServerPacket

internal class ServerSideImage(val imageId: Int, val data: ByteArray) : L2GameServerPacket() {
    
    override fun writeImpl() {
        writeC(0x6c)
        writeD(imageId)
        writeD(data.size)
        writeB(data)
    }

}