package com.google.gpsarglass;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class StreetViewActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streetview);

        WebView webView = (WebView) findViewById(R.id.webview);;

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        // Exemple : Street View centré sur Sydney (-34,151)
        String url = "https://maps.google.com/maps?q=&layer=c&cbll=-34,151&cbp=11,0,0,0,0";
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}