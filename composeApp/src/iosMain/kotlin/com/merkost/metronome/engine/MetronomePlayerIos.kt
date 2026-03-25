package com.merkost.metronome.engine

import com.merkost.metronome.model.Beat
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive

@OptIn(ExperimentalForeignApi::class)
class MetronomePlayerIos : MetronomePlayer {
    private var audioEngine: AVAudioEngine? = null
    private var playerNode: AVAudioPlayerNode? = null

    override fun initialize() {
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
        AVAudioSession.sharedInstance().setActive(true, error = null)

        val engine = AVAudioEngine()
        val player = AVAudioPlayerNode()
        engine.attachNode(player)
        engine.connect(player, engine.mainMixerNode, engine.mainMixerNode.outputFormatForBus(0u))
        engine.startAndReturnError(null)

        audioEngine = engine
        playerNode = player
    }

    override fun play(beat: Beat, stereoLeft: Float, stereoRight: Float) {
        // TODO: Load and schedule audio buffer from bundle resource
        // For now, this is a stub. Full implementation requires loading
        // wood.mp3 from the iOS bundle into AVAudioPCMBuffer.
    }

    override fun stop() {
        playerNode?.stop()
    }

    override fun release() {
        audioEngine?.stop()
        audioEngine = null
        playerNode = null
    }
}
