/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

val minecraftVersion: String = providers.gradleProperty("minecraftVersion").get()
val yarnMappings: String = providers.gradleProperty("yarnMappings").get()
val loaderVersion: String = providers.gradleProperty("loaderVersion").get()
val fabricKotlinVersion: String = providers.gradleProperty("fabricKotlinVersion").get()
val kotlinVersion: String = providers.gradleProperty("kotlinVersion").get()
val modVersion: String = providers.gradleProperty("modVersion").get()
val mavenGroup: String = providers.gradleProperty("mavenGroup").get()
val archiveBaseName: String = providers.gradleProperty("archiveBaseName").get()
val fabricVersion: String = providers.gradleProperty("fabricVersion").get()

plugins {
    id("net.fabricmc.fabric-loom")
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
    accessWidenerPath = file("src/main/resources/matrix.classtweaker")

    runConfigs.configureEach {
        ideConfigGenerated(true)
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // mappings("net.fabricmc:yarn:$yarnMappings:v2")

    implementation("net.fabricmc:fabric-loader:$loaderVersion")

    // Fabric API
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    implementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    implementation(project(":ledger"))
    include(project(":ledger"))
}

sourceSets {
    main {
        java {
            setIncludes(
                listOf(
                    "heckerpowered/matrix/mixin/DamageSourceMixin.java",
                    "heckerpowered/matrix/mixin/EntityMixin.java",
                    "heckerpowered/matrix/mixin/LevelMixin.java",
                    "heckerpowered/matrix/mixin/LivingEntityMixin.java",
                )
            )
        }
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
    }
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_25
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${archiveBaseName}" }
    }
}
