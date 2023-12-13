package tr.emreone.kotlin_utils.extensions

import java.math.BigInteger
import java.security.MessageDigest

fun String.parse(pattern: String): List<String> {
    val regex = pattern.toRegex()
    val matchResult = regex.matchEntire(this)
    return matchResult?.destructured?.toList() ?: emptyList()
}

/**
 * Converts string to md5 hash.
 *
 * @receiver String
 * @return String
 */
fun String.md5(): String = BigInteger(
    1,
    MessageDigest.getInstance("MD5").digest(toByteArray())
).toString(16)

fun String.sha256(): String = BigInteger(
    1,
    MessageDigest.getInstance("SHA256").digest(toByteArray())
).toString(16)

fun String.sha512(): String = BigInteger(
    1,
    MessageDigest.getInstance("SHA512").digest(toByteArray())
).toString(16)

operator fun String.times(n: Int): String {
    return this.repeat(n)
}

operator fun Char.times(i: Int): String {
    return this.toString() * i
}

val String.containsLatinLetter: Boolean
    get() = matches(Regex(".*[A-Za-z].*"))

val String.containsDigit: Boolean
    get() = matches(Regex(".*[0-9].*"))

val String.isAlphanumeric: Boolean
    get() = matches(Regex("[A-Za-z0-9]*"))

val String.hasLettersAndDigits: Boolean
    get() = containsLatinLetter && containsDigit

val String.isIntegerNumber: Boolean
    get() = toIntOrNull() != null

val String.isDecimalNumber: Boolean
    get() = toDoubleOrNull() != null

val String.lastPathComponent: String
    get() {
        var path = this
        if (path.endsWith("/"))
            path = path.substring(0, path.length - 1)
        var index = path.lastIndexOf('/')
        if (index < 0) {
            if (path.endsWith("\\"))
                path = path.substring(0, path.length - 1)
            index = path.lastIndexOf('\\')
            if (index < 0)
                return path
        }
        return path.substring(index + 1)
    }