package com.rexnjc.ui.widget.ma;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.alipay.mobile.mascanengine.IOnMaSDKDecodeInfo;
import com.lodz.android.eagles.R;
import com.rexnjc.ui.tool.ToolsCaptureActivity;
import com.rexnjc.ui.widget.TorchView;

/**
 * author : leilei.yll
 * date : 8/28/16.
 * email : leilei.yll@alibaba-inc.com
 * 旺旺 : 病已
 */
public class ToolScanTopView  extends RelativeLayout implements IOnMaSDKDecodeInfo {
    public static final String TAG = "ToolScanTopView";
    protected TopViewCallback topViewCallback;
    private TorchView torchView;
    private ToolsCaptureActivity mActivity;
    private int low_threshold = 70;
    private Runnable showTorchRunnable;
    private int high_threshold = 140;
    private Runnable hideTorchRunnable;

    public ToolScanTopView(Context context) {
        this(context, null);
    }

    public ToolScanTopView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToolScanTopView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void attachActivity(ToolsCaptureActivity toolsCaptureActivity) {
        this.mActivity = toolsCaptureActivity;
    }

    public void detachActivity() {
        this.mActivity = null;
    }

    private void init(Context ctx) {
        LayoutInflater.from(ctx).inflate(R.layout.view_ma_tool_top, this, true);
        torchView = (TorchView) findViewById(R.id.torch_view);
        torchView.setOnTorchClickListener(new TorchView.OnTorchClickListener() {
            @Override
            public void onTorchStateSwitch() {
                if (topViewCallback != null) {
                    boolean isTorchOn = topViewCallback.turnTorch();
                    onTorchState(isTorchOn);
                }
            }
        });
    }


    public void onResultMa(final com.alipay.mobile.mascanengine.MultiMaScanResult maScanResult) {
        if(mActivity!=null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (maScanResult == null) {
//                        Toast.makeText(getContext().getApplicationContext(), "error:maScanResult ==null", Toast.LENGTH_LONG).show();
                        Log.e("testtag", "error:maScanResult ==null");
                    } else {
//                        Toast.makeText(getContext().getApplicationContext(), maScanResult.maScanResults[0].text, Toast.LENGTH_LONG).show();
                        Log.d("testtag", "onResultMa: " + maScanResult.maScanResults[0].text);
                    }
                }
            });
        }
    }

    public void setTopViewCallback(TopViewCallback callback){
        this.topViewCallback = callback;
    }

    @Override public void onGetMaProportion(float v) {
        if(mActivity == null || mActivity.isFinishing()) {
            return;
        }
        Log.d(TAG, "The ma proportion is " + v);
        final int zoom = (int) (75  - 75 * v);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startContinuousZoom(zoom);
            }
        });
    }

    private void startContinuousZoom(int ratio) {
        if (mActivity != null) {
            mActivity.startContinueZoom(ratio);
        }
    }

    @Override public void onGetAvgGray(int gray) {
        if (gray != 0) {
            if (gray < low_threshold) {
                if (showTorchRunnable == null) {
                    showTorchRunnable = new Runnable() {
                        @Override
                        public void run() {
                            ToolScanTopView.this.showTorch();
                        }
                    };
                }
                mActivity.runOnUiThread(showTorchRunnable);
            } else if (gray > high_threshold) {
                if (hideTorchRunnable == null) {
                    hideTorchRunnable = new Runnable() {
                        @Override
                        public void run() {
                            ToolScanTopView.this.hideTorch();
                        }
                    };
                }
                mActivity.runOnUiThread(hideTorchRunnable);
            }
        }
    }

    private void onTorchState(boolean isOn) {
        if (torchView != null) {
            torchView.showTorchState(isOn);
        }
    }

    public void showTorch() {
        if (torchView != null) {
            torchView.showTorch();
        }
    }

    public void hideTorch() {
        if (mActivity != null && mActivity.isTorchOn()) {
            return;
        }
        if (torchView != null) {
            torchView.resetState();
        }
    }

    public interface TopViewCallback {
        /**
         * 切换闪光灯状态
         * @return 切换后的状态
         */
        boolean turnTorch();

        void startPreview();
        void stopPreview(boolean clearSurface);
        void clearSurface();
        void scanSuccess();
        void turnEnvDetection(boolean on);
    }
}
