package com.system32.systemCore.database.model

enum class Driver(val driver: String) {
    /**
     * Implementation driver in gradle is:
     * ```gradle
     * implementation("com.h2database:h2:2.3.232")
     * ```
     */
    H2("org.h2.Driver"),
    /**
     * Implementation driver in gradle is:
     * ```gradle
     * implementation("org.xerial:sqlite-jdbc:3.50.2.0")
     * ```
     */
    SQLITE("org.sqlite.JDBC"),
    /**
     * Implementation driver in gradle is:
     * ```gradle
     * implementation("com.mysql:mysql-connector-j:9.3.0")
     * ```
     */
    MYSQL("com.mysql.cj.jdbc.Driver"),
    /**
     * Implementation driver in gradle is:
     * ```gradle
     * implementation("org.mariadb.jdbc:mariadb-java-client:3.5.4")
     * ```
     */
    MARIADB("org.mariadb.jdbc.Driver");
    fun get(): String {
        return driver
    }
}