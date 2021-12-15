package org.projectxy.iv4xrLib.rl

import A.B.Tile
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JPanel
import kotlin.math.floor

interface NethackColorer {
    fun colorFor(x: Int, y: Int): Color
}

class ConstantColorer(val color: Color) : NethackColorer {
    override fun colorFor(x: Int, y: Int) = color
}

class InterpolatingColorer(val min: Float, val max: Float, val minColor: Color, val maxColor: Color, val value: (Int, Int) -> Float) : NethackColorer {
    private val delta = max - min

    override fun colorFor(x: Int, y: Int): Color {
        val current = value(x, y)
        val progress = max - current
        val percentage = progress / delta
        val color = Color(
                interpolate(percentage, minColor.red, maxColor.red),
                interpolate(percentage, minColor.green, maxColor.green),
                interpolate(percentage, minColor.blue, maxColor.blue),
                interpolate(percentage, minColor.alpha, maxColor.alpha),
        )
        return color
    }

    fun interpolate(percentage: Float, min: Int, max: Int): Int {
        return floor(min * (1 - percentage) + percentage * max).toInt()
    }
}

class NethackVisualization(val tiles: List<List<Tile>>, val colorer: NethackColorer = ConstantColorer(Color.RED)) : JPanel() {
    override fun paintComponent(gr: Graphics) {
        for (i in tiles.indices) {
            for (j in tiles[i].indices) {
                gr.color = Color.BLACK
                gr.fillRect(i * 10, j * 10, 10, 10)
                val color = colorer.colorFor(i, j)
                gr.color = Color(color.red, color.green, color.blue, 220)
                gr.fillRect(i * 10, j * 10, 10, 10)
            }
        }
        for (i in tiles.indices) {
            for (j in tiles[i].indices) {
                gr.color = tiles[i][j].color
                gr.drawString(tiles[i][j].getImage(), i * 10, (j + 1) * 10)
            }
        }
    }

    override fun getPreferredSize(): Dimension? {
        return Dimension(tiles.size * 10, tiles[0].size * 10)
    }

}