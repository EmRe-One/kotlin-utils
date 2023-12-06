package tr.emreone.kotlin_utils

val IntRange.size get() = (last - first + 1).coerceAtLeast(0)
val LongRange.size get() = (last - first + 1).coerceAtLeast(0)

/**
 * Tests whether the two [ClosedRange]s overlap or contain one another.
 */
infix fun <T : Comparable<T>> ClosedRange<T>.overlaps(other: ClosedRange<T>): Boolean {
    if (isEmpty() || other.isEmpty()) return false
    return !(this.endInclusive < other.start || this.start > other.endInclusive)
}

fun IntRange.combine(other: IntRange): IntRange =
    listOf(first, last, other.first, other.last).minMax().let { (f, l) -> f..l }

/**
 * Merges non-empty [IntRange]s to a potentially shorter list of not-overlapping IntRanges.
 */
fun Iterable<IntRange>.merge(): List<IntRange> {
    val sorted = this.filter { !it.isEmpty() }.sortedBy { it.first }
    sorted.isNotEmpty() || return emptyList()

    val stack = ArrayDeque<IntRange>()
    stack.add(sorted.first())
    sorted.drop(1).forEach { current ->
        if (current.last <= stack.last().last) {
            // ignore as it's completely within
        } else if (current.first > stack.last().last + 1) {
            // it's completely out and after the last
            stack.add(current)
        } else {
            // they overlap
            stack.add(stack.removeLast().first..current.last)
        }
    }
    return stack
}


/**
 * Given an [IntRange], subtract all the given [others] and calculate a list of [IntRange]s
 * with every possible value not subtracted in the resulting ranges.
 *
 * @param others the ranges to subtract - they need not be merged before.
 */
fun IntRange.subtract(vararg others: IntRange): List<IntRange> = subtract(others.asIterable())

/**
 * Given an [IntRange], subtract all the given [others] and calculate a list of [IntRange]s
 * with every possible value not subtracted in the resulting ranges.
 *
 * @param others the ranges to subtract - they need not be merged before.
 */
fun IntRange.subtract(others: Iterable<IntRange>): List<IntRange> {
    val relevant = others.merge().filter { it overlaps this }
    if (relevant.isEmpty()) return listOf(this)

    return buildList {
        var includeFrom = first
        relevant.forEach { minus ->
            if (minus.first > includeFrom)
                add(includeFrom until minus.first.coerceAtMost(last))
            includeFrom = minus.last + 1
        }
        if (includeFrom <= last)
            add(includeFrom..last)
    }
}

/**
 * Merges non-empty [LongRange]s to a potentially shorter list of not-overlapping LongRanges.
 */
@JvmName("mergeLongRanges")
fun Iterable<LongRange>.merge(): List<LongRange> {
    val sorted = this.filter { !it.isEmpty() }.sortedBy { it.first }
    sorted.isNotEmpty() || return emptyList()

    val stack = ArrayDeque<LongRange>()
    stack.add(sorted.first())
    sorted.drop(1).forEach { current ->
        if (current.last <= stack.last().last) {
            // ignore as it's completely within
        } else if (current.first > stack.last().last + 1) {
            // it's completely out and after the last
            stack.add(current)
        } else {
            // they overlap
            stack.add(stack.removeLast().first..current.last)
        }
    }
    return stack
}

/**
 * Given an [LongRange], subtract all the given [others] and calculate a list of [LongRange]s
 * with every possible value not subtracted in the resulting ranges.
 *
 * @param others the ranges to subtract - they need not be merged before.
 */
fun LongRange.subtract(vararg others: LongRange): List<LongRange> = subtract(others.asIterable())

/**
 * Given an [LongRange], subtract all the given [others] and calculate a list of [LongRange]s
 * with every possible value not subtracted in the resulting ranges.
 *
 * @param others the ranges to subtract - they need not be merged before.
 */
fun LongRange.subtract(others: Iterable<LongRange>): List<LongRange> {
    val relevant = others.merge().filter { it overlaps this }
    if (relevant.isEmpty()) return listOf(this)

    return buildList {
        var includeFrom = first
        relevant.forEach { minus ->
            if (minus.first > includeFrom)
                add(includeFrom until minus.first.coerceAtMost(last))
            includeFrom = minus.last + 1
        }
        if (includeFrom <= last)
            add(includeFrom..last)
    }
}
