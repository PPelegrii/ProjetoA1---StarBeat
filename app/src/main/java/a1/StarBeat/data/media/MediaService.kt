package a1.StarBeat.data.media

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

interface MediaListener {
    fun onPlaybackEnded()
}
interface MediaService {
    fun play(url: String)
    fun stop()
    fun resetForNewGame()
    fun release()
    fun isPlaying(): Boolean
    fun setListener(listener: MediaListener)
}

class ExoPlayerMediaService(context: Context) : MediaService {

    private val player = ExoPlayer.Builder(context).build()
    private var mediaListener: MediaListener? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {

                if (playbackState == Player.STATE_ENDED) {
                    mediaListener?.onPlaybackEnded()
                }
            }
        })
    }

    override fun setListener(listener: MediaListener) {
        this.mediaListener = listener
    }

    override fun play(url: String) {
        player.stop()

        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)

        player.prepare()
        player.play()
    }

    override fun stop() {
        player.stop()
    }

    override fun resetForNewGame() {
        player.stop()
        player.seekTo(0)
        player.clearMediaItems()
    }

    override fun release() {
        player.release()
    }

    override fun isPlaying(): Boolean {
        return player.isPlaying
    }
}