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
      properties.setProperty("YTEMAIL", System.getenv("YTEMAIL"))
      properties.setProperty("YTPASS", System.getenv("YTPASS"))
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

  val YTEMAIL: String
    get() = properties.getProperty("YTEMAIL")

  val YTPASS: String
    get() = properties.getProperty("YTPASS")
}