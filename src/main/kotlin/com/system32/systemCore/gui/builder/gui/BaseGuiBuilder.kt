package com.system32.systemCore.gui.builder.gui

import com.system32.systemCore.gui.components.InteractionModifier
import com.system32.systemCore.gui.components.exception.GuiException
import com.system32.systemCore.gui.guis.BaseGui


/**
 * The base for all the GUI builders this is due to some limitations
 * where some builders will have unique features based on the GUI type
 *
 * @param <G> The Type of [BaseGui]
</G> */
abstract class BaseGuiBuilder<G : BaseGui?, B : BaseGuiBuilder<G, B>?> {

    private lateinit var title: net.kyori.adventure.text.Component
    private val interactionModifiers: java.util.EnumSet<InteractionModifier> = java.util.EnumSet.noneOf(InteractionModifier::class.java)

    /**
     * Getter for the consumer
     *
     * @return The consumer
     */
    protected var consumer: java.util.function.Consumer<G?>? = null
        private set

    /**
     * Sets the title for the GUI
     * This will be either a Component or a String
     *
     * @param title The GUI title
     * @return The builder
     */
    @org.jetbrains.annotations.Contract("_ -> this") fun title(title: net.kyori.adventure.text.Component): B {
        this.title = title
        return this as B
    }

    /**
     * Disable item placement inside the GUI
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @org.jetbrains.annotations.Contract(" -> this") fun disableItemPlace(): B {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_PLACE)
        return this as B
    }

    /**
     * Disable item retrieval inside the GUI
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @org.jetbrains.annotations.Contract(" -> this") fun disableItemTake(): B {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_TAKE)
        return this as B
    }

    /**
     * Disable item swap inside the GUI
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @org.jetbrains.annotations.Contract(" -> this") fun disableItemSwap(): B {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_SWAP)
        return this as B
    }

    /**
     * Disable item drop inside the GUI
     *
     * @return The builder
     * @since 3.0.3
     */
    @org.jetbrains.annotations.Contract(" -> this") fun disableItemDrop(): B {
        interactionModifiers.add(InteractionModifier.PREVENT_ITEM_DROP)
        return this as B
    }

    /**
     * Disable other GUI actions
     * This option pretty much disables creating a clone stack of the item
     *
     * @return The builder
     * @since 3.0.4
     */
    @org.jetbrains.annotations.Contract(" -> this") fun disableOtherActions(): B {
        interactionModifiers.add(InteractionModifier.PREVENT_OTHER_ACTIONS)
        return this as B
    }

    /**
     * Disable all the modifications of the GUI, making it immutable by player interaction
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @org.jetbrains.annotations.Contract(" -> this") fun disableAllInteractions(): B {
        interactionModifiers.addAll(InteractionModifier.VALUES)
        return this as B
    }

    /**
     * Allows item placement inside the GUI
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @org.jetbrains.annotations.Contract(" -> this") fun enableItemPlace(): B {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_PLACE)
        return this as B
    }

    /**
     * Allow items to be taken from the GUI
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @org.jetbrains.annotations.Contract(" -> this") fun enableItemTake(): B {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_TAKE)
        return this as B
    }

    /**
     * Allows item swap inside the GUI
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @org.jetbrains.annotations.Contract(" -> this") fun enableItemSwap(): B {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_SWAP)
        return this as B
    }

    /**
     * Allows item drop inside the GUI
     *
     * @return The builder
     * @since 3.0.3
     */
    @org.jetbrains.annotations.Contract(" -> this") fun enableItemDrop(): B {
        interactionModifiers.remove(InteractionModifier.PREVENT_ITEM_DROP)
        return this as B
    }

    /**
     * Enable other GUI actions
     * This option pretty much enables creating a clone stack of the item
     *
     * @return The builder
     * @since 3.0.4
     */
    @org.jetbrains.annotations.Contract(" -> this") fun enableOtherActions(): B {
        interactionModifiers.remove(InteractionModifier.PREVENT_OTHER_ACTIONS)
        return this as B
    }

    /**
     * Enable all modifications of the GUI, making it completely mutable by player interaction
     *
     * @return The builder
     * @author SecretX
     * @since 3.0.0
     */
    @org.jetbrains.annotations.Contract(" -> this") fun enableAllInteractions(): B {
        interactionModifiers.clear()
        return this as B
    }

    /**
     * Applies anything to the GUI once it's created
     * Can be pretty useful for setting up small things like default actions
     *
     * @param consumer A [Consumer] that passes the built GUI
     * @return The builder
     */
    @org.jetbrains.annotations.Contract("_ -> this") fun apply(consumer: java.util.function.Consumer<G?>): B {
        this.consumer = consumer
        return this as B
    }

    /**
     * Creates the given GuiBase
     * Has to be abstract because each GUI are different
     *
     * @return The new [BaseGui]
     */
    @org.jetbrains.annotations.Contract(" -> new") abstract fun create(): G

    /**
     * Getter for the title
     *
     * @return The current title
     */
    protected fun getTitle(): net.kyori.adventure.text.Component {
        if (title == null) {
            throw GuiException("GUI title is missing!")
        }

        return title
    }


    protected val modifiers: MutableSet<InteractionModifier>
        /**
         * Getter for the set of interaction modifiers
         *
         * @return The set of [InteractionModifier]
         * @author SecretX
         * @since 3.0.0
         */
        get() = interactionModifiers

    protected fun consumeBuilder(builder: BaseGuiBuilder<*, *>) {
        this.title = builder.title
        this.interactionModifiers.addAll(builder.interactionModifiers)
    }
}
