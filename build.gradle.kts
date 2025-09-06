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
}

fun getLatestGitTag(): String {
    val output = ByteArrayOutputStream()
    try {
        project.exec {
            commandLine("git", "tag", "--sort=-v:refname")
            standardOutput = output
        }
    } catch (e: Exception) {
        return "1.0.0"
    }

    val tags = output.toString().trim().split("\n")
    return tags.firstOrNull()?.trim() ?: "1.0.0"
}

fun incrementVersion(version: String): String {
    val parts = version.split(".").map { it.toInt() }.toMutableList()
    if (parts.size < 3) parts.add(0)
    parts[2]++
    if(parts[2] == 10) {
        parts[2] = 0
        parts[1]++
    }
    if(parts[1] == 10) {
        parts[1] = 0
        parts[0]++
    }
    return parts.joinToString(".")
}

val lastTag = getLatestGitTag()
val newVersion = incrementVersion(lastTag)

group = "com.system32"
version = newVersion

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.triumphteam.dev/snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("me.clip:placeholderapi:2.11.6")
    api("dev.triumphteam:triumph-gui:3.1.13-SNAPSHOT")
    api("io.github.revxrsal:lamp.common:4.0.0-rc.12")
    api("io.github.revxrsal:lamp.paper:4.0.0-beta.19")
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

tasks.build {
    dependsOn("shadowJar")
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
            url = uri("https://nexus.system32dev.com/repository/maven-releases/")
            credentials {
                username = "admin"
                password = "NICKPR06"
            }
        }
    }
}

tasks.register("createGitTag", Exec::class) {
    commandLine("git", "tag", "$version")
}

tasks.register("pushGitTag", Exec::class) {
    commandLine("git", "push", "origin", "$version")
    dependsOn("createGitTag")
}

tasks.register("publishAndTag") {
    dependsOn("pushGitTag", "publishJavadocs", "publish")
    doLast{
        println("Publishing version $version")
    }
}

tasks.javadoc {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        addStringOption("sourcepath", "src/main/java")
    }
}

tasks.register("publishJavadocs") {
    dependsOn("dokkaHtml")

    doLast {
        val docsDir = File(buildDir, "dokka/html")
        if (!docsDir.exists()) {
            println("❌ No se encontraron Javadocs en $docsDir")
            return@doLast
        }

        val tempDir = File(buildDir, "gh-pages-temp")
        tempDir.deleteRecursively()

        exec {
            commandLine(
                "git", "clone", "--depth", "1", "--branch", "gh-pages",
                "https://github.com/system32developer/SystemCore.git", tempDir.absolutePath
            )
            isIgnoreExitValue = true
        }

        exec {
            workingDir = tempDir
            commandLine("git", "rm", "-rf", ".")
        }

        docsDir.copyRecursively(tempDir, overwrite = true)

        exec {
            workingDir = tempDir
            commandLine("git", "add", ".")
        }

        val status = ByteArrayOutputStream()
        exec {
            workingDir = tempDir
            commandLine("git", "status", "--porcelain")
            standardOutput = status
        }

        if (status.toString().trim().isNotEmpty()) {
            exec {
                workingDir = tempDir
                commandLine("git", "commit", "-m", "Actualizar Javadocs")
            }
            exec {
                workingDir = tempDir
                commandLine("git", "push", "--force", "origin", "gh-pages")
            }
        }
        tempDir.deleteRecursively()
    }
}


