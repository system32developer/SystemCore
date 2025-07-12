package com.system32.systemCore.database.model

enum class Driver(val driver: String, val url: String) {
    /**
     * Implementation driver in gradle is:
     * ```gradle
     * implementation("com.h2database:h2:2.3.232")
     * ```
     * In the code the "mem" part of the URL indicates an in-memory database name.
     */
    H2("org.h2.Driver", "jdbc:jdbc:h2:mem:"),
    /**
     * Implementation driver in gradle is:
     * ```gradle
     * implementation("org.xerial:sqlite-jdbc:3.50.2.0")
     * ```
     * In the code after the "jdbc:sqlite:" part of the URL you can specify the path to the SQLite database .db file.
     */
    SQLITE("org.sqlite.JDBC", "jdbc:sqlite:"),
    /**
     * Implementation driver in gradle is:
     * ```gradle
     * implementation("com.mysql:mysql-connector-j:9.3.0")
     * ```
     * In the code after the "jdbc:mysql://" part of the URL you can specify the host, port, and database name.
     */
    MYSQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://"),
    /**
     * Implementation driver in gradle is:
     * ```gradle
     * implementation("org.mariadb.jdbc:mariadb-java-client:3.5.4")
     * ```
     * In the code after the "jdbc:mariadb://" part of the URL you can specify the host, port, and database name.
     */
    MARIADB("org.mariadb.jdbc.Driver", "jdbc:mariadb://"),
}