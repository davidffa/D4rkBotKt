import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.kotlinJVM)
  alias(libs.plugins.shadowJar)
}

group = "com.github.davidffa"
version = "1.0.0"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

repositories {
  mavenCentral()
  maven("https://m2.dv8tion.net/releases")
  maven("https://jitpack.io/")
}

dependencies {
  // Kotlin
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlin.scriptEngine)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.okhttp.coroutines)


  // JDA & Lavaplayer
  implementation(libs.jda.core) {
    exclude(module="opus-java")
  }
  implementation("com.github.davidffa:opus-java:eeaf630")
  implementation(libs.jda.ktx)
  implementation(libs.lavaplayer)
  implementation(libs.jda.nas)

  // Lavaplayer filters
  implementation(libs.lavadsp)

  // YAML
  implementation(libs.snakeyaml)

  // MongoDB
  implementation(libs.kmongo)

  // Native System
  implementation(libs.oshi)

  // Logger
  runtimeOnly(libs.logback)
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
      attributes["Class-Path"] = "/libs/D4rkBotKt.jar"
      attributes["Main-Class"] = "com.github.davidffa.d4rkbotkt.Launcher"
    }
  }

  withType<ShadowJar> {
    archiveFileName.set("D4rkBotKt.jar")
  }

  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
  }
}
