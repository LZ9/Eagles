package com.lodz.android.eagles

import android.content.Context
import android.net.Uri
import com.lodz.android.eagles.event.EaglesScanEvent
import com.rexnjc.ui.tool.ToolsCaptureActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 二维码扫描管理器
 * @author zhouL
 * @date 2021/3/16
 */
class EaglesManager private constructor(){

    companion object{
        const val TAG = "EAGLES_SCAN"

        @JvmStatic
        fun create(): EaglesManager = EaglesManager()
    }

    /** 监听器 */
    private var mListener: OnScanListener? = null


    /** 设置监听器[listener] */
    fun setOnScanListener(listener: OnScanListener?) :EaglesManager{
        mListener = listener
        return this
    }

    /** 扫描二维码 */
    fun scan(context: Context){
        EventBus.getDefault().register(this)
        ToolsCaptureActivity.start(context)
    }

    fun picRecogAsync(context: Context, paht: String) {

    }

    fun picRecogAsync(context: Context, uri: Uri) {

    }

    //
    //    /**
    //     * 图片传入识别
    //     * @param context 上下文
    //     * @param picPath 图片路径
    //     */
    //    public void picRecogAsync(Context context, String picPath) {
    //        picRecogAsync(context, CommonTools.filePath2Uri(context, picPath));
    //    }
    //
    //    /**
    //     * 图片传入识别
    //     * @param context 上下文
    //     * @param uri 图片uri
    //     */
    //    public void picRecogAsync(final Context context, final Uri uri) {
    //        boolean isSuccess = checkPath(context);
    //        if (!isSuccess) {
    //            if (mListener != null) {
    //                mListener.onResult(false, "存储文件夹创建失败", "", "");
    //            }
    //            return;
    //        }
    //        ThreadManager.getInstance().execute(new Runnable() {
    //            @Override
    //            public void run() {
    //                final PicRecogHelper recog = new PicRecogHelper(context);
    //                try {
    //                    Thread.sleep(500);
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }
    //                boolean isComplete = CommonTools.copyFileFromUri(context, uri, mSavePath, "plate.jpg");
    //                if (!isComplete) {
    //                    runMainUI(false, "图片转存失败", "", "");
    //                    return;
    //                }
    //                recog.recogPicResults(mSavePath + "plate.jpg", new OnPicRecogListener() {
    //                    @Override
    //                    public void onRecogResult(String[] result) {
    //                        if (result == null || result.length <= 2) {
    //                            runMainUI(false, "识别失败", "", "");
    //                            return;
    //                        }
    //                        String number = result[0];
    //                        String color = result[1];
    //                        if (TextUtils.isEmpty(number)) {
    //                            runMainUI(false, "识别失败", "", "");
    //                            return;
    //                        }
    //                        runMainUI(true, "识别完成", number, color);
    //                        recog.releaseService();
    //                    }
    //                });
    //            }
    //        });
    //    }
    //


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEaglesScanEvent(event: EaglesScanEvent) {
        EventBus.getDefault().unregister(this);
        mListener?.onResult(event.isSuccess, event.text)
    }

    /** 扫描结果监听器回调 */
    fun interface OnScanListener {
        fun onResult(isSuccess: Boolean, text: String)
    }
}