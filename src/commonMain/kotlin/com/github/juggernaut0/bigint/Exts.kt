package com.github.juggernaut0.bigint

fun String.toBigInteger(): BigInteger {
    if (isEmpty()) throw NumberFormatException("empty string")
    var result = BigInteger.ZERO
    val neg = get(0) == '-'
    val start = if (neg) 1 else 0
    if (neg && length == 1) throw NumberFormatException(this)
    for (i in start until length) {
        val n = substring(i, i+1).toInt()
        result = result * BigInteger(10) + BigInteger(n)
    }
    return result
}

fun Byte.toBigInteger(): BigInteger = BigInteger(this)
fun Short.toBigInteger(): BigInteger = BigInteger(this)
fun Int.toBigInteger(): BigInteger = BigInteger(this)
fun Long.toBigInteger(): BigInteger = BigInteger(this)
