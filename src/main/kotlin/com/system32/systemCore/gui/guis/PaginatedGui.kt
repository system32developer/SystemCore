package com.system32.systemCore.gui.guis

import com.system32.systemCore.gui.components.GuiContainer
import com.system32.systemCore.gui.components.InteractionModifier
import net.kyori.adventure.text.Component
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.function.Consumer
import kotlin.math.ceil


/**
 * GUI that allows you to have multiple pages
 */
@Suppress("unused")
open class PaginatedGui(
    guiContainer: GuiContainer,
    /**
     * Gets the page size
     *
     * @return The page size
     */
    var pageSize: Int, interactionModifiers: MutableSet<InteractionModifier>
) : BaseGui(guiContainer, interactionModifiers) {
    // List with all the page items
    private val pageItems: MutableList<GuiItem?> = ArrayList<GuiItem?>()

    /**
     * Gets the current page items to be used on other gui types
     *
     * @return The [Map] with all the [.currentPage]
     */
    // Saves the current page items and it's slot
    val mutableCurrentPageItems: MutableMap<Int?, GuiItem>

    /**
     * Gets the page number
     *
     * @return The current page number
     */
    /**
     * Sets the page number
     *
     * @param this.currentPageNum Sets the current page to be the specified number
     */
    /**
     * Gets the current page number
     *
     * @return The current page number
     */
    var currentPageNum: Int = 1


    init {
        this.mutableCurrentPageItems = LinkedHashMap<Int?, GuiItem>()
    }

    /**
     * Sets the page size
     *
     * @param pageSize The new page size
     * @return The GUI for easier use when declaring, works like a builder
     */
    fun setPageSize(pageSize: Int): BaseGui? {
        this.pageSize = pageSize
        return this
    }

    /**
     * Adds an [GuiItem] to the next available slot in the page area
     *
     * @param item The [GuiItem] to add to the page
     */
    fun addItem(item: GuiItem) {
        pageItems.add(item)
    }

    /**
     * Overridden [BaseGui.addItem] to add the items to the page instead
     *
     * @param items Varargs for specifying the [GuiItem]s
     */
    public override fun addItem(vararg items: GuiItem) {
        pageItems.addAll(Arrays.asList<GuiItem?>(*items))
    }

    /**
     * Overridden [BaseGui.update] to use the paginated open
     */
    public override fun update() {
        getInventory().clear()
        populateGui()

        updatePage()
    }

    /**
     * Updates the page [GuiItem] on the slot in the page
     * Can get the slot from [InventoryClickEvent.getSlot]
     *
     * @param slot      The slot of the item to update
     * @param itemStack The new [ItemStack]
     */
    fun updatePageItem(slot: Int, itemStack: ItemStack) {
        if (!mutableCurrentPageItems.containsKey(slot)) return
        val guiItem: GuiItem = mutableCurrentPageItems[slot]!!
        guiItem.itemStack = itemStack
        inventory.setItem(slot, guiItem.itemStack)
    }

    /**
     * Alternative [.updatePageItem] that uses *ROWS* and *COLUMNS* instead
     *
     * @param row       The row of the slot
     * @param col       The columns of the slot
     * @param itemStack The new [ItemStack]
     */
    fun updatePageItem(row: Int, col: Int, itemStack: ItemStack) {
        updateItem(getSlotFromRowCol(row, col), itemStack)
    }

    /**
     * Alternative [.updatePageItem] that uses [GuiItem] instead
     *
     * @param slot The slot of the item to update
     * @param item The new ItemStack
     */
    fun updatePageItem(slot: Int, item: GuiItem) {
        if (!mutableCurrentPageItems.containsKey(slot)) return
        // Gets the old item and its index on the main items list
        val oldItem = mutableCurrentPageItems.get(slot)
        val index = pageItems.indexOf(mutableCurrentPageItems.get(slot))

        // Updates both lists and inventory
        mutableCurrentPageItems.put(slot, item)
        pageItems[index] = item
        inventory.setItem(slot, item.itemStack)
    }

    /**
     * Alternative [.updatePageItem] that uses *ROWS* and *COLUMNS* instead
     *
     * @param row  The row of the slot
     * @param col  The columns of the slot
     * @param item The new [GuiItem]
     */
    fun updatePageItem(row: Int, col: Int, item: GuiItem) {
        updateItem(getSlotFromRowCol(row, col), item)
    }

    /**
     * Removes a given [GuiItem] from the page.
     *
     * @param item The [GuiItem] to remove.
     */
    fun removePageItem(item: GuiItem) {
        pageItems.remove(item)
        updatePage()
    }

    /**
     * Removes a given [ItemStack] from the page.
     *
     * @param item The [ItemStack] to remove.
     */
    fun removePageItem(item: ItemStack) {
        val guiItem = pageItems.stream().filter { it: GuiItem? -> it!!.itemStack == item }.findFirst()
        guiItem.ifPresent(Consumer { item: GuiItem? -> this.removePageItem(item!!) })
    }

    /**
     * Overrides [BaseGui.open] to use the paginated populator instead
     *
     * @param player The [HumanEntity] to open the GUI to
     */
    public override fun open(player: HumanEntity) {
        open(player, 1)
    }

    /**
     * Specific open method for the Paginated GUI
     * Uses [.populatePage]
     *
     * @param player   The [HumanEntity] to open it to
     * @param openPage The specific page to open at
     */
    open fun open(player: HumanEntity, openPage: Int) {
        if (player.isSleeping()) return
        if (openPage <= this.pagesNum || openPage > 0) this.currentPageNum = openPage

        getInventory().clear()
        mutableCurrentPageItems.clear()

        populateGui()

        if (pageSize == 0) pageSize = calculatePageSize()

        populatePage()

        player.openInventory(getInventory())
    }

    /**
     * Overrides [BaseGui.updateTitle] to use the paginated populator instead
     * Updates the title of the GUI
     * *This method may cause LAG if used on a loop*
     *
     * @param title The title to set
     * @return The GUI for easier use when declaring, works like a builder
     */
    public override fun updateTitle(title: Component): BaseGui {
        isUpdating = true

        val viewers: MutableList<HumanEntity> = ArrayList<HumanEntity>(inventory.viewers)
        val guiContainer: GuiContainer = guiContainer()

        guiContainer.title(title)
        setInventory(guiContainer.createInventory(this))

        for (player in viewers) {
            open(player, this.currentPageNum)
        }

        isUpdating = false
        return this
    }

    val currentPageItems: MutableMap<Int, GuiItem>
        /**
         * Gets an immutable [Map] with all the current pages items
         *
         * @return The [Map] with all the [.currentPage]
         */
        get() = Collections.unmodifiableMap<Int, GuiItem?>(this.mutableCurrentPageItems)

    /**
     * Gets an immutable [List] with all the page items added to the GUI
     *
     * @return The  [List] with all the [.pageItems]
     */
    fun getPageItems(): MutableList<GuiItem> {
        return Collections.unmodifiableList<GuiItem?>(pageItems)
    }


    val nextPageNum: Int
        /**
         * Gets the next page number
         *
         * @return The next page number or [.pageNum] if no next is present
         */
        get() {
            if (this.currentPageNum + 1 > this.pagesNum) return this.currentPageNum
            return this.currentPageNum + 1
        }

    val prevPageNum: Int
        /**
         * Gets the previous page number
         *
         * @return The previous page number or [.pageNum] if no previous is present
         */
        get() {
            if (this.currentPageNum - 1 == 0) return this.currentPageNum
            return this.currentPageNum - 1
        }

    /**
     * Goes to the next page
     *
     * @return False if there is no next page.
     */
    open fun next(): Boolean {
        if (this.currentPageNum + 1 > this.pagesNum) return false

        this.currentPageNum++
        updatePage()
        return true
    }

    /**
     * Goes to the previous page if possible
     *
     * @return False if there is no previous page.
     */
    open fun previous(): Boolean {
        if (this.currentPageNum - 1 == 0) return false

        this.currentPageNum--
        updatePage()
        return true
    }

    /**
     * Gets the page item for the GUI listener
     *
     * @param slot The slot to get
     * @return The GuiItem on that slot
     */
    fun getPageItem(slot: Int): GuiItem? {
        return mutableCurrentPageItems.get(slot)
    }

    /**
     * Gets the items in the page
     *
     * @param givenPage The page to get
     * @return A list with all the page items
     */
    private fun getPageNum(givenPage: Int): MutableList<GuiItem> {
        val page = givenPage - 1

        val guiPage: MutableList<GuiItem> = ArrayList<GuiItem>()

        var max = ((page * pageSize) + pageSize)
        if (max > pageItems.size) max = pageItems.size

        for (i in page * pageSize..<max) {
            guiPage.add(pageItems[i]!!)
        }

        return guiPage
    }

    var pagesNum: Int = 0
        /**
         * Gets the number of pages the GUI has
         *
         * @return The pages number
         */
        get() {
            if (pageSize == 0) pageSize = calculatePageSize()
            return ceil(pageItems.size.toDouble() / pageSize).toInt()
        }

    /**
     * Populates the inventory with the page items
     */
    private fun populatePage() {
        // Adds the paginated items to the page
        var slot = 0
        val inventorySize = inventory.size
        val iterator = getPageNum(this.currentPageNum).iterator()
        while (iterator.hasNext()) {
            if (slot >= inventorySize) {
                break // Exit the loop if slot exceeds inventory size
            }

            if (getGuiItem(slot) != null || inventory.getItem(slot) != null) {
                slot++
                continue
            }

            val guiItem = iterator.next()

            mutableCurrentPageItems.put(slot, guiItem)
            inventory.setItem(slot, guiItem.itemStack)
            slot++
        }
    }

    /**
     * Clears the page content
     */
    fun clearPage() {
        for (entry in mutableCurrentPageItems.entries) {
            getInventory().setItem(entry.key!!, null)
        }
    }

    /**
     * Clears all previously added page items
     */
    @JvmOverloads
    fun clearPageItems(update: Boolean = false) {
        pageItems.clear()
        if (update) update()
    }


    /**
     * Updates the page content
     */
    open fun updatePage() {
        clearPage()
        populatePage()
    }

    /**
     * Calculates the size of the give page
     *
     * @return The page size
     */
    fun calculatePageSize(): Int {
        var counter = 0

        for (slot in 0..<rows * 9) {
            if (getGuiItem(slot) == null) counter++
        }

        if (counter == 0) return 1
        return counter
    }
}