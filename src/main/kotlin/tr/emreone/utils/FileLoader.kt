package tr.emreone.utils

import java.io.File
import java.net.URI

/**
 *
 */
object FileLoader {

    /**
     * Reads lines from the given input txt file.
     *
     * @param parent String
     * @param filename String
     * @return List<String>
     */
    fun readLines(parent: String = "src/main/resources", filename: String) =
        File(parent, filename).readLines()

    /**
     *
     * @param parent String
     * @param filename String
     * @return List<Int>
     */
    fun readLinesAsInts(parent: String = "src/main/resources", filename: String): List<Int> {
        return readLines(parent, filename).map { it.toInt() }
    }

}
