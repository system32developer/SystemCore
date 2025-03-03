package com.system32.systemCore.gui.builder.gui

import com.system32.systemCore.gui.components.GuiContainer.Typed
import com.system32.systemCore.gui.components.GuiType
import com.system32.systemCore.gui.components.InventoryProvider
import com.system32.systemCore.gui.components.util.Legacy
import com.system32.systemCore.gui.guis.Gui
import org.bukkit.Bukkit
import org.jetbrains.annotations.Contract
import java.util.function.Consumer

/**
 * The simple GUI builder is used for creating a [Gui]
 */
class TypedGuiBuilder : BaseGuiBuilder<Gui, TypedGuiBuilder> {
    private var guiType: GuiType
    private var inventoryProvider: InventoryProvider.Typed = InventoryProvider.Typed { title, owner, type ->
        Bukkit.createInventory(
            owner,
            type,
            Legacy.SERIALIZER.serialize(title!!)
        )
    }

    /**
     * Main constructor
     *
     * @param guiType The [GuiType] to default to
     */
    constructor(guiType: GuiType) {
        this.guiType = guiType
    }

    constructor(guiType: GuiType, builder: ChestGuiBuilder) {
        this.guiType = guiType
        consumeBuilder(builder)
    }

    /**
     * Sets the [GuiType] to use on the GUI
     * This method is unique to the simple GUI
     *
     * @param guiType The [GuiType]
     * @return The current builder
     */
    @Contract("_ -> this")
    fun type(guiType: GuiType): TypedGuiBuilder {
        this.guiType = guiType
        return this
    }

    @Contract("_ -> this")
    fun inventory(inventoryProvider: InventoryProvider.Typed): TypedGuiBuilder {
        this.inventoryProvider = inventoryProvider
        return this
    }

    /**
     * Creates a new [Gui]
     *
     * @return A new [Gui]
     */
    @Contract(" -> new")
    public override fun create(): Gui {
        val gui: Gui = Gui(Typed(getTitle(), inventoryProvider, guiType), modifiers)
        val consumer: Consumer<Gui?>? = consumer
        consumer?.accept(gui)
        return gui
    }
}
