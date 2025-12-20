package com.system32dev.systemCore.processor.model

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import kotlin.reflect.KClass

abstract class ProcessorSpec(
    val annotation: KClass<out Annotation>,
    val template: String,
    val packageName: String = "com.system32dev.systemCore.generated",
    val generateWhenEmpty: Boolean = false
) {

    open fun validate(symbol: KSClassDeclaration, logger: KSPLogger): Boolean = true

    abstract fun collect(symbols: List<KSClassDeclaration>): Map<String, String>
}
