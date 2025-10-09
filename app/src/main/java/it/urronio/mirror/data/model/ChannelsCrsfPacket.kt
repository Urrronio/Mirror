package it.urronio.mirror.data.model

data class ChannelsCrsfPacket(
    val ch1: Short,
    val ch2: Short,
    val ch3: Short,
    val ch4: Short,
    val ch5: Short,
    val ch6: Short,
    val ch7: Short,
    val ch8: Short,
    val ch9: Short,
    val ch10: Short,
    val ch11: Short,
    val ch12: Short,
    val ch13: Short,
    val ch14: Short,
    val ch15: Short,
    val ch16: Short,
): CrsfPacket {
    companion object {
        fun fromShortArray(arr: ShortArray): ChannelsCrsfPacket {
            if (arr.size < 16) throw IllegalArgumentException()
            return ChannelsCrsfPacket(
                ch1 = arr[0],
                ch2= arr[1],
                ch3 = arr[2],
                ch4 = arr[3],
                ch5 = arr[4],
                ch6 = arr[5],
                ch7 = arr[6],
                ch8 = arr[7],
                ch9 = arr[8],
                ch10 = arr[9],
                ch11 = arr[10],
                ch12 = arr[11],
                ch13 = arr[12],
                ch14 = arr[13],
                ch15 = arr[14],
                ch16 = arr[15]
            )
        }

    }
}
