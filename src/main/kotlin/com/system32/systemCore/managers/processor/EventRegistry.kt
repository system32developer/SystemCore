package com.system32.systemCore.managers.processor

import com.system32.systemCore.SystemCore
import com.system32.systemCore.SystemCore.event
import com.system32.systemCore.managers.processor.annotations.Event
import org.bukkit.event.Listener

object EventRegistry {
    private val listeners : List<Any> = listOf(

    )

    fun register(){
        val plugin = SystemCore.plugin
        listeners.forEach { obj ->
            val listener = object : Listener {}
            val clazz = obj::class.java
            for (method in clazz.declaredMethods) {
                val ann = method.getAnnotation(Event::class.java) ?: continue
                val params = method.parameterTypes
                if(params.size == 1 && Event::class.java.isAssignableFrom(params[0])){
                    val eventClass = params[0] as Class<out org.bukkit.event.Event>
                    plugin.server.pluginManager.registerEvent(eventClass, listener, ann.priority, { _, event ->
                        if(eventClass.isInstance(event)){
                            method.isAccessible = true
                            method.invoke(obj, eventClass.cast(event))
                        }
                    }, plugin, ann.ignoreCancelled)
                }
            }
        }
    }
}