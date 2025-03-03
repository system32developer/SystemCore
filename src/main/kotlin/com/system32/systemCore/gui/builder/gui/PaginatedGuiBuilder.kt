package com.system32.systemCore.gui.builder.gui

import com.system32.systemCore.gui.guis.PaginatedGui
import org.jetbrains.annotations.Contract
import java.util.function.Consumer

/**
 * GUI builder for creating a [PaginatedGui]
 */
class PaginatedBuilder : BaseChestGuiBuilder<PaginatedGui, PaginatedBuilder>() {
    private var pageSize = 0

    /**
     * Sets the desirable page size, most of the time this isn't needed
     *
     * @param pageSize The amount of free slots that page items should occupy
     * @return The current builder
     */
    @Contract("_ -> this")
    fun pageSize(pageSize: Int): PaginatedBuilder {
        this.pageSize = pageSize
        return this
    }

    /**
     * Creates a new [PaginatedGui]
     *
     * @return A new [PaginatedGui]
     */
    @Contract(" -> new")
    public override fun create(): PaginatedGui {
        val gui: PaginatedGui = PaginatedGui(createContainer(), pageSize, modifiers)

        val consumer: Consumer<PaginatedGui?>? = consumer
        consumer?.accept(gui)

        return gui
    }
}
