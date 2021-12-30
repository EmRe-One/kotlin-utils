package tr.emreone.utils

import java.io.File
import java.net.URI

/**
 *
 */
object Resources {

    /**
     *
     * @param fileName String
     * @param delimiter String
     * @return String
     */
    fun resourceAsString(fileName: String, delimiter: String = ""): String {
        return resourceAsList(fileName).reduce { a, b -> "$a$delimiter$b" }
    }

    /**
     *
     * @param fileName String
     * @return String
     */
    fun resourceAsText(fileName: String): String {
        return File(fileName.toURI()).readText()
    }

    /**
     *
     * @param fileName String
     * @return List<String>
     */
    fun resourceAsList(fileName: String): List<String> {
        return File(fileName.toURI()).readLines()
    }

    /**
     *
     * @param fileName String
     * @return List<Int>
     */
    fun resourceAsListOfInt(fileName: String): List<Int> {
        return resourceAsList(fileName).map { it.toInt() }
    }

    /**
     *
     * @param fileName String
     * @return List<Long>
     */
    fun resourceAsListOfLong(fileName: String): List<Long> {
        return resourceAsList(fileName).map { it.toLong() }
    }

    /**
     * Generates a Resources-URI for the given string.
     *
     * @receiver String
     * @return URI
     * @throws IllegalArgumentException if the given string is not found as resource.
     */
    fun String.toURI(): URI {
        return Resources.javaClass.classLoader.getResource(this)?.toURI()
            ?: throw IllegalArgumentException("Cannot find Resource: $this")
    }

}
