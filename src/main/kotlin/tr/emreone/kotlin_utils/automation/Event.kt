package tr.emreone.kotlin_utils.automation

@JvmInline
value class Event(private val year: Int) {
    init {
        require(year in 2015..2050) { "Invalid year $year" }
    }

    override fun toString() = "$year"
}
