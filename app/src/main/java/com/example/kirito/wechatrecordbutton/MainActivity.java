package com.example.kirito.wechatrecordbutton;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.kirito.wechatrecordbutton.adapter.ListViewAdapter;
import com.example.kirito.wechatrecordbutton.support.AudioButton;
import com.example.kirito.wechatrecordbutton.support.MediaPlay;
import com.example.kirito.wechatrecordbutton.xunfei.XunFeiSdk;
import com.iflytek.cloud.SpeechRecognizer;

import java.util.ArrayList;
import java.util.List;

/**
 * listView显示语音聊天内容
 *
 * 下方控制View的显示和隐藏模拟对话框
 *
 * View上根据不同状态来隐藏显示部分子View
 *
 * 松手回调finishRecord()及 消息回调onResult(),先后顺序不确定因此用两个bool值控制，isRelease和haveMessage
 * 最终在handler里处理
 */
public class MainActivity extends AppCompatActivity {
    private ListView lv;
    private AudioButton mAudioButton;
    private List<RecordItem> items;
    private ListViewAdapter adapter;
    private View animeView;
    private SpeechRecognizer mIat;//讯飞的录音识别类
    String TAG = getClass().getSimpleName().toString();
    private XunFeiSdk mXunFeiSdk;
    private String mMessage;
    private boolean haveMessage;
    private boolean isRelease;
    private float mTime;
    private LinearLayout mLlVoice;
    private LinearLayout mLlUpCancel;
    private LinearLayout mLlShort;
    private ImageView ivVoice;
    private Context mContext = this;
    private TextView tvLabel;
    private RelativeLayout rlDialog;
    private EditText etMessage;
    private ImageView ivLoading;
    private long current;
    private TextView tvDesc1;
    private TextView tvDesc2;
    private static final int SENG_MESSAGE=100;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case SENG_MESSAGE:
                    if(isRelease){
                        mXunFeiSdk.stop();
                    }
                    if(haveMessage && isRelease){
                        mXunFeiSdk.stop();
                        mXunFeiSdk.clearnCache();
                        dialogShow(false);
                        RecordItem item = new RecordItem(mMessage, mXunFeiSdk.getPath(), (int) mTime);
                        items.add(item);
                        adapter.notifyDataSetChanged();
                        //每次更新完listview指向最后的item
                        lv.setSelection(items.size() - 1);
                        mMessage="";
                        etMessage.setText("");
                        mXunFeiSdk.cancelRecoder();
                        isRelease=false;
                        haveMessage=false;
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initXunFei();
        initView();
        items = new ArrayList<>();

        mAudioButton.setOnAudioButtonListener(new AudioButton.onAudioListener() {
            @Override
            public void onLongClick(View v) {
                current = System.currentTimeMillis();
                mXunFeiSdk.startRecognizer();
            }

            @Override
            public void finishRecord() {//up回调
                isRelease = true;
                mTime = (int) ((System.currentTimeMillis() - current) / 1000f + 1);//从说话到松手 间隔的时间
                //松手后布局跟着变化
                ivVoice.setVisibility(View.GONE);
                ivLoading.setVisibility(View.VISIBLE);
                //做动画
                final ObjectAnimator ob = ObjectAnimator.ofFloat(ivLoading, "rotation", 0f, 360f);
                ob.setRepeatCount(ObjectAnimator.INFINITE);
                ob.setRepeatMode(ObjectAnimator.RESTART);
                ob.setDuration(2000);
                ob.start();
                mHandler.sendEmptyMessage(SENG_MESSAGE);
            }

            @Override
            public void onCancel() {
                etMessage.setText("");
                mMessage="";
                dialogShow(false);//dismiss
                mXunFeiSdk.cancelRecoder();
            }

            @Override
            public void showVoiceLevel() {//down事件回调
                dialogShow(true);
                mLlVoice.setVisibility(View.VISIBLE);
                mLlUpCancel.setVisibility(View.INVISIBLE);
                mLlShort.setVisibility(View.INVISIBLE);
                tvLabel.setText(R.string.dialog_recording);
            }

            @Override
            public void showTooShort() {
                mLlUpCancel.setVisibility(View.INVISIBLE);
                mLlVoice.setVisibility(View.INVISIBLE);
                setShortView("说话时间太短了", "按住说话，说完再松开哦");
            }

            @Override
            public void showWantToCancel() {
                mLlUpCancel.setVisibility(View.VISIBLE);
                mLlVoice.setVisibility(View.INVISIBLE);
                mLlShort.setVisibility(View.INVISIBLE);
            }
        });

        adapter = new ListViewAdapter(MainActivity.this, items);
        lv.setAdapter(adapter);
        lv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        dialogShow(false);
                        break;
                }
                return false;
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialogShow(false);
                //每次开启动画前，先设置为默认状态
                if (animeView != null) {
                    animeView.setBackgroundResource(R.drawable.adj);
                    animeView = null;
                }
                //通过view获取animeView
                animeView = view.findViewById(R.id.v_anime);
                animeView.setBackgroundResource(R.drawable.voice_animation);
                AnimationDrawable ad = (AnimationDrawable) animeView.getBackground();
                ad.start();

                //播放录制的音频
                MediaPlay.playAudio(items.get(position).getPath(), new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        //播放完成，停止动画效果
                        animeView.setBackgroundResource(R.drawable.adj);
                    }
                });
            }
        });
    }

    private void setShortView(String desc1, String desc2) {
        mLlShort.setVisibility(View.VISIBLE);
        tvDesc1.setText(desc1);//"说话时间太短了"
        tvDesc2.setText(desc2);//"按住说话，说完再松开哦"
    }


    private void initView() {
        lv = (ListView) findViewById(R.id.lv);
        mAudioButton = (AudioButton) findViewById(R.id.btn);
        mLlVoice = (LinearLayout) findViewById(R.id.ll_voice);
        mLlUpCancel = (LinearLayout) findViewById(R.id.ll_up_cancel);
        mLlShort = (LinearLayout) findViewById(R.id.ll_short);
        ivVoice = (ImageView) findViewById(R.id.iv_voice);
        tvLabel = (TextView) findViewById(R.id.tv_label);
        rlDialog = (RelativeLayout) findViewById(R.id.rl_dialog);
        etMessage = (EditText) findViewById(R.id.et_message);
        ivLoading = (ImageView) findViewById(R.id.iv_loading);
        tvDesc1 = (TextView) findViewById(R.id.tv_desc1);
        tvDesc2 = (TextView) findViewById(R.id.tv_desc2);

    }

    private void initXunFei() {
        mXunFeiSdk = XunFeiSdk.getInstance(this);
        mXunFeiSdk.setOnXFSdkListener(new XunFeiSdk.XunFeiSdkListener() {
            @Override
            public void onVolumeChanged(int volume) {
//                mAudioButton.setVolume(volume);
                int res_id = mContext.getResources().getIdentifier("record_animate_" + volume, "drawable", mContext.getPackageName());
                ivVoice.setImageResource(res_id);
            }

            @Override
            public void onResult(String message, boolean isLast) {
                mMessage = message;
                etMessage.setText(message);
                etMessage.setSelection(message.length() - 1);

                //要确定是最后的识别 才dismiss
                haveMessage = isLast;
                mHandler.sendEmptyMessage(SENG_MESSAGE);
            }

            @Override
            public void onInitComplete() {
            }

            //没有识别出内容时会回调
            @Override
            public void onError() {
                setShortView("抱歉，我没听清", "请说话大声些，或换个安静的环境");
                mLlUpCancel.setVisibility(View.INVISIBLE);
                mLlVoice.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaPlay.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaPlay.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaPlay.onRelease();
    }

    public void dialogShow(boolean isShow) {
        if (isShow) {
            ivVoice.setVisibility(View.VISIBLE);
            rlDialog.setVisibility(View.VISIBLE);
            ivLoading.setVisibility(View.GONE);
        } else {
            rlDialog.setVisibility(View.GONE);
        }

    }

    //RecordItem放置录音文件的路径和录音时间
    public class RecordItem {
        private String path;
        private int time;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        private String message;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public RecordItem(String message, String path, int time) {
            this.message = message;
            this.path = path;
            this.time = time;
        }
    }
}
