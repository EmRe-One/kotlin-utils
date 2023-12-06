package tr.emreone.kotlin_utils.extensions

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