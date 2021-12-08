import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "0.9-SNAPSHOT"
    kotlin("jvm") version "1.5.31"
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

val archives_base_name: String by project
base {
    archivesName.set(archives_base_name)
}

val mod_version: String by project
val maven_group: String by project

version = mod_version
group = maven_group

repositories {
    // Add repositories to retrieve artifacts from (in here.)
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from (automatically.)
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/")
    maven("https://jitpack.io")
    maven("https://uten2c.github.io/repo/")
}

val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")

    modImplementation("net.fabricmc:fabric-language-kotlin:1.6.5+kotlin.1.5.31")

    modImplementation("com.gitlab.Lortseam:completeconfig:1.2.0")

    includeAndExpose("dev.uten2c:cmdlib-fabric:1.17+1")
    // PSA: Some older mods, compiled on Loom 0.2.1, might have outdated Maven POMs.
    // You may need to force-disable transitiveness on them.
}

fun DependencyHandlerScope.includeAndExpose(value: String) {
    modApi(value)
    include(value)
}

loom {
    accessWidenerPath.set(file("src/main/resources/blockhideandseekmod.accesswidener"))
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"

    // Minecraft 1.17 (21w19a) upwards uses Java 16.
    options.release.set(16)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

tasks.withType<Jar> {
    from("LICENSE") {
        rename { "${it}_${archives_base_name}" }
    }
}

val remapJar = tasks.getByName<RemapJarTask>("remapJar")
val sourcesJar = tasks.getByName<Jar>("sourcesJar")
val remapSourcesJar = tasks.getByName<RemapSourcesJarTask>("remapSourcesJar")

// configure the maven publication
publishing {
    publications {
        create("mavenJava", MavenPublication::class.java) {
            // add all the jars that should be included when publishing to maven
            artifact(remapJar) {
                builtBy(remapJar)
            }
            artifact(sourcesJar) {
                builtBy(remapSourcesJar)
            }
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "16"
    }
}