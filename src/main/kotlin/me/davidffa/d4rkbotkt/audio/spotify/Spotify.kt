package me.davidffa.d4rkbotkt.audio.spotify

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.davidffa.d4rkbotkt.D4rkBot
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.FormBody
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.time.Instant
import java.util.*

class Spotify(
  clientId: String,
  clientSecret: String
) {
  private val authorization = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())
  private var token: String? = null

  private var renewDate = 0L

  private val renewFormBody = FormBody.Builder().add("grant_type", "client_credentials").build()
  private val renewRequest = Request.Builder()
    .url("https://accounts.spotify.com/api/token")
    .addHeader("Authorization", "Basic $authorization")
    .post(renewFormBody)
    .build()

  suspend fun getTrack(id: String, requester: Member): SpotifyTrack {
    val json = makeRequest("tracks/$id")

    return buildTrack(json, requester)
  }

  suspend fun getAlbum(id: String, requester: Member): List<SpotifyTrack> {
    val json = makeRequest("albums/$id/tracks")

    val tracks = json.getArray("items")

    val spotifyTracks = mutableListOf<SpotifyTrack>()

    for (i in 0 until tracks.length()) {
      spotifyTracks.add(buildTrack(tracks.getObject(i), requester))
    }

    return spotifyTracks.toList()
  }

  suspend fun getPlaylist(id: String, requester: Member): List<SpotifyTrack> {
    val spotifyTracks = mutableListOf<SpotifyTrack>()

    var offset = 0
    var total = 0

    do {
      val json = makeRequest("playlists/$id/tracks?offset=$offset&limit=100")

      if (total == 0) total = json.getInt("total")

      val tracks = json.getArray("items")

      for (i in 0 until tracks.length()) {
        if (tracks.getObject(i).isNull("track")) continue

        spotifyTracks.add(buildTrack(tracks.getObject(i).getObject("track"), requester))
      }

      offset += 100
    } while (spotifyTracks.size < total && spotifyTracks.size < 400)

    return spotifyTracks.toList()
  }

  private fun buildTrack(json: DataObject, requester: Member): SpotifyTrack {
    val title = json.getString("name")
    val artists = json.getArray("artists")
    val duration = json.getLong("duration_ms")
    val uri = json.getObject("external_urls").getString("spotify")
    var artistNames = ""

    for (i in 0 until artists.length()) {
      artistNames += artists.getObject(i).getString("name")
    }

    return SpotifyTrack(
      title,
      artistNames,
      duration,
      uri,
      requester
    )
  }

  private suspend fun makeRequest(endpoint: String): DataObject {
    if (token == null || renewDate == 0L || Instant.now().toEpochMilli() > renewDate) this.renewToken()
    val request = Request.Builder()
      .url("https://api.spotify.com/v1/$endpoint")
      .addHeader("Authorization", "$token")
      .build()

    val res = D4rkBot.okHttpClient.newCall(request).await()
    val json = DataObject.fromJson(withContext(Dispatchers.IO) { res.body()!!.string() })
    res.close()
    return json
  }

  private suspend fun renewToken() {
    val res = D4rkBot.okHttpClient.newCall(renewRequest).await()
    val body = res.body()

    if (body == null) {
      token = null
      return
    }

    val json = DataObject.fromJson(withContext(Dispatchers.IO) { body.string() })
    res.close()
    renewDate = Instant.now().toEpochMilli() + json.getLong("expires_in") * 1000
    token = "${json.getString("token_type")} ${json.getString("access_token")}"
  }
}