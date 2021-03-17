package com.rexnjc.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lodz.android.eagles.R;

/** 手电筒控件 */
public class TorchView extends LinearLayout implements View.OnClickListener{

    private ImageView torchIv;
    private TextView torchTv;
    private OnTorchClickListener onTorchClickListener;

    public TorchView(Context context) {
        this(context, null);
    }

    public TorchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOrientation(VERTICAL);
        this.setOnClickListener(this);
        this.setGravity(Gravity.CENTER_HORIZONTAL);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.torch_layout, this, true);
        torchIv = (ImageView) findViewById(R.id.torch_image_view);
        torchTv = (TextView) findViewById(R.id.torch_tips_view);
    }

    public void showTorch() {
        this.setVisibility(VISIBLE);
    }

    public void resetState() {
        this.setVisibility(GONE);
    }

    public void showTorchState(boolean on) {
        torchIv.setImageDrawable(getResources().getDrawable(on ? R.drawable.torch_on : R.drawable.torch_off));
        CharSequence torchTipText = getResources().getText(on ? R.string.close_torch : R.string.open_torch);
        torchTv.setText(torchTipText);
        // 盲人操作提示
        this.setContentDescription(torchTipText);
    }

    @Override
    public void onClick(View v) {
        switchTorch();
    }

    private void switchTorch() {
        if(onTorchClickListener != null) {
            onTorchClickListener.onTorchStateSwitch();
        }
    }

    public void setOnTorchClickListener(OnTorchClickListener onTorchClickListener) {
        this.onTorchClickListener = onTorchClickListener;
    }

    public interface OnTorchClickListener {
        void onTorchStateSwitch();
    }
}
