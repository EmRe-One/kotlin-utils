package tr.emreone.kotlin_utils.math

import tr.emreone.kotlin_utils.extensions.safeTimes
import kotlin.math.absoluteValue
import kotlin.math.pow

/**
 * Euclid's algorithm for finding the greatest common divisor of a and b.
 */
fun gcd(a: Int, b: Int): Int = if (b == 0) a.absoluteValue else gcd(b, a % b)
fun gcd(f: Int, vararg n: Int): Int = n.fold(f, ::gcd)
fun Iterable<Int>.gcd(): Int = reduce(::gcd)

/**
 * Euclid's algorithm for finding the greatest common divisor of a and b.
 */
fun gcd(a: Long, b: Long): Long = if (b == 0L) a.absoluteValue else gcd(b, a % b)
fun gcd(f: Long, vararg n: Long): Long = n.fold(f, ::gcd)
fun Iterable<Long>.gcd(): Long = reduce(::gcd)

/**
 * Find the least common multiple of a and b using the gcd of a and b.
 */
fun lcm(a: Int, b: Int) = (a safeTimes b) / gcd(a, b)
fun lcm(f: Int, vararg n: Int): Long = n.map { it.toLong() }.fold(f.toLong(), ::lcm)
@JvmName("lcmForInt")
fun Iterable<Int>.lcm(): Long = map { it.toLong() }.reduce(::lcm)

/**
 * Find the least common multiple of a and b using the gcd of a and b.
 */
fun lcm(a: Long, b: Long) = (a safeTimes b) / gcd(a, b)
fun lcm(f: Long, vararg n: Long): Long = n.fold(f, ::lcm)
fun Iterable<Long>.lcm(): Long = reduce(::lcm)


/**
 * Takes a list of base/modulo combinations and returns the lowest number for which the states coincide such that:
 *
 * for all i: state(i) == base_state(i).
 *
 * E.g. chineseRemainder((3,4), (5,6), (2,5)) == 47
 */
fun chineseRemainder(values: List<Pair<Long, Long>>): Pair<Long, Long>? {
    if (values.isEmpty()) {
        return null
    }
    var (result, lcm) = values[0]
    outer@ for (i in 1 until values.size) {
        val (base, modulo) = values[i]
        val target = base % modulo
        for (j in 0L until modulo) {
            if (result % modulo == target) {
                lcm = lcm(lcm, modulo)
                continue@outer
            }
            result += lcm
        }
        return null
    }
    return result to lcm
}

/**
 * Simple algorithm to find the primes of the given Int.
 */
fun Int.primes(): Sequence<Int> = sequence {
    var n = this@primes
    var j = 2
    while (j * j <= n) {
        while (n % j == 0) {
            yield(j)
            n /= j
        }
        j++
    }
    if (n > 1)
        yield(n)
}

/**
 * Simple algorithm to find the primes of the given Long.
 */
fun Long.primes(): Sequence<Long> = sequence {
    var n = this@primes
    var j = 2L
    while (j * j <= n) {
        while (n % j == 0L) {
            yield(j)
            n /= j
        }
        j++
    }
    if (n > 1)
        yield(n)
}

infix fun Number.pow(power: Number): Double =
    this.toDouble().pow(power.toDouble())

infix fun Int.pow(power: Int): Int =
    this.toDouble().pow(power.toDouble()).toInt()
