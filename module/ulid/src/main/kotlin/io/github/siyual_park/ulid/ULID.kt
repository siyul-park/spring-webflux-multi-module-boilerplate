package io.github.siyual_park.ulid

import java.io.Serializable
import java.security.SecureRandom

class ULID(
    private val mostSignificantBits: Long,
    private val leastSignificantBits: Long
) : Comparable<ULID>,
    Serializable {

    fun timestamp(): Long {
        return mostSignificantBits ushr 16
    }

    fun toBytes(): ByteArray {
        val result = ByteArray(16)
        for (i in 0..7) {
            result[i] = (mostSignificantBits shr (7 - i) * 8 and 0xFF).toByte()
        }
        for (i in 8..15) {
            result[i] = (leastSignificantBits shr (15 - i) * 8 and 0xFF).toByte()
        }
        return result
    }

    fun increment(): ULID {
        val lsb = leastSignificantBits
        if (lsb != -0x1L) {
            return ULID(mostSignificantBits, lsb + 1)
        }
        val msb = mostSignificantBits
        return if (msb and RANDOM_MSB_MASK != RANDOM_MSB_MASK) {
            ULID(msb + 1, 0)
        } else {
            ULID(msb and TIMESTAMP_MSB_MASK, 0)
        }
    }

    override fun hashCode(): Int {
        val hilo = mostSignificantBits xor leastSignificantBits
        return (hilo shr 32).toInt() xor hilo.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val ULID = other as ULID
        return (
            mostSignificantBits == ULID.mostSignificantBits &&
                leastSignificantBits == ULID.leastSignificantBits
            )
    }

    override fun compareTo(other: ULID): Int {
        return if (mostSignificantBits < other.mostSignificantBits) -1
        else if (mostSignificantBits > other.mostSignificantBits) 1
        else if (leastSignificantBits < other.leastSignificantBits) -1
        else if (leastSignificantBits > other.leastSignificantBits) 1
        else 0
    }

    override fun toString(): String {
        val buffer = CharArray(26)
        writeCrockford(buffer, timestamp(), 10, 0)
        var ULID = mostSignificantBits and 0xFFFFL shl 24
        val interim = leastSignificantBits ushr 40
        ULID = ULID or interim
        writeCrockford(buffer, ULID, 8, 10)
        writeCrockford(buffer, leastSignificantBits, 8, 18)
        return String(buffer)
    }

    companion object {
        private const val serialVersionUID: Long = -7631108191830779116L

        private val numberGenerator = SecureRandom()

        private val ENCODING_CHARS = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
            'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X',
            'Y', 'Z'
        )
        private val DECODING_CHARS = byteArrayOf( // 0
            -1, -1, -1, -1, -1, -1, -1, -1, // 8
            -1, -1, -1, -1, -1, -1, -1, -1, // 16
            -1, -1, -1, -1, -1, -1, -1, -1, // 24
            -1, -1, -1, -1, -1, -1, -1, -1, // 32
            -1, -1, -1, -1, -1, -1, -1, -1, // 40
            -1, -1, -1, -1, -1, -1, -1, -1, // 48
            0, 1, 2, 3, 4, 5, 6, 7, // 56
            8, 9, -1, -1, -1, -1, -1, -1, // 64
            -1, 10, 11, 12, 13, 14, 15, 16, // 72
            17, 1, 18, 19, 1, 20, 21, 0, // 80
            22, 23, 24, 25, 26, -1, 27, 28, // 88
            29, 30, 31, -1, -1, -1, -1, -1, // 96
            -1, 10, 11, 12, 13, 14, 15, 16, // 104
            17, 1, 18, 19, 1, 20, 21, 0, // 112
            22, 23, 24, 25, 26, -1, 27, 28, // 120
            29, 30, 31
        )
        private const val MASK = 0x1F
        private const val MASK_BITS = 5
        private const val TIMESTAMP_OVERFLOW_MASK = -0x1000000000000L
        private const val TIMESTAMP_MSB_MASK = -0x10000L
        private const val RANDOM_MSB_MASK = 0xFFFFL

        fun randomULID(): ULID {
            val timestamp = System.currentTimeMillis()
            checkTimestamp(timestamp)
            var mostSignificantBits = numberGenerator.nextLong()
            val leastSignificantBits = numberGenerator.nextLong()
            mostSignificantBits = mostSignificantBits and 0xFFFF
            mostSignificantBits = mostSignificantBits or (timestamp shl 16)
            return ULID(mostSignificantBits, leastSignificantBits)
        }

        fun fromBytes(data: ByteArray): ULID {
            require(data.size == 16) { "data length must not exceed 16 but was ${data.size}!" }
            var mostSignificantBits: Long = 0
            var leastSignificantBits: Long = 0
            for (i in 0..7) {
                mostSignificantBits = (mostSignificantBits shl 8) or (data[i].toLong() and 0xFF)
            }
            for (i in 8..15) {
                leastSignificantBits = (leastSignificantBits shl 8) or (data[i].toLong() and 0xFF)
            }
            return ULID(mostSignificantBits, leastSignificantBits)
        }

        fun fromString(name: String): ULID {
            require(name.length == 26) { "name length must not exceed 26 but was ${name.length}!" }
            val timeString = name.substring(0, 10)
            val time = parseCrockford(timeString)
            require(time and TIMESTAMP_OVERFLOW_MASK == 0L)
            val part1String = name.substring(10, 18)
            val part2String = name.substring(18)
            val part1 = parseCrockford(part1String)
            val part2 = parseCrockford(part2String)
            val most = time shl 16 or (part1 ushr 24)
            val least = part2 or (part1 shl 40)
            return ULID(most, least)
        }

        private fun parseCrockford(input: String): Long {
            val length = input.length
            require(length <= 12) { "input length must not exceed 12 but was $length!" }
            var result: Long = 0
            for (i in 0 until length) {
                val current = input[i]
                var ULID: Byte = -1
                if (current.code < DECODING_CHARS.size) {
                    ULID = DECODING_CHARS[current.code]
                }
                require(ULID >= 0) { "Illegal character '$current'!" }
                result = result or (ULID.toLong() shl (length - 1 - i) * MASK_BITS)
            }
            return result
        }

        private fun writeCrockford(buffer: CharArray, ULID: Long, count: Int, offset: Int) {
            for (i in 0 until count) {
                val index = (ULID ushr (count - i - 1) * MASK_BITS and MASK.toLong()).toInt()
                buffer[offset + i] = ENCODING_CHARS[index]
            }
        }
        private fun checkTimestamp(timestamp: Long) {
            require(timestamp and TIMESTAMP_OVERFLOW_MASK == 0L) { "ULID does not support timestamps after +10889-08-02T05:31:50.655Z!" }
        }
    }
}
