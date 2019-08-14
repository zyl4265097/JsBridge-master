package com.github.lzyzsd.jsbridge.example;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;

public class MainActivity extends Activity{

	private final String TAG = "MainActivity";

	BridgeWebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

        webView = (BridgeWebView) findViewById(R.id.webView);

		//button = (Button) findViewById(R.id.button);
        //
		//button.setOnClickListener(this);

		webView.setDefaultHandler(new DefaultHandler());


		//webView.loadUrl("file:///android_asset/demo.html");
        //
		//webView.registerHandler("submitFromWeb", new BridgeHandler() {
        //
		//	@Override
		//	public void handler(String data, CallBackFunction function) {
		//		Log.i(TAG, "handler = submitFromWeb, data from web = " + data);
        //        function.onCallBack("submitFromWeb exe, response data 中文 from Java");
		//	}
        //
		//});
        //
        //User user = new User();
        //Location location = new Location();
        //location.address = "SDU";
        //user.location = location;
        //user.name = "大头鬼";
        //
        //webView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
        //    @Override
        //    public void onCallBack(String data) {
        //        System.out.println("callHandler data ="+data);
        //    }
        //});
        //
        //webView.send("xxx");

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
                function.onCallBack("获取token");
            }

        });

        webView.registerHandler("TTs", new BridgeHandler() {

            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i(TAG, "handler = TTs, data from web = " + data);
                function.onCallBack("TTs发音");
            }

        });

	}

}
