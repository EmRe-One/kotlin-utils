package tr.emreone.kotlin_utils.automation

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal

@Suppress("MemberVisibilityCanBePrivate")
open class Day private constructor(
    val fqd: FQD,
    val title: String,
    private val terminal: Terminal,
) {
    constructor(day: Int, year: Int, title: String = "unknown", terminal: Terminal = aocTerminal) : this(
        FQD(day, Event(year)), title, terminal
    )

    val day = fqd.day
    val year = fqd.year

    var testInput = false

    private val header: Unit by lazy { if (verbose) println("--- AoC $year, Day $day: $title ---\n") }

    private val rawInput: List<String> by lazy {
        globalTestData?.also {
            logEnabled = true
            testInput = true
        }?.split("\n") ?: AoC.getPuzzleInput(day, year).also {
            logEnabled = false
        }
    }

    // all the different ways to get your input
    val input: List<String> by lazy { rawInput.show("Raw") }
    val inputAsGrid: List<List<Char>> by lazy { rawInput.map { it.toList() }.show("Grid") }
    val inputAsInts: List<Int> by lazy { rawInput.map { it.extractInt() }.show("Int") }
    val inputAsLongs: List<Long> by lazy { rawInput.map { it.extractLong() }.show("Long") }
    val inputAsString: String by lazy { rawInput.joinToString("\n").also { listOf(it).show("One string") } }

    var groupDelimiter: (String) -> Boolean = String::isEmpty
    val inputAsGroups: List<List<String>> by lazy { groupedInput(groupDelimiter) }

    fun groupedInput(delimiter: (String) -> Boolean): List<List<String>> {
        val result = mutableListOf<List<String>>()
        var currentSubList: MutableList<String>? = null
        for (line in rawInput) {
            if (delimiter(line)) {
                currentSubList = null
            } else {
                if (currentSubList == null) {
                    currentSubList = mutableListOf(line)
                    result += currentSubList
                } else
                    currentSubList.add(line)
            }
        }
        return result.show("Chunked into ${result.size} chunks of")
    }

    fun <T> mappedInput(lbd: (String) -> T): List<T> =
        rawInput.map(catchingMapper(lbd)).show("Mapped")

    fun <T> parsedInput(
        columnSeparator: Regex = Regex("\\s+"),
        predicate: (String) -> Boolean = { true },
        lbd: ParserContext.(String) -> T,
    ): List<T> =
        rawInput.filter(predicate).map(parsingMapper(columnSeparator, lbd)).show("Parsed")

    fun <T> matchedInput(regex: Regex, lbd: (List<String>) -> T): List<T> =
        rawInput.map(matchingMapper(regex, lbd)).show("Matched")

    val part1: Any? by lazy { part1() }
    val part2: Any? by lazy { part2() }

    open fun part1(): Any? = NotYetImplemented

    open fun part2(): Any? = NotYetImplemented

    fun solve(offerSubmit: Boolean) {
        header
        runWithTiming("1") { part1 }
        runWithTiming("2") { part2 }
        if (offerSubmit) submit(part1, part2)
    }

    private fun submit(part1: Any?, part2: Any?) {
        println()
        if ("$part1" == "$part2") {
            aocTerminal.println(TextColors.yellow("The two answers are identical. No submitting allowed."))
            return
        }
        listOfNotNull(part2.isPossibleAnswerOrNull(Part.P2), part1.isPossibleAnswerOrNull(Part.P1)).firstOrNull()
            ?.let { (part, answer) ->
                with(aocTerminal) {
                    val previouslySubmitted =
                        AoC.previouslySubmitted(day, year, part)
                    if (answer in previouslySubmitted) {
                        println(TextColors.brightMagenta("This answer to part $part has been previously submitted!"))
                        return
                    }
                    if (previouslySubmitted.isNotEmpty()) {
                        println(previouslySubmitted)
                    }
                    val extra = previouslySubmitted.waitSecondsOrNull?.let {
                        "wait $it seconds and then "
                    }.orEmpty()
                    val choice = prompt(
                        TextColors.brightCyan("""Should I ${extra}submit "${TextColors.brightBlue(answer)}" as answer to part $part?"""),
                        choices = listOf("y", "n"),
                        default = "n"
                    )
                    if (choice == "y") {
                        previouslySubmitted.waitUntilFree()
                        val verdict = AoC.submitAnswer(fqd, part, answer)
                        println(verdict)
                        AoC.appendSubmitLog(day, year, part, answer, verdict)
                    }
                }
            }
    }

    private fun Any?.isPossibleAnswerOrNull(part: Part): Pair<Part, String>? =
        (part to "$this").takeIf { (_, sAnswer) ->
            sAnswer !in listOf("null", "-1", "$NotYetImplemented") && sAnswer.length > 1 && "\n" !in sAnswer
        }

    fun <T> T.show(prompt: String = "", maxLines: Int = 10): T {
        verbose || return this

        header
        if (this is List<*>)
            show(prompt, maxLines)
        else
            println("$prompt: $this")
        return this
    }

    private fun <T : Any?> List<T>.show(type: String, maxLines: Int = 10): List<T> {
        verbose || return this

        header
        with(listOfNotNull(type.takeIf { it.isNotEmpty() }, "input data").joinToString(" ")) {
            println("==== $this ${"=".repeat(50 - length - 6)}")
        }
        val idxWidth = lastIndex.toString().length
        preview(maxLines) { idx, data ->
            val original = rawInput.getOrNull(idx)
            val s = when {
                rawInput.size != this.size -> "$data"
                original != "$data" -> "${original.restrictWidth(40, 40)} => $data"
                else -> original
            }
            println("${idx.toString().padStart(idxWidth)}: ${s.restrictWidth(0, 160)}")
        }
        println("=".repeat(50))
        return this
    }

    object NotYetImplemented {
        override fun toString() = "not yet implemented"
    }

    companion object {

        private fun <T> matchingMapper(regex: Regex, lbd: (List<String>) -> T): (String) -> T = { s ->
            regex.matchEntire(s)?.groupValues?.let {
                runCatching { lbd(it) }.getOrElse { ex(s, it) }
            } ?: error("Input line does not match regex: \"$s\"")
        }

        private fun <T> catchingMapper(lbd: (String) -> T): (String) -> T = { s ->
            runCatching { lbd(s) }.getOrElse { ex(s, it) }
        }

        private fun <T> parsingMapper(columnSeparator: Regex, lbd: ParserContext.(String) -> T): (String) -> T = { s ->
            runCatching {
                ParserContext(columnSeparator, s).lbd(s)
            }.getOrElse { ex(s, it) }
        }

        private fun ex(input: String, ex: Throwable): Nothing =
            error("Exception on input line\n\n\"$input\"\n\n$ex")

        private fun <T> List<T>.preview(maxLines: Int, f: (idx: Int, data: T) -> Unit) {
            if (size <= maxLines) {
                forEachIndexed(f)
            } else {
                val cut = (maxLines - 1) / 2
                (0 until maxLines - cut - 1).forEach { f(it, this[it]!!) }
                if (size > maxLines) println("...")
                (lastIndex - cut + 1..lastIndex).forEach { f(it, this[it]!!) }
            }
        }

    }

}