import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "2.1.20-RC"
    id("com.gradleup.shadow") version "8.3.2"
    `maven-publish`
    id("org.jetbrains.dokka") version "2.0.0"
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
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.tchristofferson:ConfigUpdater:2.2-SNAPSHOT")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
val path = "com.system32.systemCore.shade"

tasks.named<ShadowJar>("shadowJar") {
    minimize()
    relocate("org.bstats", "$path.bstats")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.github.system32developer"
            artifactId = "SystemCore"
            version = version
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
    dependsOn("pushGitTag", "publishJavadocs")
    doLast{
        println("Publishing version $version")
    }
}

tasks.register("publishJavadocs") {
    dependsOn("dokkaHtml")

    doLast {
        val docsDir = File(buildDir, "docs/javadoc")
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
            println("✅ Javadocs publicados correctamente en GitHub Pages")
        } else {
            println("⚠️ No hay cambios en los Javadocs, no se realizó ningún commit.")
        }

        tempDir.deleteRecursively()
    }
}


