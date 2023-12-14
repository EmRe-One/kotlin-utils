package tr.emreone.kotlin_utils.data_structures

import asInfiniteSequence


class InfiniteList<T>(private val backingList: List<T>) : List<T> by backingList {
    init {
        require(backingList.isNotEmpty()) { "Cannot build an ${this::class.simpleName} from an empty list" }
    }

    override val size: Int
        get() = Int.MAX_VALUE

    override fun get(index: Int): T = backingList[index % backingList.size]

    override fun iterator(): Iterator<T> = backingList.asInfiniteSequence().iterator()

    override fun listIterator(): ListIterator<T> = listIterator(0)

    override fun listIterator(index: Int): ListIterator<T> = object : ListIterator<T> {
        val backingList = this@InfiniteList.backingList
        var nextIndex = index
        override fun hasNext(): Boolean = true

        override fun hasPrevious(): Boolean = true

        override fun next(): T = backingList[nextIndex].also {
            nextIndex++
            if (nextIndex == backingList.size) nextIndex = 0
        }

        override fun nextIndex(): Int = nextIndex

        override fun previous(): T {
            nextIndex--
            if (nextIndex < 0) nextIndex = backingList.lastIndex
            return backingList[nextIndex]
        }

        override fun previousIndex(): Int = if (nextIndex == 0) backingList.lastIndex else nextIndex - 1

    }

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        error("subList has not been implemented by EndlessList")
    }

    override fun toString(): String = "inf$backingList"
}
