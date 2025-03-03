package com.system32.systemCore.gui.builder.gui

import com.system32.systemCore.gui.guis.StorageGui
import org.jetbrains.annotations.Contract
import java.util.function.Consumer

/**
 * The simple GUI builder is used for creating a [StorageGui]
 */
class StorageBuilder : BaseChestGuiBuilder<StorageGui, StorageBuilder>() {
    /**
     * Creates a new [StorageGui]
     *
     * @return A new [StorageGui]
     */
    @Contract(" -> new")
    public override fun create(): StorageGui {
        val gui: StorageGui = StorageGui(createContainer(), modifiers)

        val consumer: Consumer<StorageGui?>? = consumer
        consumer?.accept(gui)

        return gui
    }
}
