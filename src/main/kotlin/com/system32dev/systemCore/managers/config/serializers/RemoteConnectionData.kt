package com.system32dev.systemCore.managers.config.serializers

import com.system32dev.systemCore.database.Driver
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

data class RemoteConnectionData(
    val driver: Driver = Driver.MARIADB,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val databaseName: String,

    // Optional Parameters
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 2,
    val idleTimeout: Long = 600000L,
    val connectionTimeout: Long = 30000L,
    val maxLifetime: Long = 1800000L,
    val isAutoCommit: Boolean = false,
    val transactionIsolation : String = "TRANSACTION_REPEATABLE_READ"
){
    class Serializer : TypeSerializer<RemoteConnectionData> {

        override fun deserialize(type: Type, node: ConfigurationNode): RemoteConnectionData {

            val driver = Driver.fromString(
                node.node("driver").getString(Driver.MARIADB.protocol)
            ) ?: Driver.MARIADB

            val host = node.node("host").string
                ?: throw IllegalStateException("Database host is required")

            val port = node.node("port").getInt(3306)

            val username = node.node("username").string
                ?: throw IllegalStateException("Database username is required")

            val password = node.node("password").string
                ?: throw IllegalStateException("Database password is required")

            val databaseName = node.node("database").string
                ?: throw IllegalStateException("Database name is required")

            // Optional
            val maximumPoolSize = node.node("maximumPoolSize").getInt(10)
            val minimumIdle = node.node("minimumIdle").getInt(2)
            val idleTimeout = node.node("idleTimeout").getLong(600_000L)
            val connectionTimeout = node.node("connectionTimeout").getLong(30_000L)
            val maxLifetime = node.node("maxLifetime").getLong(1_800_000L)
            val isAutoCommit = node.node("autoCommit").getBoolean(false)
            val transactionIsolation = node.node("transactionIsolation")
                .getString("TRANSACTION_REPEATABLE_READ")

            return RemoteConnectionData(
                driver = driver,
                host = host,
                port = port,
                username = username,
                password = password,
                databaseName = databaseName,
                maximumPoolSize = maximumPoolSize,
                minimumIdle = minimumIdle,
                idleTimeout = idleTimeout,
                connectionTimeout = connectionTimeout,
                maxLifetime = maxLifetime,
                isAutoCommit = isAutoCommit,
                transactionIsolation = transactionIsolation
            )
        }

        override fun serialize(type: Type, obj: RemoteConnectionData?, node: ConfigurationNode) {

            if (obj == null) {
                node.set(null)
                return
            }

            node.node("driver").set(obj.driver.protocol)
            node.node("host").set(obj.host)
            node.node("port").set(obj.port)
            node.node("username").set(obj.username)
            node.node("password").set(obj.password)
            node.node("database").set(obj.databaseName)

            if (obj.maximumPoolSize != 10)
                node.node("maximumPoolSize").set(obj.maximumPoolSize)
            else node.node("maximumPoolSize").set(null)

            if (obj.minimumIdle != 2)
                node.node("minimumIdle").set(obj.minimumIdle)
            else node.node("minimumIdle").set(null)

            if (obj.idleTimeout != 600_000L)
                node.node("idleTimeout").set(obj.idleTimeout)
            else node.node("idleTimeout").set(null)

            if (obj.connectionTimeout != 30_000L)
                node.node("connectionTimeout").set(obj.connectionTimeout)
            else node.node("connectionTimeout").set(null)

            if (obj.maxLifetime != 1_800_000L)
                node.node("maxLifetime").set(obj.maxLifetime)
            else node.node("maxLifetime").set(null)

            if (obj.isAutoCommit)
                node.node("autoCommit").set(true)
            else node.node("autoCommit").set(null)

            if (obj.transactionIsolation != "TRANSACTION_REPEATABLE_READ")
                node.node("transactionIsolation").set(obj.transactionIsolation)
            else node.node("transactionIsolation").set(null)
        }
    }
}