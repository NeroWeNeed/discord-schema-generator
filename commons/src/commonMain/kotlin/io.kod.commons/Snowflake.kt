package io.kod.commons

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

//Lifted from kotlinx.datetime.math.kt for proper Unsigned long conversions
internal const val NANOS_PER_MILLI = 1_000_000UL
internal const val MILLIS_PER_ONE = 1_000UL


@Serializable
@JvmInline
value class Snowflake(private val value: ULong) {
    val timestamp: ULong
        get() = value.shr(22) + 1420070400000UL
    val workerId: UByte
        get() = value.and(0x3E0000UL).shr(17).toUByte()
    val processId: UByte
        get() = value.and(0x1F000UL).shr(12).toUByte()
    val increment: UShort
        get() = value.and(0xFFFUL).toUShort()
    val instant: Instant
        get() = timestamp.run {
            Instant.fromEpochSeconds(
                (this / MILLIS_PER_ONE).toLong(),
                (this % MILLIS_PER_ONE * NANOS_PER_MILLI).toLong()
            )
        }

}

