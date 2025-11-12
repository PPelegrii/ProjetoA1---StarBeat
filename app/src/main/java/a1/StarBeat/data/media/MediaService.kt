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

    // Instancia o ExoPlayer
    private val player = ExoPlayer.Builder(context).build()
    private var mediaListener: MediaListener? = null

    init {
        // Adiciona um listener nativo do ExoPlayer para capturar eventos
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                // Se o estado for STATE_ENDED, a música acabou
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
        // Libera os recursos do player quando não for mais necessário
        player.release()
    }

    override fun isPlaying(): Boolean {
        return player.isPlaying
    }
}