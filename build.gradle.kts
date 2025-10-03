import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "2.2.0"
    java
    id("com.gradleup.shadow") version "8.3.2"
    `maven-publish`
    `java-library`
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("com.system32dev.autoversion") version "1.0.0"
}


group = "com.system32"

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
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.30-1.0.0")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveBaseName.set("SystemCore")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    minimize()
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = groupId
            artifactId = artifactId
            version = version

            artifact(tasks.getByName("sourcesJar"))
        }
    }

    repositories {
        maven {
            url = uri("http://207.180.248.101:8081/repository/maven-releases/")
            isAllowInsecureProtocol = true
            credentials {
                username = "admin"
                password = "NICKPR06"
            }
        }
    }
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        addStringOption("sourcepath", "src/main/java")
    }
}


