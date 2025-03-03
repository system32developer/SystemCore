package com.system32.systemCore.gui.components.util

import com.system32.systemCore.gui.components.GuiType
import com.system32.systemCore.gui.components.exception.GuiException
import com.system32.systemCore.gui.guis.BaseGui
import com.system32.systemCore.gui.guis.GuiItem
import com.system32.systemCore.gui.guis.PaginatedGui
import java.util.Collections
import java.util.function.Consumer
import kotlin.math.max
import kotlin.math.min

class GuiFiller(gui: BaseGui) {
    private val gui: BaseGui

    init {
        this.gui = gui
    }
    
    fun fillTop(guiItem: GuiItem) {
        fillTop(mutableListOf<GuiItem>(guiItem))
    }
    
    fun fillTop(guiItems: MutableList<GuiItem>) {
        val items: MutableList<GuiItem> = repeatList(guiItems)
        for (i in 0..8) {
            if (!gui.getGuiItems().containsKey(i)) gui.setItem(i, items[i])
        }
    }
    
    fun fillBottom(guiItem: GuiItem) {
        fillBottom(mutableListOf<GuiItem>(guiItem))
    }
    
    fun fillBottom(guiItems: MutableList<GuiItem>) {
        val rows: Int = gui.rows
        val items: MutableList<GuiItem> = repeatList(guiItems)
        for (i in 9 downTo 1) {
            if (gui.getGuiItems()[(rows * 9) - i] == null) {
                gui.setItem((rows * 9) - i, items[i])
            }
        }
    }
    
    fun fillBorder(guiItem: GuiItem) {
        fillBorder(mutableListOf<GuiItem>(guiItem))
    }
    
    fun fillBorder(guiItems: MutableList<GuiItem>) {
        val rows: Int = gui.rows
        if (rows <= 2) return

        val items: MutableList<GuiItem> = repeatList(guiItems)

        for (i in 0..<rows * 9) {
            if ((i <= 8)
                || (i >= (rows * 9) - 8) && (i <= (rows * 9) - 2) || i % 9 == 0 || i % 9 == 8
            ) gui.setItem(i, items[i])
        }
    }
    
    fun fillBetweenPoints(rowFrom: Int, colFrom: Int, rowTo: Int, colTo: Int, guiItem: GuiItem) {
        fillBetweenPoints(rowFrom, colFrom, rowTo, colTo, mutableListOf<GuiItem>(guiItem))
    }
    
    fun fillBetweenPoints(rowFrom: Int, colFrom: Int, rowTo: Int, colTo: Int, guiItems: MutableList<GuiItem>) {
        val minRow = min(rowFrom.toDouble(), rowTo.toDouble()).toInt()
        val maxRow = max(rowFrom.toDouble(), rowTo.toDouble()).toInt()
        val minCol = min(colFrom.toDouble(), colTo.toDouble()).toInt()
        val maxCol = max(colFrom.toDouble(), colTo.toDouble()).toInt()

        val rows: Int = gui.rows
        val items: MutableList<GuiItem> = repeatList(guiItems)

        for (row in 1..rows) {
            for (col in 1..9) {
                val slot = getSlotFromRowCol(row, col)
                if (!((row >= minRow && row <= maxRow) && (col >= minCol && col <= maxCol))) continue

                gui.setItem(slot, items[slot])
            }
        }
    }
    
    fun fill(guiItem: GuiItem) {
        fill(mutableListOf<GuiItem>(guiItem))
    }


    fun fill(guiItems: MutableList<GuiItem>) {
        if (gui is PaginatedGui) {
            throw GuiException("Full filling a GUI is not supported in a Paginated GUI!")
        }

        val type: GuiType = gui.guiType()
        val fill: Int = if (type === GuiType.CHEST) {
            gui.rows * type.limit
        } else {
            type.fillSize
        }

        val items: MutableList<GuiItem> = repeatList(guiItems)
        for (i in 0..<fill) {
            if (gui.getGuiItems()[i] == null) gui.setItem(i, items[i])
        }
    }

    fun fillSide(side: Side, guiItems: MutableList<GuiItem>) {
        when (side) {
            Side.LEFT -> this.fillBetweenPoints(1, 1, gui.rows, 1, guiItems)
            Side.RIGHT -> this.fillBetweenPoints(1, 9, gui.rows, 9, guiItems)
            Side.BOTH -> {
                this.fillSide(Side.LEFT, guiItems)
                this.fillSide(Side.RIGHT, guiItems)
            }
        }
    }

    private fun repeatList(guiItems: MutableList<GuiItem>): MutableList<GuiItem> {
        val repeated: MutableList<GuiItem> = ArrayList<GuiItem>()
        Collections.nCopies<MutableList<GuiItem>>(gui.rows * 9, guiItems)
            .forEach(Consumer { c: MutableList<GuiItem> -> repeated.addAll(c) })
        return repeated
    }

    private fun getSlotFromRowCol(row: Int, col: Int): Int {
        return (col + (row - 1) * 9) - 1
    }

    enum class Side {
        LEFT,
        RIGHT,
        BOTH
    }
}
