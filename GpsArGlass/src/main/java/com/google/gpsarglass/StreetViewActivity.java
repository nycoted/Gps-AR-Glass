package com.google.gpsarglass;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

public class StreetViewActivity extends Activity implements OnStreetViewPanoramaReadyCallback {

    private LatLng position;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streetview);

        // 🔹 Récupération des coordonnées transmises par MapsActivity
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);
        position = new LatLng(lat, lng);

        // 🔹 Récupération du fragment StreetView
        StreetViewPanoramaFragment streetViewFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama);

        if (streetViewFragment != null) {
            streetViewFragment.getStreetViewPanoramaAsync(this);
        }

        // 🔹 Détecteur de gestes (tap simple = quitter)
        gestureDetector = new GestureDetector(this, new GestureListener());
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        if (position != null) {
            panorama.setPosition(position);
            panorama.setStreetNamesEnabled(true);
            panorama.setUserNavigationEnabled(true);
            panorama.setZoomGesturesEnabled(true);

            Toast.makeText(this,
                    "Street View chargé : " + position.latitude + ", " + position.longitude,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    // 🔹 Classe interne pour gérer les gestes
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Toast.makeText(StreetViewActivity.this, "Retour à la carte", Toast.LENGTH_SHORT).show();
            finish(); // 🔹 Ferme StreetViewActivity et revient à MapsActivity
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffY) > 100 && velocityY > 100) {
                Toast.makeText(StreetViewActivity.this, "Swipe bas : quitter Street View", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            }
            return false;
        }
    }
}



