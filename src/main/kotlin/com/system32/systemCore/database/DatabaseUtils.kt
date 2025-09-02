package com.system32.systemCore.database

import com.system32.systemCore.SystemCore
import com.system32.systemCore.database.model.Driver
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.exists
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File

object DatabaseUtils {

    fun localDatabase(vararg tables: Table){
        Database.connect(localDatabaseUrl(), Driver.SQLITE.driver)
        transaction { SchemaUtils.create(tables = tables) }
    }

    fun localDatabaseUrl(): String {
        return "jdbc:sqlite:${localDatabaseFile().absolutePath}"
    }

    fun localDatabaseFile(): File {
        val file = File(SystemCore.plugin.dataFolder, "database.db")
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()
        return file
    }

    private fun fromBlob(blob: ExposedBlob): List<ItemStack> {
        return ItemStack.deserializeItemsFromBytes(blob.bytes).toList()
    }

    private fun toBlob(data: Collection<ItemStack>): ExposedBlob {
        return ExposedBlob(ItemStack.serializeItemsAsBytes(data))
    }
}