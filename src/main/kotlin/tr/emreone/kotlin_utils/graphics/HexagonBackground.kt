package tr.emreone.kotlin_utils.graphics

import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import kotlin.math.cos
import kotlin.math.sin

class HexagonBackground {
    private val hexagonSize = 30.0
    private val hexagonHeight = Math.sqrt(3.0) * hexagonSize / 2
    private val color = Color(255, 215, 0) // Goldfarbe

    fun drawHexagon(x: Double, y: Double, size: Double, g2d: Graphics2D): Path2D.Double {
        val hex = Path2D.Double()
        hex.moveTo(x + size * cos(0.0), y + size * sin(0.0))

        for (i in 1..6) {
            hex.lineTo(
                x + size * cos(i * Math.PI / 3),
                y + size * sin(i * Math.PI / 3)
            )
        }

        hex.closePath()
        return hex
    }

    fun createHexagonPatternImage(imageWidth: Int, imageHeight: Int): BufferedImage {
        val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()

        // Background color
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, imageWidth, imageHeight)

        // Hexagon color
        g2d.color = Color(0xFF, 0xD7, 0x00) // Gold color

        for (row in 0 until imageHeight / hexagonHeight.toInt() + 1) {
            for (col in 0 until imageWidth / hexagonSize.toInt() + 1) {
                val x = col * hexagonSize * 1.5
                val y = row * hexagonHeight + (if (col % 2 == 0) 0.0 else hexagonHeight)

                val hex = drawHexagon(x, y, hexagonSize, g2d)
                g2d.draw(hex)
            }
        }

        g2d.dispose()
        return image
    }

}
