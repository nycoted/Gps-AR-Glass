package com.google.gpsarglass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView webView;
    private TextView gpsInfo;
    private ImageView arrow;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupération des vues
        webView = findViewById(R.id.webview);
        gpsInfo = findViewById(R.id.gpsInfo);
        arrow = findViewById(R.id.arrow);

        // Configurer le WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadUrl("https://www.google.com/maps/@48.8588443,2.2943506,3a,75y,90t");

        // Initialiser le détecteur de gestes
        gestureDetector = new GestureDetector(this, new GestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();

        if ("com.example.ACTION_BUS_TIME".equals(action)) {
            gpsInfo.setText("Bus 72 dans 5 minutes");
        } else if ("com.example.ACTION_LOCATION".equals(action)) {
            webView.loadUrl("https://maps.google.com/maps?q=ma_position&layer=sv");
        } else if ("com.example.ACTION_ZOOM_IN".equals(action)) {
            webView.zoomIn();
        } else if ("com.example.ACTION_ZOOM_OUT".equals(action)) {
            webView.zoomOut();
        }
    }

    // Classe interne pour gérer les gestes
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Toast.makeText(MainActivity.this, "Tap détecté", Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // 🔹 Double-tap → recentrer sur la Tour Eiffel
            Toast.makeText(MainActivity.this, "Double-tap : recentrage", Toast.LENGTH_SHORT).show();
            webView.loadUrl("https://www.google.com/maps/@48.8588443,2.2943506,17z");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                // Swipe horizontal
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        arrow.setRotation(90); // droite
                        Toast.makeText(MainActivity.this, "Swipe droite", Toast.LENGTH_SHORT).show();
                    } else {
                        arrow.setRotation(270); // gauche
                        Toast.makeText(MainActivity.this, "Swipe gauche", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            } else {
                // Swipe vertical
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        webView.zoomOut();
                        Toast.makeText(MainActivity.this, "Swipe bas : zoom out", Toast.LENGTH_SHORT).show();
                    } else {
                        webView.zoomIn();
                        Toast.makeText(MainActivity.this, "Swipe haut : zoom in", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }
            return false;
        }
    }
}