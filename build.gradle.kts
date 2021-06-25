import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.5.20"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "net.d4rkb.d4rkbotkt"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation(kotlin("stdlib"))

    // JDA & Lavaplayer
    implementation("net.dv8tion:JDA:4.3.0_283")
    implementation("com.sedmelluq:lavaplayer:1.3.77")

    // MongoDB driver
    implementation("org.mongodb:mongodb-driver-sync:4.2.3")

    // Logger
    runtimeOnly("ch.qos.logback","logback-classic", "1.2.3")

    // Eval engine
    implementation("org.openjdk.nashorn:nashorn-core:15.2")
}

tasks {
    test {
        useJUnitPlatform()
    }

    register("stage") {
        dependsOn("build", "shadowJar", "clean", "test")
    }

    build {
        mustRunAfter("clean", "test")
    }

    withType<Jar> {
        manifest {
            attributes["Class-Path"] = "/libs/D4rkBot.jar"
            attributes["Main-Class"] = "net.d4rkb.d4rkbotkt.Main"
        }
    }

    withType<ShadowJar> {
        archiveFileName.set("D4rkBot.jar")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
    }
}