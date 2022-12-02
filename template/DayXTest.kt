package tr.emreone.adventofcode.days

import tr.emreone.utils.Resources
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class Day$1Test {

    @Test
    fun part1() {
        val input = Resources.resourceAsList("day$1_example.txt")
        assertEquals(-1, Day$1.part1(input), "Day$1, Part1 should be -1.")
    }

    @Test
    fun part2() {
        val input = Resources.resourceAsList("day$1_example.txt")
        assertEquals(-1, Day$1.part2(input), "Day$1, Part2 should be -1.")
    }

}