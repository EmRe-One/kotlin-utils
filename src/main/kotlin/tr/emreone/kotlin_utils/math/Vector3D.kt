package tr.emreone.kotlin_utils.math

class Vector3D(val x: Long, val y: Long, val z: Long) {

    /**
     * Returns the sum of this vector and the given vector.
     * ```
     *   |x|   |other_x|    |x + other_x|
     *   |y| + |other_y| =  |y + other_y|
     *   |z|   |other_z|    |z + other_z|
     * ```
     * @param other Vector3D
     * @return Vector3D this + other
     */
    operator fun plus(other: Vector3D): Vector3D {
        return Vector3D(x + other.x, y + other.y, z + other.z)
    }

    /**
     *  Returns the difference vector between this vector and the given vector.
     * ```
     *   |x|   |other_x|    |x - other_x|
     *   |y| - |other_y| =  |y - other_y|
     *   |z|   |other_z|    |z - other_z|
     * ```
     * @param other Vector3D
     * @return Vector3D this - other
     */
    operator fun minus(other: Vector3D): Vector3D {
        return Vector3D(x - other.x, y - other.y, z - other.z)
    }

    /**
     * Returns the scalar multiplication of this vector and the given scalar.
     * ```
     *   |x|            |x * other|
     *   |y| * other =  |y * other|
     *   |z|            |z * other|
     * ```
     * @param other Long
     * @return Vector3D this * other
     */
    operator fun times(other: Long): Vector3D {
        return Vector3D(x * other, y * other, z * other)
    }

    /**
     * Returns the scalar multiplication of this scalar and the given Vector.
     * ```
     *           |x|    |other * x|
     *   other * |y| =  |other * y|
     *           |z|    |other * z|
     * ```
     * @param other Vector3D
     * @return Vector3D this * other
     */
    operator fun Long.times(other: Vector3D): Vector3D {
        return Vector3D(this * other.x, this * other.y, this * other.z)
    }

    /**
     * Returns the dot product of this vector and the specified vector.
     * ```
     *   |x|   |other_x|
     * < |y| , |other_y| > =  (x * other_x) + (y * other_y) + (z - other_z)
     *   |z|   |other_z|
     * ```
     * @param other Vector3D
     * @return Long <this, other>
     */
    operator fun times(other: Vector3D): Long {
        return x * other.x +  y * other.y + z * other.z
    }

    /**
     * Returns the dot product of this vector and the specified vector.
     * ```
     *   |x|   |other_x|
     * < |y| , |other_y| > =  (x * other_x) + (y * other_y) + (z - other_z)
     *   |z|   |other_z|
     * ```
     * @param other Vector3D
     * @return Long <this, other>
     */
    infix fun dot(other: Vector3D): Long {
        return this * other
    }

    /**
     * Returns the cross product of this vector and the specified vector.
     * ```
     *   |x|   |other_x|     |y * other_z - z * other_y|
     *   |y| x |other_y|  =  |z * other_x - x * other_z|
     *   |z|   |other_z|     |x * other_y - y * other_x|
     * ```
     * @param other Vector3D
     * @return Vector3D this x other
     */
    infix fun cross(other: Vector3D): Vector3D {
        return Vector3D(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x)
    }

    /**
     * Returns a string representation of this vector.
     */
    override fun toString(): String {
        return "[$x,$y,$z)]"
    }

    /**
     * @return Boolean true if this vector is equal to the given vector.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3D

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
