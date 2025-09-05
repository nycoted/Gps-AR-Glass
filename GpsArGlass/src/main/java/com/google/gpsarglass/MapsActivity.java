package com.google.gpsarglass;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends Activity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialisation de la map
        FragmentManager fm = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Lancer la reconnaissance vocale
        startVoiceRecognition();
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Donnez une commande (bus, train, streetview, aller à ...)");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                handleVoiceCommand(results.get(0).toLowerCase());
            }
        }
    }

    private void handleVoiceCommand(String command) {
        if (command.contains("arrêt de bus")) {
            showBusStops();
        } else if (command.contains("train")) {
            showTrainSchedules();
        } else if (command.contains("streetview")) {
            openStreetView();
        } else if (command.startsWith("aller à")) {
            String destination = command.replace("aller à", "").trim();
            goToDestination(destination);
        } else {
            Toast.makeText(this,
                    "Commande non reconnue : " + command,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showBusStops() {
        LatLng busStop = new LatLng(-34.001, 151.002);
        mMap.addMarker(new MarkerOptions().position(busStop).title("Arrêt de bus"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busStop, 15));
    }

    private void showTrainSchedules() {
        Toast.makeText(this, "Horaires de train affichés (exemple)",
                Toast.LENGTH_SHORT).show();
    }

    private void openStreetView() {
        Toast.makeText(this, "Ouverture de Street View...",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, StreetViewActivity.class);
        intent.putExtra("lat", -34.0);
        intent.putExtra("lng", 151.0);
        startActivity(intent);
    }

    // 🔹 Fonction pour traiter "aller à [adresse]"
    private void goToDestination(String destinationName) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(destinationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng destinationPoint = new LatLng(address.getLatitude(), address.getLongitude());

                // Place un marker
                mMap.addMarker(new MarkerOptions().position(destinationPoint).title(destinationName));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationPoint, 16));

                // Lance StreetView sur la destination
                Intent intent = new Intent(this, StreetViewActivity.class);
                intent.putExtra("lat", address.getLatitude());
                intent.putExtra("lng", address.getLongitude());
                startActivity(intent);
            } else {
                Toast.makeText(this,
                        "Adresse introuvable : " + destinationName,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Erreur lors de la recherche d'adresse",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 🔹 Activer le pinch pour zoomer
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        Toast toast = Toast.makeText(getApplicationContext(),
                "Swipe vers le bas pour fermer l'application",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        // Exemple : un point par défaut
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));
    }
}

