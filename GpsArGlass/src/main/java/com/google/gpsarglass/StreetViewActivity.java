package com.google.gpsarglass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class StreetViewActivity extends Activity {

    private WebView webView;
    private double startLat = 48.8584;   // Tour Eiffel par défaut
    private double startLng = 2.2945;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streetview);

        // Récupération des coordonnées envoyées
        if (getIntent() != null) {
            if (getIntent().hasExtra("lat")) {
                startLat = getIntent().getDoubleExtra("lat", startLat);
            }
            if (getIntent().hasExtra("lng")) {
                startLng = getIntent().getDoubleExtra("lng", startLng);
            }
        }

        webView =(WebView) findViewById(R.id.webview_streetview);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new JSBridge(), "Android");

        String apiKey;
        try {
            apiKey = getString(R.string.google_maps_key);
        } catch (Exception e) {
            apiKey = "";
        }

        // HTML embarqué
        final String html = "<!doctype html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=yes\" />\n" +
                "  <style>html,body,#streetview{height:100%;margin:0;padding:0}</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div id=\"streetview\"></div>\n" +
                "  <script src=\"https://maps.googleapis.com/maps/api/js?key=" + apiKey + "&callback=initStreetView\" async defer></script>\n" +
                "  <script>\n" +
                "    var panorama;\n" +
                "    function initStreetView() {\n" +
                "      var lat = " + startLat + ";\n" +
                "      var lng = " + startLng + ";\n" +
                "      panorama = new google.maps.StreetViewPanorama(document.getElementById('streetview'), {\n" +
                "        position: {lat: lat, lng: lng},\n" +
                "        pov: {heading: 0, pitch: 0},\n" +
                "        zoom: 1,\n" +
                "        addressControl: true,\n" +
                "        linksControl: true,\n" +
                "        panControl: true\n" +
                "      });\n" +
                "      notifyPosition(); notifyPov();\n" +
                "      panorama.addListener('position_changed', function() { notifyPosition(); });\n" +
                "      panorama.addListener('pov_changed', function() { notifyPov(); });\n" +
                "    }\n" +
                "    function notifyPosition(){\n" +
                "      if (!panorama) return;\n" +
                "      var p = panorama.getPosition();\n" +
                "      if (p && window.Android && window.Android.onPositionChanged){\n" +
                "        window.Android.onPositionChanged(p.lat()+\",\"+p.lng());\n" +
                "      }\n" +
                "    }\n" +
                "    function notifyPov(){\n" +
                "      if (!panorama) return;\n" +
                "      var pov = panorama.getPov();\n" +
                "      var heading = pov && pov.heading ? pov.heading : 0;\n" +
                "      var pitch = pov && pov.pitch ? pov.pitch : 0;\n" +
                "      if (window.Android && window.Android.onPOVChanged){\n" +
                "        window.Android.onPOVChanged(heading+\",\"+pitch);\n" +
                "      }\n" +
                "    }\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";

        webView.loadDataWithBaseURL("https://maps.googleapis.com/", html, "text/html", "utf-8", null);
    }

    private class JSBridge {
        @JavascriptInterface
        public void onPositionChanged(String latLng) {
            try {
                if (latLng == null) return;
                String[] parts = latLng.split(",");
                if (parts.length < 2) return;
                final double lat = Double.parseDouble(parts[0]);
                final double lng = Double.parseDouble(parts[1]);

                Intent intent = new Intent("com.google.gpsarglass.UPDATE_MAP");
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                sendBroadcast(intent);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StreetViewActivity.this,
                                String.format("StreetView: lat=%.6f lon=%.6f", lat, lng),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void onPOVChanged(String headingPitch) {
            try {
                if (headingPitch == null) return;
                String[] p = headingPitch.split(",");
                if (p.length < 2) return;
                final float heading = Float.parseFloat(p[0]);
                final float pitch = Float.parseFloat(p[1]);

                Intent intent = new Intent("com.google.gpsarglass.UPDATE_MAP");
                intent.putExtra("heading", heading);
                intent.putExtra("pitch", pitch);
                sendBroadcast(intent);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StreetViewActivity.this,
                                String.format("Caméra: heading=%.1f°, pitch=%.1f°", heading, pitch),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}