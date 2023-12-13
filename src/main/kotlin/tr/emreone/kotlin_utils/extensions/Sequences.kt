import tr.emreone.kotlin_utils.extensions.minMaxOrNull
import tr.emreone.kotlin_utils.extensions.safeTimes

class InfiniteSequence<T>(base: Sequence<T>) : Sequence<T> by base

fun <T> Iterable<T>.asInfiniteSequence() =
    InfiniteSequence(sequence { while (true) yieldAll(this@asInfiniteSequence) })

fun <T> Sequence<T>.repeatLastForever() = InfiniteSequence(sequence {
    val it = this@repeatLastForever.iterator()
    if (it.hasNext()) {
        var elem: T
        do {
            elem = it.next()
            yield(elem)
        } while (it.hasNext())
        while (true) yield(elem)
    }
})

@JvmName("intProduct")
fun Sequence<Int>.product(): Long = fold(1L, Long::safeTimes)

fun Sequence<Long>.product(): Long = reduce(Long::safeTimes)

/**
 * Returns the smallest and largest element or `null` if there are no elements.
 */
fun <T : Comparable<T>> Sequence<T>.minMaxOrNull(): Pair<T, T>? = asIterable().minMaxOrNull()

/**
 * Returns the smallest and largest element or throws [NoSuchElementException] if there are no elements.
 */
fun <T : Comparable<T>> Sequence<T>.minMax(): Pair<T, T> = minMaxOrNull() ?: throw NoSuchElementException()

/**
 * Returns a sequence containing the runs of equal elements and their respective count as Pairs.
 */
fun <T> Sequence<T>.runs(): Sequence<Pair<T, Int>> = sequence {
    val iterator = iterator()
    if (iterator.hasNext()) {
        var current = iterator.next()
        var count = 1
        while (iterator.hasNext()) {
            val next: T = iterator.next()
            if (next != current) {
                yield(current to count)
                current = next
                count = 0
            }
            count++
        }
        yield(current to count)
    }
}

fun <T> Sequence<T>.runsOf(e: T): Sequence<Int> = runs().filter { it.first == e }.map { it.second }
