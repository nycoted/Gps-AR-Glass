package com.google.gpsarglass;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
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
                "Donnez une commande (bus, train, streetview)");
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
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Toast toast = Toast.makeText(getApplicationContext(),
                "Swipe vers le bas pour fermer l'application",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));
    }
}



