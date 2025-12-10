package com.system32dev.systemCore.database

enum class Driver(val driverClass: String, val protocol: String) {
    /**
     * Implementation driverClass in gradle is:
     * ```gradle
     * implementation("org.xerial:sqlite-jdbc:3.50.2.0")
     * ```
     * In the code after the "jdbc:sqlite:" part of the URL you can specify the path to the SQLite database .db file.
     */
    SQLITE("org.sqlite.JDBC", "sqlite"),
    /**
     * Implementation driverClass in gradle is:
     * ```gradle
     * implementation("com.mysql:mysql-connector-j:9.3.0")
     * ```
     * In the code after the "jdbc:mysql://" part of the URL you can specify the host, port, and database name.
     */
    MYSQL("com.mysql.cj.jdbc.Driver", "mysql"),
    /**
     * Implementation driverClass in gradle is:
     * ```gradle
     * implementation("org.mariadb.jdbc:mariadb-java-client:3.5.4")
     * ```
     * In the code after the "jdbc:mariadb://" part of the URL you can specify the host, port, and database name.
     */
    MARIADB("org.mariadb.jdbc.Driver", "mariadb");

    companion object {
        fun fromString(value: String): Driver? {
            return values().firstOrNull { it.protocol.equals(value, ignoreCase = true) }
        }
    }

}