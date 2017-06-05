package com.example.kirito.wechatrecordbutton.view;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 *
 */

public class NoFocusEditText extends android.support.v7.widget.AppCompatEditText {
    public NoFocusEditText(Context context) {
        super(context);
        init();
    }

    public NoFocusEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoFocusEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setInputType(InputType.TYPE_NULL);
        setSingleLine(false);
        setMaxLines(2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
