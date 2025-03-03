package com.system32.systemCore.gui.components


interface Serializable {
    fun encodeGui(): MutableList<String?>?

    fun decodeGui(gui: MutableList<String?>)
}
