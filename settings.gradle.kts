rootProject.name = "D4rkBotKt"

include("D4rkBotKtMain")
include("D4rkBotNatives")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      version("kotlin", "2.3.0")
      version("jdave", "0.1.5")

      plugin("kotlinJVM", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
      plugin("shadowJar", "com.gradleup.shadow").version("9.3.1")

      // Kotlin
      library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
      library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
      library("kotlin.scriptEngine", "org.jetbrains.kotlin", "kotlin-scripting-jsr223").versionRef("kotlin")
      library("kotlinx-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.10.2")
      library("okhttp-coroutines", "ru.gildor.coroutines", "kotlin-coroutines-okhttp").version("1.0")

      // JDA & Lavaplayer
      library("jda-core", "net.dv8tion", "JDA").version("6.3.0")
      library("opus", "com.github.davidffa", "opus-java").version("0dabaa7")
      library("jda-ktx", "com.github.minndevelopment", "jda-ktx").version("429437c")
      library("lavaplayer", "com.github.davidffa", "lavaplayer-fork").version("a993d26")
      library("lavaplayer-yt", "dev.lavalink.youtube", "common").version("1.16.0")
      library("lavaplayer-yt-v2", "dev.lavalink.youtube", "v2").version("1.16.0")
      library("jda-nas", "com.github.davidffa", "jda-nas-fork").version("1.0.3")
      library("jdave-api", "club.minnced", "jdave-api").versionRef("jdave")
      library("jdave-native-linux-amd64", "club.minnced", "jdave-native-linux-x86-64").versionRef("jdave")
      library("jdave-native-linux-arm64", "club.minnced", "jdave-native-linux-aarch64").versionRef("jdave")
      library("jdave-native-darwin", "club.minnced", "jdave-native-darwin").versionRef("jdave")

      // Lavaplayer audio filters
      library("lavadsp", "com.github.davidffa", "lavadsp-fork").version("0.7.8")

      // YAML parsing
      library("snakeyaml", "org.yaml", "snakeyaml").version("2.5")

      // MongoDB
      library("kmongo", "org.litote.kmongo", "kmongo-coroutine").version("4.11.0")

      // Native System
      library("oshi", "com.github.oshi", "oshi-core").version("6.9.2")

      // Logger impl
      library("logback", "ch.qos.logback", "logback-classic").version("1.5.25")
    }
  }
}