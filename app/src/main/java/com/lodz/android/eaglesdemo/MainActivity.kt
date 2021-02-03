package com.lodz.android.eaglesdemo

import android.os.Bundle
import android.widget.Button
import com.lodz.android.corekt.anko.bindView
import com.lodz.android.pandora.base.activity.BaseActivity
import com.lodz.android.pandora.widget.base.TitleBarLayout

class MainActivity : BaseActivity() {

    private val mScanBtn by bindView<Button>(R.id.scan_btn)

    private val mPhotoBtn by bindView<Button>(R.id.photo_btn)

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

        }

        mPhotoBtn.setOnClickListener {

        }
    }

    override fun initData() {
        super.initData()
        showStatusCompleted()
    }
}