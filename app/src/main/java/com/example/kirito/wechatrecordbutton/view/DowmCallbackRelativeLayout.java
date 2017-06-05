package com.example.kirito.wechatrecordbutton.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by jiawei on 2017/6/2.
 */

public class DowmCallbackRelativeLayout extends RelativeLayout {
    public DowmCallbackRelativeLayout(Context context) {
        super(context);
        init();
    }

    public DowmCallbackRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DowmCallbackRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("Call", "onTouchEvent: " );
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

                break;
        }
        return super.onTouchEvent(event);
    }
}
