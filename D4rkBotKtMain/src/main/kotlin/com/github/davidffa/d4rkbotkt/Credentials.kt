package com.github.davidffa.d4rkbotkt

import java.io.File
import java.io.FileInputStream
import java.util.*

object Credentials {
  private val properties = Properties()

  init {
    try {
      val file = File("./credentials.properties")
      FileInputStream(file).use { properties.load(it) }
    } catch (_: Exception) {
      properties.setProperty("TOKEN", System.getenv("TOKEN"))
      properties.setProperty("MONGODBURI", System.getenv("MONGODBURI"))
      properties.setProperty("SPOTIFYID", System.getenv("SPOTIFYID"))
      properties.setProperty("SPOTIFYSECRET", System.getenv("SPOTIFYSECRET"))
      properties.setProperty("YT_REFRESH_TOKEN", System.getenv("YT_REFRESH_TOKEN"))
      properties.setProperty("YT_REMOTE_CIPHER_URL", System.getenv("YT_REMOTE_CIPHER_PASSWORD"))
    }
  }

  val TOKEN: String
    get() = properties.getProperty("TOKEN")

  val MONGODBURI: String
    get() = properties.getProperty("MONGODBURI")

  val SPOTIFYID: String
    get() = properties.getProperty("SPOTIFYID")

  val SPOTIFYSECRET: String
    get() = properties.getProperty("SPOTIFYSECRET")

  val YT_REFRESH_TOKEN: String
    get() = properties.getProperty("YT_REFRESH_TOKEN")

  val YT_REMOTE_CIPHER_URL: String
    get() = properties.getProperty("YT_REMOTE_CIPHER_URL")

  val YT_REMOTE_CIPHER_PASSWORD: String
    get() = properties.getProperty("YT_REMOTE_CIPHER_PASSWORD")
}