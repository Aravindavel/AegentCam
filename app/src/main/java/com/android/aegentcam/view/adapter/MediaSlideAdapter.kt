package com.android.aegentcam.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.android.aegentcam.R
import com.bumptech.glide.Glide

class MediaSliderAdapter(
    private val context: Context,
    private val mediaList: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_IMAGE = 0
        private const val TYPE_VIDEO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (mediaList[position].endsWith(".mp4", true)) TYPE_VIDEO else TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_IMAGE) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_image_slider, parent, false)
            ImageViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_video_slider, parent, false)
            VideoViewHolder(view)
        }
    }

    @OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mediaPath = mediaList[position]

        if (holder is ImageViewHolder) {
            Glide.with(context)
                .load(mediaPath)
                .into(holder.imageView)
        }

        if (holder is VideoViewHolder) {
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)

            val mediaItem = MediaItem.Builder()
                .setUri(mediaPath)
                .setMimeType(MimeTypes.VIDEO_MP4)  // Force MP4 type
                .build()

            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)

            val player = SimpleExoPlayer.Builder(context).build()
            holder.playerView.player = player
            player.setMediaSource(mediaSource)
            player.prepare()
            player.playWhenReady = true

            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("ExoPlayer Error", "Error playing video: ${error.message}")
                    Toast.makeText(context, "Video playback error", Toast.LENGTH_SHORT).show()
                }
            })


        }
    }

    override fun getItemCount(): Int = mediaList.size

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivFullScreenImage)
    }

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerView: PlayerView = view.findViewById(R.id.videoPlayerView)
    }
}
