import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.21"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.davidffa"
version = "1.0.0"

java {
  sourceCompatibility = JavaVersion.VERSION_18
  targetCompatibility = JavaVersion.VERSION_18
}

repositories {
  mavenCentral()
  maven("https://m2.dv8tion.net/releases")
  maven("https://jitpack.io/")
}

val kotlinVersion           = "1.6.21"
val coroutinesVersion       = "1.6.1"

val okhttpCoroutinesVersion = "1.0"
val jdaVersion              = "5.0.0-alpha.12"
val jdaKtxVersion           = "9f01b74"
val lavaplayerVersion       = "c5d24b2"
val jdaNasVersion           = "1.0.0"
val lavadspVersion          = "0.7.8"

val snakeyamlVersion        = "1.30"
val kmongoVersion           = "4.5.1"
val oshiVersion             = "6.1.6"
val logbackVersion          = "1.2.11"

dependencies {
  // Kotlin
  implementation(kotlin("stdlib", kotlinVersion))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
  implementation("ru.gildor.coroutines:kotlin-coroutines-okhttp:$okhttpCoroutinesVersion")
  implementation(kotlin("scripting-jsr223", kotlinVersion))

  // JDA & Lavaplayer
  implementation("net.dv8tion:JDA:$jdaVersion") {
    exclude("opus-java")
  }
  implementation("com.github.minndevelopment:jda-ktx:$jdaKtxVersion")
  implementation("com.github.davidffa:lavaplayer-fork:$lavaplayerVersion")
  implementation("com.github.davidffa:jda-nas-fork:$jdaNasVersion")

  // Lavaplayer filters
  implementation("com.github.davidffa:lavadsp-fork:$lavadspVersion")

  // YAML
  implementation("org.yaml:snakeyaml:$snakeyamlVersion")

  // MongoDB
  implementation("org.litote.kmongo:kmongo-coroutine:$kmongoVersion")

  // Native System
  implementation("com.github.oshi:oshi-core:$oshiVersion")

  // Logger
  runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
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
      attributes["Main-Class"] = "me.davidffa.d4rkbotkt.Launcher"
    }
  }

  withType<ShadowJar> {
    archiveFileName.set("D4rkBotKt.jar")
  }

  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_18.toString()
  }
}
