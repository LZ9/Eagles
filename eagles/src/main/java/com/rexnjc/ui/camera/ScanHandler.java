package com.rexnjc.ui.camera;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;

import com.alipay.mobile.bqcscanservice.BQCScanEngine;
import com.alipay.mobile.bqcscanservice.BQCScanService;
import com.alipay.mobile.mascanengine.MaScanEngineService;
import com.alipay.mobile.mascanengine.impl.MaScanEngineServiceImpl;
import com.lodz.android.eagles.R;
import com.rexnjc.ui.widget.ScanType;


/**
 * author : leilei.yll
 * date : 8/30/16.
 * email : leilei.yll@alibaba-inc.com
 * 旺旺 : 病已
 */
public class ScanHandler {

    private HandlerThread scanHandlerThread;
    private Handler scanHandler;
    private Context context;
    private ScanResultCallbackProducer scanResultCallbackProducer;
    private MediaPlayer shootMP;
    private BQCScanService bqcScanService;
    private int curState = 0;

    public ScanHandler() {
        scanHandlerThread = new HandlerThread("Scan-Recognized", Thread.MAX_PRIORITY);
        scanHandlerThread.start();
        scanHandler = new Handler(scanHandlerThread.getLooper());
    }

    public void destroy() {
        scanHandlerThread.quit();
    }

    public void setBqcScanService(final BQCScanService bqcScanService) {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                ScanHandler.this.bqcScanService = bqcScanService;
                curState = 1;
            }
        });
    }

    public void registerAllEngine(final boolean switchScanAR) {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                if (scanResultCallbackProducer == null) {
                    return;
                }
                MaScanEngineService maScanEngineService = new MaScanEngineServiceImpl();
                bqcScanService.regScanEngine(ScanType.SCAN_MA.toBqcScanType(), maScanEngineService.getEngineClazz(),
                        scanResultCallbackProducer.makeScanResultCallback(ScanType.SCAN_MA));
            }
        });
    }

    public void enableScan() {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                curState = 4;
                bqcScanService.setScanEnable(true);
            }
        });
    }

    public void setScanType(final ScanType mScanType) {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                curState = 5;
                bqcScanService.setScanType(mScanType.toBqcScanType());
            }
        });
    }

    public void disableScan() {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                curState = 6;
                bqcScanService.setScanEnable(false);
            }
        });
    }

    public void shootSound() {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                if (context != null) {
                    AudioManager meng = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                    if (volume != 0) {
                        if (shootMP == null) {
                            shootMP = MediaPlayer.create(context, R.raw.beep);
                        }
                        if (shootMP != null) {
                            shootMP.start();
                        }
                    }
                }
            }
        });
    }

    public void removeContext() {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                context = null;
                scanResultCallbackProducer = null;
                if (shootMP != null) {
                    shootMP.release();
                    shootMP = null;
                }
            }
        });
    }

    public void reset() {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                curState = 0;
            }
        });
    }

    public void setContext(final Context context, final ScanResultCallbackProducer scanResultCallbackProducer) {
        scanHandler.post(new Runnable() {
            @Override
            public void run() {
                ScanHandler.this.context = context;
                ScanHandler.this.scanResultCallbackProducer = scanResultCallbackProducer;
            }
        });
    }

    public interface ScanResultCallbackProducer {
        BQCScanEngine.EngineCallback makeScanResultCallback(ScanType type);
    }
}
