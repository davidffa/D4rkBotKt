package me.davidffa.d4rkbotkt.audio.filters.configs

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun soft(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
    val bands = FloatArray(Equalizer.BAND_COUNT)
    bands[0] = 0f
    bands[1] = 0f
    bands[2] = 0f
    bands[3] = 0f
    bands[4] = 0f
    bands[5] = 0f
    bands[6] = 0f
    bands[7] = 0f
    bands[8] = -0.25f
    bands[9] = -0.25f
    bands[10] = -0.25f
    bands[11] = -0.25f
    bands[12] = -0.25f
    bands[13] = -0.25f

    return Equalizer(format.channelCount, output, bands)
}