package org.innovationsforlearning.StudentPortal;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.content.pm.ActivityInfo;

import com.amazon.android.webkit.AmazonWebKitFactories;
import com.amazon.android.webkit.AmazonWebKitFactory;
import com.amazon.android.webkit.AmazonWebView;
import com.amazon.android.webkit.AmazonWebViewClient;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.innovationsforlearning.StudentPortal.BuildConfig.FLAVOR;


public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final String TAG = "SP-RnR";

    private static final class URLS {
        public static final String production = "https://portal.sp.tutormate.org/";
        public static final String staging = "https://portal.sp-staging.tutormate.org/";
        public static final String development = "http://10.0.0.21:3000/";
    }

    private WebView mWebView;
    private static boolean sFactoryInit = false;
    private AmazonWebKitFactory factory = null;
    private TextToSpeech tts = null;
    private Audio audio = null;
    private static String mFileName = null;
    private static boolean isSpeaking = false;


//    @SuppressWarnings("deprecation")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        // Record to the external cache directory for visibility
        mFileName = getExternalCacheDir().getAbsolutePath();
        mFileName += "/recording.aac";

        tts = new TextToSpeech(this, this);
        audio = new Audio();


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
        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);


        // Set cache size to 8 mb by default. should be more than enough
        mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);

        // This next one is crazy. It's the DEFAULT location for your app's cache
        // But it didn't work for me without this line
        mWebView.getSettings().setAppCachePath("/data/data/" + getPackageName() + "/cache");
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);

        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);


        mWebView.setWebViewClient(new MyAppWebViewClient());
        // bridge interface java/javascript
        mWebView.addJavascriptInterface(new WebViewBridge(this, mWebView), "android");

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Speaking started.
                isSpeaking = true;

            }

            @Override
            public void onDone(String utteranceId) {
                // Speaking stopped.

                isSpeaking = false;
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

        String url;
        if (FLAVOR.equals("staging")) {
            url = URLS.staging;
        } else if (FLAVOR.equals("development")) {
            url = URLS.development;
        } else {
            url = URLS.production;
        }

        Log.e(TAG, "url:"+url);
        // Use remote resource
        mWebView.loadUrl(url);

        // Stop local links and redirects from opening in browser instead of WebView
        mWebView.setWebViewClient(new MyAppWebViewClient());

//        mWebView.loadUrl("javascript:speechSynthesis.speak('hello');");

        // Use local resource
        // mWebView.loadUrl("file:///android_asset/www/index.html");
    }

    final class WebViewBridge {
        Context mContext;
        AmazonWebView mWebView;

       /*My interface*/

        /**
         * Instantiate the interface and set the context
         */
        WebViewBridge(Context c, AmazonWebView w) {
            mContext = c;
            mWebView = w;
        }

        @JavascriptInterface
        public void speak(String inputText) {
            /*
            tts.setLanguage(new Locale(setLang));
            Toast.makeText(getApplicationContext(), tts.toString(),
                    Toast.LENGTH_SHORT).show();
            */

            tts.speak(inputText, TextToSpeech.QUEUE_FLUSH, null, inputText);
            Log.e(TAG, "speak");
        }

        @JavascriptInterface
        public void ttsStop() {
            if (tts != null) {
                tts.stop();
                // tts.shutdown();
            }
        }

        @JavascriptInterface
        public boolean isPlaying() {
            return isSpeaking;
        }

        @JavascriptInterface
        public void startRecording(String file) {

            Log.e(TAG, "start recording:" + file);
            audio.startRecording();
        }

        @JavascriptInterface
        public void stopRecording() {

            Log.e(TAG, "stop recording");
            audio.stopRecording();
        }

        @JavascriptInterface
        public void startPlayback() {

            Log.e(TAG, "start playback");
            audio.startPlaying(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {

                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "playback complete");
                            String script = "window.firePlayCallback();" +
                                    "";
                            mWebView.loadUrl("javascript:" + script);
                        }
                    });
                }

            });
        }

        @JavascriptInterface
        public void stopPlayback() {
            Log.e(TAG, "stop playback");
        }

        @JavascriptInterface
        public String getBase64() {
            String data = "";
            try {
                data = audio.getBase64();
                Log.e(TAG, "getBase64 length:" + data.length());
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return data;
        }

    }

    public void onInit(int status) {
        // TODO Auto-generated method stub
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
        if (mWebView.canGoBack()) {
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