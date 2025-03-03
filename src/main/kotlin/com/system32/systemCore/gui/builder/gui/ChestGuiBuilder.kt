package com.system32.systemCore.gui.builder.gui

import com.system32.systemCore.gui.components.GuiType
import com.system32.systemCore.gui.guis.Gui
import org.jetbrains.annotations.Contract
import java.util.function.Consumer

/**
 * The simple GUI builder is used for creating a [Gui]
 */
class ChestGuiBuilder : BaseChestGuiBuilder<Gui?, ChestGuiBuilder>() {
    /**
     * Sets the [GuiType] to use on the GUI
     * This method is unique to the simple GUI
     *
     * @param guiType The [GuiType]
     * @return The current builder
     */
    @Contract("_ -> new")
    fun type(guiType: GuiType): TypedGuiBuilder {
        return TypedGuiBuilder(guiType, this)
    }

    /**
     * Creates a new [Gui]
     *
     * @return A new [Gui]
     */
    @Contract(" -> new")
    public override fun create(): Gui {
        val gui: Gui = Gui(createContainer(), modifiers)
        val consumer: Consumer<Gui?>? = consumer
        consumer?.accept(gui)
        return gui
    }
}
