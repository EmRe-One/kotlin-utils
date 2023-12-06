package tr.emreone.kotlin_utils.automation
data class FQD(val day: Int, val year: Event) {
    init {
        require(day in 1..25) { "Invalid day $day" }
    }

    override fun toString() = "$year day $day"
}
