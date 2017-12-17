package esme.projet_video

/**
 * Created by darrieu on 10/12/17.
 */


import android.R.raw
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.support.v7.app.AppCompatActivity

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView

import java.io.IOException
import java.net.URI
import java.sql.Time
import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    private var mPrevButton: Button? = null
    private var mPlayButton: Button? = null
    private var mPauseButton: Button? = null
    private var mNextButton: Button? = null
    private var mBackwardButton: Button? = null
    private var mForwardButton: Button? = null
    private var mVideoView: VideoView? = null
    private var arrayList: ArrayList<android.net.Uri>? = null
    private var index: Int = 0
    private val seekForwardTime = 5000
    private val seekBackwardTime = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPrevButton = findViewById(R.id.PrevButton) as Button
        mPlayButton = findViewById(R.id.PlayButton) as Button
        mPauseButton = findViewById(R.id.PauseButton) as Button
        mNextButton = findViewById(R.id.NextButton) as Button
        mForwardButton = findViewById(R.id.ForwardButton) as Button
        mBackwardButton = findViewById(R.id.BackwardButton) as Button
        mVideoView = findViewById(R.id.videoView) as VideoView
        arrayList = ArrayList()



        arrayList!!.add(Uri.parse("android.resource://" + packageName + "/" + R.raw.fusee))
        arrayList!!.add(Uri.parse("android.resource://" + packageName + "/" + R.raw.ski))

        setupMedia()
        setupListeners()
    }

    private fun setupMedia() {

        index = 0
        mVideoView!!.setVideoURI(arrayList!![index])
    }

    private fun setupListeners() {

        mPlayButton!!.setOnClickListener { mVideoView!!.start() }

        mPauseButton!!.setOnClickListener { mVideoView!!.pause() }

        mNextButton!!.setOnClickListener {
            if (index < arrayList!!.size - 1) {
                mVideoView!!.setVideoURI(arrayList!![++index])
                mVideoView!!.start()
            } else
                setupMedia()
        }

        mPrevButton!!.setOnClickListener {
            if (index > 0) {
                mVideoView!!.setVideoURI(arrayList!![--index])
                mVideoView!!.start()
            } else {
                index = arrayList!!.size - 1
                mVideoView!!.setVideoURI(arrayList!![index])
                mVideoView!!.start()
            }
        }

        mBackwardButton!!.setOnClickListener { rewindSong() }

        mForwardButton!!.setOnClickListener { forwardSong() }

    }


    fun forwardSong() {
        if (mVideoView != null) {
            val currentPosition = mVideoView!!.currentPosition
            if (currentPosition + seekForwardTime <= mVideoView!!.duration) {
                mVideoView!!.seekTo(currentPosition + seekForwardTime)
            } else {
                mVideoView!!.seekTo(mVideoView!!.duration)
            }

            mVideoView!!.start()//start your video.
        }
    }

    fun rewindSong() {
        if (mVideoView != null) {
            val currentPosition = mVideoView!!.currentPosition
            if (currentPosition - seekBackwardTime >= 0) {
                mVideoView!!.seekTo(currentPosition - seekBackwardTime)
            } else {
                mVideoView!!.seekTo(0)
            }
        }
    }

}


/*public void test(VideoView videoView, final float vitesse){
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                PlaybackParams myPlayBackParams = new PlaybackParams();
                myPlayBackParams.setSpeed(vitesse);
                mp.setPlaybackParams(myPlayBackParams);*/