package tr.emreone.kotlin_utils.automation

enum class Part(private val level: Int) {

    P1(1), P2(2);

    override fun toString() = "$level"

}
