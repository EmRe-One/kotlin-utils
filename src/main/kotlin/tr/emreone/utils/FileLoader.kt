package tr.emreone.utils

import java.io.File
import java.net.URI

object FileLoader {

    private fun String.toURI(): URI =
        FileLoader.javaClass.classLoader.getResource(this)?.toURI()
            ?: throw IllegalArgumentException("Cannot find Resource: $this")

    /**
     * Reads lines from the given input txt file.
     */
    fun readLines(parent: String = "src/main/resources", filename: String) =
        File(parent, filename).readLines()

    fun readLinesAsInts(parent: String = "src/main/resources", filename: String): List<Int> {
        return readLines(parent, filename).map { it.toInt() }
    }

}
