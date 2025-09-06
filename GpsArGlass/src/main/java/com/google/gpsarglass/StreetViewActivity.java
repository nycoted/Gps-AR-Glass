package com.google.gpsarglass;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

public class StreetViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streetview);

        // Récupérer la position passée depuis MapsActivity
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);
        final LatLng position = new LatLng(lat, lng);

        // Charger le fragment Street View
        StreetViewPanoramaFragment streetViewFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama);

        if (streetViewFragment != null) {
            streetViewFragment.getStreetViewPanoramaAsync(
                    new OnStreetViewPanoramaReadyCallback() {
                        @Override
                        public void onStreetViewPanoramaReady(final StreetViewPanorama panorama) {
                            // Placer la caméra à la position demandée
                            panorama.setPosition(position);

                            // ✅ Activer toutes les options d'interaction
                            panorama.setUserNavigationEnabled(true);   // Se déplacer dans la rue
                            panorama.setZoomGesturesEnabled(true);    // Pinch pour zoom
                            panorama.setPanningGesturesEnabled(true); // Swipe pour tourner caméra
                            panorama.setStreetNamesEnabled(true);     // Affichage des rues

                            // 🔹 Synchroniser la position quand on bouge dans Street View
                            panorama.setOnStreetViewPanoramaChangeListener(
                                    new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
                                        @Override
                                        public void onStreetViewPanoramaChange(StreetViewPanoramaLocation location) {
                                            if (location != null && location.position != null) {
                                                LatLng newPos = location.position;
                                                Toast.makeText(StreetViewActivity.this,
                                                        "Nouvelle position : " +
                                                                newPos.latitude + ", " + newPos.longitude,
                                                        Toast.LENGTH_SHORT).show();

                                                // Ici, tu pourrais envoyer cette nouvelle position à MapsActivity
                                                // via Intent ou BroadcastReceiver pour déplacer aussi la carte.
                                            }
                                        }
                                    });
                        }
                    });
        }
    }
}



