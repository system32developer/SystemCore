package com.system32.systemCore.gui.components.util

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer


class Legacy private constructor() {
    init {
        throw UnsupportedOperationException("Class should not be instantiated!")
    }

    companion object {

        val SERIALIZER: LegacyComponentSerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build()
    }
}
