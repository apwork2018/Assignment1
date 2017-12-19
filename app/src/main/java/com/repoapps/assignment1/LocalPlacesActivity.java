package com.repoapps.assignment1;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by ap1 on 12/19/17.
 */

public class LocalPlacesActivity extends AppCompatActivity {
    private static final String TAG = "LocalPlaces";

    private Button btnGetLocalPlaces;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localplaces);

        btnGetLocalPlaces = findViewById(R.id.btnGetPlaces);
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, failedConnListener)
                .build();
        btnGetLocalPlaces.setOnClickListener(btnClickListener);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(placesMapReadyCallback);


    }

    @Override
    protected void onStart() {
        super.onStart();
        btnGetLocalPlaces.performClick();
    }

    final OnMapReadyCallback placesMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
        }
    };

    private void updateMap(LatLng mPlace, String title, String desc, LatLngBounds bounds) {
        mMap.addMarker(new MarkerOptions()
                .position(mPlace)
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet(desc));
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,0);
        mMap.animateCamera(cu);
    }

    @Override
    protected void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    final GoogleApiClient.OnConnectionFailedListener failedConnListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(TAG, connectionResult.getErrorMessage());
        }
    };

    final View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dumpLocalPlaces();
        }
    };

    private void dumpLocalPlaces() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);

        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    String sName = placeLikelihood.getPlace().getName().toString();
                    String sDesc = placeLikelihood.getPlace().getAddress().toString();
                    LatLng loc = placeLikelihood.getPlace().getLatLng();
                    boundsBuilder.include(loc);
                    updateMap(loc, sName,sDesc, boundsBuilder.build());
                }
                likelyPlaces.release();
            }
        });

    }
}
