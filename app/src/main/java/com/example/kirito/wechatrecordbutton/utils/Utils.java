package com.example.kirito.wechatrecordbutton.utils;

import android.content.res.Resources;

/**
 * Created by jiawei on 2017/4/14.
 */

public class Utils {
    public static int px2dip(int pxValue)
    {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static float dip2px(float dipValue)
    {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return  (dipValue * scale + 0.5f);
    }
}
