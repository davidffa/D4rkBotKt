import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.5.21"
  id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "me.davidffa.d4rkbotkt"
version = "1.0.0"

java {
  sourceCompatibility = JavaVersion.VERSION_16
  targetCompatibility = JavaVersion.VERSION_16
}

repositories {
  mavenCentral()
  @Suppress("DEPRECATION")
  jcenter() // JDA-NAS & lavadsp
  maven("https://m2.dv8tion.net/releases")
  maven("https://jitpack.io/")
}

dependencies {
  // Kotlin
  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
  implementation("ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0")
  implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.5.21")

  // JDA & Lavaplayer
  implementation("net.dv8tion:JDA:4.3.0_299") {
    exclude("opus-java")
  }
  implementation("com.github.minndevelopment:jda-ktx:ea0a1b2")
  implementation("com.sedmelluq:lavaplayer:1.3.78")
  implementation("com.sedmelluq:jda-nas:1.1.0")

  // Lavaplayer filters
  implementation("com.github.natanbc:lavadsp:0.7.7")

  // MongoDB
  implementation("org.litote.kmongo:kmongo-coroutine:4.2.8")

  // Native System
  implementation("com.github.oshi:oshi-core:5.7.5")

  // Logger
  runtimeOnly("ch.qos.logback", "logback-classic", "1.2.3")
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
      attributes["Main-Class"] = "me.davidffa.d4rkbotkt.Launcher"
    }
  }

  withType<ShadowJar> {
    archiveFileName.set("D4rkBot.jar")
  }

  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
  }
}
