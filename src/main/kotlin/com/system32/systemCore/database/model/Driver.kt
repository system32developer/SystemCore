package com.system32.systemCore.database.model

enum class Driver(val driver: String) {
    H2("org.h2.Driver"),
    SQLITE("org.sqlite.JDBC"),
    MYSQL("com.mysql.cj.jdbc.Driver"),
    MARIADB("org.mariadb.jdbc.Driver")

    ;
    fun get(): String {
        return driver
    }
}