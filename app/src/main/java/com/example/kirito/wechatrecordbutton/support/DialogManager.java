package com.example.kirito.wechatrecordbutton.support;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kirito.wechatrecordbutton.R;
import com.example.kirito.wechatrecordbutton.utils.Utils;

/**
 * Created by kirito on 2016.11.07.
 */

public class DialogManager {
    private final DisplayMetrics dm;
    private LayoutInflater mLayoutInflater;
    private Dialog mDialog;
    private Context mContext;
    private ImageView /*iv_icon*/iv_voice;
    private TextView tv_label;

    private static final String TAG = "DialogManager";
    private LinearLayout llShort;
    private LinearLayout llUpCancel;
    private LinearLayout llVoice;
    private TextView tvMessage;

    public DialogManager(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        dm = context.getResources().getDisplayMetrics();
    }

    /**
     * 显示dialog需要在button上边
     * @param height
     */
    public void showDialog(int height){
        mDialog = new Dialog(mContext, R.style.Dialog_Theme);
        View view = mLayoutInflater.inflate(R.layout.dialog,null,false);
        mDialog.setContentView(view);
        Window dialogWindow = mDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y=height;
        lp.width = dm.widthPixels; //设置宽度
        lp.height = (int) Utils.dip2px(305);
        dialogWindow.setAttributes(lp);
//        iv_icon = (ImageView) mDialog.findViewById(R.id.iv_icon);
        iv_voice = (ImageView) mDialog.findViewById(R.id.iv_voice);
        tvMessage = (TextView) mDialog.findViewById(R.id.tv_message);
        tv_label = (TextView) mDialog.findViewById(R.id.tv_label);
        llShort = (LinearLayout) mDialog.findViewById(R.id.ll_short);
        llUpCancel = (LinearLayout) mDialog.findViewById(R.id.ll_up_cancel);
        llVoice = (LinearLayout) mDialog.findViewById(R.id.ll_voice);
        mDialog.show();
    }

    /**
     * 正常录音的状态
     */
    public void showRecording(){
        if (mDialog != null && mDialog.isShowing()){
//            iv_icon.setVisibility(View.VISIBLE);
//            iv_voice.setVisibility(View.VISIBLE);
//            iv_icon.setImageResource(R.drawable.recorder);
//            iv_voice.setImageResource(R.drawable.record_animate_1);
//            tv_label.setText(R.string.dialog_recording);

            llUpCancel.setVisibility(View.INVISIBLE);
            llVoice.setVisibility(View.VISIBLE);
            llShort.setVisibility(View.INVISIBLE);

        }
    }

    public void wantToCancel(){
        if (mDialog != null && mDialog.isShowing()){
//            iv_icon.setVisibility(View.VISIBLE);
//            iv_voice.setVisibility(View.GONE);
//            iv_icon.setImageResource(R.drawable.cancel);
//            tv_label.setText(R.string.dialog_cancel);

            llUpCancel.setVisibility(View.VISIBLE);
            llVoice.setVisibility(View.INVISIBLE);
            llShort.setVisibility(View.INVISIBLE);
        }
    }

    public void tooShort(){
        if (mDialog != null && mDialog.isShowing()){
//            iv_icon.setVisibility(View.VISIBLE);
//            iv_voice.setVisibility(View.GONE);
//            iv_icon.setImageResource(R.drawable.voice_to_short);

            llUpCancel.setVisibility(View.INVISIBLE);
            llVoice.setVisibility(View.INVISIBLE);
            llShort.setVisibility(View.VISIBLE);
//            tv_label.setText(R.string.dialog_too_short);
        }
    }

    public void dismissDialog(){
        if (mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void setVoiceLevel(int level){
        if (mDialog != null && mDialog.isShowing()){
//            iv_icon.setVisibility(View.VISIBLE);
            iv_voice.setVisibility(View.VISIBLE);
//            iv_icon.setImageResource(R.drawable.recorder);
            int res_id = mContext.getResources().getIdentifier("record_animate_" + level,"drawable",mContext.getPackageName());
            iv_voice.setImageResource(res_id);
            tv_label.setText(R.string.dialog_recording);
        }
    }

    public void setMessage(String message){
        tvMessage.setText(message);
    }
}
