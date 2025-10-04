/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

val minecraftVersion = providers.gradleProperty("minecraftVersion").get()
val yarnMappings = providers.gradleProperty("yarnMappings").get()
val loaderVersion = providers.gradleProperty("loaderVersion").get()
val fabricKotlinVersion = providers.gradleProperty("fabricKotlinVersion").get()
val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
val modVersion = providers.gradleProperty("modVersion").get()
val mavenGroup = providers.gradleProperty("mavenGroup").get()
val archiveBaseName = providers.gradleProperty("archiveBaseName").get()
val fabricVersion = providers.gradleProperty("fabricVersion").get()

plugins {
    id("fabric-loom")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

version = modVersion
group = mavenGroup

base {
    archivesName = archiveBaseName
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

loom {
    accessWidenerPath = file("src/main/resources/matrix.accesswidener")
}

fabricApi {
    configureDataGeneration()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")

    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    // Fabric API
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
    }
}

tasks.processResources {
    inputs.property("version", modVersion)

    val properties = mapOf("version" to modVersion)
    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${archiveBaseName}" }
    }
}