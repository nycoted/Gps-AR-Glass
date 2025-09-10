package com.google.gpsarglass;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends Activity {

    private WebView webView;
    private BroadcastReceiver streetViewReceiver;
    private String apiKey; // 🔑 clé récupérée depuis google_maps_api.xml

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Charger la clé API depuis res/values/google_maps_api.xml
        apiKey = getString(R.string.google_maps_key);

        // WebView Google Maps
        webView = (WebView) findViewById(R.id.webview_map);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.google.com/maps");

        // Message d’accueil
        Toast toast = Toast.makeText(getApplicationContext(),
                "Swipe vers le bas pour quitter",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        // Bouton micro flottant
        ImageButton btnMicro =(ImageButton) findViewById(R.id.btn_micro);
        btnMicro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Exemple : lancer recherche bus quand on clique
                fetchTransitInfo("bus");
            }
        });

        // Receiver StreetView
        streetViewReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("OPEN_STREETVIEW".equals(intent.getAction())) {
                    double lat = intent.getDoubleExtra("lat", 0);
                    double lng = intent.getDoubleExtra("lng", 0);
                    Intent streetViewIntent = new Intent(MapsActivity.this, StreetViewActivity.class);
                    streetViewIntent.putExtra("lat", lat);
                    streetViewIntent.putExtra("lng", lng);
                    startActivity(streetViewIntent);
                }
            }
        };
        registerReceiver(streetViewReceiver, new IntentFilter("OPEN_STREETVIEW"));

        // Exemple : ouvrir Street View fixe (Tour Eiffel) si clic long
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MapsActivity.this, StreetViewActivity.class);
                intent.putExtra("lat", 48.8584);
                intent.putExtra("lng", 2.2945);
                startActivity(intent);
                return true;
            }
        });
    }

    // 🔹 Recherche horaires bus/train
    private void fetchTransitInfo(String mode) {
        try {
            String origin = "48.8584,2.2945"; // Tour Eiffel
            String destination = "48.8809,2.3553"; // Gare du Nord

            String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + origin +
                    "&destination=" + destination +
                    "&mode=transit&transit_mode=" + mode +
                    "&key=" + apiKey;

            new FetchTransitTask().execute(url);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur préparation requête transit", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 AsyncTask pour interroger l’API Directions
    private class FetchTransitTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String json) {
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray routes = obj.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);

                    String departure = leg.has("departure_time")
                            ? leg.getJSONObject("departure_time").getString("text")
                            : "Non disponible";

                    String arrival = leg.has("arrival_time")
                            ? leg.getJSONObject("arrival_time").getString("text")
                            : "Non disponible";

                    // Affichage
                    Toast.makeText(MapsActivity.this,
                            "🚍 Départ : " + departure + "\n🏁 Arrivée : " + arrival,
                            Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(MapsActivity.this, "Aucun trajet trouvé", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MapsActivity.this, "Erreur parsing horaires", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🔹 Décode polyline (si besoin d’afficher un trajet plus tard)
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng(((double) lat / 1E5),
                    ((double) lng / 1E5));
            poly.add(p);
        }
        return poly;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (streetViewReceiver != null) {
            unregisterReceiver(streetViewReceiver);
        }
    }
}