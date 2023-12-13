package tr.emreone.kotlin_utils.extensions

import kotlin.math.max
import kotlin.math.min


/**
 *
 * @receiver MutableList<Pair<String, Long>>
 * @param from MutableList<Pair<String, Long>>
 * @param deep Boolean
 */
fun MutableList<Pair<String, Long>>.copy(from: MutableList<Pair<String, Long>>, deep: Boolean = false) {
    this.clear()

    if (!deep) {
        this.addAll(from)
    }
    else {
        from.forEach {
            this.add(it.first to it.second)
        }
    }
}

/**
 * Create a permutation of the given list
 *
 * @receiver List<T>
 * @return List<List<T>>
 */
fun <T> List<T>.permutations(): List<List<T>> {
    if (size == 1) return listOf(this)
    return indices.flatMap { i ->
        val rest = subList(0, i) + subList(i + 1, size)
        rest.permutations().map {
            listOf(this[i]) + it
        }
    }
}

/*
 *  POWERSETS
 */

/**
 *
 * @receiver Collection<T>
 * @return Set<Set<T>>
 */
fun <T> Collection<T>.powerset(): Set<Set<T>> = powerset(this, setOf(setOf()))

/**
 *
 * @param left Collection<T>
 * @param acc Set<Set<T>>
 * @return Set<Set<T>>
 */
private tailrec fun <T> powerset(left: Collection<T>, acc: Set<Set<T>>): Set<Set<T>> = when {
    left.isEmpty() -> acc
    else -> powerset(left.drop(1), acc + acc.map { it + left.first() })
}

/*
 *  COMBINATIONS
 */

/**
 * In mathematics, a combination is a way of selecting items from a collection, such that (unlike permutations) the
 * order of selection does not matter.
 *
 * For set {1, 2, 3}, 2 elements combinations are {1, 2}, {2, 3}, {1, 3}.
 * All possible combinations is called 'powerset' and can be found as an
 * extension function for set under this name
 *
 * @receiver Set<T>
 * @param combinationSize Int
 * @return Set<Set<T>>
 */
fun <T> Set<T>.combinations(combinationSize: Int): Set<Set<T>> = when {
    combinationSize < 0 -> throw Error("combinationSize cannot be smaller then 0. It is equal to $combinationSize")
    combinationSize == 0 -> setOf(setOf())
    combinationSize >= size -> setOf(toSet())
    else -> powerset() // TODO this is not the best implementation
        .filter { it.size == combinationSize }
        .toSet()
}

/**
 *
 * @receiver Set<T>
 * @param combinationSize Int
 * @return Set<Map<T, Int>>
 */
fun <T> Set<T>.combinationsWithRepetitions(combinationSize: Int): Set<Map<T, Int>> = when {
    combinationSize < 0 -> throw Error("combinationSize cannot be smaller then 0. It is equal to $combinationSize")
    combinationSize == 0 -> setOf(mapOf())
    else -> combinationsWithRepetitions(combinationSize - 1)
        .flatMap { subset -> this.map { subset + (it to (subset.getOrElse(it) { 0 } + 1)) } }
        .toSet()
}

/*
 * SPLITS
 */

/**
 * Takes set of elements and returns set of splits and each of them is set of sets
 *
 * @receiver Set<T>
 * @param groupsNum Int
 * @return Set<Set<Set<T>>>
 */
fun <T> Set<T>.splits(groupsNum: Int): Set<Set<Set<T>>> = when {
    groupsNum < 0 -> throw Error("groupsNum cannot be smaller then 0. It is equal to $groupsNum")
    groupsNum == 0 -> if (isEmpty()) setOf(emptySet()) else emptySet()
    groupsNum == 1 -> setOf(setOf(this))
    groupsNum == size -> setOf(this.map { setOf(it) }.toSet())
    groupsNum > size -> emptySet()
    else -> setOf<Set<Set<T>>>()
        .plus(splitsWhereFirstIsAlone(groupsNum))
        .plus(splitsForFirstIsInAllGroups(groupsNum))
}

/**
 *
 * @receiver Set<T>
 * @param groupsNum Int
 * @return List<Set<Set<T>>>
 */
private fun <T> Set<T>.splitsWhereFirstIsAlone(groupsNum: Int): List<Set<Set<T>>> = this
    .minusElement(first())
    .splits(groupsNum - 1)
    .map { it.plusElement(setOf(first())) }

/**
 *
 * @receiver Set<T>
 * @param groupsNum Int
 * @return List<Set<Set<T>>>
 */
private fun <T> Set<T>.splitsForFirstIsInAllGroups(groupsNum: Int): List<Set<Set<T>>> = this
    .minusElement(first())
    .splits(groupsNum)
    .flatMap { split -> split.map { group -> split.minusElement(group).plusElement(group + first()) } }


/**
 * Splits elements by a defined [delimiter] predicate into groups of elements.
 *
 * @param limit limits the number of generated groups.
 * @param keepEmpty if true, groups without elements are preserved, otherwise will be omitted in the result.
 * @return a List of the groups of elements.
 */
fun <T> Iterable<T>.splitBy(limit: Int = 0, keepEmpty: Boolean = true, delimiter: (T) -> Boolean): List<List<T>> {
    require(limit >= 0) { "Limit must not be negative, but was $limit" }
    val isLimited = limit > 0

    val result = ArrayList<List<T>>(if (isLimited) limit.coerceAtMost(10) else 10)
    var currentSubList = mutableListOf<T>()
    for (element in this) {
        if ((!isLimited || (result.size < limit - 1)) && delimiter(element)) {
            if (keepEmpty || currentSubList.isNotEmpty()) {
                result += currentSubList
                currentSubList = mutableListOf()
            }
        } else {
            currentSubList += element
        }
    }
    if (keepEmpty || currentSubList.isNotEmpty())
        result += currentSubList
    return result
}

/**
 * Splits nullable elements by `null` values. The resulting groups will not contain any nulls.
 *
 * @param keepEmpty if true, groups without elements are preserved, otherwise will be omitted in the result.
 * @return a List of the groups of elements.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> Iterable<T?>.splitByNulls(keepEmpty: Boolean = true): List<List<T>> =
    splitBy(keepEmpty = keepEmpty) { it == null } as List<List<T>>

fun Pair<Int, Int>.asRange(): IntRange = min(first, second)..max(first, second)
fun Pair<Long, Long>.asRange(): LongRange = min(first, second)..max(first, second)

/**
 * Returns the smallest and largest element or `null` if there are no elements.
 */
fun <T : Comparable<T>> Iterable<T>.minMaxOrNull(): Pair<T, T>? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var min = iterator.next()
    var max = min
    while (iterator.hasNext()) {
        val e = iterator.next()
        if (min > e) min = e
        if (e > max) max = e
    }
    return min to max
}

/**
 * Returns the smallest and largest element or throws [NoSuchElementException] if there are no elements.
 */
fun <T : Comparable<T>> Iterable<T>.minMax(): Pair<T, T> = minMaxOrNull() ?: throw NoSuchElementException()

/**
 * Returns the first element yielding the smallest and the first element yielding the largest value
 * of the given function or `null` if there are no elements.
 */
inline fun <T, R : Comparable<R>> Iterable<T>.minMaxByOrNull(selector: (T) -> R): Pair<T, T>? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var minElem = iterator.next()
    var maxElem = minElem
    if (!iterator.hasNext()) return minElem to maxElem
    var minValue = selector(minElem)
    var maxValue = minValue
    do {
        val e = iterator.next()
        val v = selector(e)
        if (minValue > v) {
            minElem = e
            minValue = v
        }
        if (v > maxValue) {
            maxElem = e
            maxValue = v
        }
    } while (iterator.hasNext())
    return minElem to maxElem
}

/**
 * Returns the first element yielding the smallest and the first element yielding the largest value
 * of the given function or throws [NoSuchElementException] if there are no elements.
 */
inline fun <T, R : Comparable<R>> Iterable<T>.minMaxBy(selector: (T) -> R): Pair<T, T> =
    minMaxByOrNull(selector) ?: throw NoSuchElementException()

/**
 * Returns the smallest and largest value as a range or `null` if there are no elements.
 */
fun Iterable<Int>.rangeOrNull(): IntRange? = minMaxOrNull()?.let { it.first..it.second }

/**
 * Returns the smallest and largest value as a range or throws [NoSuchElementException] if there are no elements.
 */
fun Iterable<Int>.range(): IntRange = rangeOrNull() ?: throw NoSuchElementException()

/**
 * Returns the smallest and largest value as a range or `null` if there are no elements.
 */
fun Iterable<Long>.rangeOrNull(): LongRange? = minMaxOrNull()?.let { it.first..it.second }

/**
 * Returns the smallest and largest value as a range or throws [NoSuchElementException] if there are no elements.
 */
fun Iterable<Long>.range(): LongRange = rangeOrNull() ?: throw NoSuchElementException()

/**
 * Efficiently generate the top [n] smallest elements without sorting all elements.
 */
@Suppress("DuplicatedCode")
fun <T : Comparable<T>> Iterable<T>.minN(n: Int): List<T> {
    require(n >= 0) { "Number of smallest elements must not be negative" }
    val iterator = iterator()
    when {
        n == 0 || !iterator.hasNext() -> return emptyList()
        n == 1 -> return minOrNull()?.let { listOf(it) } ?: emptyList()
        this is Collection<T> && n >= size -> return this.sorted()
    }

    val smallest = ArrayList<T>(n.coerceAtMost(10))
    var min = iterator.next()
        .also { smallest += it }
        .let { it to it }

    while (iterator.hasNext()) {
        val e = iterator.next()
        when {
            smallest.size < n -> {
                smallest += e
                min = when {
                    e < min.first -> e to min.second
                    e > min.second -> min.first to e
                    else -> min
                }
            }

            e < min.second -> {
                val removeAt = smallest.indexOfLast { it.compareTo(min.second) == 0 }
                smallest.removeAt(removeAt)
                smallest += e
                min = smallest.minMax()
            }
        }
    }
    return smallest.sorted()
}

/**
 * Efficiently generate the top [n] largest elements without sorting all elements.
 */
@Suppress("DuplicatedCode")
fun <T : Comparable<T>> Iterable<T>.maxN(n: Int): List<T> {
    require(n >= 0) { "Number of largest elements must not be negative" }
    val iterator = iterator()
    when {
        n == 0 || !iterator.hasNext() -> return emptyList()
        n == 1 -> return maxOrNull()?.let { listOf(it) } ?: emptyList()
        this is Collection<T> && n >= size -> return this.sortedDescending()
    }

    val largest = ArrayList<T>(n.coerceAtMost(10))
    var max = iterator.next()
        .also { largest += it }
        .let { it to it }

    while (iterator.hasNext()) {
        val e = iterator.next()
        when {
            largest.size < n -> {
                largest += e
                max = when {
                    e < max.first -> e to max.second
                    e > max.second -> max.first to e
                    else -> max
                }
            }

            e > max.first -> {
                val removeAt = largest.indexOfLast { it.compareTo(max.first) == 0 }
                largest.removeAt(removeAt)
                largest += e
                max = largest.minMax()
            }
        }
    }
    return largest.sortedDescending()
}

fun <K, V> Map<K, V>.flip(): Map<V, K> = asIterable().associate { (k, v) -> v to k }

fun Iterable<Long>.product(): Long = reduce(Long::safeTimes)

@JvmName("intProduct")
fun Iterable<Int>.product(): Long = fold(1L, Long::safeTimes)

infix fun Int.safeTimes(other: Int) = (this * other).also {
    check(other == 0 || it / other == this) { "Integer Overflow at $this * $other" }
}

infix fun Long.safeTimes(other: Long) = (this * other).also {
    check(other == 0L || it / other == this) { "Long Overflow at $this * $other" }
}

infix fun Long.safeTimes(other: Int) = (this * other).also {
    check(other == 0 || it / other == this) { "Long Overflow at $this * $other" }
}

infix fun Int.safeTimes(other: Long) = (this.toLong() * other).also {
    check(other == 0L || it / other == this.toLong()) { "Long Overflow at $this * $other" }
}

fun Long.checkedToInt(): Int = let {
    check(it in Int.MIN_VALUE..Int.MAX_VALUE) { "Value does not fit in Int: $it" }
    it.toInt()
}

/**
 * Returns a list containing the runs of equal elements and their respective count as Pairs.
 */
fun <T> Iterable<T>.runs(): List<Pair<T, Int>> {
    val iterator = iterator()
    if (!iterator.hasNext())
        return emptyList()
    val result = mutableListOf<Pair<T, Int>>()
    var current = iterator.next()
    var count = 1
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (next != current) {
            result.add(current to count)
            current = next
            count = 0
        }
        count++
    }
    result.add(current to count)
    return result
}

fun <T> Iterable<T>.runsOf(e: T): List<Int> {
    val iterator = iterator()
    if (!iterator.hasNext())
        return emptyList()
    val result = mutableListOf<Int>()
    var count = 0
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (next == e) {
            count++
        } else if (count > 0) {
            result.add(count)
            count = 0
        }
    }
    if (count > 0)
        result.add(count)
    return result
}

fun <T> T.applyTimes(n: Int, f: (T) -> T): T = when (n) {
    0 -> this
    else -> f(this).applyTimes(n - 1, f)
}
