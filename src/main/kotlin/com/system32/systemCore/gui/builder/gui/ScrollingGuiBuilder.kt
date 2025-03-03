package com.system32.systemCore.gui.builder.gui

import com.system32.systemCore.gui.components.ScrollType
import com.system32.systemCore.gui.guis.ScrollingGui

import org.jetbrains.annotations.Contract
import java.util.function.Consumer

class ScrollingBuilder(scrollType: ScrollType) : BaseChestGuiBuilder<ScrollingGui, ScrollingBuilder>() {
    private var scrollType: ScrollType
    private var pageSize = 0

    /**
     * Main constructor
     *
     * @param scrollType The [ScrollType] to default to
     */
    init {
        this.scrollType = scrollType
    }

    /**
     * Sets the [ScrollType] to be used
     *
     * @param scrollType Either horizontal or vertical scrolling
     * @return The current builder
     */
    @Contract("_ -> this")
    fun scrollType(scrollType: ScrollType): ScrollingBuilder {
        this.scrollType = scrollType
        return this
    }

    /**
     * Sets the desirable page size, most of the times this isn't needed
     *
     * @param pageSize The amount of free slots that page items should occupy
     * @return The current builder
     */
    @Contract("_ -> this")
    fun pageSize(pageSize: Int): ScrollingBuilder {
        this.pageSize = pageSize
        return this
    }

    /**
     * Creates a new [ScrollingGui]
     *
     * @return A new [ScrollingGui]
     */
    @Contract(" -> new")
    override fun create(): ScrollingGui {
        val gui: ScrollingGui = ScrollingGui(createContainer(), pageSize, scrollType, modifiers)

        val consumer: Consumer<ScrollingGui?>? = consumer
        consumer?.accept(gui)

        return gui
    }
}
