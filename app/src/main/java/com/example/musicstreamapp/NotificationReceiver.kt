package com.example.musicstreamapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.system.exitProcess

class NotificationReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {


        when(intent?.action){
            //only play next or prev song, when music list contains more than one song
            ApplicationClass.PREVIOUS -> if(PlayerActivity.musicListPA.size > 1) prevNextSong(increment = false,context=context!!)
            ApplicationClass.PLAY -> if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> if(PlayerActivity.musicListPA.size > 1) prevNextSong(increment = true, context = context!!)
            ApplicationClass.EXIT ->{
//                exitApplication()
                PlayerActivity.musicService!!.stopForeground(false)
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                PlayerActivity.musicService=null
                exitProcess(1)
            }
        }
    }

    private fun playMusic(){
        PlayerActivity.isPlaying =true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
//        val playerActivityInstance = PlayerActivity()
//        val playpausebtn =playerActivityInstance.findViewById<FloatingActionButton>(R.id.playPauseBtnPA)
        PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
        PlayerActivity.binding.songGifImageView.visibility = View.VISIBLE
        NowPlaying.binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon)
    }
    private fun pauseMusic(){
        PlayerActivity.isPlaying =false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
//        val playerActivityInstance = PlayerActivity()
//        val playpausebtn =playerActivityInstance.findViewById<FloatingActionButton>(R.id.playPauseBtnPA)
        PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.play_icon)
        PlayerActivity.binding.songGifImageView.visibility = View.INVISIBLE
        NowPlaying.binding.playPauseBtnNP.setImageResource(R.drawable.play_icon)
    }


    private fun prevNextSong(increment:Boolean,context: Context)
    {
        setSongPosition(increment=increment)
        try{
            FirebaseFirestore.getInstance().collection("songs")
                .document(PlayerActivity.SongList[PlayerActivity.songPosition]).get()
                .addOnSuccessListener {
                    val song = it.toObject(SongModel::class.java)
                    song?.apply {

                        Glide.with(context).load(coverUrl)
                            .circleCrop()
                            .into(PlayerActivity.binding.songCoverImageView22)
                        PlayerActivity.nowPlayingID =id
                        PlayerActivity.binding.songNamePA.text=title
                        PlayerActivity.binding.sub.text=subtitle


                        PlayerActivity.musicService!!.mediaPlayer!!.reset()
                        PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(context,url.toUri())
                        PlayerActivity.musicService!!.mediaPlayer!!.prepare()
                        PlayerActivity.musicService!!.mediaPlayer!!.start()
                        PlayerActivity.isPlaying =true
                        showGif(PlayerActivity.isPlaying)
                        PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
                        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
                        PlayerActivity.binding.tvSeekBarStart.text=formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
                        PlayerActivity.binding.tvSeekBarEnd.text=formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.duration.toLong())
                        PlayerActivity.binding.seekBarPA.progress=0
                        PlayerActivity.binding.seekBarPA.max= PlayerActivity.musicService!!.mediaPlayer!!.duration


                        val handler= Handler()
                        handler.postDelayed(object:Runnable{
                            override fun run() {
                                try{
                                    PlayerActivity.binding.tvSeekBarStart.text=formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
                                    PlayerActivity.binding.tvSeekBarEnd.text=formatDuration((PlayerActivity.musicService!!.mediaPlayer!!.duration.toLong()- PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong()))
                                    PlayerActivity.binding.seekBarPA.progress= PlayerActivity.musicService!!.mediaPlayer!!.currentPosition
                                    handler.postDelayed(this,1000)
                                } catch(exception: java.lang.Exception){
                                    PlayerActivity.binding.seekBarPA.progress=0
                                }
                            }
                        },0)


                        Glide.with(context)
                            .load(coverUrl)
                            .apply(RequestOptions().placeholder(R.drawable.logo).centerCrop())
                            .into(NowPlaying.binding.songImgNP)
                        NowPlaying.binding.songNameNP.isSelected = true
                        NowPlaying.binding.songNameNP.text= "Now Playing: $title"
                    }
                }
        }catch(e: Exception)
        {
            return
        }
    }
}