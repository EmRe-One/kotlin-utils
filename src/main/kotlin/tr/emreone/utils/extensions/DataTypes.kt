package tr.emreone.utils.extensions

fun List<Pair<Long, Long>>.sum() = reduce { acc, pair -> acc + pair }

operator fun Pair<Long, Long>.plus(other: Pair<Long, Long>): Pair<Long, Long> {
    return first + other.first to second + other.second
}

fun IntRange.size(): Int {
    return this.last - this.first + 1
}

infix fun IntRange.intersects(other: IntRange): Boolean =
    first <= other.last && last >= other.first

infix fun IntRange.intersect(other: IntRange): IntRange =
    maxOf(first, other.first)..minOf(last, other.last)
