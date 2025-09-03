package com.google.gpsarglass;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.Google.gpsarglass.R;

public class MainActivity extends Activity {

    private WebView webView;
    private TextView gpsInfo;
    private ImageView arrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView arrow = findViewById(R.id.my_arrow);

        webView = findViewById(R.id.webview);
        gpsInfo = findViewById(R.id.gpsInfo);
        arrow = findViewById(R.id.arrow);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        // Charger un point Street View (Tour Eiffel par ex)
        webView.loadUrl("https://www.google.com/maps/@48.8588443,2.2943506,3a,75y,90t");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        if ("com.example.ACTION_BUS_TIME".equals(action)) {
            gpsInfo.setText("Bus 72 dans 5 minutes");
        } else if ("com.example.ACTION_LOCATION".equals(action)) {
            webView.loadUrl("https://maps.google.com/maps?q=ma_position&layer=s"); // vue satellite
        } else if ("com.example.ACTION_ZOOM_IN".equals(action)) {
            webView.zoomIn();
        } else if ("com.example.ACTION_ZOOM_OUT".equals(action)) {
            webView.zoomOut();
        }
    }
}
