import com.matthewprenger.cursegradle.*
import com.modrinth.minotaur.TaskModrinthUpload
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "0.11-SNAPSHOT"
    kotlin("jvm") version "1.6.21"
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "1.2.1"
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


val env: MutableMap<String, String> = System.getenv()

val minecraft_version: String by project

val mod_version = (env["TAG_VERSION"] ?: "SNAPSHOT") + "+" + minecraft_version
val maven_group: String by project

version = mod_version
group = maven_group

repositories {
    // Add repositories to retrieve artifacts from (in here.)
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from (automatically.)
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven("https://uten2c.github.io/repo/")
    maven("https://api.modrinth.com/maven")
    maven("https://server.bbkr.space/artifactory/libs-release")
}

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

    modImplementation("net.fabricmc:fabric-language-kotlin:1.7.4+kotlin.1.6.21")

    includeAndExpose("dev.uten2c:strobo:71")
    includeAndExpose("maven.modrinth:paradox-config:0.5.1-beta")
    includeAndExpose("io.github.cottonmc:Jankson-Fabric:3.0.1+j1.2.0")
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

@Suppress("UnstableApiUsage")
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

    // Minecraft 1.18 upwards uses Java 17.
    options.release.set(17)
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

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=all")
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

val artifactPath = "${project.buildDir}/libs/${base.archivesName.get()}-${mod_version}.jar"

val curseApiKey: String? = env["CURSEFORGE_API_KEY"]
if (curseApiKey != null) {
    curseforge {
        options(closureOf<Options> {
            forgeGradleIntegration = false
        })
        apiKey = curseApiKey

        val curseId = 554426
        project(closureOf<CurseProject> {
            id = "$curseId"
            changelog = env["CAHNGE_LOG"] ?: "No description provided"
            releaseType = "release"
            addGameVersion(minecraft_version)
            addGameVersion("Fabric")
            relations(closureOf<CurseRelation> {
                requiredDependency("fabric-api")
                requiredDependency("fabric-language-kotlin")
                requiredDependency("completeconfig")
            })

            mainArtifact(
                artifactPath,
                closureOf<CurseArtifact> {
                    displayName = "BlockHideAndSeek $mod_version"
                }
            )
        })

        project.afterEvaluate {
            tasks.getByName<CurseUploadTask>("curseforge${curseId}") {
                dependsOn(remapJar)
            }
        }
    }
}

tasks.create<TaskModrinthUpload>("publishModrinth") {
    val modrinthApiToken = env["MODRINTH_TOKEN"]
    onlyIf {
        modrinthApiToken != null
    }
    token = modrinthApiToken
    projectId = "C3KKoSI2"
    versionNumber = mod_version
    uploadFile = file(artifactPath)
    addGameVersion(minecraft_version)
    addLoader("fabric")
}.also {
    it.dependsOn(remapJar)
}
