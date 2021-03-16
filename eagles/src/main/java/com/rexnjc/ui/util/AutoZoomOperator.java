package com.rexnjc.ui.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.rexnjc.ui.tool.ToolsCaptureActivity;

/**
 * author : leilei.yll
 * date : 4/21/17.
 * email : leilei.yll@alibaba-inc.com
 * 旺旺 : 病已
 */
public class AutoZoomOperator {
    private static final String TAG = "AutoZoomOperator";
    private volatile boolean disableConitueZoom;
    private ToolsCaptureActivity mActivity;
    private static Handler handler = new Handler(Looper.getMainLooper());
    private final int MaxIndex = 10;

    public AutoZoomOperator(ToolsCaptureActivity activity) {
        this.mActivity = activity;
    }

    public void clearActivity() {
        this.mActivity = null;
    }

    public void startAutoZoom(final float rate, final int curIndex) {
        Log.d(TAG, "startAutoZoom : rate is " + rate + ", curIndex is " + curIndex);
        if(rate < 0 || disableConitueZoom || curIndex >= MaxIndex) {
            disableConitueZoom = false;
            return;
        }
        disableConitueZoom = true;
        invalidate(0, (int) rate);
    }

    private void setZoom(int curRate, int curIndex, int maxZoom) {
        if(mActivity == null) {
            return;
        }
        mActivity.setZoom(curRate);
        invalidate(++curIndex, maxZoom);
    }

    private void invalidate(final int curIndex, final int maxZoom) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(curIndex < MaxIndex) {
                    setZoom((int)(maxZoom*1.f/MaxIndex)*(curIndex+1), curIndex, maxZoom);
                } else {
                    disableConitueZoom = false;
                }
            }
        }, 20);

    }

}
