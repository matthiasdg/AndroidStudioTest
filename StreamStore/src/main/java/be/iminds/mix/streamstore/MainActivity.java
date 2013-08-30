package be.iminds.mix.streamstore;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.content.pm.ActivityInfo;

public class MainActivity extends Activity {
    WebView myWebView;
    MyProgressDialog dialog;
    SensorData sensorData;
    NetworkState networkState;
    String lastOriginalUrl = "";
    String device ="";
    String osversion = Build.VERSION.RELEASE;
    String devicemodel = Build.MANUFACTURER + " " + Build.PRODUCT;
    String natief = "true";
    String baseUserAgent = "";
    String userAgentString = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if(tabletSize){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            device = "tablet";
        }else{
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            device = "smartphone";
        }
        setContentView(R.layout.activity_main);
//        Comment this for testing in emulator
        sensorData = new SensorData(MainActivity.this);
        networkState = new NetworkState(MainActivity.this);
//        HeartRateTracker hr = new HeartRateTracker(MainActivity.this,MainActivity.this );
        baseUserAgent = "{\"device\":\"" + device + "\",\"os\": \"Android\",\"osversion\":\"" + osversion + "\",\"devicemodel\":\""+ devicemodel + "\",\"native\": " + natief + ",";
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    userAgentString = baseUserAgent + sensorData.toString() + ","+ networkState.toString() + "}";
                    myWebView.getSettings().setUserAgentString(userAgentString);
                    Log.d("TOUCHDOWN!", event.toString());
//                event moet nog geprocessed worden in webview
                }
                return false;
            }
        });
        userAgentString = baseUserAgent + sensorData.toString() + ","+ networkState.toString() + "}";
        myWebView.getSettings().setUserAgentString(userAgentString);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyApplication", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });
//        Comment this for testing in emulator
//        myWebView.addJavascriptInterface(sensorData, "Android");
        myWebView.loadUrl("http://straalstroom.mixlab.be");
        myWebView.setWebViewClient(new WebViewClient(){
//              problem with redirects in Android > 3 (http://www.catchingtales.com/android-webview-shouldoverrideurlloading-and-redirect/416/)
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url){
                if(wv.getOriginalUrl()== null || !lastOriginalUrl.equals(wv.getOriginalUrl())){
                    dialog = MyProgressDialog.show(MainActivity.this, null, null, true, false, null);
                }
                Log.d("MyApplication", "shouldoverride: "+ url+" original: "+wv.getOriginalUrl());
                lastOriginalUrl = (wv.getOriginalUrl() != null) ? wv.getOriginalUrl(): "";
                wv.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView wv, String url){
                if(dialog != null && dialog.isShowing()) dialog.dismiss();
                Log.d("MyApplication", "pagefinished: " + url +" original: "+wv.getOriginalUrl());
//                Toast.makeText(MainActivity.this, sensorData.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
                if(dialog != null && dialog.isShowing()) dialog.dismiss();
                Toast.makeText(MainActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
//                runOnUiThread(new Runnable(){
//                    @Override
//                    public void run()
//                    {}
//                });
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
//            FILTHY HACK!!!
            if(!myWebView.getUrl().endsWith("/reader/") && !myWebView.getUrl().endsWith("/reader/#streams/now")){
                myWebView.goBack();
                Log.d("TOUCHDOWN!", myWebView.getUrl());
                return true;
            }
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
