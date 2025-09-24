package com.system32.systemCore.managers.processor.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Dependency(
    val coordinates: String,
    val repository: String = ""
)

