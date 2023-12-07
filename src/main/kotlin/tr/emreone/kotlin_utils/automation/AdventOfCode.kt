package tr.emreone.kotlin_utils.automation

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import org.reflections.Reflections
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.time.*

const val FAST_MODE = false

val aocTerminal = Terminal()
var logEnabled = false
var verbose = true

/**
 * Dirty, but effective way to inject test data globally for one-time use only!
 * Will be reset on first read.
 */
var globalTestData: String? = null
    get() = field?.also {
        println("\n!!!! USING TEST DATA !!!!\n")
        field = null
    }

//@ExperimentalTime
//fun main() {
//    verbose = false
//    with(aocTerminal) {
//        println(red("\n~~~ Advent Of Code Runner ~~~\n"))
//        val dayClasses = getAllDayClasses().sortedBy(::dayNumber)
//        val totalDuration = dayClasses.map { it.execute() }.reduceOrNull(Duration::plus)
//        println("\nTotal runtime: ${red("$totalDuration")}")
//    }
//}

class AdventOfCode(var session: String? = null) {

    private val web = AoCWebInterface(getSessionCookie())

    fun sendToClipboard(a: Any?): Boolean {
        if (a in listOf(null, 0, -1, Day.NotYetImplemented)) return false
        return runCatching {
            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val transferable: Transferable = StringSelection(a.toString())
            clipboard.setContents(transferable, null)
        }.isSuccess
    }

    fun getPuzzleInput(aocPuzzle: AoCPuzzle): List<String> {
        val cached = readInputFileOrNull(aocPuzzle)
        if (!cached.isNullOrEmpty()) return cached

        return web.downloadInput(aocPuzzle).onSuccess {
            writeInputFile(aocPuzzle, it)
        }.getOrElse {
            listOf("Unable to download ${aocPuzzle}: $it")
        }
    }

    fun submitAnswer(aocPuzzle: AoCPuzzle, part: Part, answer: String): AoCWebInterface.Verdict =
        web.submitAnswer(aocPuzzle, part, answer)

    private val logFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun appendSubmitLog(aocPuzzle: AoCPuzzle, part: Part, answer: String, verdict: AoCWebInterface.Verdict) {
        val now = LocalDateTime.now()
        val nowText = logFormat.format(now)
        val id = idFor(aocPuzzle, part)
        val text =
            "$nowText - $id - submitted \"$answer\" - ${if (verdict is AoCWebInterface.Verdict.Correct) "OK" else "FAIL with ${verdict::class.simpleName}"}"
        appendSubmitLog(aocPuzzle, text)
        appendSubmitLog(aocPuzzle, verdict.text)
        if (verdict is AoCWebInterface.Verdict.WithWait) {
            val locked = now + verdict.wait.toJavaDuration()
            appendSubmitLog(
                aocPuzzle,
                "$nowText - $id - LOCKED until ${DateTimeFormatter.ISO_DATE_TIME.format(locked)}"
            )
        }
    }

    class PreviousSubmitted(
        private val locked: LocalDateTime?,
        private val answers: List<String>,
        private val log: List<String>,
    ) {
        operator fun contains(answer: String) = answer in answers
        fun isNotEmpty() = answers.isNotEmpty()

        override fun toString() = (
                listOf(
                    brightMagenta("Previously submitted answers were:"),
                    "${log.size} attempts in total".takeIf { log.size > 3 }
                ) +
                        log.takeLast(3).map { it.highlight() } +
                        lockInfo()
                )
            .filterNotNull()
            .joinToString("\n", postfix = "\n ")

        private fun String.highlight() =
            split("\"", limit = 3).mapIndexed { index, s -> if (index == 1) brightMagenta(s) else s }.joinToString("")

        private fun lockInfo() = locked?.let {
            if (isStillLocked)
                brightRed("Locked until $it")
            else
                yellow("Had been locked, but is free again!")
        }

        private val isStillLocked get() = locked?.let { LocalDateTime.now() < it } == true

        val waitSecondsOrNull
            get() = locked?.let {
                val now = LocalDateTime.now()
                (it.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC))
            }.takeIf { (it ?: 0) > 0 }

        fun waitUntilFree() {
            isStillLocked || return
            with(aocTerminal) {
                do {
                    cursor.move { startOfLine();clearLine() }
                    print(brightRed("Waiting $waitSecondsOrNull more seconds..."))
                    Thread.sleep(500)
                } while (LocalDateTime.now() < locked!!)
                cursor.move { startOfLine(); clearLine() }
                println("Fire!")
            }
        }
    }

    fun previouslySubmitted(aocPuzzle: AoCPuzzle, part: Part): PreviousSubmitted =
        readSubmitLog(aocPuzzle)
            .filter { idFor(aocPuzzle, part) in it }
            .let { relevant ->
                val answers = relevant
                    .filter { "submitted" in it }
                    .mapNotNull { log ->
                        log.split("\"").getOrNull(1)?.let { it to log }
                    }

                val locked = if ("LOCKED" in relevant.lastOrNull().orEmpty()) {
                    val lock = relevant.last().substringAfter("until ")
                    LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(lock))
                } else null

                PreviousSubmitted(locked, answers.map { it.first }, answers.map { it.second })
            }

    private fun idFor(aocPuzzle: AoCPuzzle, part: Part) =
        "${aocPuzzle.year} day ${aocPuzzle.day} part $part"

    private fun getSessionCookie() =
        this.session
            ?: System.getenv("AOC_COOKIE")
            ?: object {}.javaClass.getResource("session-cookie")
                ?.readText()
                ?.lines()
                ?.firstOrNull { it.isNotBlank() }
            ?: warn("No session cookie in environment or file found, will not be able to talk to AoC server.")

    private fun readInputFileOrNull(aocPuzzle: AoCPuzzle): List<String>? {
        val file = File(fileNameFor(aocPuzzle))
        file.exists() || return null
        return file.readLines()
    }

    private fun writeInputFile(aocPuzzle: AoCPuzzle, content: List<String>) {
        File(pathNameForYear(aocPuzzle)).mkdirs()
        File(fileNameFor(aocPuzzle)).writeText(content.joinToString("\n"))
    }

    private fun readSubmitLog(aocPuzzle: AoCPuzzle): List<String> {
        val file = File(submitLogFor(aocPuzzle))
        file.exists() || return emptyList()
        return file.readLines()
    }

    private fun pathNameForYear(aocPuzzle: AoCPuzzle): String {
        return "puzzles/${aocPuzzle.year}"
    }

    private fun fileNameFor(aocPuzzle: AoCPuzzle): String {
        return "${pathNameForYear(aocPuzzle)}/day${"%02d".format(aocPuzzle.day)}.txt"
    }

    private fun submitLogFor(aocPuzzle: AoCPuzzle): String {
        return "${pathNameForYear(aocPuzzle)}/submit.log"
    }

    private fun appendSubmitLog(aocPuzzle: AoCPuzzle, content: String) {
        File(submitLogFor(aocPuzzle)).appendText("\n$content")
    }
}


fun getAllDayClasses(): Collection<Class<out Day>> =
    Reflections("").getSubTypesOf(Day::class.java).filter { it.simpleName.matches(Regex("Day\\d+")) }

fun dayNumber(day: Class<out Day>) = day.simpleName.replace("Day", "").toInt()

@ExperimentalTime
fun Class<out Day>.execute(): Duration {
    fun TimedValue<Any?>.show(n: Int, padded: Int) {
        val x = " ".repeat(padded) + "Part $n [$duration]: "
        println(
            "$x$value"
                .trimEnd()
                .split("\n")
                .joinToString("\n".padEnd(x.length + 1, ' '))
        )
    }

    val day = constructors[0].newInstance() as Day

    print("${day.puzzle.day}: ${day.title}".restrictWidth(30, 30))

    val part1 = measureTimedValue { day.part1 }
    part1.show(1, 0)

    val part2 = measureTimedValue { day.part2 }
    part2.show(2, 30)

    return part1.duration + part2.duration
}

fun log(message: Terminal.() -> Any?) {
    if (logEnabled && verbose) alog(message)
}

fun alog(message: Terminal.() -> Any?) {
    if (verbose) println(aocTerminal.message().takeIf { it != Unit } ?: "")
}

fun <T : Day> create(dayClass: KClass<T>): T =
    dayClass.constructors.first { it.parameters.isEmpty() }.call()

data class TestData(val input: String, val expectedPart1: Any?, val expectedPart2: Any?) {

    fun <T : Day> passesTestsUsing(dayClass: KClass<T>): Boolean {
        (expectedPart1 != null || expectedPart2 != null) || return true
        globalTestData = input
        val day = create(dayClass)

        return listOfNotNull(
            Triple(1, { day.part1() }, "$expectedPart1").takeIf { expectedPart1 != null },
            Triple(2, { day.part2() }, "$expectedPart2").takeIf { expectedPart2 != null }
        ).all { (part, partFun, expectation) ->
            println(gray("Checking part $part against $expectation..."))
            val actual = partFun()
            val match = actual == Day.NotYetImplemented || "$actual" == expectation
            if (match) {
                aocTerminal.success("✅ Test succeeded.")
            }
            else {
                aocTerminal.danger("❌ Checking of part $part failed")
                println("Expected: ${brightRed(expectation)}")
                println("  Actual: ${brightRed("$actual")}")
                println(yellow("Check demo ${TextStyles.bold("input")} and demo ${TextStyles.bold("expectation")}!"))
            }
            match
        }
    }
}

inline fun <reified T : Day> solve(offerSubmit: Boolean = false, test: SolveDsl<T>.() -> Unit = {}) {
    if (SolveDsl(T::class).apply(test).isEverythingOK())
        create(T::class).solve(offerSubmit)
}

class SolveDsl<T : Day>(private val dayClass: KClass<T>) {
    val tests = mutableListOf<TestData>()

    var ok = true
    operator fun String.invoke(part1: Any? = null, part2: Any? = null) {
        ok || return
        ok = TestData(this, part1, part2).passesTestsUsing(dayClass)
    }

    infix fun String.part1(expectedPart1: Any?): TestData =
        TestData(this, expectedPart1, null).also { tests += it }

    infix fun String.part2(expectedPart2: Any?) {
        TestData(this, null, expectedPart2).also { tests += it }
    }

    infix fun TestData.part2(expectedPart2: Any?) {
        tests.remove(this)
        copy(expectedPart2 = expectedPart2).also { tests += it }
    }

    fun isEverythingOK() =
        ok && tests.all { it.passesTestsUsing(dayClass) }
}

@OptIn(ExperimentalTime::class)
inline fun runWithTiming(part: String, f: () -> Any?) {
    val (result, duration) = measureTimedValue(f)
    with(aocTerminal) {
        success("\nSolution $part: (took $duration)\n" + brightBlue("$result"))
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class ParserContext(private val columnSeparator: Regex, private val line: String) {
    val cols: List<String> by lazy { line.split(columnSeparator) }
    val nonEmptyCols: List<String> by lazy { cols.filter { it.isNotEmpty() } }
    val nonBlankCols: List<String> by lazy { cols.filter { it.isNotBlank() } }
    val ints: List<Int> by lazy { line.extractAllIntegers() }
    val longs: List<Long> by lazy { line.extractAllLongs() }
}

fun String.extractInt() = toIntOrNull() ?: sequenceContainedIntegers().first()
fun String.extractLong() = toLongOrNull() ?: sequenceContainedLongs().first()

private val numberRegex = Regex("(-+)?\\d+")
private val positiveNumberRegex = Regex("\\d+")

fun String.sequenceContainedIntegers(startIndex: Int = 0, includeNegativeNumbers: Boolean = true): Sequence<Int> =
    (if (includeNegativeNumbers) numberRegex else positiveNumberRegex).findAll(this, startIndex)
        .mapNotNull { m -> m.value.toIntOrNull() ?: warn("Number too large for Int: ${m.value}") }

fun String.sequenceContainedLongs(startIndex: Int = 0, includeNegativeNumbers: Boolean = true): Sequence<Long> =
    (if (includeNegativeNumbers) numberRegex else positiveNumberRegex).findAll(this, startIndex)
        .mapNotNull { m -> m.value.toLongOrNull() ?: warn("Number too large for Long: ${m.value}") }

fun String.extractAllIntegers(startIndex: Int = 0, includeNegativeNumbers: Boolean = true): List<Int> =
    sequenceContainedIntegers(startIndex, includeNegativeNumbers).toList()

fun String.extractAllLongs(startIndex: Int = 0, includeNegativeNumbers: Boolean = true): List<Long> =
    sequenceContainedLongs(startIndex, includeNegativeNumbers).toList()

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> String.extractAllNumbers(
    startIndex: Int = 0,
    includeNegativeNumbers: Boolean = true,
    klass: KClass<T> = T::class,
): List<T> = when (klass) {
    Int::class -> extractAllIntegers(startIndex, includeNegativeNumbers)
    UInt::class -> extractAllIntegers(startIndex, false).map { it.toUInt() }
    Long::class -> extractAllLongs(startIndex, includeNegativeNumbers)
    ULong::class -> extractAllLongs(startIndex, false).map { it.toULong() }
    else -> error("Cannot extract numbers of type ${klass.simpleName}")
} as List<T>

fun <T> warn(msg: String): T? {
    with(aocTerminal) { warning("WARNING: $msg") }
    return null
}

fun Any?.restrictWidth(minWidth: Int, maxWidth: Int) = with("$this") {
    when {
        length > maxWidth -> substring(0, maxWidth - 3) + "..."
        length < minWidth -> padEnd(minWidth)
        else -> this
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun useSystemProxies() {
    System.setProperty("java.net.useSystemProxies", "true")
}
