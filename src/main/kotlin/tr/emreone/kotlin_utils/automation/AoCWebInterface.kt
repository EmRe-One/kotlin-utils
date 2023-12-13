package tr.emreone.kotlin_utils.automation

import com.github.ajalt.mordant.rendering.TextColors
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class AoCWebInterface(private val sessionCookie: String?) {

    companion object {
        private const val BASE_URL = "https://adventofcode.com"
        private const val RIGHT = "That's the right answer"
        private const val FAIL = "That's not the right answer"
        private const val TOO_RECENT = "You gave an answer too recently"

        fun AoCPuzzle.toUri() = "$BASE_URL/$year/day/$day"

        private fun String.urlEncode() = URLEncoder.encode(this, "UTF-8")
    }

    fun downloadInput(aocPuzzle: AoCPuzzle): Result<List<String>> = runCatching {
        with(aocPuzzle) {
            println("Downloading puzzle for $aocPuzzle...")
            val url = URI("${aocPuzzle.toUri()}/input").toURL()
            val cookies = mapOf("session" to sessionCookie)

            with(url.openConnection()) {
                setRequestProperty(
                    "Cookie", cookies.entries.joinToString(separator = "; ") { (k, v) -> "$k=$v" }
                )
                connect()
                getInputStream().bufferedReader().readLines()
            }
        }
    }

    fun submitAnswer(aocPuzzle: AoCPuzzle, part: Part, answer: String): Verdict = runCatching {
        with(aocPuzzle) {
            println("Submitting answer for $aocPuzzle...")
            val url = URI("${aocPuzzle.toUri()}/answer").toURL()
            val cookies = mapOf("session" to sessionCookie)
            val payload = "level=$part&answer=${answer.urlEncode()}"
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                setRequestProperty(
                    "Cookie", cookies.entries.joinToString(separator = "; ") { (k, v) -> "$k=$v" }
                )
                setRequestProperty("content-type", "application/x-www-form-urlencoded")
                doOutput = true
                outputStream.bufferedWriter().use { it.write(payload) }
                inputStream.reader().readText()
            }
        }
    }.map { Verdict.of(it) }.getOrElse { Verdict.ofException(it) }

    sealed class Verdict private constructor(val text: String) {

        class Correct(response: String) : Verdict(response) {
            override fun toString() = TextColors.brightGreen(text)
        }

        abstract class WithWait(response: String, val wait: Duration) : Verdict(response)

        class Incorrect(response: String, wait: Duration) : WithWait(response, wait) {
            override fun toString(): String = TextColors.brightRed("Incorrect result, wait $wait")
        }

        class TooRecent(response: String, wait: Duration) : WithWait(response, wait) {
            override fun toString(): String = TextColors.brightRed("Too recent submission, wait $wait")
        }

        class SomethingWrong(response: String) : Verdict(response) {
            override fun toString() = TextColors.brightRed(text)
        }

        companion object {
            private fun String.pleaseWait() =
                lowercase().substringAfter("please wait ", "")
                    .substringBefore(" before trying", "").ifEmpty { null }

            private fun String.parseDuration(): Duration? {
                val (a, u) = split(" ", limit = 2)
                val amount = a.toIntOrNull() ?: when (a) {
                    "one" -> 1
                    "two" -> 2
                    else -> return null
                }
                return when (u.let { if (u.endsWith("s")) it.dropLast(1) else it }) {
                    "second" -> amount.seconds
                    "minute" -> amount.minutes
                    "hour" -> amount.hours
                    else -> null
                }
            }

            private fun String.parseMinSec(): Duration? {
                val (min, sec) =
                    (Regex(".* you have (\\d+)m (\\d+)s left to wait.*").matchEntire(lowercase())?.destructured
                        ?: return null)
                return min.toInt().minutes + sec.toInt().seconds
            }

            fun ofException(exception: Throwable): Verdict = SomethingWrong("Exception encountered: $exception")

            fun of(response: String): Verdict {
                val article = response.lines().filter { "<article>" in it }
                    .joinToString("") { it.replace(Regex("</?[^>]+(>|\$)"), "") }
                    .ifEmpty {
                        println(TextColors.brightRed("WARNING: no <article> tag found in response!"))
                        "WARNING: no <article> tag found in response: $response"
                    }

                val pleaseWait = article.pleaseWait()?.parseDuration() ?: article.parseMinSec()

                return when {
                    FAIL in article && pleaseWait != null -> Incorrect(article, pleaseWait)
                    RIGHT in article && pleaseWait == null -> Correct(article)
                    TOO_RECENT in article && pleaseWait != null -> TooRecent(article, pleaseWait)
                    else -> SomethingWrong(article)
                }
            }
        }
    }
}
