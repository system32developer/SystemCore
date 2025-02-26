import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "2.1.20-RC"
    id("com.gradleup.shadow") version "8.3.2"
    `maven-publish`
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

tasks.register<Exec>("publishAndTag") {
    dependsOn("pushGitTag", "publishJavadocs")
}

tasks.register("publishJavadocs") {
    dependsOn("javadoc")

    doLast {
        val docsDir = File(buildDir, "docs/javadoc")
        if (!docsDir.exists()) {
            println("❌ No se encontraron Javadocs en $docsDir")
            return@doLast
        }

        // Crear una carpeta temporal para manejar los archivos
        val tempDir = File(buildDir, "gh-pages")
        tempDir.deleteRecursively()
        tempDir.mkdirs()

        // Copiar los Javadocs generados a la carpeta temporal
        docsDir.copyRecursively(tempDir, overwrite = true)

        // Comprobar si la rama `gh-pages` ya existe
        val result = exec {
            commandLine("git", "rev-parse", "--verify", "gh-pages")
            isIgnoreExitValue = true // Evitar que falle si la rama no existe
        }

        // Si la rama no existe, crearla
        if (result.exitValue != 0) {
            exec { commandLine("git", "checkout", "--orphan", "gh-pages") }
        } else {
            exec { commandLine("git", "checkout", "gh-pages") }
        }

        exec { commandLine("git", "rm", "-rf", ".") } // Limpiar la rama actual
        exec { commandLine("cp", "-r", "${tempDir.absolutePath}/.", ".") } // Copiar Javadocs al repositorio
        exec { commandLine("git", "add", ".") }
        exec { commandLine("git", "commit", "-m", "Actualizar Javadocs") }
        exec { commandLine("git", "push", "--force", "origin", "gh-pages") }

        println("✅ Javadocs publicados en la rama gh-pages")
        println("✅ Versión $version publicada")
    }
}


