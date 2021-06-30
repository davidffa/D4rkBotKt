package me.davidffa.d4rkbotkt.audio.filters

import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.davidffa.d4rkbotkt.audio.filters.configs.*

class FilterFactory(private val filters: List<Filter>): PcmFilterFactory {
    override fun buildChain(
        track: AudioTrack?,
        format: AudioDataFormat,
        output: UniversalPcmAudioFilter
    ): MutableList<AudioFilter> {
        val list = mutableListOf<FloatPcmAudioFilter>()

        for (filter in filters) {
            list.add(when (filter) {
                Filter.BASS -> bass(format, list.lastOrNull() ?: output)
                Filter.NIGHTCORE -> nightcore(format, list.lastOrNull() ?: output)
                Filter.POP -> pop(format, list.lastOrNull() ?: output)
                Filter.SOFT -> soft(format, list.lastOrNull() ?: output)
                Filter.TREBLEBASS -> treblebass(format, list.lastOrNull() ?: output)
                Filter.VAPORWAVE -> vaporwave(format, list.lastOrNull() ?: output)
                Filter.VOCALS -> vocals(format, list.lastOrNull() ?: output)
                Filter.EIGHTD -> eightD(format, list.lastOrNull() ?: output)
            })
        }
        return list.reversed().toMutableList()
    }
}