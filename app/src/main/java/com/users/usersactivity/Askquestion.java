package com.users.usersactivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.db.UserDataManager;
import com.iflytek.cloud.ErrorCode ;
import com.iflytek.cloud.InitListener ;
import com.iflytek.cloud.RecognizerListener ;
import com.iflytek.cloud.RecognizerResult ;
import com.iflytek.cloud.SpeechConstant ;
import com.iflytek.cloud.SpeechError ;
import com.iflytek.cloud.SpeechRecognizer ;
import com.iflytek.cloud.SpeechSynthesizer ;
import com.iflytek.cloud.SpeechUtility ;
import com.iflytek.cloud.SynthesizerListener ;
import com.iflytek.cloud.ui.RecognizerDialog ;
import com.iflytek.cloud.ui.RecognizerDialogListener ;

import org.json.JSONException ;
import org.json.JSONObject ;

import com.cheng.example.truemission.R;
import com.users.usersactivity.Json.JsonParser;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Askquestion extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = Askquestion.class .getSimpleName();
    private EditText et_input;
    private Button btn_startspeech, btn_startspeektext,sure;
    private SharedPreferences askquestion_sp;
    private UserDataManager mUserDataManager;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String , String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.askquestion);
        initSpeech();
        initView();
        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();                              //建立本地数据库
        }
    }

    private void initView() {
        et_input = (EditText) findViewById(R.id.et_input );
        btn_startspeech = (Button) findViewById(R.id.btn_startspeech );
        btn_startspeektext = (Button) findViewById(R.id.btn_startspeektext );
        sure=(Button)findViewById(R.id.sure);
        btn_startspeech .setOnClickListener(this) ;
        btn_startspeektext .setOnClickListener(this) ;
        sure.setOnClickListener(this);
        ImageView image = (ImageView) findViewById(R.id.logo);             //使用ImageView显示logo
        image.setImageResource(R.drawable.timg2_meitu_1);
    }

    private void initSpeech() {
        SpeechUtility. createUtility( this, SpeechConstant. APPID + "=5ace1666" );
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_startspeech: //语音识别（把声音转文字）
                startSpeechDialog();
                break;
            case R.id. btn_startspeektext:// 语音合成（把文字转声音）
                speekText();
                break;
            case R.id.sure:
                question_add();
        }

    }

    private void speekText() {
        //1. 创建 SpeechSynthesizer 对象 , 第二个参数： 本地合成时传 InitListener
        SpeechSynthesizer mTts = SpeechSynthesizer.createSynthesizer( this, null);
//2.合成参数设置，详见《 MSC Reference Manual》 SpeechSynthesizer 类
//设置发音人（更多在线发音人，用户可参见 附录 13.2
        mTts.setParameter(SpeechConstant. VOICE_NAME, "vixyun" ); // 设置发音人
        mTts.setParameter(SpeechConstant. SPEED, "50" );// 设置语速
        mTts.setParameter(SpeechConstant. VOLUME, "80" );// 设置音量，范围 0~100
        mTts.setParameter(SpeechConstant. ENGINE_TYPE, SpeechConstant. TYPE_CLOUD); //设置云端
//设置合成音频保存位置（可自定义保存位置），保存在 “./sdcard/iflytek.pcm”
//保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
//仅支持保存为 pcm 和 wav 格式， 如果不需要保存合成音频，注释该行代码
        mTts.setParameter(SpeechConstant. TTS_AUDIO_PATH, "./sdcard/iflytek.pcm" );
//3.开始合成
        mTts.startSpeaking( et_input.getText().toString(), new MySynthesizerListener()) ;

    }

    class MySynthesizerListener implements SynthesizerListener {
//接口public interface SynthesizerListener，合成监听器，获取当前合成的状态和结果
        @Override
        public void onSpeakBegin() {
            showTip(" 开始播放 ");
        }
//开始播放 SDK回调此函数，通知应用层，将要进行播放
        @Override
        public void onSpeakPaused() {
            showTip(" 暂停播放 ");
        }
//暂停播放 SDK回调此接口，通知应用，将暂停播放。
        @Override
        public void onSpeakResumed() {
            showTip(" 继续播放 ");
        }
//播放进度 SDK回调此接口，通知应用，当前的播放进度。
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos ,
                                     String info) {
            // 合成进度 缓冲进度 SDK回调此函数，通知应用层，当前合成音频的缓冲进度。
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度 播放进度 SDK回调此接口，通知应用，当前的播放进度。
        }

        @Override
        public void onCompleted(SpeechError error) {
            //结束 SDK回调此接口，通知应用，将结束会话。
            if (error == null) {
                showTip("播放完成 ");
            } else if (error != null ) {
                showTip(error.getPlainDescription( true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1 , int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话 id，当业务出错时将会话 id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话 id为null
            //if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //     String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //     Log.d(TAG, "session id =" + sid);
            //}
        }
    }

    private void startSpeechDialog() {
        //1. 创建RecognizerDialog对象
        //初始化一个识别对话框。
        //context - 上下文环境变量
        //listener - 初始化监听器
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener()) ;
        //2. 设置accent、 language等参数
        mDialog.setParameter(SpeechConstant. LANGUAGE, "zh_cn" );// 设置中文
        mDialog.setParameter(SpeechConstant. ACCENT, "mandarin" );//语言区域mandarin（普通话）
        // 若要将UI控件用于语义理解，必须添加以下参数设置，设置之后 onResult回调返回将是语义理解
        // 结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        //设置识别对话框监听器
        //通过监听器，获取识别状态和结果。
        mDialog.setListener( new MyRecognizerDialogListener()) ;
        //4. 显示dialog，接收语音输入
        //显示对话框，并开始识别，即相当于调用
        // 了SpeechRecognizer.startListening(com.iflytek.cloud.RecognizerListener)。
        // 若当前音频源为麦克风， 将自动开启录音；若音频源为 写音频流，则应用
        // 可通过调用SpeechRecognizer.writeAudio(byte[], int, int)写 入音频数据。
        mDialog.show() ;
    }

    class MyRecognizerDialogListener implements RecognizerDialogListener {
//  public interface RecognizerDialogListener识别对话框监听器
// 通过实现此接口，获取识别对话框识别过程的结果和错误信息。
        /**
         * @param results
         * @param isLast  是否说完了
         */
        //onResult() 返回的结果可能为null，请增加判断处理。

        //一次识别会话的结果可能会多次返回（即多次回调此函数），通过参数2,判断是否是最后一个结果，
        // true时为最后一个结果，否则不是。当最后一个结果返回时，本次会话结束，录音也会停止，
        // 在重新调用 SpeechRecognizer.startListening(com.iflytek.cloud.RecognizerListener)开启
        // 新的识别会话前， 停止调用SpeechRecognizer.writeAudio(byte[], int, int)写入音频(当音频源设置
        // 为音频流时 （SpeechConstant.AUDIO_SOURCE为-1时)。 当出现错误，或应用层调用SpeechRecognizer.cancel()
        // 取消当次识别时，在当次识别会话过程可能不会回调此函数。

        //识别采用边录边上传的分次上传音频数据方式，可能在结束录音前，就有结果返回。

        //参数
        //    result - 结果数据
        //islast - 是否最后一次结果标记
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = results.getResultString(); //要解析的
            showTip(result) ;
            System. out.println(" 没有解析的 :" + result);

            String text = JsonParser.parseIatResult(result) ;//解析过后的
            System. out.println(" 解析后的 :" + text);
            showTip(text);
            String sn = null;
            // 读取json结果中的 sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString()) ;
                sn = resultJson.optString("sn" );
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults .put(sn, text) ;//每得到一句，添加到mIatResults

            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults .get(key));
            }

            et_input.setText(resultBuffer.toString());// 设置输入框的文本
            et_input .setSelection(et_input.length()) ;//把光标定位末尾
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    }

    class MyInitListener implements InitListener {
//初始化回调接口InitListener

        //初始化单例对象时，通过此回调接口，获取初始化状态。
        @Override
        public void onInit(int code) {
            //初始化结束回调 初始化结束时，回调此接口通知应用层，初始的状态。
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败 ");
            }

        }
    }


//    private void startSpeech() {
//        //1. 创建SpeechRecognizer对象，第二个参数： 本地识别时传 InitListener
//        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer( this, null); //语音识别器
//        //2. 设置听写参数，详见《 MSC Reference Manual》 SpeechConstant类
//        mIat.setParameter(SpeechConstant. DOMAIN, "iat" );// 短信和日常用语： iat (默认)
//        mIat.setParameter(SpeechConstant. LANGUAGE, "zh_cn" );// 设置中文
//        mIat.setParameter(SpeechConstant. ACCENT, "mandarin" );// 设置普通话
//        //3. 开始听写
//        mIat.startListening( mRecoListener);
//    }


    // 听写监听器
//    private RecognizerListener mRecoListener = new RecognizerListener() {
//        // 听写结果回调接口 (返回Json 格式结果，用户可参见附录 13.1)；
////一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
////关于解析Json的代码可参见 Demo中JsonParser 类；
////isLast等于true 时会话结束。
//        public void onResult(RecognizerResult results, boolean isLast) {
//            Log.e (TAG, results.getResultString());
//            System.out.println(results.getResultString()) ;
//            showTip(results.getResultString()) ;
//        }
//
//        // 会话发生错误回调接口
//        public void onError(SpeechError error) {
//            showTip(error.getPlainDescription(true)) ;
//            // 获取错误码描述
//            Log. e(TAG, "error.getPlainDescription(true)==" + error.getPlainDescription(true ));
//        }
//
//        // 开始录音
//        public void onBeginOfSpeech() {
//            showTip(" 开始录音 ");
//        }
//
//        //volume 音量值0~30， data音频数据
//        public void onVolumeChanged(int volume, byte[] data) {
//            showTip(" 声音改变了 ");
//        }
//
//        // 结束录音
//        public void onEndOfSpeech() {
//            showTip(" 结束录音 ");
//        }
//
//        // 扩展用接口
//        public void onEvent(int eventType, int arg1 , int arg2, Bundle obj) {
//        }
//    };

    private void showTip (String data) {
        Toast.makeText( this, data, Toast.LENGTH_SHORT).show() ;
    }

    public void turn_to_userstable(View view) {
        //setContentView(R.layout.login);
        Intent intent = new Intent(Askquestion.this,Userstable.class) ;
        startActivity(intent);
        finish();
    }
    public void question_add(){
        String question=et_input.getText().toString().trim();
        askquestion_sp = getSharedPreferences("userInfo", 0);
        String name=askquestion_sp.getString("USER_NAME", "");
        int asker_id=mUserDataManager.findIdByUsername(name);
        //QuestionData mQuestion=new QuestionData(asker_id,question,0,null);
        mUserDataManager.openDataBase();
        long flag = mUserDataManager.insertQuestionData(asker_id,question,0,null); //新建问题信息，插入SQLite数据库
        if (flag == -1) {
            Toast.makeText(this, getString(R.string.insertquestion_fail),Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, getString(R.string.insertquestion_success),Toast.LENGTH_SHORT).show();
        }

    }
}
