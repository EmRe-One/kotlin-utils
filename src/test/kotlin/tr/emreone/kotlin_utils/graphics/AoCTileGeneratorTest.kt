package tr.emreone.kotlin_utils.graphics

import java.nio.file.Paths
import kotlin.io.path.absolutePathString

// Main logic similar to Python but adapted to Kotlin syntax and libraries
fun main() {
    val aocTileGenerator = AoCTileGenerator(2023)

//    val leaderboardHtml = aocTileGenerator.requestLeaderboard()
//    println(leaderboardHtml) // Do something with the leaderboard HTML
//
//    println()
//
//    val dayScores = aocTileGenerator.parseLeaderboard(leaderboardHtml)
//    for(d in dayScores) {
//        println("+========= DAY %02d ========+".format(d.key))
//        println("|Puzzle |  Time  |  Rank  |")
//        println("|   1   |%8s|%8s|".format(d.value.time1, d.value.rank1))
//        println("|   2   |%8s|%8s|".format(d.value.time2, d.value.rank2))
//        println()
//    }

    val dayScores = mutableMapOf(
        7 to AoCTileGenerator.DayScores(
            "00:01:12", "1", "100",
            "00:12:53", "123523", "20"
        )
    )

    val path = Paths.get("").toAbsolutePath()
    aocTileGenerator.generateDayTileImage("7", dayScores.get(7), path)

}
