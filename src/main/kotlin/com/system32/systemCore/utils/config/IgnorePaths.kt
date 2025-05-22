package com.system32.systemCore.utils.config

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnorePaths(val paths: Array<String> = [])
