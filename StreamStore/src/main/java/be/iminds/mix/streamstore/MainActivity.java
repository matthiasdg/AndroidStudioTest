package be.iminds.mix.streamstore;


import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.content.pm.ActivityInfo;



public class MainActivity extends Activity {
    VideoEnabledWebView myWebView;
    VideoEnabledWebChromeClient webChromeClient;
    MyProgressDialog dialog;
    SensorData sensorData;
    NetworkState networkState;
    BatteryState batteryState;
    ActivityRecognizer acrec;
    ActivityRecognitionReceiver receiver;
    String lastOriginalUrl = "";
    String device ="";
    String osversion = Build.VERSION.RELEASE;
    String devicemodel = Build.MANUFACTURER + " " + Build.PRODUCT;
    String natief = "true";
    String baseUserAgent = "";
    String userAgentString = "";
    public String activityState;
    IntentFilter intentFilter;
    private boolean initialized = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        intentFilter = new IntentFilter("StreamStore");
        receiver = new ActivityRecognitionReceiver();
        activityState = "\"activity\":\"unknown\"";
        acrec = new ActivityRecognizer(MainActivity.this);
        acrec.onCreate(savedInstanceState);
//        acrec.startUpdates();
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
        batteryState = new BatteryState(MainActivity.this);
//        HeartRateTracker hr = new HeartRateTracker(MainActivity.this,MainActivity.this );
        baseUserAgent = "{\"device\":\"" + device + "\",\"os\": \"Android\",\"osversion\":\"" + osversion + "\",\"devicemodel\":\""+ devicemodel + "\",\"native\": " + natief + ",";
        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout); // Your own view, read class comments

        myWebView = (VideoEnabledWebView) findViewById(R.id.webview);
        myWebView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    userAgentString = baseUserAgent + sensorData.toString() + ","+ networkState.toString() + "," + batteryState.toString() +","+ activityState + "}";
                    myWebView.getSettings().setUserAgentString(userAgentString);
//                event moet nog geprocessed worden in webview
                }
                return false;
            }
        });
        userAgentString = baseUserAgent + sensorData.toString() + ","+ networkState.toString() + "," + batteryState.toString() +","+ activityState + "}";
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setUserAgentString(userAgentString);
        webSettings.setJavaScriptEnabled(true);
//        next line necessary to enable local storage
        webSettings.setDomStorageEnabled(true);
//        console logging van browser ook hier in de debugger zichtbaar
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout,null, myWebView, MainActivity.this){
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyApplication", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                // Your code to handle the full-screen change, for example showing and hiding the title bar
            }
        });
        myWebView.setWebChromeClient(webChromeClient);
//        Comment this for testing in emulator
//        myWebView.addJavascriptInterface(sensorData, "Android");

        myWebView.setWebViewClient(new WebViewClient(){
//              problem with redirects in Android > 3 (http://www.catchingtales.com/android-webview-shouldoverrideurlloading-and-redirect/416/)
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url){
                String regEx = "(?i).*(title=disclaimer|disclaimercim|gebruiksvoorwaarden|overview-projects).*";
                if(url.matches(regEx)){
//                    Log.d("MyApplication", "true");
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                }else{
                    if(initialized && (wv.getOriginalUrl()== null || !lastOriginalUrl.equals(wv.getOriginalUrl()))){
                        dialog = MyProgressDialog.show(MainActivity.this, null, null, true, false, null);
                    }
//                    Log.d("MyApplication", "shouldoverride: "+ url+" original: "+wv.getOriginalUrl());
                    lastOriginalUrl = (wv.getOriginalUrl() != null) ? wv.getOriginalUrl(): "";
                    wv.loadUrl(url);
//                    Log.d("MyApplication", "false");
                    return true;
                }
            }

            @Override
            public void onPageFinished(WebView wv, String url){
                if(dialog != null && dialog.isShowing()) dialog.dismiss();
//                Log.d("MyApplication", "pagefinished: " + url +" original: "+wv.getOriginalUrl());
//                Toast.makeText(MainActivity.this, sensorData.toString(), Toast.LENGTH_LONG).show();
                if(!initialized){
                    findViewById(R.id.webview).setVisibility(View.VISIBLE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            findViewById(R.id.splash).setVisibility(View.GONE);
                            initialized = true;
                        }
                    }, 3000);
                }
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

            @Override
            //            if request to file with extension -> don't use special useragent
            //            don't send to media partners
            //            POSSIBLE PROBLEMS THOUGH -> UI THREAD + MEMORY LEAK
            public WebResourceResponse shouldInterceptRequest(WebView wv, String url){

                if(url.matches("(?i).*\\/[a-z0-9]+\\.[a-z0-9]+$")){
                    //                   default user agent
                    wv.getSettings().setUserAgentString("");
                }
                return null;
            }
        });

        myWebView.loadUrl("http://straalstroom.mixlab.be");


    }
    @Override
    protected void onResume(){
        super.onResume();
        receiver = new ActivityRecognitionReceiver();
        registerReceiver(receiver, intentFilter);
        sensorData.resume();
        acrec.startUpdates();

    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorData.stop();
        acrec.stopUpdates();
        try{
            unregisterReceiver(receiver);
        }catch(Exception e){
            Log.d("StreamStore", e.toString());
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webChromeClient.onBackPressed()) return true;
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
//            FILTHY HACK!!!
            if(!myWebView.getUrl().endsWith("/reader/") && !myWebView.getUrl().endsWith("/reader/#streams/now")){
                myWebView.goBack();
//                Log.d("TOUCHDOWN!", myWebView.getUrl());
                return true;
            }
        }
        if(keyCode == KeyEvent.KEYCODE_MENU){
            myWebView.loadUrl("javascript:App.Views.sidebar.toggle()");
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

}
