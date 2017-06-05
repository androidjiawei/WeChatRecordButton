package com.example.kirito.wechatrecordbutton.support;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.kirito.wechatrecordbutton.R;

/**
 * Created by kirito on 2016.11.07.
 */

public class AudioButton extends android.support.v7.widget.AppCompatButton {
    private static final int BTN_STATE_NORMAL = 0x100;
    private static final int BTN_STATE_RECORDING = 0x101;
    private static final int BTN_STATE_WANTTOCANCEL = 0x102;

    private static final int AUDIO_PREPARED = 0x110;
    private static final int AUDIO_CANCEL = 0x111;
    private static final int AUDIO_VOICE_CHANGE = 0x112;

    //mTime 必须是float型，不能是int型
    private float mTime;

    private int cur_state = BTN_STATE_NORMAL;

//    private DialogManager mDialogManager;
    //标志audio recorder是否prepare完毕
    private boolean isRecording;
    private static final int MIN_CABCEL_Y = 100;

    //    private AudioManager am;
    //是否触发了onLongClick
    private boolean isLongClick;

    private boolean isVoiceConflict;

    private onAudioListener mListener;

    private Handler handler = new Handler();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AUDIO_PREPARED:
                    //在准备好audio recorder之后
                    isRecording = true;
                    //这里使用的是父容器的高 根据实际使用情况改变
//                    mDialogManager.showDialog(((View) getParent()).getHeight());
//                    handler.post(timeRunnable);
                    break;
                case AUDIO_CANCEL:
//                    mDialogManager.dismissDialog();
                    break;
                case AUDIO_VOICE_CHANGE:
//                    mDialogManager.setVoiceLevel(am.getVoiceLevel(7));
                    break;
            }
        }
    };

    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording && !isVoiceConflict) {
                mTime += 0.1;
//              mHandler.sendEmptyMessage(AUDIO_VOICE_CHANGE);
//                handler.postDelayed(timeRunnable, 100);
            }
        }
    };

    public void setVolume(int volume) {
//        mDialogManager.setVoiceLevel(volume);
    }

    //录音完成的回调
    public interface onAudioListener {
        void onLongClick(View v);

        void finishRecord();

        void onCancel();

        void showVoiceLevel();

        void showTooShort();

        void showWantToCancel();
    }

    public void setOnAudioButtonListener(onAudioListener listener) {
        mListener = listener;
    }

    private static final String TAG = "AudioButton";

    public AudioButton(Context context) {
        this(context, null);
    }

    public AudioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
//        mDialogManager = new DialogManager(context);
//        am = AudioManager.getInstance(Environment.getExternalStorageDirectory() + "/zzl_audio");
//        am.setAudioPrepareListener(this);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.e(TAG, "setOnLongClickListener " );
                isLongClick = true;
                //调用sdk开始录音 且监控音量大小 松手后停止
                if (mListener != null) {
                    mListener.onLongClick(v);
                }
//                am.prepareAudio();
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        //是event.getX() 不是getX()
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onTouchEvent:ACTION_DOWN " );
                changeButtonState(BTN_STATE_RECORDING);
                
                //显示对话框 开始计时
//              audioPrepared();
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "onTouchEvent:ACTION_UP " );
                //松开手指有三种状态
                //1.时间太短
                //2.未识别内容
                //3.识别出内容 1.状态是 正常做动画发送且关闭对话框 2.状态是取消 dismiss什么也不做
                if (!isLongClick) {//如果不是长按就说明时间太短
                    if(mListener!=null){// TODO: 展示showRecording
                        mListener.showTooShort();
                    }
//                    reset();
//                    mDialogManager.dismissDialog();
//                    mDialogManager.tooShort();
//                    return super.onTouchEvent(event);
                }/*else if(!isRecording || mTime < 0.6){
                    //设置isVoiceConflict 为true防止setVoiceLevel方法更改图标，造成图标显示混乱！
                    isVoiceConflict = true;
                    mDialogManager.tooShort();
                    if(mListener!=null){
                        mListener.onCancel();
                    }
//                    am.cancelAudio();
                    //延时让tooShort的图标显示出来
//                    mHandler.sendEmptyMessageDelayed(AUDIO_CANCEL,1300);
                }*/
                else if (cur_state == BTN_STATE_RECORDING) {//正常回调
//                    am.releaseAudio();
//                    mDialogManager.dismissDialog();
                    if (mListener != null) {
                        mListener.finishRecord();
                    }
                } else if (cur_state == BTN_STATE_WANTTOCANCEL) {
//                    am.cancelAudio();
                    if (mListener != null) {
                        mListener.onCancel();
                    }
//                    mDialogManager.dismissDialog();
                }
                reset();
                break;
            case MotionEvent.ACTION_MOVE:
                if (true) {
                    if (wantToCancel(x, y)) {
                        isVoiceConflict = true;
                        changeButtonState(BTN_STATE_WANTTOCANCEL);
                    } else if (!wantToCancel(x, y)) {
                        changeButtonState(BTN_STATE_RECORDING);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean wantToCancel(int x, int y) {
        //getWidth---520    getHeight---96
        if (x > getWidth() || x < 0) {
            return true;
        } else if (y > getHeight() + MIN_CABCEL_Y || y < -MIN_CABCEL_Y) {
            return true;
        }
        return false;
    }

    public void reset() {
        isLongClick = false;
        isRecording = false;
        isVoiceConflict = false;
        mTime = 0;
        changeButtonState(BTN_STATE_NORMAL);
    }

    /**
     * 根据状态setText 和bg
     * 切换显示对应内容
     *
     * @param ste
     */
    public void changeButtonState(int ste) {
        if (cur_state != ste) {
            cur_state = ste;
            switch (ste) {
                case BTN_STATE_NORMAL:
                    setText(getResources().getString(R.string.btn_state_normal));
                    setBackgroundResource(R.drawable.btn_state_normal_bcg);
                    break;
                case BTN_STATE_RECORDING:
                    setText(getResources().getString(R.string.btn_state_recordinng));
//                    setBackgroundResource(R.drawable.btn_state_recording_bcg);
//                    if (isRecording){
//                    mDialogManager.showRecording();
//                    }
                    if(mListener!=null){// TODO: 展示showRecording
                        mListener.showVoiceLevel();
                    }
                    break;
                case BTN_STATE_WANTTOCANCEL:
                    setText(getResources().getString(R.string.btn_state_recordinng));
                    setBackgroundResource(R.drawable.btn_state_recording_bcg);
//                    mDialogManager.wantToCancel();
                    if(mListener!=null){// TODO: 展示showRecording
                        mListener.showWantToCancel();
                    }
                    break;
            }
        }
    }

    public void audioPrepared() {
        mHandler.sendEmptyMessage(AUDIO_PREPARED);
    }

    public void setMessage(String message) {
//        mDialogManager.setMessage(message);
    }
}
