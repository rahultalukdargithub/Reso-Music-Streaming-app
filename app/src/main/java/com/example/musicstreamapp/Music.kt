package com.example.musicstreamapp

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.View
import android.widget.ImageView
import java.util.concurrent.TimeUnit

class Music {
}

fun setSongPosition(increment: Boolean) {
    if(!PlayerActivity.repeate)
    {
        if(increment)
        {
            if(PlayerActivity.musicListPA.size-1== PlayerActivity.songPosition)
            {
                PlayerActivity.songPosition =0
            }
            else{
                ++PlayerActivity.songPosition
            }
        }
        else
        {
            if(PlayerActivity.songPosition ==0)
            {
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size-1
            }
            else{
                --PlayerActivity.songPosition
            }
        }
    }

}

fun formatDuration(duration: Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes* TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

fun showGif(show : Boolean){
    if(show)
    {
        PlayerActivity.binding.songGifImageView.visibility = View.VISIBLE
    }
    else
    {
        PlayerActivity.binding.songGifImageView.visibility = View.INVISIBLE
    }
}



fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}


