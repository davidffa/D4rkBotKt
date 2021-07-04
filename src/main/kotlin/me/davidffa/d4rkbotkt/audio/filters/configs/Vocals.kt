package me.davidffa.d4rkbotkt.audio.filters.configs

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun vocals(format: AudioDataFormat, output: FloatPcmAudioFilter): List<FloatPcmAudioFilter> {
    val bands = FloatArray(Equalizer.BAND_COUNT){ 0f }
    bands[0] = -0.25f
    bands[1] = -0.25f
    bands[2] = -0.25f
    bands[3] = -0.23f
    bands[4] = -0.20f
    bands[5] = -0.15f
    bands[7] = 0.10f
    bands[8] = 0.15f
    bands[9] = 0.15f
    bands[10] = 0.15f
    bands[11] = 0.10f
    bands[12] = 0.15f
    bands[13] = 0.15f
    bands[14] = -0.25f

    return listOf(Equalizer(format.channelCount, output, bands))
}