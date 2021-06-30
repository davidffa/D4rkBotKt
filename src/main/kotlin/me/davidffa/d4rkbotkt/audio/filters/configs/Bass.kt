package me.davidffa.d4rkbotkt.audio.filters.configs

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

fun bass(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
    val bands = FloatArray(Equalizer.BAND_COUNT)
    bands[0] = 0.6f
    bands[1] = 0.67f
    bands[2] = 0.67f
    bands[3] = 0f
    bands[4] = -0.5f
    bands[5] = 0.15f
    bands[6] = -0.45f
    bands[7] = 0.23f
    bands[8] = 0.35f
    bands[9] = 0.45f
    bands[10] = 0.55f
    bands[11] = 0.6f
    bands[12] = 0.55f
    bands[13] = 0f

    return Equalizer(format.channelCount, output, bands)
}