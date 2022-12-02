package tr.emreone.utils.extensions

import java.math.BigInteger
import java.security.MessageDigest

/**
 * Converts string to md5 hash.
 *
 * @receiver String
 * @return String
 */
fun String.md5(): String = BigInteger(1,
    MessageDigest.getInstance("MD5").digest(toByteArray())
).toString(16)

// TODO check if this is correct
fun String.sha256(): String = BigInteger(1,
    MessageDigest.getInstance("SHA256").digest(toByteArray())
).toString(16)

fun String.sha512(): String = BigInteger(1,
    MessageDigest.getInstance("SHA512").digest(toByteArray())
).toString(16)

operator fun String.times(n: Int): String {
    return this.repeat(n)
}

operator fun Char.times(i: Int): String {
    return this.toString() * i
}

fun List<Pair<Long, Long>>.sum() = reduce { acc, pair -> acc + pair }

operator fun Pair<Long, Long>.plus(other: Pair<Long, Long>): Pair<Long, Long> {
    return first + other.first to second + other.second
}

fun IntRange.size(): Int {
    return this.last - this.first + 1
}

infix fun IntRange.intersects(other: IntRange): Boolean =
    first <= other.last && last >= other.first

infix fun IntRange.intersect(other: IntRange): IntRange =
    maxOf(first, other.first)..minOf(last, other.last)
