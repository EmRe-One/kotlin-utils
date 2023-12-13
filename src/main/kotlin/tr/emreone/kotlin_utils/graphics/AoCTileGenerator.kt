package tr.emreone.kotlin_utils.graphics

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.apache.commons.text.StringEscapeUtils
import tr.emreone.kotlin_utils.Resources
import tr.emreone.kotlin_utils.automation.AoCPuzzle
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Definieren Sie hier Ihre Konstanten, Farben und Schriftarten
val TEXT_COLOR = Color.WHITE
val OUTLINE_COLOR = Color.BLACK
val NOT_COMPLETED_COLOR = Color.GRAY
val TILE_WIDTH_PX = 161 // Die Breite jedes Kachelteils
val SHOW_CHECKMARK_INSTEAD_OF_TIME_RANK = false // Ändern Sie dies entsprechend Ihrer Anforderung

class AoCTileGenerator(val year: Int) {

    data class DayScores(
        val time1: String?, val rank1: String?, val score1: String?,
        val time2: String?, val rank2: String?, val score2: String?
    )

    // Paths similar to the Python script
    val sourceCodeDir = Paths.get("").toAbsolutePath().parent.parent
    val imageDir = sourceCodeDir.resolve("aoc_tiles")
    val sessionCookiePath = sourceCodeDir.resolve("src/main/kotlin/resources/session.cookie")
    val dayImplementationDir = sourceCodeDir.resolve("src/main/kotlin/tr/emreone/adventofcode/days")

    fun requestLeaderboard(): String {
        val sessionCookie = Resources.resourceAsString("session.cookie")
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://adventofcode.com/$year/leaderboard/self")
            .header("Cookie", "session=$sessionCookie")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            return StringEscapeUtils.unescapeHtml4(response.body?.string() ?: "")
        }
    }


    // Other utility functions adapted to Kotlin
    fun getSolutionPathsDictForYears(): Map<Int, Map<Int, List<String>>> {
        // Similar logic to Python, but use Kotlin File and Path APIs


        return emptyMap()
    }


    fun parseLeaderboard(leaderboardContent: String): Map<Int, DayScores> {
        val noStars = "You haven't collected any stars... yet."
        val start = "<span class=\"leaderboard-daydesc-both\"> *Time *Rank *Score</span>\n"
        val end = "</pre>"

        if (noStars in leaderboardContent) {
            return emptyMap()
        }

        val pattern = Pattern.compile("$start(.*?)$end", Pattern.DOTALL or Pattern.MULTILINE)
        val matcher = pattern.matcher(leaderboardContent)

        if (!matcher.find()) {
            throw IllegalStateException("No leaderboard found!")
        }

        val tableRows = matcher.group(1).trim().split("\n")
        val leaderboard = mutableMapOf<Int, DayScores>()

        for (line in tableRows) {
            val parts = line.trim().split("\\s+".toRegex())
            val day = parts[0].toInt()
            val scores = parts.drop(1).map { if (it == "-") null else it }

            if (scores.size !in setOf(3, 6)) {
                throw IllegalArgumentException("Number of scores for day $day is not 3 or 6.")
            }

            val dayScores = DayScores(
                time1 = scores.getOrNull(0),
                rank1 = scores.getOrNull(1),
                score1 = scores.getOrNull(2),
                time2 = scores.getOrNull(3),
                rank2 = scores.getOrNull(4),
                score2 = scores.getOrNull(5)
            )

            leaderboard[day] = dayScores
        }

        return leaderboard
    }

    fun drawStar(
        g2d: Graphics2D,
        at: Pair<Int, Int>,
        size: Int = 9,
        color: Color = Color(255, 255, 0, 34),
        numPoints: Int = 5
    ) {
        val diff = PI * 2 / numPoints / 2
        val points = mutableListOf<Pair<Double, Double>>()

        for (i in 0 until numPoints * 2) {
            val angle = diff * i - PI / 2
            val factor = if (i % 2 == 0) size.toDouble() else size * 0.4
            points.add(Pair(at.first + cos(angle) * factor, at.second + sin(angle) * factor))
        }

        val xPoints = points.map { it.first.toInt() }.toIntArray()
        val yPoints = points.map { it.second.toInt() }.toIntArray()

        g2d.color = color
        g2d.fillPolygon(xPoints, yPoints, numPoints * 2)
    }

    fun getAlternatingBackground(languages: List<String>, allDaysCompleted: Boolean): BufferedImage {
        // Implementieren Sie die Hintergrunderstellung und geben Sie ein BufferedImage zurück
        // Dies ist nur ein Platzhalter
        val image = BufferedImage(TILE_WIDTH_PX, TILE_WIDTH_PX, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        g2d.color = Color(0, 128, 0) // Beispiel für einen grünen Hintergrund
        g2d.fillRect(0, 0, TILE_WIDTH_PX, TILE_WIDTH_PX)
        g2d.dispose()
        return image
    }

    private fun mainFont(size: Int): Font {
        // Implement the logic to get the main font
        return Font("Serif", Font.BOLD, size)
    }

    private fun secondaryFont(size: Int): Font {
        // Implement the logic to get the secondary font
        return Font("Serif", Font.PLAIN, size)
    }

    fun colorSimilarity(color: Color, toColor: Color, threshold: Int): Boolean {
        // Implement the logic to check color similarity
        return false
    }

    fun formatTime(time: String): String {
        // Implement the logic to format time
        return time
    }

    private fun pathNameForTile(aocPuzzle: AoCPuzzle): String {
        return "../aoc_tiles/${aocPuzzle.year}"
    }

    fun generateDayTileImage(
        puzzle: AoCPuzzle,
        dayScores: DayScores?,
        languages: List<String> = listOf("kt")
    ) {
        /*
        val width = 400
        val height = 200
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d: Graphics2D = image.createGraphics()

        // Draw the background
        g2d.color = Color(0, 128, 0) // A green background
        g2d.fillRect(0, 0, width, height)

        // Set font and color for the text
        g2d.font = Font("Arial", Font.BOLD, 30)
        g2d.color = Color.WHITE

        // Draw the 'Day 01' text
        g2d.drawString("Day 01", 20, 40)

        // Draw the '.kt' text
        g2d.font = Font("Arial", Font.PLAIN, 18)
        g2d.drawString(".kt", 20, height - 20)

        // Draw the sections for P1 and P2
        val middle = width / 2
        g2d.drawLine(middle, 0, middle, height)
        g2d.drawString("P1", middle + 5, 20)
        g2d.drawString(">24h", middle + 5, 35)
        g2d.drawString("time rank 192221", middle + 5, 50)

        g2d.drawString("P2", middle + 5, 70)
        g2d.drawString(">24h", middle + 5, 85)
        g2d.drawString("time rank 148344", middle + 5, 100)

        // Dispose graphics to save memory
        g2d.dispose()
*/
        // Assume these are defined as per the colors used in the original script
        val textColor = Color.WHITE
        val notCompletedColor = Color.GRAY
        val outlineColor = Color.BLACK // Used if text outline is needed

        // Initial settings
        val image = getAlternatingBackground(languages, dayScores?.time2 == null)
        val g2: Graphics2D = image.createGraphics()

        // Texteigenschaften und Farben
        g2.color = TEXT_COLOR
        g2.font = mainFont(20)

        // Zeichnen der "Day" und der Tagesnummer
        g2.drawString("Day", 3, 20)
        g2.font = mainFont(75)
        g2.drawString(puzzle.day.toString(), 10, 70)

        // Zeichnen Sie die Sprachen
        g2.font = secondaryFont(15)
        val langAsStr = languages.joinToString(" ")
        g2.drawString(langAsStr, 5, 95)

        // Zeichnen von P1 und P2
        g2.font = secondaryFont(10)
        for (part in 1..2) {
            val y = if (part == 2) 50 else 0
            val time = dayScores?.let { if (part == 1) it.time1 else it.time2 }
            val rank = dayScores?.let { if (part == 1) it.rank1 else it.rank2 }
            g2.drawString("P$part", 105, y + 20)

            if (time != null && rank != null) {
                g2.drawString("time", 105, y + 35)
                g2.drawString("rank", 105, y + 50)
                g2.drawString(time, 140, y + 35)
                g2.drawString(rank, 140, y + 50)
            } else {
                // Zeichnen Sie ein Kreuz, wenn keine Zeit oder Rang vorhanden ist
                g2.drawLine(135, y + 15, 155, y + 35)
                g2.drawLine(135, y + 35, 155, y + 15)
            }
        }

        // Zeichnen von Trennlinien
        g2.color = NOT_COMPLETED_COLOR
        g2.drawLine(100, 5, 100, 95)
        g2.drawLine(105, 50, 195, 50)

        // Save the image
        val tileDir = Paths.get(this.pathNameForTile(puzzle))
        if (!tileDir.exists()) {
            Files.createDirectories(tileDir)
        }
        ImageIO.write(image, "png", File(tileDir.resolve("Day%02d.png".format(puzzle.day)).toString()))
    }

}
