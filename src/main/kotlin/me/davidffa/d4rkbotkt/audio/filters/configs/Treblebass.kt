package me.davidffa.d4rkbotkt.audio.filters.configs

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun treblebass(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
    val bands = FloatArray(Equalizer.BAND_COUNT)
    bands[0] = 0.55f
    bands[1] = 0.55f
    bands[2] = 0.5f
    bands[3] = 0f
    bands[4] = 0.15f
    bands[5] = 0.3f
    bands[6] = 0.45f
    bands[7] = 0.23f
    bands[8] = 0.35f
    bands[9] = 0.45f
    bands[10] = 0.55f
    bands[11] = 0.55f
    bands[12] = 0.5f
    bands[13] = 0.10f

    return listOf(Equalizer(format.channelCount, output, bands))
}