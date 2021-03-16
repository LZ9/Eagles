package com.rexnjc.ui.widget.ma;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.alipay.mobile.mascanengine.IOnMaSDKDecodeInfo;
import com.lodz.android.eagles.R;
import com.rexnjc.ui.tool.ToolsCaptureActivity;
import com.rexnjc.ui.widget.ScaleFinderView;
import com.rexnjc.ui.widget.ScanRayView;
import com.rexnjc.ui.widget.TorchView;

/**
 * author : leilei.yll
 * date : 8/28/16.
 * email : leilei.yll@alibaba-inc.com
 * 旺旺 : 病已
 */
public class ToolScanTopView  extends RelativeLayout implements IOnMaSDKDecodeInfo,
        TorchView.OnTorchClickListener{
    public static final String TAG = "ToolScanTopView";
    protected TopViewCallback topViewCallback;
    private ScaleFinderView scaleFinderView;    // 扫描框
    private ScanRayView scanRayView;            // 扫描动画
    private TorchView torchView;
    private ToolsCaptureActivity mActivity;
    private int low_threshold = 70;
    private Runnable showTorchRunnable;
    private int high_threshold = 140;
    private Runnable hideTorchRunnable;
    private int autoZoomState;
    private int frameNum = 0;

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
        scaleFinderView = (ScaleFinderView) findViewById(R.id.scale_finder_view);
        scanRayView = (ScanRayView) findViewById(R.id.scan_ray_view);
        scanRayView.setFinderView(scaleFinderView);
        torchView = (TorchView) findViewById(R.id.torch_view);
        torchView.setOnTorchClickListener(this);
    }


    public void onStartScan() {
        scanRayView.startScaleAnimation();
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

    public float getCropWidth() {
        // 裁剪框大小 = 网格动画框大小＊1.1
        return scanRayView.getWidth() * 1.1f;
    }

    public Rect getScanRect(Camera camera, int previewWidth, int previewHeight) {
        if (camera == null) {
            return null;
        }
        int[] location = new int[2];
        scanRayView.getLocationOnScreen(location);
        Rect r = new Rect(location[0], location[1],
                location[0] + scanRayView.getWidth(), location[1] + scanRayView.getHeight());
        Camera.Size size = null;
        try {
            size = camera.getParameters().getPreviewSize();
        } catch (Exception ex) {
            return null;
        }
        if (size == null) {
            return null;
        }
        double rateX = (double) size.height / (double) previewWidth;
        double rateY = (double) size.width / (double) previewHeight;
        // 裁剪框大小 = 网格动画框大小＊1.1
        int expandX = (int) (scanRayView.getWidth() * 0.05);
        int expandY = (int) (scanRayView.getHeight() * 0.05);
        Rect resRect = new Rect(
                (int) ((r.top - expandY) * rateY),
                (int) ((r.left - expandX) * rateX),
                (int) ((r.bottom + expandY) * rateY),
                (int) ((r.right + expandX) * rateX));

        Rect finalRect = new Rect(
                resRect.left < 0 ? 0 : resRect.left,
                resRect.top < 0 ? 0 : resRect.top,
                resRect.width() > size.width ? size.width : resRect.width(),
                resRect.height() > size.height ? size.height : resRect.height());

        Rect rect1 = new Rect(
                finalRect.left / 4 * 4,
                finalRect.top / 4 * 4,
                finalRect.right / 4 * 4,
                finalRect.bottom / 4 * 4);

        int max = Math.max(rect1.right, rect1.bottom);
        int diff = Math.abs(rect1.right - rect1.bottom) / 8 * 4;

        Rect rect2;
        if (rect1.right > rect1.bottom) {
            rect2 = new Rect(rect1.left, rect1.top - diff, max, max);
        } else {
            rect2 = new Rect(rect1.left - diff, rect1.top, max, max);
        }
        return rect2;
    }

    public void setTopViewCallback(TopViewCallback callback){
        this.topViewCallback = callback;
    }

    @Override public void onGetMaProportion(float v) {
        if(mActivity == null || mActivity.isFinishing()) {
            return;
        }
        Log.d(TAG, "The ma proportion is " + v);
        if(autoZoomState > 1) {
            return;
        }
        if (v <= 0.05 || v >= 0.4 || (++frameNum < 5)) {
            autoZoomState = 0;
            return;
        }
        autoZoomState = 2;
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

    @Override public void onTorchStateSwitch() {
        if (topViewCallback != null) {
            boolean isTorchOn = topViewCallback.turnTorch();
            onTorchState(isTorchOn);
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
