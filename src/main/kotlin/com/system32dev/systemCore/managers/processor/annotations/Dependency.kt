package com.system32dev.systemCore.managers.processor.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Dependency(
    val coordinates: String,
    val repository: String = ""
)

