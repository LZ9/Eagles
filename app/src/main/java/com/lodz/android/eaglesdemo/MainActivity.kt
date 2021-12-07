package com.lodz.android.eaglesdemo

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.lodz.android.corekt.anko.bindView
import com.lodz.android.eagles.EaglesManager
import com.lodz.android.pandora.base.activity.BaseActivity
import com.lodz.android.pandora.widget.base.TitleBarLayout

class MainActivity : BaseActivity() {

    private val mScanBtn by bindView<Button>(R.id.scan_btn)

    private val mPhotoBtn by bindView<Button>(R.id.photo_btn)

    private val mResultTv by bindView<TextView>(R.id.result_tv)

    private var mMediaPlayer: MediaPlayer? = null


    private val mLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        mResultTv.text = uri?.toString() ?: "未选择图片"
    }

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun findViews(savedInstanceState: Bundle?) {
        super.findViews(savedInstanceState)
        initTitleBar(getTitleBarLayout())
    }

    private fun initTitleBar(titleBarLayout: TitleBarLayout) {
        titleBarLayout.setTitleName(R.string.app_name)
        titleBarLayout.needBackButton(false)
        titleBarLayout.setBackgroundResource(R.color.purple_500)
    }

    override fun setListeners() {
        super.setListeners()
        mScanBtn.setOnClickListener {
            mResultTv.text = ""
            EaglesManager.create()
                .setOnScanListener { isSuccess, text ->
                    soundBeep()
                    mResultTv.text = "isSuccess : $isSuccess \n $text"
                }
                .scan(getContext())
        }

        mPhotoBtn.setOnClickListener {
            mLauncher.launch("image/*")
        }
    }

    override fun initData() {
        super.initData()
        initMediaPlayer()
        showStatusCompleted()
    }

    private fun initMediaPlayer(){
        val audioManager = getContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        if (volume > 0){
            mMediaPlayer = MediaPlayer.create(getContext(), R.raw.beep)
        }
    }

    private fun soundBeep(){
        mMediaPlayer?.start()
    }

    private fun releaseMediaPlayer(){
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    override fun finish() {
        super.finish()
        releaseMediaPlayer()
    }
}