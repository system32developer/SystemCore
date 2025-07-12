package com.system32.systemCore.managers.config.serializers

import com.system32.systemCore.database.model.Driver
import com.system32.systemCore.managers.config.serializers.models.Feedback
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

class FeedbackSerializer  : TypeSerializer<Feedback> {

    override fun deserialize(type: Type?, node: ConfigurationNode?): Feedback? {
        if (node == null || node.string == null) return null
        return Feedback(if(node.isList) node.getList(String::class.java)!! else listOf(node.string?: "No message provided"))
    }

    override fun serialize(type: Type?, obj: Feedback?, node: ConfigurationNode?) {
        if (node == null) return
        if (obj == null) {
            node.set(null)
            println("No Component to save, setting node to null")
            Driver.H2
            return
        }

        if (!obj.isList) node.set(obj.message[0]) else node.setList(String::class.java, obj.message)
    }
}