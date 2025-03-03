package com.system32.systemCore.gui.components.nbt

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*


/**
 * Class to set / get NBT tags from items.
 * I hate this class.
 */
class LegacyNbt : NbtWrapper {

    public override fun setString(itemStack: ItemStack, key: String?, value: String?): ItemStack? {
        if (itemStack.type == Material.AIR) return itemStack

        val nmsItemStack = asNMSCopy(itemStack)
        val itemCompound = if (hasTag(nmsItemStack)) getTag(nmsItemStack) else newNBTTagCompound()

        setString(itemCompound, key, value)
        setTag(nmsItemStack, itemCompound)

        return asBukkitCopy(nmsItemStack)
    }

    public override fun removeTag(itemStack: ItemStack, key: String?): ItemStack? {
        if (itemStack.type == Material.AIR) return itemStack

        val nmsItemStack = asNMSCopy(itemStack)
        val itemCompound = if (hasTag(nmsItemStack)) getTag(nmsItemStack) else newNBTTagCompound()

        remove(itemCompound, key)
        setTag(nmsItemStack, itemCompound)

        return asBukkitCopy(nmsItemStack)
    }

    public override fun setBoolean(itemStack: ItemStack, key: String?, value: Boolean): ItemStack? {
        if (itemStack.type == Material.AIR) return itemStack

        val nmsItemStack = asNMSCopy(itemStack)
        val itemCompound = if (hasTag(nmsItemStack)) getTag(nmsItemStack) else newNBTTagCompound()

        setBoolean(itemCompound, key, value)
        setTag(nmsItemStack, itemCompound)

        return asBukkitCopy(nmsItemStack)
    }

    public override fun getString(itemStack: ItemStack, key: String?): String? {
        if (itemStack.type == Material.AIR) return null

        val nmsItemStack = asNMSCopy(itemStack)
        val itemCompound = if (hasTag(nmsItemStack)) getTag(nmsItemStack) else newNBTTagCompound()

        return getString(itemCompound, key)
    }

    companion object {
        val PACKAGE_NAME: String = Bukkit.getServer().javaClass.getPackage().name
        val NMS_VERSION: String = PACKAGE_NAME.substring(PACKAGE_NAME.lastIndexOf(46.toChar()) + 1)

        private lateinit var getStringMethod: Method
        private lateinit var setStringMethod: Method
        private lateinit var setBooleanMethod: Method
        private lateinit var hasTagMethod: Method
        private lateinit var getTagMethod: Method
        private lateinit var setTagMethod: Method
        private lateinit var removeTagMethod: Method
        private lateinit var asNMSCopyMethod: Method
        private lateinit var asBukkitCopyMethod: Method

        private lateinit var nbtCompoundConstructor: Constructor<*>

        init {
            try {
                getStringMethod = Objects.requireNonNull(getNMSClass("NBTTagCompound"))!!
                    .getMethod("getString", String::class.java)
                removeTagMethod =
                    Objects.requireNonNull(getNMSClass("NBTTagCompound"))!!.getMethod("remove", String::class.java)
                setStringMethod = Objects.requireNonNull(getNMSClass("NBTTagCompound"))!!
                    .getMethod("setString", String::class.java, String::class.java)
                setBooleanMethod = Objects.requireNonNull(getNMSClass("NBTTagCompound"))!!
                    .getMethod("setBoolean", String::class.java, Boolean::class.javaPrimitiveType)
                hasTagMethod = Objects.requireNonNull(getNMSClass("ItemStack"))!!.getMethod("hasTag")
                getTagMethod = Objects.requireNonNull(getNMSClass("ItemStack"))!!.getMethod("getTag")
                setTagMethod = Objects.requireNonNull(getNMSClass("ItemStack"))!!
                    .getMethod("setTag", getNMSClass("NBTTagCompound"))
                nbtCompoundConstructor =
                    Objects.requireNonNull(getNMSClass("NBTTagCompound"))!!.getDeclaredConstructor()
                asNMSCopyMethod =
                    Objects.requireNonNull(craftItemStackClass)!!.getMethod("asNMSCopy", ItemStack::class.java)
                asBukkitCopyMethod = Objects.requireNonNull(craftItemStackClass)!!
                    .getMethod("asBukkitCopy", getNMSClass("ItemStack"))
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
        }

        private fun setString(itemCompound: Any?, key: String?, value: String?) {
            try {
                setStringMethod!!.invoke(itemCompound, key, value)
            } catch (ignored: IllegalAccessException) {
            } catch (ignored: InvocationTargetException) {
            }
        }

        private fun setBoolean(itemCompound: Any?, key: String?, value: Boolean) {
            try {
                setBooleanMethod.invoke(itemCompound, key, value)
            } catch (ignored: IllegalAccessException) {
            } catch (ignored: InvocationTargetException) {
            }
        }

        private fun remove(itemCompound: Any?, key: String?) {
            try {
                removeTagMethod!!.invoke(itemCompound, key)
            } catch (ignored: IllegalAccessException) {
            } catch (ignored: InvocationTargetException) {
            }
        }

        private fun getString(itemCompound: Any?, key: String?): String? {
            return try {
                getStringMethod!!.invoke(itemCompound, key) as String?
            } catch (e: IllegalAccessException) {
                null
            } catch (e: InvocationTargetException) {
                null
            }
        }

        private fun hasTag(nmsItemStack: Any?): Boolean {
            return try {
                hasTagMethod!!.invoke(nmsItemStack) as Boolean
            } catch (e: IllegalAccessException) {
                false
            } catch (e: InvocationTargetException) {
                false
            }
        }

        fun getTag(nmsItemStack: Any?): Any? {
            return try {
                getTagMethod!!.invoke(nmsItemStack)
            } catch (e: IllegalAccessException) {
                null
            } catch (e: InvocationTargetException) {
                null
            }
        }

        private fun setTag(nmsItemStack: Any?, itemCompound: Any?) {
            try {
                setTagMethod!!.invoke(nmsItemStack, itemCompound)
            } catch (ignored: IllegalAccessException) {
            } catch (ignored: InvocationTargetException) {
            }
        }


        private fun newNBTTagCompound(): Any? {
            return try {
                nbtCompoundConstructor!!.newInstance()
            } catch (e: IllegalAccessException) {
                null
            } catch (e: InstantiationException) {
                null
            } catch (e: InvocationTargetException) {
                null
            }
        }

        fun asNMSCopy(itemStack: ItemStack?): Any? {
            return try {
                asNMSCopyMethod!!.invoke(null, itemStack)
            } catch (e: IllegalAccessException) {
                null
            } catch (e: InvocationTargetException) {
                null
            }
        }


        fun asBukkitCopy(nmsItemStack: Any?): ItemStack? {
            return try {
                asBukkitCopyMethod!!.invoke(null, nmsItemStack) as ItemStack?
            } catch (e: IllegalAccessException) {
                null
            } catch (e: InvocationTargetException) {
                null
            }
        }

        private fun getNMSClass(className: String?): Class<*>? {
            try {
                return Class.forName("net.minecraft.server.$NMS_VERSION.$className")
            } catch (e: ClassNotFoundException) {
                return null
            }
        }

        private val craftItemStackClass: Class<*>?
            get() {
                try {
                    return Class.forName("org.bukkit.craftbukkit.$NMS_VERSION.inventory.CraftItemStack")
                } catch (e: ClassNotFoundException) {
                    return null
                }
            }
    }
}