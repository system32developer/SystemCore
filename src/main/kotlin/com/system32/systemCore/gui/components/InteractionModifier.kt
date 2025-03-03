package com.system32.systemCore.gui.components

import java.util.*

enum class InteractionModifier {
    PREVENT_ITEM_PLACE,
    PREVENT_ITEM_TAKE,
    PREVENT_ITEM_SWAP,
    PREVENT_ITEM_DROP,
    PREVENT_OTHER_ACTIONS;

    companion object {
        val VALUES: MutableSet<InteractionModifier> =
            Collections.unmodifiableSet<InteractionModifier>(EnumSet.allOf<InteractionModifier?>(InteractionModifier::class.java))
    }
}
