package com.example.musicstreamapp

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicstreamapp.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.palette.graphics.Palette
import com.example.musicstreamapp.databinding.FragmentNowPlayingBinding

class PlayerActivity : AppCompatActivity(),ServiceConnection,MediaPlayer.OnCompletionListener{


    companion object {
        lateinit var musicListPA : ArrayList<String>
        var songPosition: Int = 0
//        var mediaPlayer: MediaPlayer?=null
        var isPlaying:Boolean=false
        lateinit var SongList : List<String>
        var repeate:Boolean =false
        var min15: Boolean =false
        var min30: Boolean =false
        var min60: Boolean =false
        var songTitle :String = "songname"
        var songsubTitle :String = "ArtistnName"
        var songUUrl :String = "songname"
        var musicService:MusicService?=null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var nowPlayingID:String=" "
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val gradient = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(Color.BLACK, Color.WHITE))
        binding.root.background = gradient
        window?.statusBarColor = Color.WHITE
        songPosition=intent.getIntExtra("index",0)




        when(intent.getStringExtra("class"))
        {
            "SongListAdapter"->{
                //for starting service
                val intent=Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                setLayout()
            }
            "SectionSongListAdapter"->{
                //for starting service
                val intent=Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                setLayout()
            }
            "NowPlaying"->{
                setLayout()

                binding.seekBarPA.max= musicService!!.mediaPlayer!!.duration

                val handler= Handler()
                handler.postDelayed(object:Runnable{
                    override fun run() {
                        try{
                            binding.tvSeekBarStart.text= formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                            binding.tvSeekBarEnd.text=formatDuration((musicService!!.mediaPlayer!!.duration.toLong()-musicService!!.mediaPlayer!!.currentPosition.toLong()))
                            binding.seekBarPA.progress= musicService!!.mediaPlayer!!.currentPosition
                            handler.postDelayed(this,1000)
                        } catch(exception: java.lang.Exception){
                            binding.seekBarPA.progress=0
                        }

                    }

                },0)
                if(isPlaying)
                {
                    binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
                }else{
                    binding.playPauseBtnPA.setImageResource(R.drawable.play_icon)
                }
                musicService!!.mediaPlayer!!.setOnCompletionListener(this@PlayerActivity)
            }
        }


//        musicListPA= ArrayList<String>()


        val playpausebtn =findViewById<FloatingActionButton>(R.id.playPauseBtnPA)
        playpausebtn.setOnClickListener{
            if(isPlaying)
            {
                pauseMusic()
            }
            else{
                playMusic()
            }
        }
        val prevbtn =findViewById<FloatingActionButton>(R.id.previousBtnPA)
        val nxtbtn =findViewById<FloatingActionButton>(R.id.nextBtnPA)
        prevbtn.setOnClickListener{
            prevNextSong(increment = false)
            musicService!!.showNotification(R.drawable.pause_icon)
        }
        nxtbtn.setOnClickListener {
            prevNextSong(increment = true)
            musicService!!.showNotification(R.drawable.pause_icon)
        }
        val seekbar=findViewById<SeekBar>(R.id.seekBarPA)
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

        val repeatbutton = findViewById<ImageButton>(R.id.repeatBtnPA)
        repeatbutton.setOnClickListener {
            if(!repeate)
            {
                repeate=true
                repeatbutton.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
            }else
            {
                repeate=false
                repeatbutton.setColorFilter(ContextCompat.getColor(this,R.color.restcolor))
            }
        }
        val backbutton = findViewById<ImageButton>(R.id.imageButton)
        backbutton.setOnClickListener {
            finish()
        }
        val eqbutton = findViewById<ImageButton>(R.id.equalizerBtnPA)
        eqbutton.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE,AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent,13)
            }catch(e: Exception)
            {
                Toast.makeText(this,"Equilizer Feature not Supported",Toast.LENGTH_SHORT).show()
            }

        }
        val tibuttom = findViewById<ImageButton>(R.id.timerBtnPA)
        tibuttom.setOnClickListener {
            val timer =min15|| min30|| min60
            if(!timer)
            {
                showBottomSheetDialog()
            }else{
                val builder =AlertDialog.Builder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do You Want to Stop Timer?")
                    .setIcon(R.drawable.logo)
                    .setPositiveButton("Yes"){_,_->
                        min15=false
                        min30=false
                        min60=false
                        val tibuttom = findViewById<ImageButton>(R.id.timerBtnPA)
                        tibuttom.setColorFilter(ContextCompat.getColor(this,R.color.restcolor))
                    }
                    .setNegativeButton("No"){dialog,_->
                        dialog.dismiss()
                    }
                val customDialog=builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }

        }
        val shbutton = findViewById<ImageView>(R.id.shareBtnPA)
        shbutton.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action=Intent.ACTION_SEND
            shareIntent.type="audio/"
            FirebaseFirestore.getInstance().collection("songs")
                .document(SongList[songPosition]).get()
                .addOnSuccessListener {
                    val song = it.toObject(SongModel::class.java)
                    song?.apply {
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url))
                    }
                }

            startActivity(Intent.createChooser(shareIntent,"Sharing Muisic File!!"))

        }

    }

     fun setLayout(){
        FirebaseFirestore.getInstance().collection("songs")
            .document(SongList[songPosition]).get()
            .addOnSuccessListener {
                val song = it.toObject(SongModel::class.java)
                song?.apply {
                    val civ=findViewById<ImageView>(R.id.song_cover_image_view22)
                    songUUrl=coverUrl
                    Glide.with(baseContext).load(coverUrl)
                        .circleCrop()
                        .into(civ)
                    val songtitle =findViewById<TextView>(R.id.songNamePA)
                    songtitle.text=title

                    val subsongtitle =findViewById<TextView>(R.id.sub)
                    subsongtitle.text=subtitle

                    lifecycleScope.launch {

                        val bgColor = getMainColor(getBitmap(coverUrl))
                        val gradient = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(Color.BLACK, bgColor))
                        binding.root.background = gradient
                        window?.statusBarColor = bgColor


                    }
                }
            }
        val civ1=findViewById<ImageView>(R.id.song_gif_image_view)
        Glide.with(baseContext).load(R.drawable.media_playing)
            .circleCrop()
            .into(civ1)
        showGif(isPlaying)
        if(repeate)
        {
            val repeatbutton = findViewById<ImageButton>(R.id.repeatBtnPA)
            repeatbutton.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
        }
        if(min30|| min60|| min15)
        {
            val tibuttom = findViewById<ImageButton>(R.id.timerBtnPA)
            tibuttom.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
        }
    }


    private suspend fun getBitmap(url:String):Bitmap
    {
        val loading:ImageLoader = ImageLoader(baseContext)
        val request =ImageRequest.Builder(baseContext)
            .data(url)
            .build()
        val result =(loading.execute(request)as SuccessResult).drawable
        return (result as BitmapDrawable).bitmap

    }
    private fun getMainColor(img: Bitmap): Int {
        val newBitmap = img.copy(Bitmap.Config.ARGB_8888, true)
        val newImg = Bitmap.createScaledBitmap(newBitmap, 2, 2, true)

        // Calculate the average color of the 2x2 grid
        val color = calculateAverageColor(newImg)

        newImg.recycle()
        return color
    }

    private fun calculateAverageColor(img: Bitmap): Int {
        var red = 0
        var green = 0
        var blue = 0

        // Iterate through the pixels in the 2x2 grid
        for (x in 0 until img.width) {
            for (y in 0 until img.height) {
                val pixelColor = img.getPixel(x, y)
                red += Color.red(pixelColor)
                green += Color.green(pixelColor)
                blue += Color.blue(pixelColor)
            }
        }

        // Calculate the average color
        val pixelCount = img.width * img.height
        red /= pixelCount
        green /= pixelCount
        blue /= pixelCount

        // Compose the average color
        return Color.rgb(red, green, blue)
    }

    private fun createMediaPlayer()
    {
        try{
            FirebaseFirestore.getInstance().collection("songs")
                .document(SongList[songPosition]).get()
                .addOnSuccessListener {
                    val song = it.toObject(SongModel::class.java)
                    song?.apply {
                        nowPlayingID=id
                        songTitle=title
                        songsubTitle=subtitle
                        if(musicService!!.mediaPlayer ==null) musicService!!.mediaPlayer= MediaPlayer()
                        musicService!!.mediaPlayer!!.reset()

                        musicService!!.mediaPlayer!!.setDataSource(this@PlayerActivity,url.toUri())
                        musicService!!.mediaPlayer!!.prepare()
                        musicService!!.mediaPlayer!!.start()
                        isPlaying=true
                        showGif(isPlaying)
                        val playpausebtn =findViewById<FloatingActionButton>(R.id.playPauseBtnPA)
                        playpausebtn.setImageResource(R.drawable.pause_icon)
                        val tvseekbarsttxt =findViewById<TextView>(R.id.tvSeekBarStart)
                        val tvseekbarentxt =findViewById<TextView>(R.id.tvSeekBarEnd)
                        tvseekbarsttxt.text=formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                        tvseekbarentxt.text=formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                        val seekbar=findViewById<SeekBar>(R.id.seekBarPA)
                        seekbar.progress=0
                        seekbar.max= musicService!!.mediaPlayer!!.duration


                        val handler= Handler()
                        handler.postDelayed(object:Runnable{
                            override fun run() {
                                try{
                                    tvseekbarsttxt.text=formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                                    tvseekbarentxt.text=formatDuration((musicService!!.mediaPlayer!!.duration.toLong()-musicService!!.mediaPlayer!!.currentPosition.toLong()))
                                    seekbar.progress=musicService!!.mediaPlayer!!.currentPosition
                                    handler.postDelayed(this,1000)
                                } catch(exception: java.lang.Exception){
                                    seekbar.progress=0
                                }

                            }

                        },0)
                        musicService!!.mediaPlayer!!.setOnCompletionListener(this@PlayerActivity)
                    }
                }
        }catch(e: Exception)
        {
            return
        }


//        if(mediaPlayer ==null) mediaPlayer= MediaPlayer()
//        mediaPlayer!!.reset()
//        mediaPlayer!!.setDataSource(this@PlayerActivity,musicListPA[songPosition].toUri())
//        mediaPlayer!!.prepare()
//        mediaPlayer!!.start()
//        isPlaying=true
//        showGif(isPlaying)
//        val playpausebtn =findViewById<FloatingActionButton>(R.id.playPauseBtnPA)
//        playpausebtn.setImageResource(R.drawable.pause_icon)
    }


    fun playMusic(){
        val playpausebtn =findViewById<FloatingActionButton>(R.id.playPauseBtnPA)
        playpausebtn.setImageResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying=true
        showGif(isPlaying)
        musicService!!.mediaPlayer!!.start()
        NowPlaying.binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon)
    }
    fun pauseMusic(){
        val playpausebtn =findViewById<FloatingActionButton>(R.id.playPauseBtnPA)
        playpausebtn.setImageResource(R.drawable.play_icon)
        musicService!!.showNotification(R.drawable.play_icon)
        isPlaying=false
        showGif(isPlaying)
        musicService!!.mediaPlayer!!.pause()
        NowPlaying.binding.playPauseBtnNP.setImageResource(R.drawable.play_icon)
    }

    fun prevNextSong(increment: Boolean) {
        if(increment)
        {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }
        else
        {
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }



    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        setLayout()
        createMediaPlayer()
        musicService!!.showNotification(R.drawable.pause_icon)
        NowPlaying.binding.songNameNP.isSelected = true

        FirebaseFirestore.getInstance().collection("songs")
            .document(SongList[songPosition]).get()
            .addOnSuccessListener {
                val song = it.toObject(SongModel::class.java)
                song?.apply {
                    Glide.with(applicationContext)
                        .load(songUUrl)
                        .apply(RequestOptions().placeholder(R.drawable.logo).centerCrop())
                        .into(NowPlaying.binding.songImgNP)
                    NowPlaying.binding.songNameNP.text = "Now Playing: $title"
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==13 || resultCode== RESULT_OK)
        {
            return
        }
    }

    private fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 15 minutes!!",Toast.LENGTH_SHORT).show()
            val tibuttom = findViewById<ImageButton>(R.id.timerBtnPA)
            tibuttom.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
            Thread{Thread.sleep(15*60000)
            if(min15){
                if(musicService!!.mediaPlayer!=null) {
                    musicService!!.mediaPlayer!!.release()
                    musicService!!.mediaPlayer = null
                }
            }
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 30 minutes!!",Toast.LENGTH_SHORT).show()
            val tibuttom = findViewById<ImageButton>(R.id.timerBtnPA)
            tibuttom.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
            Thread{Thread.sleep(30*60000)
                if(min30){
                    if(musicService!!.mediaPlayer!=null) {
                        musicService!!.mediaPlayer!!.release()
                        musicService!!.mediaPlayer = null
                    }
                }
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(baseContext,"Music will stop after 60 minutes!!",Toast.LENGTH_SHORT).show()
            val tibuttom = findViewById<ImageButton>(R.id.timerBtnPA)
            tibuttom.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink))
            Thread{Thread.sleep(60*60000)
                if(min60){
                    if(musicService!!.mediaPlayer!=null) {
                        musicService!!.mediaPlayer!!.release()
                        musicService!!.mediaPlayer = null
                    }
                }
            }.start()
            dialog.dismiss()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder=service as MusicService.MyBinder
        musicService=binder.currentService()
        createMediaPlayer()
        songPosition=intent.getIntExtra("index",0)
        musicService!!.showNotification(R.drawable.pause_icon)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService=null
    }


}