package com.system32dev.systemCore.database

import org.jetbrains.exposed.v1.core.Table

import com.system32dev.systemCore.SystemCore
import com.system32dev.systemCore.managers.config.serializers.RemoteConnectionData
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File

object DatabaseUtils {

    private var dataSource: HikariDataSource? = null

    fun localDatabase(vararg tables: Table){
        Database.connect(localDatabaseUrl(), Driver.SQLITE.driverClass)
        transaction { SchemaUtils.create(tables = tables, inBatch = tables.size > 1) }
    }

    fun remoteDatabase(data: RemoteConnectionData){
        dataSource?.close()

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:${data.driver.protocol}://${data.host}:${data.port}/${data.databaseName}"
            driverClassName = data.driver.driverClass
            username = data.username
            password = data.password

            maximumPoolSize = data.maximumPoolSize
            minimumIdle = data.minimumIdle
            idleTimeout = data.idleTimeout
            connectionTimeout = data.connectionTimeout
            maxLifetime = data.maxLifetime

            isAutoCommit = data.isAutoCommit
            transactionIsolation = data.transactionIsolation
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
    }

    private fun localDatabaseUrl(): String {
        return "jdbc:sqlite:${localDatabaseFile().absolutePath}"
    }

    private fun localDatabaseFile(): File {
        val file = File(SystemCore.plugin.dataFolder, "database.db")
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()
        return file
    }

    fun fromBlob(blob: ExposedBlob): List<ItemStack> {
        return ItemStack.deserializeItemsFromBytes(blob.bytes).toList()
    }

    fun toBlob(data: Collection<ItemStack>): ExposedBlob {
        return ExposedBlob(ItemStack.serializeItemsAsBytes(data))
    }
}