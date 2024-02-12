package com.example.musicstreamapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicstreamapp.databinding.FragmentNowPlayingBinding
import com.google.firebase.firestore.FirebaseFirestore

class NowPlaying : Fragment() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {

        val view=inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        binding.playPauseBtnNP.setOnClickListener {
            if(PlayerActivity.isPlaying)
            {
                pauseMusic()
            }else
            {
                playMusic()
            }
        }
        binding.nextBtnNP.setOnClickListener {
            prevNextSong(increment = true, context = requireContext())
            binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        }

        binding.root.setOnClickListener {
            val intent= Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index",PlayerActivity.songPosition)
            intent.putExtra("class","NowPlaying")
//            PlayerActivity.SongList=songIdList
//            PlayerActivity.musicListPA=musicListPA
            ContextCompat.startActivity(requireContext(),intent,null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if(PlayerActivity.musicService != null){

            binding.root.visibility = View.VISIBLE
            FirebaseFirestore.getInstance().collection("songs")
                .document(PlayerActivity.SongList[PlayerActivity.songPosition]).get()
                .addOnSuccessListener {
                    val song = it.toObject(SongModel::class.java)
                    song?.apply {
                        Glide.with(requireContext())
                            .load(coverUrl)
                            .apply(RequestOptions().placeholder(R.drawable.logo).centerCrop())
                            .into(binding.songImgNP)
                        binding.songNameNP.isSelected = true

                        binding.songNameNP.text= "Now Playing: $title"
                    }
                }
            if(PlayerActivity.isPlaying) binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon)
            else binding.playPauseBtnNP.setImageResource(R.drawable.play_icon)

        }
    }

    private fun playMusic(){
        binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon)
        PlayerActivity.isPlaying =true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
//        val playerActivityInstance = PlayerActivity()
//        val playpausebtn =playerActivityInstance.findViewById<FloatingActionButton>(R.id.playPauseBtnPA)
        PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
        PlayerActivity.binding.songGifImageView.visibility = View.VISIBLE
    }
    private fun pauseMusic(){
        binding.playPauseBtnNP.setImageResource(R.drawable.play_icon)
        PlayerActivity.isPlaying =false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
//        val playerActivityInstance = PlayerActivity()
//        val playpausebtn =playerActivityInstance.findViewById<FloatingActionButton>(R.id.playPauseBtnPA)
        PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.play_icon)
        PlayerActivity.binding.songGifImageView.visibility = View.INVISIBLE
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
                        PlayerActivity.nowPlayingID =id

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
                            .into(binding.songImgNP)
                        binding.songNameNP.isSelected = true

                        binding.songNameNP.text= "Now Playing: $title"
                    }
                }
        }catch(e: Exception)
        {
            return
        }
    }

}