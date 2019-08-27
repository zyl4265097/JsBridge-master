package com.github.lzyzsd.jsbridge.example;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.json.JSONException;

public class MainActivity extends Activity {

    private final String TAG = "MainActivity";

    BridgeWebView webView;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private Toast mToast;
    public static String voicerLocal = "xiaofeng";
    private static Socket socket;
    private boolean isOnAttacheWindow = false;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        //初始化sharepreference,应用内数据保存
        sp = getSharedPreferences("date", Context.MODE_PRIVATE);
    }

    private void initWebView() {
        webView = (BridgeWebView)findViewById(R.id.webView);
        webView.setDefaultHandler(new DefaultHandler());

        webView.loadUrl("file:///android_asset/test.html");

        webView.registerHandler("save", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "handler = save, data from web = " + data);
                function.onCallBack("存储token");
            }
        });

        webView.registerHandler("get", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "handler = get, data from web = " + data);
                Log.i(TAG, "getToken = " + getToken());
                function.onCallBack("获取token");
            }
        });

        webView.registerHandler("TTs", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "handler = TTs, data from web = " + data);
                tts();
                setToken("1234");
                function.onCallBack("TTs发音");
            }
        });

        // 调用前端 js 方法 发送 socket 消息
        webView.callHandler("onSocketMsg", "测试服务器消息asdfasdfasdf", new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                System.out.println("callHandler data =" + data);
            }
        });
    }

    /**
     * 发音初始化
     */
    private void tts(){
        // 初始化合成对象
        if(null == mTts) {
            mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);

            //// 取消合成
            //mTts.stopSpeaking();
            //// 暂停播放
            //mTts.pauseSpeaking();
            //// 继续播放
            //mTts.resumeSpeaking();
            setParam();
        }

        int code = mTts.startSpeaking("测试发音", mTtsListener);
        //			/**
        //			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
        //			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
        //			*/
        //			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
        //			int code = mTts.synthesizeToUri(text, path, mTtsListener);

        if (code != ErrorCode.SUCCESS) {
            showTip("语音合成失败,错误码: " + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        }
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："
                                + code
                                + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onCompleted(SpeechError speechError) {
            if (speechError == null) {
                showTip("播放完成");
            } else {
                showTip("播放失败");
            }
        }

        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            //// 合成进度
            //mPercentForBuffering = percent;
            //showTip(String.format(getString(R.string.tts_toast_format),
            //                      mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            //mPercentForPlaying = percent;
            //showTip(String.format(getString(R.string.tts_toast_format),
            //                      mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}

            //实时音频流输出参考
			/*if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
				byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
				Log.e("MscSpeechLog", "buf is =" + buf);
			}*/
        }
    };

    private void showTip(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(str);
                mToast.show();
            }
        });
    }

    /**
     * 参数设置
     */
    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        ////设置合成
        //if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
        //    //设置使用云端引擎
        //    mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //    //设置发音人
        //    mTts.setParameter(SpeechConstant.VOICE_NAME, voicerCloud);
        //} else {
            //设置使用本地引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //    //设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
        //    //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicerLocal);
        //}
        //mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY,"1");//支持实时音频流抛出，仅在synthesizeToUri条件下支持
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED,"50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH,"50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME,"50");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE,"3");

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
                          Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    //获取发音人资源路径
    private String getResourcePath(){
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/"+voicerLocal+".jet"));
        return tempBuffer.toString();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isOnAttacheWindow = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(isOnAttacheWindow ){
            isOnAttacheWindow = false;
            //这里才代表view的xml加载完了，可以安全的操作主线程了
            //权限请求
            requestPermissions();

            //初始化webview相关
            initWebView();

            try {
                connectSocketIO();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 建立socket连接
     * @throws URISyntaxException
     * @throws JSONException
     */
    private void connectSocketIO() throws URISyntaxException, JSONException {
        IO.Options options = new IO.Options();
        options.transports = new String[]{"websocket"};//,"xhr-polling","jsonp-polling"
        options.reconnectionAttempts = 2;     // 重连尝试次数
        options.reconnectionDelay = 5000;     // 失败重连的时间间隔(ms)
        options.timeout = 20000;              // 连接超时时间(ms)
        options.forceNew = true;

        options.query = "token=c45108ef2250b8a523952c841c78244b&appCode=outp-1-1002";
        socket = IO.socket("http://220.168.30.123:18021", options);

        /*
         * options.query = "token=c45108ef2250b8a523952c841c78244b&appCode=outp-1-1002";
         * socket = IO.socket("http://localhost:8021", options);
         */
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket call","Socket.CONNECT_SUCSSFUL");
            }
        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket call","Socket.EVENT_CONNECT_ERROR");
                socket.disconnect();
            }
        }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket call","Socket.EVENT_CONNECT_TIMEOUT");
                socket.disconnect();
            }
        }).on(Socket.EVENT_PING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket call","Socket.EVENT_PING");
            }
        }).on(Socket.EVENT_PONG, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket call","Socket.EVENT_PONG");
            }
        }).on("push_event", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket call","-----------接受到消息啦--------" + Arrays.toString(args));
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("socket call","客户端断开连接啦。。。");
                socket.disconnect();
            }
        });
        socket.connect();
        //给所有客户端广播消息
        //socket.emit(Constants.BROADCAST_KEY,"{data:\"hello,all\"}");
        //加入房间（分组）
        //socket.emit(Constants.ROOM_CHAT_KEY, "1081262");

        MessageInfo sendData = new MessageInfo();
        //		sendData.setSourceSessionId("12");
        //		sendData.setTargetSessionId("12");
        sendData.setMsg("客户端发送的消息");

        socket.send(JsonConverterUtils.objectToJSONObject(sendData));
    }

    //读取token
    public String getToken(){
        return sp.getString("token","");
    }

    //这里使用的是 apply() 方法保存，将不会有返回值
    public void setToken(String token){
        SharedPreferences.Editor e = sp.edit();
        e.putString("token",token);
        e.commit();
    }

    /**
     * 6.0以上请求权限
     */
    private void requestPermissions(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.LOCATION_HARDWARE,Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_SETTINGS,Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_CONTACTS},0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

