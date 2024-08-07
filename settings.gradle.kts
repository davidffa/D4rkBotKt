rootProject.name = "D4rkBotKt"

include("D4rkBotKtMain")
include("D4rkBotNatives")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      version("kotlin", "2.0.0")

      plugin("kotlinJVM", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
      plugin("shadowJar", "com.github.johnrengelman.shadow").version("8.1.1")

      // Kotlin
      library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
      library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
      library("kotlin.scriptEngine", "org.jetbrains.kotlin", "kotlin-scripting-jsr223").versionRef("kotlin")
      library("kotlinx-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.8.1")
      library("okhttp-coroutines", "ru.gildor.coroutines", "kotlin-coroutines-okhttp").version("1.0")

      // JDA & Lavaplayer
      library("jda-core", "net.dv8tion", "JDA").version("5.0.2")
      library("opus", "com.github.davidffa", "opus-java").version("c6194b1")
      library("jda-ktx", "com.github.minndevelopment", "jda-ktx").version("0.12.0")
      library("lavaplayer", "com.github.davidffa", "lavaplayer-fork").version("5a3a782")
      library("lavaplayer-yt", "dev.lavalink.youtube", "common").version("1.5.2")
      library("lavaplayer-yt-v2", "dev.lavalink.youtube", "v2").version("1.5.2")
      library("jda-nas", "com.github.davidffa", "jda-nas-fork").version("1.0.3")

      // Lavaplayer audio filters
      library("lavadsp", "com.github.davidffa", "lavadsp-fork").version("0.7.8")

      // YAML parsing
      library("snakeyaml", "org.yaml", "snakeyaml").version("2.2")

      // MongoDB
      library("kmongo", "org.litote.kmongo", "kmongo-coroutine").version("4.11.0")

      // Native System
      library("oshi", "com.github.oshi", "oshi-core").version("6.6.2")

      // Logger impl
      library("logback", "ch.qos.logback", "logback-classic").version("1.5.6")
    }
  }
}