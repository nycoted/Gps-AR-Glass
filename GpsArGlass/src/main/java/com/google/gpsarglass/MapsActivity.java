package com.google.gpsarglass;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends Activity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int VOICE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FragmentManager fm = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 🔹 Démarrage auto reconnaissance vocale
        startVoiceRecognition();
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Commande : bus, train, streetview, aller à [adresse]");
        startActivityForResult(intent, VOICE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                handleVoiceCommand(results.get(0).toLowerCase());
            }
        }
    }

    private void handleVoiceCommand(String command) {
        if (command.contains("bus")) {
            Toast.makeText(this, "Récupération horaires bus...", Toast.LENGTH_SHORT).show();
            fetchTransitInfo("bus");
        } else if (command.contains("train")) {
            Toast.makeText(this, "Récupération horaires train...", Toast.LENGTH_SHORT).show();
            fetchTransitInfo("train");
        } else if (command.contains("streetview")) {
            openStreetView(-34, 151);
        } else if (command.contains("aller à")) {
            String destination = command.replace("aller à", "").trim();
            goToDestination(destination);
        } else {
            Toast.makeText(this, "Commande non reconnue : " + command, Toast.LENGTH_SHORT).show();
        }
    }

    private void openStreetView(double lat, double lng) {
        Intent intent = new Intent(this, StreetViewActivity.class);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        startActivity(intent);
    }

    private void goToDestination(String destinationName) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(destinationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng point = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(point).title(destinationName));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 16));
                openStreetView(address.getLatitude(), address.getLongitude());
            } else {
                Toast.makeText(this, "Adresse introuvable : " + destinationName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la recherche d'adresse", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔹 API Google Directions pour horaires bus/train
    private void fetchTransitInfo(String mode) {
        try {
            String origin = "48.8584,2.2945"; // Exemple : Tour Eiffel
            String destination = "48.8809,2.3553"; // Exemple : Gare du Nord
            String apiKey = getString(R.string.google_maps_key); // 🔹 clé stockée dans res/values/google_maps_api.xml

            String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + origin +
                    "&destination=" + destination +
                    "&mode=transit&transit_mode=" + mode +
                    "&key=" + apiKey;

            new FetchTransitTask().execute(url);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Transit horaires
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

                    Toast.makeText(MapsActivity.this,
                            "Départ : " + departure + "\nArrivée : " + arrival,
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

    // 🔹 Décode polyline
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
            LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            poly.add(p);
        }
        return poly;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        Toast toast = Toast.makeText(getApplicationContext(),
                "Swipe vers le bas pour quitter",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));
    }
}