package it.urronio.mirror.data

import it.urronio.mirror.data.model.AttitudeCrsfPacket
import it.urronio.mirror.data.model.BatteryCrsfPacket
import it.urronio.mirror.data.model.CrsfPacket
import it.urronio.mirror.data.model.GpsCrsfPacket
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.Queue
import kotlin.random.Random

class CrsfParser {
    private val buffer: ByteBuffer = ByteBuffer.allocate(256)
    private val packets: Queue<CrsfPacket> = LinkedList()
    init {
        buffer.flip()
    }
    fun put(bytes: ByteArray) {
        buffer.compact()
        buffer.put(bytes)
        buffer.flip()
        parse()
    }
    fun nextPacket(): CrsfPacket? {
        return packets.poll()
    }
    private fun parse() {
        while (buffer.remaining() > 2) {
            buffer.mark()
            val byte = buffer.get()
            if (byte != 0xEA.toByte() && byte != 0xC8.toByte()) continue
            if (buffer.remaining() < 1) {
                buffer.reset()
                break
            }
            val len = buffer.get().toInt()
            if (len < 2 || len > 64) { // invalid packet length
                buffer.reset()
                buffer.get()
                continue
            }
            if (buffer.remaining() < len) { // the rest of the packet is not here
                buffer.reset()
                break
            }
            val frame = ByteArray(len)
            buffer.get(frame)
            // TODO: deconstruct frame
            try {
                packets.add(deconstructFrame(barr = frame))
            } catch (e: Exception) { // implement custom exception or change type to nullable and perform null-check
                // unsupported packet
            }
        }
    }
    private fun deconstructFrame(barr: ByteArray): CrsfPacket {
        val type = barr[0]
        return when (type) {
            0x02.toByte() -> { // GPS
                GpsCrsfPacket(
                    latitude = ByteBuffer.wrap(barr, 1, 4).int,
                    longitude = ByteBuffer.wrap(barr, 5 ,4).int,
                    groundSpeed = ByteBuffer.wrap(barr, 9, 2).short,
                    heading = ByteBuffer.wrap(barr, 11, 2).short,
                    altitude = ByteBuffer.wrap(barr, 13, 2).short,
                    satellites = ByteBuffer.wrap(barr, 15, 1).get()
                )
            }
            0x08.toByte() -> {
                BatteryCrsfPacket(
                    voltage = ByteBuffer.wrap(barr, 1, 2).short,
                    current = ByteBuffer.wrap(barr, 3, 2).short,
                    usedCap = ByteBuffer.wrap(barr, 5, 4).int,
                    remBatt = ByteBuffer.wrap(barr, 9, 1).get()
                )
            }
            0x1E.toByte() -> {
                AttitudeCrsfPacket(
                    pitch = ByteBuffer.wrap(barr, 1, 2).short,
                    roll = ByteBuffer.wrap(barr, 3, 2).short,
                    yaw = ByteBuffer.wrap(barr, 5, 2).short,
                )
            }
            else -> throw Exception()
        }
    }

}