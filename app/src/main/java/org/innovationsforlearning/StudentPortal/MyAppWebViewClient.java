package org.innovationsforlearning.StudentPortal;

import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Window;

import android.webkit.JavascriptInterface;
import com.amazon.android.webkit.AmazonWebView;
import com.amazon.android.webkit.AmazonWebViewClient;

public class MyAppWebViewClient extends AmazonWebViewClient {

    AmazonWebView webView;
    Window window;


    @Override
    public boolean shouldOverrideUrlLoading(AmazonWebView view, String url) {
        /*
        if (Uri.parse(url).getHost().endsWith("tutormate.org")) {
            return false;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
        return true;*/

        return false;

    }
}