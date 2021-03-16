
package com.rexnjc.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class ScanRayView extends ImageView {
	public static final String TAG = "ScanRayView";

	private ScaleAnimation animation;
	private ScaleFinderView mFinderView = null;

	public ScanRayView(Context context) {
		super(context);
	}

	public ScanRayView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void draw(Canvas canvas) {
		Log.d(TAG, "draw()");
		int[] location = new int[2];
		getLocationOnScreen(location);
		//设置FinderView的大小
		if (mFinderView != null) {
			int[] l = new int[2];
			mFinderView.getLocationInWindow(l);
			location[1] -= l[1];    //这个需要按照自己的位置计算相对位置，要剪掉自己到顶的距离
			mFinderView.setTargetLocation(location[0], location[1], location[0] + getWidth(), location[1] + getHeight());
		}
		super.draw(canvas);
	}

	public void startScaleAnimation() {
		this.setVisibility(VISIBLE);
		if (animation == null) {
			animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
			animation.setDuration(3000L);
			animation.setFillAfter(true);
			animation.setRepeatCount(Animation.INFINITE);
			animation.setInterpolator(new AccelerateDecelerateInterpolator());
			this.startAnimation(animation);
		}
	}

	public void setFinderView(ScaleFinderView FinderView) {
		mFinderView = FinderView;
	}

	public void stopScaleAnimation() {
		this.setVisibility(INVISIBLE);
		if (animation != null) {
			this.clearAnimation();
			animation = null;
		}
	}

}
