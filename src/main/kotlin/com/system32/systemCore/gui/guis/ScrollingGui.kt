package com.system32.systemCore.gui.guis

import com.system32.systemCore.gui.components.GuiContainer
import com.system32.systemCore.gui.components.InteractionModifier
import com.system32.systemCore.gui.components.ScrollType
import org.bukkit.entity.HumanEntity

/**
 * GUI that allows you to scroll through items
 */
@Suppress("unused")
class ScrollingGui(
    guiContainer: GuiContainer,
    pageSize: Int,
    private val scrollType: ScrollType,
    interactionModifiers: MutableSet<InteractionModifier>
) : PaginatedGui(guiContainer, pageSize, interactionModifiers) {
    private var scrollSize = 0

    /**
     * Overrides [PaginatedGui.next] to make it work with the specific scrolls
     */
     override fun next(): Boolean {
        if (pagesNum * scrollSize + pageSize >= getPageItems().size + scrollSize) return false

        pagesNum += 1
        updatePage()
        return true
    }

    /**
     * Overrides [PaginatedGui.previous] to make it work with the specific scrolls
     */
     override fun previous(): Boolean {
        if(pagesNum <= 1) return false

        pagesNum -= 1
        updatePage()
        return true
    }

    /**
     * Overrides [PaginatedGui.open] to make it work with the specific scrolls
     *
     * @param player The [HumanEntity] to open the GUI to
     */
     override fun open(player: HumanEntity) {
        open(player, 1)
    }

    /**
     * Overrides [PaginatedGui.open] to make it work with the specific scrolls
     *
     * @param player   The [HumanEntity] to open the GUI to
     * @param openPage The page to open on
     */
     override fun open(player: HumanEntity, openPage: Int) {
        if (player.isSleeping) return
        inventory.clear()
        mutableCurrentPageItems.clear()

        populateGui()

        if (pageSize == 0) setPageSize(calculatePageSize())
        if (scrollSize == 0) scrollSize = calculateScrollSize()
        if (openPage > 0 && (openPage * scrollSize + pageSize <= getPageItems().size + scrollSize)) {
            pagesNum = openPage
        }

        populatePage()

        player.openInventory(getInventory())
    }

    /**
     * Overrides [PaginatedGui.updatePage] to make it work with the specific scrolls
     */
     override fun updatePage() {
        clearPage()
        populatePage()
    }

    /**
     * Fills the page with the items
     */
    private fun populatePage() {
        // Adds the paginated items to the page
        for (guiItem in getPage(pagesNum)) {
            if (scrollType === ScrollType.HORIZONTAL) {
                putItemHorizontally(guiItem)
                continue
            }

            putItemVertically(guiItem)
        }
    }

    /**
     * Calculates the size of each scroll
     *
     * @return The size of he scroll
     */
    private fun calculateScrollSize(): Int {
        var counter = 0

        if (scrollType === ScrollType.VERTICAL) {
            var foundCol = false

            for (row in 1..rows) {
                for (col in 1..9) {
                    val slot = getSlotFromRowCol(row, col)
                    if (getInventory().getItem(slot) == null) {
                        if (!foundCol) foundCol = true
                        counter++
                    }
                }

                if (foundCol) return counter
            }

            return counter
        }

        var foundRow = false

        for (col in 1..9) {
            for (row in 1..rows) {
                val slot = getSlotFromRowCol(row, col)
                if (getInventory().getItem(slot) == null) {
                    if (!foundRow) foundRow = true
                    counter++
                }
            }

            if (foundRow) return counter
        }

        return counter
    }

    /**
     * Puts the item in the GUI for horizontal scrolling
     *
     * @param guiItem The gui item
     */
    private fun putItemVertically(guiItem: GuiItem) {
        for (slot in 0..<rows * 9) {
            if (getGuiItem(slot) != null || inventory.getItem(slot) != null) continue
            mutableCurrentPageItems.put(slot, guiItem)
            getInventory().setItem(slot, guiItem.itemStack)
            break
        }
    }

    /**
     * Puts item into the GUI for vertical scrolling
     *
     * @param guiItem The gui item
     */
    private fun putItemHorizontally(guiItem: GuiItem) {
        for (col in 1..9) {
            for (row in 1..rows) {
                val slot = getSlotFromRowCol(row, col)
                if (getGuiItem(slot) != null || getInventory().getItem(slot) != null) continue
                mutableCurrentPageItems.put(slot, guiItem)
                getInventory().setItem(slot, guiItem.itemStack)
                return
            }
        }
    }

    /**
     * Gets the items from the current page
     *
     * @param givenPage The page number
     * @return A list with all the items
     */
    private fun getPage(givenPage: Int): MutableList<GuiItem> {
        val page = givenPage - 1
        val pageItemsSize: Int = getPageItems().size

        val guiPage: MutableList<GuiItem> = ArrayList<GuiItem>()

        var max: Int = page * scrollSize + pageSize
        if (max > pageItemsSize) max = pageItemsSize

        for (i in page * scrollSize..<max) {
            guiPage.add(getPageItems().get(i))
        }

        return guiPage
    }
}
