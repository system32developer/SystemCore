package com.system32.systemCore.gui.guis


import com.system32.systemCore.gui.builder.gui.ChestGuiBuilder
import com.system32.systemCore.gui.builder.gui.PaginatedBuilder
import com.system32.systemCore.gui.builder.gui.ScrollingBuilder
import com.system32.systemCore.gui.builder.gui.StorageBuilder
import com.system32.systemCore.gui.builder.gui.TypedGuiBuilder
import com.system32.systemCore.gui.components.GuiContainer
import com.system32.systemCore.gui.components.GuiType
import com.system32.systemCore.gui.components.InteractionModifier
import com.system32.systemCore.gui.components.ScrollType
import org.jetbrains.annotations.Contract

/**
 * Standard GUI implementation of [BaseGui]
 */
class Gui(guiContainer: GuiContainer, interactionModifiers: MutableSet<InteractionModifier>) :
    BaseGui(guiContainer, interactionModifiers) {
    companion object {
        /**
         * Creates a [TypedGuiBuilder] to build a [dev.triumphteam.gui.guis.Gui]
         *
         * @param type The [GuiType] to be used
         * @return A [TypedGuiBuilder]
         * @since 3.0.0
         */
        @Contract("_ -> new")
        fun gui(type: GuiType): TypedGuiBuilder {
            return TypedGuiBuilder(type)
        }

        /**
         * Creates a [ChestGuiBuilder] with CHEST as the [GuiType]
         *
         * @return A CHEST [ChestGuiBuilder]
         * @since 3.0.0
         */
        @Contract(" -> new")
        fun gui(): ChestGuiBuilder {
            return ChestGuiBuilder()
        }

        /**
         * Creates a [StorageBuilder].
         *
         * @return A CHEST [StorageBuilder].
         * @since 3.0.0.
         */
        @Contract(" -> new")
        fun storage(): StorageBuilder {
            return StorageBuilder()
        }

        /**
         * Creates a [PaginatedBuilder] to build a [dev.triumphteam.gui.guis.PaginatedGui]
         *
         * @return A [PaginatedBuilder]
         * @since 3.0.0
         */
        @Contract(" -> new")
        fun paginated(): PaginatedBuilder {
            return PaginatedBuilder()
        }

        /**
         * Creates a [ScrollingBuilder] to build a [dev.triumphteam.gui.guis.ScrollingGui]
         *
         * @param scrollType The [ScrollType] to be used by the GUI
         * @return A [ScrollingBuilder]
         * @since 3.0.0
         */
        @Contract("_ -> new")
        fun scrolling(scrollType: ScrollType): ScrollingBuilder {
            return ScrollingBuilder(scrollType)
        }

        /**
         * Creates a [ScrollingBuilder] with VERTICAL as the [ScrollType]
         *
         * @return A vertical [ChestGuiBuilder]
         * @since 3.0.0
         */
        @Contract(" -> new")
        fun scrolling(): ScrollingBuilder {
            return scrolling(ScrollType.VERTICAL)
        }
    }
}
