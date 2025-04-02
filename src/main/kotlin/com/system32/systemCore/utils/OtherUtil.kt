package com.system32.systemCore.utils

import com.system32.systemCore.SystemCore

class OtherUtil {
    companion object{
        val logger = SystemCore.plugin.logger

        fun fromRGB(r: Int, g: Int, b: Int): String {
            return "<color:${String.format("#%02x%02x%02x", r, g, b)}>"
        }
    }
}