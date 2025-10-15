package it.urronio.mirror.data

import android.util.Log
import it.urronio.mirror.data.model.AttitudeCrsfPacket
import it.urronio.mirror.data.model.BatteryCrsfPacket
import it.urronio.mirror.data.model.ChannelsCrsfPacket
import it.urronio.mirror.data.model.CrsfPacket
import it.urronio.mirror.data.model.GpsCrsfPacket
import it.urronio.mirror.data.model.RemoteRelatedCrsfPacket
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.Queue
import kotlin.experimental.or
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
                Log.d("Parser", e.message ?: "An error occurred in parsing the frame")
            }
        }
    }
    private fun deconstructFrame(barr: ByteArray): CrsfPacket {
        val type = barr[0]
        return when (type) {
            0x02.toByte() -> { // GPS
                Log.d("Parser", "GPS packet: ${barr.joinToString(" ")}")
                GpsCrsfPacket(
                    latitude = ByteBuffer.wrap(barr, 1, 4).int,
                    longitude = ByteBuffer.wrap(barr, 5 ,4).int,
                    groundSpeed = ByteBuffer.wrap(barr, 9, 2).short,
                    heading = ByteBuffer.wrap(barr, 11, 2).short,
                    altitude = ByteBuffer.wrap(barr, 13, 2).short,
                    satellites = ByteBuffer.wrap(barr, 15, 1).get()
                )
            }
            0x08.toByte() -> { // Battery
                Log.d("Parser", "Battery packet: ${barr.joinToString(" ")}")
                val b5 = barr[5].toInt() and 0xFF
                val b6 = barr[6].toInt() and 0xFF
                val b7 = barr[7].toInt() and 0xFF
                val usedCap = b5 or (b6 shl 8) or (b7 shl 16)
                BatteryCrsfPacket(
                    voltage = ByteBuffer.wrap(barr, 1, 2).short,
                    current = ByteBuffer.wrap(barr, 3, 2).short,
                    usedCap = usedCap,
                    remBatt = barr[8]
                )
            }
            0x1E.toByte() -> { // Attitude
                Log.d("Parser", "Attitude packet: ${barr.joinToString(" ")}")
                AttitudeCrsfPacket(
                    pitch = ByteBuffer.wrap(barr, 1, 2).short,
                    roll = ByteBuffer.wrap(barr, 3, 2).short,
                    yaw = ByteBuffer.wrap(barr, 5, 2).short,
                )
            }
            0x16.toByte() -> { // Channels
                Log.d("Parser", "Channels packet: ${barr.joinToString(" ")}")
                val chs = ShortArray(size = 16)
                var offset = 0
                for (i in 0 until 16) {
                    val startB = offset / 8
                    val startb = offset % 8
                    var value = 0
                    var remb = 11
                    var currB = startB
                    var currb = startb
                    while (remb > 0) {
                        val bInB = minOf(8 - currb, remb)
                        val mask = (1 shl bInB) - 1
                        val bits = (barr[currB].toInt() and 0xFF) shr currb and mask
                        value = value or (bits shl (11 - remb))
                        remb -= bInB
                        currB++
                        currb = 0
                    }
                    chs[i] = value.toShort()
                    offset += 11
                }
                ChannelsCrsfPacket.fromShortArray(arr = chs)
            }
            0x3A.toByte() -> { // Remote related
                RemoteRelatedCrsfPacket(
                    dest = barr[2],
                    orig = barr[3]
                )
            }
            else -> throw Exception()
        }
    }

}