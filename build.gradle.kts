import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.10"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.davidffa"
version = "1.0.0"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

repositories {
  mavenCentral()
  @Suppress("DEPRECATION")
  jcenter() // JDA-NAS & lavadsp
  maven("https://m2.dv8tion.net/releases")
  maven("https://jitpack.io/")
}

val kotlinVersion           = "1.6.10"
val coroutinesVersion       = "1.6.0"

val okhttpCoroutinesVersion = "1.0"
val jdaVersion              = "5.0.0-alpha.9"
val jdaKtxVersion           = "6527755"
val lavaplayerVersion       = "f9e6bce"
val jdaNasVersion           = "1.1.0"
val lavadspVersion          = "0.7.7"

val snakeyamlVersion        = "1.30"
val kmongoVersion           = "4.5.0"
val oshiVersion             = "6.1.4"
val logbackVersion          = "1.2.10"

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
  implementation("com.sedmelluq:jda-nas:$jdaNasVersion")

  // Lavaplayer filters
  implementation("com.github.natanbc:lavadsp:$lavadspVersion")

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
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
  }
}
