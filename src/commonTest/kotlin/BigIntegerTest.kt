import com.github.juggernaut0.bigint.BigInteger
import com.github.juggernaut0.bigint.toBigInteger
import kotlin.test.*

@ExperimentalUnsignedTypes
class BigIntegerTest {
    @Test
    fun fromByte() {
        val b: Byte = 100
        val n = BigInteger(b)
        assertTrue { ubyteArrayOf(100u) contentEquals n.bytes() }
        assertEquals(1, n.sign)
    }

    @Test
    fun fromShort() {
        val s: Short = 1000
        val n = BigInteger(s)
        assertTrue { ubyteArrayOf(232u, 3u) contentEquals n.bytes() }
        assertEquals(1, n.sign)
    }

    @Test
    fun fromInt() {
        val i: Int = 100000001
        val n = BigInteger(i)
        assertTrue { ubyteArrayOf(1u, 225u, 245u, 5u) contentEquals n.bytes() }
        assertEquals(1, n.sign)
    }

    @Test
    fun fromLong() {
        val l: Long = 123456789012345678
        val n = BigInteger(l)
        assertEquals(ubyteArrayOf(78u, 243u, 48u, 166u, 75u, 155u, 182u, 1u), n.bytes())
        assertEquals(1, n.sign)
    }

    @Test
    fun fromNegative() {
        val n = BigInteger(-5)
        assertEquals(ubyteArrayOf(5u), n.bytes())
        assertEquals(-1, n.sign)
    }

    @Test
    fun fromZero() {
        val n = BigInteger(0)
        assertEquals(ubyteArrayOf(), n.bytes())
        assertEquals(0, n.sign)
    }

    @Test
    fun fromString() {
        assertEquals(ubyteArrayOf(210u, 2u, 150u, 73u), "1234567890".toBigInteger().bytes())
        assertEquals(ubyteArrayOf(178u, 123u, 187u, 18u, 103u, 240u, 104u, 7u, 47u), "-867530912345678904242".toBigInteger().bytes())
    }

    @Test
    fun toStringTest() {
        assertEquals("12345678", BigInteger(12345678).toString())
        assertEquals("-152415787526596567801", (BigInteger(-12345678901) * BigInteger(12345678901)).toString())
    }

    @Test
    fun equalsTest() {
        assertEquals(BigInteger(1234), BigInteger(1234))
        assertEquals(BigInteger(1234).hashCode(), BigInteger(1234).hashCode())
        assertNotEquals(BigInteger(1234), BigInteger(-1234))
    }

    @Test
    fun toLong() {
        assertEquals(123456, BigInteger(123456).toLong())
    }

    @Test
    fun plusBigInteger() {
        assertEquals(BigInteger(1234567), BigInteger(1000560) + BigInteger(234007))
        assertEquals(BigInteger(1000007), BigInteger(1234567) + BigInteger(-234560))
        assertEquals(BigInteger(-1000007), BigInteger(-1234567) + BigInteger(234560))
        assertEquals(BigInteger(-1234567), BigInteger(-1000560) + BigInteger(-234007))
    }

    @Test
    fun plusInt() {
        val a = BigInteger(100056)
        val b = 23400
        assertEquals(BigInteger(123456), a + b)
    }

    @Test
    fun minusBigInteger() {
        assertEquals(BigInteger(1234567), BigInteger(1000560) - BigInteger(-234007))
        assertEquals(BigInteger(1000007), BigInteger(1234567) - BigInteger(234560))
        assertEquals(BigInteger(-1000007), BigInteger(-1234567) - BigInteger(-234560))
        assertEquals(BigInteger(-1234567), BigInteger(-1000560) - BigInteger(234007))
        assertEquals(BigInteger(-3500), BigInteger(2500) - BigInteger(6000))
        assertEquals(BigInteger(-65536), BigInteger.ZERO - BigInteger(65536))
    }

    @Test
    fun timesBigInteger() {
        assertEquals(BigInteger(838102050), BigInteger(12345) * BigInteger(67890))
        assertEquals(BigInteger(12345), BigInteger(12345) * BigInteger(1))
        assertEquals(BigInteger(0), BigInteger(12345) * BigInteger(0))
        assertEquals(BigInteger(-838102050), BigInteger(-12345) * BigInteger(67890))
        assertEquals(BigInteger(-838102050), BigInteger(12345) * BigInteger(-67890))
        assertEquals(BigInteger(838102050), BigInteger(-12345) * BigInteger(-67890))
    }

    @Test
    fun divBigInteger() {
        assertEquals(BigInteger(1465), BigInteger(12345678) / BigInteger(8424))
        assertEquals(BigInteger(12345678), BigInteger(12345678) / BigInteger(1))
        assertEquals(BigInteger(1), BigInteger(12345678) / BigInteger(12345678))
        assertEquals(BigInteger(0), BigInteger(1) / BigInteger(12345678))
        assertEquals(BigInteger(-22736), BigInteger(12345678) / BigInteger(-543))
    }

    @Test
    fun modBigInteger() {
        assertEquals(BigInteger(4518), BigInteger(12345678) % BigInteger(8424))
        assertEquals(BigInteger(0), BigInteger(12345678) % BigInteger(1))
        assertEquals(BigInteger(0), BigInteger(12345678) % BigInteger(12345678))
        assertEquals(BigInteger(5), BigInteger(5) % BigInteger(12345678))
        assertEquals(BigInteger(30), BigInteger(12345678) % BigInteger(-543))
    }

    @Test
    fun compare() {
        assertTrue { BigInteger(123) > BigInteger(100) }
        assertTrue { BigInteger(100) < BigInteger(123) }
        assertTrue { BigInteger(-5) < BigInteger(5) }
        assertTrue { BigInteger(5) > BigInteger(-5) }
        assertFalse { BigInteger(0) < BigInteger(0) }
        assertFalse { BigInteger(0) > BigInteger(0) }
    }

    private fun assertEquals(expected: UByteArray, actual: UByteArray) {
        @Suppress("RemoveToStringInStringTemplate") // For some reason it doesn't print properly otherwise
        assertTrue("expected: ${expected.toString()} actual: ${actual.toString()}") { expected contentEquals actual }
    }
}
