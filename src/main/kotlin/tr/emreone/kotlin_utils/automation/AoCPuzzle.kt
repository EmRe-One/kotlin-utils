package tr.emreone.kotlin_utils.automation

data class AoCPuzzle(val day: Int, val year: Int) {

    init {
        require(day in 1..25) { "Invalid day $day" }
        require(year in 2015..2023) { "Invalid year $year" }
    }

    override fun toString() = "AoC-Puzzle: %02d. Dec. %4d".format(day, year)

}
