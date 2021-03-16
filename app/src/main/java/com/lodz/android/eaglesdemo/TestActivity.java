package com.lodz.android.eaglesdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.alipay.mobile.mascanengine.MaScanEngineService;
import com.alipay.mobile.mascanengine.MaScanResult;
import com.alipay.mobile.mascanengine.impl.MaScanEngineServiceImpl;
import com.rexnjc.ui.tool.ToolsCaptureActivity;

import java.io.InputStream;

/**
 * @author zhouL
 * @date 2021/3/16
 */
public class TestActivity extends AppCompatActivity {
    private final static int LOCAL_PICS_REQUEST = 2;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.scan_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TestActivity.this, ToolsCaptureActivity.class));
            }
        });
        findViewById(R.id.photo_btn).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent picsIn = new Intent(Intent.ACTION_GET_CONTENT);
                picsIn.setType("image/*");
                startActivityForResult(picsIn, LOCAL_PICS_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case LOCAL_PICS_REQUEST:
                Uri uri = data.getData();//从图片的Uri是以cotent://格式开头的
                recognizedBitmap(uri);
                break;
            default:
                return;
        }
    }

    private void recognizedBitmap(Uri uri) {
        final Bitmap bitmap = uri2Bitmap(this, uri);
        if(bitmap == null) {
            showAlertDialog("没有图片被获取");
        } else {
            new Thread(new Runnable() {
                @Override public void run() {
                    MaScanEngineService maScanEngineService = new MaScanEngineServiceImpl();
                    MaScanResult result = maScanEngineService.process(bitmap);
                    processBitmapRecognizeResult(result);
                }
            }).start();
        }
    }

    private void processBitmapRecognizeResult(final MaScanResult maScanResult) {
        this.runOnUiThread(new Runnable() {
            @Override public void run() {
                if(TestActivity.this.isFinishing()) {
                    return;
                }
                if(maScanResult != null) {
                    showAlertDialog(maScanResult.text);
                } else {
                    showAlertDialog("没有发现二维码/条形码");
                }
            }
        });
    }

    private void showAlertDialog(String msg) {
        alertDialog = new AlertDialog.Builder(this).setMessage(msg).setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if(alertDialog != null && alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                    }
                }).create();
        alertDialog.show();
    }

    private Bitmap uri2Bitmap(Context mContext, Uri uri) {
        InputStream in;
        try {
            in = mContext.getContentResolver().openInputStream(uri);
            //从输入流中获取到图片
            android.graphics.Bitmap bm = BitmapFactory.decodeStream(in);
            in.close();
            return bm;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
