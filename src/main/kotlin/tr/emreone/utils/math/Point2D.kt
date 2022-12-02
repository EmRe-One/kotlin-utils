package tr.emreone.utils.math

import kotlin.math.absoluteValue
import kotlin.math.sign

typealias Coords = Pair<Int, Int>

class Point2D(val x: Long, val y: Long) {

    infix fun sharesAxisWith(that: Point2D): Boolean =
        x == that.x || y == that.y

    infix fun lineTo(that: Point2D): List<Point2D> {
        val xDelta = (that.x - x).sign
        val yDelta = (that.y - y).sign
        val steps = maxOf((x - that.x).absoluteValue, (y - that.y).absoluteValue)
        return (1..steps).scan(this) { last, _ ->
            Point2D(last.x + xDelta, last.y + yDelta)
        }
    }

    /**
     * Returns all the direct neighbors of this point.
     *
     * ```
     *             (x, y-1)
     *                ^
     *                |
     *  (x-1, y) <- (x,y) -> (x+1, y)
     *                |
     *                v
     *             (x, y+1)
     * ```
     *
     * @return List<Point2D> A list of all the direct neighbors of this point.
     */
    fun neighbors(): List<Point2D> =
        listOf(
            Point2D(x, y - 1),  // north
            Point2D(x + 1, y),  // east
            Point2D(x, y + 1),  // south
            Point2D(x - 1, y)   // west
        )

    /**
     *
     * @return List<Point2D>
     */
    fun allNeighbors(): List<Point2D> =
        neighbors() + listOf(
            Point2D(x + 1, y - 1),  // north-east
            Point2D(x + 1, y + 1),  // south-east
            Point2D(x - 1, y + 1),  // south-west
            Point2D(x - 1, y - 1)   // north-west
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Point2D

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result.toInt()
    }

    override fun toString(): String {
        return "($x|$y)"
    }

}
