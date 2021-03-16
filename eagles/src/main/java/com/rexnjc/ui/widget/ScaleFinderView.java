package com.rexnjc.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.lodz.android.eagles.R;


public class ScaleFinderView extends View {
    public static final String TAG = "ScaleFinderView";

    private Bitmap angle_leftTop, angle_rightTop, angle_leftBottom, angle_rightBottom;
    private Paint paint;
    private int targetLeft, targetRight, targetTop, targetBottom;
    private int shadowColor = 0x96000000;

    public ScaleFinderView(Context context) {
        this(context, null);
    }

    public ScaleFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAngleBitmap(context);
        initPaint();
    }

    private void initAngleBitmap(Context c) {
        Resources res = c.getResources();
        angle_leftTop = BitmapFactory.decodeResource(res, R.drawable.scan_aimingbox_lu);
        angle_rightTop = BitmapFactory.decodeResource(res, R.drawable.scan_aimingbox_ru);
        angle_leftBottom = BitmapFactory.decodeResource(res, R.drawable.scan_aimingbox_ld);
        angle_rightBottom = BitmapFactory.decodeResource(res, R.drawable.scan_aimingbox_rd);
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void setTargetLocation(int left, int top, int right, int bottom) {
        targetLeft = left;
        targetRight = right;
        targetTop = top;
        targetBottom = bottom;
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, "targetLeft : " + targetLeft + ", targetRight : " + targetRight
                + ", targetTop : " + targetTop + ", targetBottom : " + targetBottom);
        super.draw(canvas);
        if(targetLeft == 0 || targetRight == 0 || targetBottom == 0 || targetTop == 0) {
            return;
        }
        drawAngle(canvas);
        drawShadow(canvas);
    }

    private void drawAngle(Canvas canvas) {
        paint.setAlpha(255);
        // 左上
        canvas.drawBitmap(angle_leftTop, targetLeft, targetTop, paint);
        // 右上
        canvas.drawBitmap(angle_rightTop, targetRight - angle_rightTop.getWidth(), targetTop, paint);
        // 左下
        canvas.drawBitmap(angle_leftBottom, targetLeft, targetBottom - angle_leftBottom.getHeight(), paint);
        // 右下
        canvas.drawBitmap(angle_rightBottom, targetRight - angle_rightBottom.getWidth(),
                targetBottom - angle_rightBottom.getHeight(), paint);
    }

    private void drawShadow(Canvas canvas) {
        paint.setColor(shadowColor);
        // 上
        canvas.drawRect(0, 0, getWidth(), targetTop, paint);
        // 左
        canvas.drawRect(0, targetTop, targetLeft, targetBottom, paint);
        // 右
        canvas.drawRect(targetRight, targetTop, getWidth(), targetBottom, paint);
        // 下
        canvas.drawRect(0, targetBottom, getWidth(), getHeight(), paint);
    }
}
