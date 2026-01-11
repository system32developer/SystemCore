import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.2"
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("com.system32dev.autoversion") version "1.0.0"
    id("com.vanniktech.maven.publish") version "0.34.0"
    `java-library`
}

group = "com.system32dev"

autoversion {
    owner = "system32developer"
    repo = "SystemCore"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("dev.triumphteam:triumph-gui:3.1.13")
    api("io.github.revxrsal:lamp.common:4.0.0-rc.12")
    api("io.github.revxrsal:lamp.bukkit:4.0.0-rc.12")
    api("org.spongepowered:configurate-yaml:4.2.0")
    api("org.spongepowered:configurate-core:4.2.0")
    api("org.jetbrains.exposed:exposed-core:1.0.0-beta-5")
    api("org.jetbrains.exposed:exposed-jdbc:1.0.0-beta-5")
    api("org.mariadb.jdbc:mariadb-java-client:3.5.6")
    api("com.zaxxer:HikariCP:7.0.2")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.30-1.0.0")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    shadowJar {
        archiveVersion.set(project.version.toString())
    }

    build {
        dependsOn(shadowJar)
    }
}


tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        addStringOption("sourcepath", "src/main/java")
    }
}

afterEvaluate {
    mavenPublishing {
        coordinates(group.toString(), name, project.version.toString())
        pom {
            name.set("SystemCore")
            description.set("A library to help you develop Paper plugins faster and easier.")
            inceptionYear.set("2025")
            url.set("https://github.com/system32developer/SystemCore")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("system32developer")
                    name.set("System32")
                    url.set("https://github.com/system32developer")
                }
            }
            scm {
                url.set("https://github.com/system32developer/SystemCore")
                connection.set("scm:git:git://github.com/system32developer/SystemCore.git")
                developerConnection.set("scm:git:ssh://git@github.com/system32developer/SystemCore.git")
            }
        }
    }
}
