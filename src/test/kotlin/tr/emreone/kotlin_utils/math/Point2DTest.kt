package tr.emreone.kotlin_utils.math

import org.junit.jupiter.api.Test
import tr.emreone.kotlin_utils.math.Point2D
import kotlin.test.assertContains

internal class Point2DTest {

    @Test
    fun `check direct neighbors`() {
        val point2D = Point2D(5, 3)

        val expected = arrayOf(
            Point2D(5, 2),   // north
            Point2D(6, 3),   // east
            Point2D(5, 4),   // south
            Point2D(4, 3)    // west
        )
        expected.forEach { point ->
            assertContains(point2D.neighbors(), point, "The neighbors of (5, 3) should contain $point")
        }
    }

    @Test
    fun `check diagonal neighbors`() {
        val point2D = Point2D(5, 3)

        val expected = arrayOf(
            Point2D(6, 2),   // north-east
            Point2D(6, 4),   // south-east
            Point2D(4, 4),   // south-west
            Point2D(4, 2)    // north-west
        )
        expected.forEach { point ->
            assertContains(point2D.allNeighbors(), point, "The neighbors of (5, 3) should contain $point")
        }
    }
}