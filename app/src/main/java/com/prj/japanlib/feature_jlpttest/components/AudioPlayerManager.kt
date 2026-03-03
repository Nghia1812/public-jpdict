package com.prj.japanlib.feature_jlpttest.components

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

class AudioPlayerManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)
) {

    private val mExoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val mCurrentAudioUrl = MutableStateFlow<String?>(null)
    private val mIsPlaying = MutableStateFlow(false)
    private val mDuration = MutableStateFlow(0L)
    private val mPosition = MutableStateFlow(0L)
    private val mProgress = MutableStateFlow(0f)

    val currentAudioUrl: StateFlow<String?> = mCurrentAudioUrl.asStateFlow()
    val isPlaying: StateFlow<Boolean> = mIsPlaying.asStateFlow()
    val duration: StateFlow<Long> = mDuration.asStateFlow()
    val position: StateFlow<Long> = mPosition.asStateFlow()
    val progress: StateFlow<Float> = mProgress.asStateFlow()

    // Polling progress  playing
    private var pollingJob: Job? = null

    init {
        mExoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                mIsPlaying.value = isPlayingNow
                Timber.i("onIsPlayingChanged: isPlaying = $isPlayingNow")

                // Start/stop polling playing
                if (isPlayingNow) {
                    startProgressPolling()
                } else {
                    stopProgressPolling()
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                // Duration & position
                val newDuration = player.duration.coerceAtLeast(0L)
                val newPosition = player.currentPosition.coerceAtLeast(0L)

                mDuration.value = newDuration
                mPosition.value = newPosition
                updateProgress(newDuration, newPosition)

                Timber.i("onEvents: duration = $newDuration, position = $newPosition")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    mIsPlaying.value = false
                    mPosition.value = 0L
                    mProgress.value = 0f
                    Timber.i("onPlaybackStateChanged: STATE_ENDED -> reset progress")
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Timber.e(error, "onPlayerError")
                mIsPlaying.value = false
                mPosition.value = 0L
                mProgress.value = 0f
            }
        })
    }

    private fun startProgressPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                try {
                    val currentPos = mExoPlayer.currentPosition.coerceAtLeast(0L)
                    val dur = mDuration.value
                    mPosition.value = currentPos
                    updateProgress(dur, currentPos)
                } catch (e: Exception) {
                    Timber.e(e, "progress polling error")
                }
                delay(300.milliseconds)
            }
        }
        Timber.i("startProgressPolling: started")
    }

    private fun stopProgressPolling() {
        pollingJob?.cancel()
        pollingJob = null
        Timber.i("stopProgressPolling: stopped")
    }

    private fun updateProgress(duration: Long, position: Long) {
        mProgress.value = if (duration > 0) {
            (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    fun play(audioUrl: String) {
        if (audioUrl.isBlank()) {
            Timber.i("play: audioUrl is blank -> skip (abnormal case)")
            return
        }

        if (mCurrentAudioUrl.value != audioUrl) {
            mExoPlayer.setMediaItem(MediaItem.fromUri(audioUrl))
            mExoPlayer.prepare()
            mCurrentAudioUrl.value = audioUrl
            Timber.i("play: prepared new media item -> $audioUrl")
        }

        mExoPlayer.play()
        Timber.i("play: invoked play()")
    }

    fun pause() {
        mExoPlayer.pause()
        Timber.i("pause: invoked pause()")
    }

    fun togglePlayPause(audioUrl: String) {
        if (mCurrentAudioUrl.value == audioUrl && mIsPlaying.value) {
            pause()
        } else {
            play(audioUrl)
        }
    }

    fun release() {
        stopProgressPolling()
        mExoPlayer.release()
        scope.cancel()
        Timber.i("release: ExoPlayer released")
    }

    /**
     * Seeks to the specified position in milliseconds.
     * Only performs seek if there is an active media item.
     */
    fun seekTo(positionMillis: Long) {
        if (mCurrentAudioUrl.value.isNullOrBlank()) {
            // Abnormal case: no active audio → do nothing
            Timber.i("seekTo: no active audio, skipping seek")
            return
        }

        if (positionMillis < 0 || positionMillis > mDuration.value) {
            // Abnormal case: invalid position -> skip
            Timber.i("seekTo: invalid position $positionMillis (duration = ${mDuration.value}), clamping")
            return
        }

        try {
            mExoPlayer.seekTo(positionMillis)
            mPosition.value = positionMillis.coerceIn(0L, mDuration.value)
            updateProgress(mDuration.value, positionMillis)
            Timber.i("seekTo: sought to $positionMillis ms")
        } catch (e: Exception) {
            Timber.e(e, "seekTo: failed to seek to $positionMillis ms")
            // Error case: reset to safe state if needed
        }
    }
}
