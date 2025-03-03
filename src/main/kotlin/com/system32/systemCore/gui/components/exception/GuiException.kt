package com.system32.systemCore.gui.components.exception

class GuiException : RuntimeException {
    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Exception?) : super(message, cause)
}