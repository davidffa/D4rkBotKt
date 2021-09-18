package me.davidffa.d4rkbotkt.audio.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.davidffa.d4rkbotkt.D4rkBot
import me.davidffa.d4rkbotkt.audio.UnresolvedTrack
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await

class Deezer {
  suspend fun getTrack(id: String, requester: Member): UnresolvedTrack {
    val json = makeRequest("track/$id")

    return buildTrack(json, requester)
  }

  suspend fun getAlbum(id: String, requester: Member): List<UnresolvedTrack> {
    val unresolvedTracks = mutableListOf<UnresolvedTrack>()


    val tracks = makeRequest("album/$id/tracks").getArray("data")

    for (i in 0 until tracks.length()) {
      unresolvedTracks.add(buildTrack(tracks.getObject(i), requester))
    }

    return unresolvedTracks.toList()
  }

  suspend fun getPlaylist(id: String, requester: Member): List<UnresolvedTrack> {
    val unresolvedTracks = mutableListOf<UnresolvedTrack>()

    val tracks = makeRequest("playlist/$id/tracks").getArray("data")

    for (i in 0 until tracks.length()) {
      unresolvedTracks.add(buildTrack(tracks.getObject(i).getObject("track"), requester))
    }

    return unresolvedTracks.toList()
  }

  private fun buildTrack(json: DataObject, requester: Member): UnresolvedTrack {
    val title = json.getString("title")
    val artist = json.getObject("artist").getString("name")
    val duration = json.getLong("duration") * 1000
    val uri = json.getString("link")

    return UnresolvedTrack(
      title,
      artist,
      duration,
      uri,
      requester
    )
  }

  private suspend fun makeRequest(endpoint: String): DataObject {
    val request = Request.Builder()
      .url("https://api.deezer.com/$endpoint")
      .build()

    val res = D4rkBot.okHttpClient.newCall(request).await()
    val json = DataObject.fromJson(withContext(Dispatchers.IO) { res.body()!!.string() })
    res.close()

    return json
  }
}