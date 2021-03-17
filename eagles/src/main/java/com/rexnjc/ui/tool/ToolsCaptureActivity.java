
package com.rexnjc.ui.tool;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.alipay.mobile.bqcscanservice.BQCScanCallback;
import com.alipay.mobile.bqcscanservice.BQCScanEngine;
import com.alipay.mobile.bqcscanservice.BQCScanError;
import com.alipay.mobile.bqcscanservice.BQCScanService;
import com.alipay.mobile.bqcscanservice.CameraHandler;
import com.alipay.mobile.bqcscanservice.impl.MPaasScanServiceImpl;
import com.alipay.mobile.mascanengine.IOnMaSDKDecodeInfo;
import com.alipay.mobile.mascanengine.MaScanCallback;
import com.alipay.mobile.mascanengine.MultiMaScanResult;
import com.lodz.android.eagles.R;
import com.rexnjc.ui.camera.ScanHandler;
import com.rexnjc.ui.util.AutoZoomOperator;
import com.rexnjc.ui.widget.APTextureView;
import com.rexnjc.ui.widget.ma.ToolScanTopView;

public class ToolsCaptureActivity extends Activity implements ScanHandler.ScanResultCallbackProducer{

    public static void start(Context context) {
        Intent starter = new Intent(context, ToolsCaptureActivity.class);
        context.startActivity(starter);
    }

    private final String TAG = "ToolsCaptureActivity";
    private APTextureView mSurfaceView;
    private ToolScanTopView mScanTopView;
    private BQCScanService bqcScanService;
    private boolean bqcServiceSetup;
    private CameraHandler cameraScanHandler;
    private ScanHandler scanHandler;
    private boolean firstAutoStarted = false;
    private boolean isPermissionGranted = false;

    // 是否已经摄像头扫码成功
    private int pauseOrResume = 0;
    private boolean scanSuccess = false;
    // 裁剪区域改为局部变量，避免onResume后的再次计算
    private Rect scanRect;
    private long postcode = -1;
    private AutoZoomOperator autoZoomOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        autoZoomOperator = new AutoZoomOperator(this);
        bqcScanService = new MPaasScanServiceImpl();
        bqcScanService.serviceInit();
        cameraScanHandler = bqcScanService.getCameraHandler();
        scanHandler = new ScanHandler();
        scanHandler.setBqcScanService(bqcScanService);
        String permission = Manifest.permission.CAMERA;
        if(PermissionChecker.checkSelfPermission(this, permission)
                != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
        } else {
            isPermissionGranted = true;
            firstAutoStarted = true;
            try {
                autoStartScan();
            } catch (Exception ex) {
                Log.e(TAG, "autoStartScan: Exception " + ex.getMessage());
            }
        }
        initViews();
    }

    private void initViews() {
        mSurfaceView = (APTextureView) findViewById(R.id.surfaceView);
        configPreviewAndRecognitionEngine();
        mScanTopView = (ToolScanTopView) findViewById(R.id.top_view);
        mScanTopView.setTopViewCallback(topViewCallback);
        mScanTopView.attachActivity(this);
    }

    /**
     * bqcService的setup过程设置好后以及ui准备好后,即可以进行preview的设置和识别引擎配置
     */
    private void configPreviewAndRecognitionEngine() {
        if (mSurfaceView != null && bqcServiceSetup) {
            bqcScanService.setDisplay(mSurfaceView);
            cameraScanHandler.onSurfaceViewAvailable();
            if (scanHandler == null) {
                scanHandler = new ScanHandler();
                scanHandler.setBqcScanService(bqcScanService);
            }
            scanHandler.registerAllEngine(false);
            setScanType(true);
        }
    }


    public void setScanType( boolean firstAutoStarted) {
        if (!firstAutoStarted  || bqcScanService == null) {
            return;
        }
        scanHandler.disableScan();
        scanHandler.setScanType();
        scanHandler.enableScan();
    }

    private void autoStartScan() {
        // 初始时都为关闭状态
        cameraScanHandler.init(this, bqcCallback);
        scanHandler.setContext(this, this);
        startPreview();
    }

    void showPermissionDenied() {
        if (!this.isFinishing()) {
            showAlertDialog(getString(R.string.camera_no_permission));
        }
    }

    interface MaScanCallbackWithDecodeInfoSupport extends MaScanCallback, IOnMaSDKDecodeInfo {
    }

    @Override
    public BQCScanEngine.EngineCallback makeScanResultCallback() {
        MaScanCallbackWithDecodeInfoSupport maCallback = new MaScanCallbackWithDecodeInfoSupport() {
            @Override
            public void onResultMa(final MultiMaScanResult maScanResult) {
                scanSuccess = true;
                if(scanHandler != null) {
                    scanHandler.disableScan();
                    scanHandler.shootSound();
                }
//                    onResultShow();
//                    if (mScanTopView != null) {
//                        mScanTopView.onResultMa(maScanResult);
//                    }
                if (maScanResult == null) {
//                        Toast.makeText(getContext().getApplicationContext(), "error:maScanResult ==null", Toast.LENGTH_LONG).show();
                    Log.e("testtag", "error:maScanResult ==null");
                    restartScan();
                    return;
                }
                Log.d("testtag", "onResultMa: " + maScanResult.maScanResults[0].text);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(maScanResult.maScanResults[0].text);
                    }
                });
//                    scanHandler.enableScan();
            }

            @Override
            public void onGetMaProportion(float v) {
                if (null != mScanTopView) {
                    mScanTopView.onGetMaProportion(v);
                }
            }

            @Override
            public void onGetAvgGray(int i) {
                if (null != mScanTopView) {
                    mScanTopView.onGetAvgGray(i);
                }
            }
        };
        return maCallback;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1 && permissions != null && grantResults != null) {
            for(int i = 0; i < permissions.length && i < grantResults.length; ++i) {
                if(TextUtils.equals(permissions[i],Manifest.permission.CAMERA)) {
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED ) {
                        showPermissionDenied();
                        break;
                    } else {
                        firstAutoStarted = true;
                        try {
                            autoStartScan();
                        } catch (Exception ex) {
                            Log.e(TAG, "autoStartScan: Exception " + ex.getMessage());
                        }
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void startPreview() {
        cameraScanHandler.openCamera();
        // 重设对焦参数
        bqcScanService.setScanEnable(true);
    }

    private void realStopPreview() {
        cameraScanHandler.closeCamera();
        scanHandler.disableScan();
    }

    private void restartScan() {
        if(scanHandler != null) {
            scanHandler.enableScan();
            scanSuccess = false;
        }
    }

    private void initScanRect() {
        if (scanRect == null) {

            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            float screenWith = wm.getDefaultDisplay().getWidth();
            float screenHeight = wm.getDefaultDisplay().getHeight();
            scanRect = new Rect(0, 0, wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight());
            float previewScale  = 1.0f;
//            float previewScale = screenWith / cropWidth;
//            if (previewScale < 1.0f) {
//                previewScale = 1.0f;
//            }
//            if (previewScale > 1.5f) {
//                previewScale = 1.5f;
//            }
            Log.d(TAG, "previewScale: " + previewScale);
            Matrix transform = new Matrix();
            transform.setScale(previewScale, previewScale, screenWith , screenHeight );
            mSurfaceView.setTransform(transform);
            bqcScanService.setScanRegion(scanRect);
        } else {
            bqcScanService.setScanRegion(scanRect);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        pauseOrResume = 1;
        if (scanHandler == null) {
            scanHandler = new ScanHandler();
            scanHandler.setBqcScanService(bqcScanService);
        }
        if (!firstAutoStarted && !scanSuccess
                && mScanTopView != null) {
            if (isPermissionGranted) {
                try {
                    autoStartScan();
                } catch (Exception ex) {
                    Log.e(TAG, "autoStartScan: Exception " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseOrResume = -1;
        firstAutoStarted = false;
        if (isPermissionGranted) {
            realStopPreview();
        }
        if (bqcScanService != null && cameraScanHandler != null) {
            cameraScanHandler.release(postcode);
        }
        if(scanHandler != null) {
            scanHandler.reset();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(bqcScanService != null) {
            bqcScanService.serviceOut();
        }
        if (scanHandler != null) {
            scanHandler.removeContext();
            scanHandler.destroy();
        }
        if(mScanTopView != null) {
            mScanTopView.detachActivity();
        }
        if(autoZoomOperator != null) {
            autoZoomOperator.clearActivity();
        }
    }

    private void showAlertDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).create().show();
    }

    private BQCScanCallback bqcCallback = new BQCScanCallback() {

        @Override
        public void onParametersSetted(final long pcode) {
            if(!isFinishing()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        postcode = pcode;
                        bqcServiceSetup = true;
                        configPreviewAndRecognitionEngine();
                    }
                });
            }
        }

        @Override
        public void onSurfaceAvaliable() {
            if (pauseOrResume == -1) {
                return;
            }
            if (bqcScanService != null) {
                cameraScanHandler.onSurfaceViewAvailable();
            }
        }

        @Override
        public void onCameraOpened() {
            if (pauseOrResume == -1) {
                return;
            }
        }

        @Override
        public void onPreviewFrameShow() {
            if (pauseOrResume == -1) {
                return;
            }
            if (isFinishing()) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initScanRect();
                }
            });
        }

        @Override
        public void onError(final BQCScanError bqcError) {
            Log.d(TAG, "onError()");
            if (pauseOrResume == -1) {
                return;
            }
            if (isFinishing()) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAlertDialog(getString(R.string.camera_open_error));
                }
            });
        }

        @Override
        public void onCameraAutoFocus(boolean success) {
        }

        @Override
        public void onOuterEnvDetected(boolean shouldShow) {
        }

        @Override public void onCameraReady() {

        }
        @Override public void onCameraClose() {

        }
    };

    private ToolScanTopView.TopViewCallback topViewCallback = new ToolScanTopView.TopViewCallback() {
        @Override
        public boolean turnTorch() {
            if (bqcScanService != null) {
                boolean torchState = bqcScanService.isTorchOn();
                bqcScanService.setTorch(!torchState);
                torchState = bqcScanService.isTorchOn();
                return torchState;
            }
            return false;
        }

        @Override
        public void startPreview() {
            // 选取照片识别后, 会走客户端的BaseFragment的onPause流程,此时CameraManager被cleanup,
            if (scanHandler == null) {
                scanHandler = new ScanHandler();
                scanHandler.setBqcScanService(bqcScanService);
            }
            if (bqcScanService != null && bqcScanService.getCamera() == null) {
                autoStartScan();
            }
        }

        @Override
        public void stopPreview(boolean clearSurface) {
            realStopPreview();
        }

        @Override
        public void clearSurface() {
        }

        @Override
        public void scanSuccess() {
            scanSuccess = true;
        }

        @Override
        public void turnEnvDetection(boolean on) {
        }
    };

    public boolean isTorchOn() {
        return bqcScanService != null ? bqcScanService.isTorchOn() : false;
    }

    public void startContinueZoom(int zoom) {
        if(autoZoomOperator != null) {
            autoZoomOperator.startAutoZoom(zoom, 0);
        }
    }

    public void setZoom(int zoom) {
        if (bqcScanService != null) {
            bqcScanService.setZoom(zoom);
        }
    }
}
