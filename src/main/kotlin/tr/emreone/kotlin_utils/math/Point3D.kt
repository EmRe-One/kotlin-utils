package tr.emreone.kotlin_utils.math

import kotlin.math.abs

class Point3D(val x: Long, val y: Long, val z: Long) {

    /**
     * Returns the distance between this point and the given point.
     *
     * @param other Point3D
     * @return Long
     */
    fun manhattanDistanceTo(other: Point3D): Long {
        return abs(x - other.x) + abs(y - other.y) + abs(z - other.z)
    }

    fun asList(): List<Long> {
        return listOf(x, y, z)
    }

    /**
     *
     * @param translate Vector3D
     * @return Point3D
     */
    operator fun plus(translate: Vector3D): Point3D {
        return Point3D(x + translate.x, y + translate.y, z + translate.z)
    }

    /**
     *
     * @param translate Vector3D
     * @return Point3D
     */
    operator fun minus(translate: Vector3D): Point3D {
        return Point3D(x - translate.x, y - translate.y, z - translate.z)
    }

    /**
     *
     * @param other Point3D
     * @return Vector3D
     */
    operator fun minus(other: Point3D): Vector3D {
        return Vector3D(x - other.x, y - other.y, z - other.z)
    }

    /**
     *
     * @param facing Int -> one of (0, 1, 2, 3, 4, 5)
     * @return Point3D
     */
    fun face(facing: Int): Point3D =
        when (facing) {
            0 -> this
            1 -> Point3D(x, -y, -z)
            2 -> Point3D(x, -z, y)
            3 -> Point3D(-y, -z, x)
            4 -> Point3D(y, -z, -x)
            5 -> Point3D(-x, -z, -y)
            else -> error("Invalid facing")
        }

    /**
     *
     * @param rotating Int -> one of (0, 1, 2, 3)
     * @return Point3D
     */
    fun rotate(rotating: Int): Point3D =
        when (rotating) {
            0 -> this
            1 -> Point3D(-y, x, z)
            2 -> Point3D(-x, -y, z)
            3 -> Point3D(y, -x, z)
            else -> error("Invalid rotation")
        }

    override fun toString(): String {
        return "($x|$y|$z)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Point3D

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        return result.toInt()
    }

}
