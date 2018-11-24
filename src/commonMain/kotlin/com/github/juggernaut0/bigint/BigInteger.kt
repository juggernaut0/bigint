package com.github.juggernaut0.bigint

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

@UseExperimental(ExperimentalUnsignedTypes::class)
class BigInteger private constructor(private val bytes: UByteArray, private val neg: Boolean) {
    constructor(n: Byte) : this(n.toBytes(), n < 0)
    constructor(n: Short) : this(n.toLong().toBytes(), n < 0)
    constructor(n: Int) : this(n.toLong().toBytes(), n < 0)
    constructor(n: Long) : this(n.toBytes(), n < 0)

    fun bytes() = bytes.trim()
    val sign get() = when {
        bytes.all { it == ZERO_BYTE } -> 0
        neg -> -1
        else -> 1
    }

    override operator fun equals(other: Any?): Boolean {
        return when {
            other === null -> false
            this === other -> true
            other !is BigInteger -> false
            isZero() -> other.isZero()
            else -> bytes.trim() contentEquals other.bytes.trim() && neg == other.neg
        }
    }

    override fun hashCode(): Int {
        return bytes.trim().contentHashCode() + 31 * neg.hashCode()
    }

    override fun toString(): String {
        if (bytes.size <= 7) return toLong().toString()
        val digits = mutableListOf<Char>()
        val ten = BigInteger(10)
        var q = abs(this)
        while (!q.isZero()) {
            val (nq, r) = q.divmod(ten)
            val c = if (r.bytes.isEmpty()) 48 else r.bytes[0].toInt() + 48
            digits.add(c.toChar())
            q = nq
        }
        if (neg) digits.add('-')
        digits.reverse()
        return digits.joinToString(separator = "")
    }

    fun toLong(): Long {
        var result: Long = 0
        for (i in bytes.indices) {
            result += (bytes[i].toInt() * 256.0.pow(i).toLong())
        }
        if (neg) result = -result
        return result
    }

    operator fun unaryMinus(): BigInteger {
        return BigInteger(bytes, !neg)
    }

    operator fun plus(other: Int): BigInteger {
        return plus(BigInteger(other))
    }

    operator fun plus(other: BigInteger): BigInteger {
        if (other.neg != neg) return minus(-other)

        val len = max(bytes.size, other.bytes.size)
        val result = UByteArray(len + 1)
        var carry = 0u
        for (i in 0 until len) {
            val a = if (i < bytes.size) bytes[i] else 0u
            val b = if (i < other.bytes.size) other.bytes[i] else 0u
            val c = a + b + carry
            carry = c / 256u
            result[i] = (c % 256u).toUByte()
        }
        return BigInteger(result.trim(), neg)
    }

    operator fun minus(other: BigInteger): BigInteger {
        if (other.neg != neg) return plus(-other)
        val cmp = abs(this).compareTo(abs(other))
        if (cmp == 0) return ZERO
        if (cmp < 0) return -(other - this)

        val len = max(bytes.size, other.bytes.size)
        val result = UByteArray(len)
        var borrow = 0
        for (i in 0 until len) {
            val a = if (i < bytes.size) bytes[i].toInt() else 0
            val b = if (i < other.bytes.size) other.bytes[i].toInt() else 0
            var c = a - b - borrow
            if (c < 0) {
                c += 256
                borrow = 1
            } else {
                borrow = 0
            }
            result[i] = c.toUByte()
        }
        if (borrow != 0) {
            throw IllegalStateException("borrow is $borrow")
        }
        return BigInteger(result.trim(), neg)
    }

    operator fun times(other: BigInteger): BigInteger {
        if (isZero() || other.isZero()) return ZERO

        var result = ZERO

        for (i in 0 until other.bytes.size) {
            val b = other.bytes[i]
            val row = UByteArray(bytes.size + i + 1)
            for (j in 0 until bytes.size) {
                val p = b * bytes[j] + row[i + j]
                row[i + j] = (p and 0xffu).toUByte()
                row[i + j + 1] = ((p and 0xff00u) shr 8).toUByte()
            }
            result += BigInteger(row, false)
        }

        return BigInteger(result.bytes, neg xor other.neg)
    }

    operator fun div(other: BigInteger) = divmod(other).first
    operator fun rem(other: BigInteger) = divmod(other).second

    private fun divmod(other: BigInteger): Pair<BigInteger, BigInteger> {
        if (other.isZero()) throw ArithmeticException("Division by zero")
        if (abs(other) > abs(this)) return ZERO to this

        var q = ZERO
        var r = this
        val trimmed = other.bytes.trim()
        val shamt = trimmed.size - 1
        val msd = trimmed[shamt]
        while (true) {
            val rsh = (r.bytes.size - 2).coerceAtLeast(0)
            val dq = mkBytes(r.msd2()/msd, rsh - shamt, r.neg xor other.neg)
            if (dq.isZero()) {
                if (r.neg) {
                    r += other
                    q -= BigInteger(1)
                }
                return q to r
            }
            q += dq
            r = this - other * q
        }
    }

    private fun msd2(): UInt {
        val b = bytes.trim()
        return when {
            b.isEmpty() -> 0u
            b.size == 1 -> b[0].toUInt()
            else -> b[b.size - 1] * 256u + b[b.size - 2]
        }
    }

    private fun mkBytes(msd2: UInt, shamt: Int, neg: Boolean): BigInteger {
        val b1 = (msd2 and 0x00ffu)
        val b2 = (msd2 and 0xff00u) shr 8
        val bs = UByteArray((2 + shamt).coerceAtLeast(0))
        if (shamt > -2) bs[shamt + 1] = b2.toUByte()
        if (shamt > -1) bs[shamt] = b1.toUByte()
        return BigInteger(bs, neg)
    }

    operator fun compareTo(other: BigInteger): Int {
        val thisZero = isZero()
        val otherZero = other.isZero()
        if (thisZero && otherZero) return 0
        if (neg && (otherZero || !other.neg)) return -1
        if (!neg && (otherZero || other.neg)) return 1

        val len = max(bytes.size, other.bytes.size) - 1
        for (i in len downTo 0) {
            val a = if (i < bytes.size) bytes[i] else 0u
            val b = if (i < other.bytes.size) other.bytes[i] else 0u
            if (a > b) return 1
            if (a < b) return -1
        }
        return 0
    }

    fun isZero() = bytes.all { it == ZERO_BYTE }

    companion object {
        val ZERO = BigInteger(ubyteArrayOf(), false)

        private const val ZERO_BYTE: UByte = 0u

        private fun Byte.toBytes() = ubyteArrayOf(abs(toInt()).toUByte())

        private fun Long.toBytes(): UByteArray {
            val a = abs(this)
            val b1 = (a and 0x00000000000000ff)
            val b2 = (a and 0x000000000000ff00) ushr 8
            val b3 = (a and 0x0000000000ff0000) ushr 16
            val b4 = (a and 0x00000000ff000000) ushr 24
            val b5 = (a and 0x000000ff00000000) ushr 32
            val b6 = (a and 0x0000ff0000000000) ushr 40
            val b7 = (a and 0x00ff000000000000) ushr 48
            val b8 = (a and 0x0f00000000000000) ushr 56
            return ubyteArrayOf(
                b1.toUByte(),
                b2.toUByte(),
                b3.toUByte(),
                b4.toUByte(),
                b5.toUByte(),
                b6.toUByte(),
                b7.toUByte(),
                b8.toUByte()).trim()
        }

        fun abs(n: BigInteger): BigInteger {
            return BigInteger(n.bytes, false)
        }

        private fun UByteArray.trim(): UByteArray {
            if (size == 0) return this
            if (this[size-1] != ZERO_BYTE) return this.copyOf()
            if (size == 1 && this[0] == ZERO_BYTE) return ubyteArrayOf()
            for (i in (size-2) downTo 0) {
                if (this[i] != ZERO_BYTE) {
                    return this.copyOfRange(0, i+1)
                }
            }
            return ubyteArrayOf()
        }
    }
}