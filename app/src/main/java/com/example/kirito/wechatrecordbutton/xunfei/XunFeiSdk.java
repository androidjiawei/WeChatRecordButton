package com.example.kirito.wechatrecordbutton.xunfei;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Created by jiawei on 2017/5/26.
 */

public class XunFeiSdk {

    private final String TAG = "XunFeiSdk";

    public static XunFeiSdk sXunFeiSdk;
    private final SpeechRecognizer mIat;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private XunFeiSdk(Context context) {
        mIat = SpeechRecognizer.createRecognizer(context, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Log.e(TAG, "初始化失败，错误码: " + code);
                } else {
//                    if(mListener!=null){
//                        mListener.onInitComplete();
//                    }
                }
            }
        });
    }

    public synchronized static XunFeiSdk getInstance(Context context) {
        if (sXunFeiSdk == null) {
            sXunFeiSdk = new XunFeiSdk(context.getApplicationContext());
        }
        return sXunFeiSdk;
    }

    public void startRecognizer() {
        setParam();
        if (mIat.startListening(mRecognizerListener) != ErrorCode.SUCCESS) {
            Log.e(TAG, "start: 听写失败,错误码");
        } else {
            Log.e(TAG, "start:开始说话");
        }
    }

    //取消所有的回话 在会话被取消后，当前会话结束，未返回的结果将不再返回
    public void cancelRecoder() {
        mIat.cancel();
    }

    public String getPath() {
        return mIat.getParameter(SpeechConstant.ASR_AUDIO_PATH);
    }

    public void stop() {
        mIat.stopListening();
    }

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.e(TAG, "onBeginOfSpeech");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
//            showTip(error.getPlainDescription(true));
            Log.e(TAG, "onError");
            if(mListener!=null){
                mListener.onError();
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.e(TAG, "onEndOfSpeech" + System.currentTimeMillis());
//            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = printResult(results);
            Log.e(TAG, "printResult:返回的result "+result);
//            if (isLast) {
                if (mListener != null) {
                    mListener.onResult(result,isLast);
//                }
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            Log.e(TAG, "onVolumeChanged" + volume);
            if (mListener != null) {
                mListener.onVolumeChanged(getVoiceLevel(volume));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private String printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        Log.e(TAG, "printResult: text"+text );
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
            Log.e(TAG, "printResult: 循环内"+ mIatResults.get(key));
        }
        return resultBuffer.toString();
    }

    public void clearnCache(){
        mIatResults.clear();
    }

    private void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎 云端翻译
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

//        String lag = mSharedPreferences.getString("iat_language_preference",
//                "mandarin");
//        if (lag.equals("en_us")) {
//            // 设置语言
//            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
//        } else {
        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "zh_cn");
//        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "5000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/" + getFileName() + ".wav");
    }

    private String getFileName() {
        return UUID.randomUUID().toString();
    }

    //最大值是23
    public int getVoiceLevel(int current) {
        return 23 * current / 30 + 1;
    }

    XunFeiSdkListener mListener;

    public void setOnXFSdkListener(XunFeiSdkListener listener) {
        mListener = listener;
    }

    public interface XunFeiSdkListener {
        /**
         * 声音大小改变回调
         * @param volume 1-30
         */
        void onVolumeChanged(int volume);

        /**
         * 识别出说话内容时回调
         * @param result
         * @param isLast
         */
        void onResult(String result, boolean isLast);
        void onInitComplete();
        void onError();
    }
}
