package com.example.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;

import com.amazon.android.webkit.AmazonWebKitFactories;
import com.amazon.android.webkit.AmazonWebKitFactory;
import com.amazon.android.webkit.AmazonWebView;
import com.amazon.android.webkit.AmazonWebViewClient;

import android.util.Log;


public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final String TAG = "SP-TTS";
    private static final String URL = "http://10.0.0.21:3000/?debug=true";

    private WebView mWebView;
    private static boolean sFactoryInit = false;
    private AmazonWebKitFactory factory = null;
    private TextToSpeech tts = null;

    @SuppressWarnings("deprecation")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, this);

        if (!sFactoryInit) {
            factory = AmazonWebKitFactories.getDefaultFactory();
            if (factory.isRenderProcess(this)) {
                return; // Do nothing if this is on render process
            }
            factory.initialize(this.getApplicationContext());

// factory configuration is done here, for example:
            factory.getCookieManager().setAcceptCookie(true);

            sFactoryInit = true;
        } else {
            factory = AmazonWebKitFactories.getDefaultFactory();
        }

        final AmazonWebView mWebView;

        mWebView = (AmazonWebView) findViewById(R.id.activity_main_webview);
        factory.initializeWebView(mWebView, 0xFFFFFF, false, null);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);



        // Set cache size to 8 mb by default. should be more than enough
        mWebView.getSettings().setAppCacheMaxSize(1024*1024*8);

        // This next one is crazy. It's the DEFAULT location for your app's cache
        // But it didn't work for me without this line
        mWebView.getSettings().setAppCachePath("/data/data/"+ getPackageName() +"/cache");
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);

        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);



        mWebView.setWebViewClient(new MyAppWebViewClient());
        // bridge interface java/javascript
        mWebView.addJavascriptInterface(new SpeechSynthesis(this), "android");

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Speaking started.

            }

            @Override
            public void onDone(String utteranceId) {
                // Speaking stopped.

                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:window.TTSCallback.onDone();");
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                // Speaking stopped.

            }

        });


        // Use remote resource
        mWebView.loadUrl(URL);

        // Stop local links and redirects from opening in browser instead of WebView
        mWebView.setWebViewClient(new MyAppWebViewClient());

//        mWebView.loadUrl("javascript:speechSynthesis.speak('hello');");

        // Use local resource
        // mWebView.loadUrl("file:///android_asset/www/index.html");
    }

    final class SpeechSynthesis {
        Context mContext;
       /*My interface*/
        /** Instantiate the interface and set the context */
        SpeechSynthesis(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void speak(String inputText){
            /*
            tts.setLanguage(new Locale(setLang));
            Toast.makeText(getApplicationContext(), tts.toString(),
                    Toast.LENGTH_SHORT).show();
            */

            tts.speak(inputText, TextToSpeech.QUEUE_FLUSH, null, inputText);
        }

        @JavascriptInterface
        public void ttsStop(){
            if (tts != null) {
                tts.stop();
                // tts.shutdown();
            }
        }

    }
    public void onInit(int status) {
        // TODO Auto-generated method stub
        return;
    }

    private TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() {
        public void onInit(int version) {
            //Don't care now!
            //Maybe let the user know hello!
        }
    };
    // Prevent the back-button from closing the app
    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}