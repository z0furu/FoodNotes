package com.example.professorlee.foodnotes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private LocationManager mLocationManager = null;
    private String provider = null;
    private Marker mCurrentMarker = null;
    private Geocoder geocoder;
    private String strAddress;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mMap);
        mapFragment.getMapAsync(this);
        configGoogleApiClient();
    }

    private synchronized void configGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {
        updateWithLocation(location);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (isProviderAvailable() && provider != null) {
            Location location = mLocationManager.getLastKnownLocation(provider);
            updateWithLocation(location);
        }
    }

    private boolean isProviderAvailable() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        provider = mLocationManager.getBestProvider(criteria, true);
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        }

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(), "請開啟GPS提高精準度", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }else {
            provider = LocationManager.GPS_PROVIDER;
            Log.i(TAG, "isProviderAvailable: " + provider);
            return true;
        }

        return false;
    }



    private void updateWithLocation(Location location) {
        if (location != null && provider != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            addMarker(lat, lon);

            CameraPosition cam = new CameraPosition.Builder()
                    .target(new LatLng(lat, lon)).zoom(17f).build();
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam));
            }else {
                Log.i(TAG, "updateWithLocation: Location Error");
            }
        }
    }

    private void addMarker(double lat, double lon) {
        geocoder = new Geocoder(this, Locale.TAIWAN);
        try {
            List<Address> listAddress = geocoder.getFromLocation(lat, lon, 1);
            strAddress = listAddress.get(0).getAddressLine(0);
            Log.i(TAG, "addMarker: " + strAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MarkerOptions mMarkOption = new MarkerOptions();
        mMarkOption.position(new LatLng(lat, lon));
        mMarkOption.title(strAddress);
        mMarkOption.anchor(0.5f, 0.5f);

        CircleOptions circleOption = new CircleOptions()
                .center(new LatLng(lat, lon)).radius(100)
                .strokeColor(0x110000FF).strokeWidth(1).fillColor(0x110000FF);
        Circle circle = mMap.addCircle(circleOption);

        if (mCurrentMarker != null) {
            mCurrentMarker.remove();
            Log.i(TAG, "addBoundaryToCurrentPosition: removeMarker");
            circle.remove();
            mCurrentMarker = mMap.addMarker(mMarkOption);
            Log.i(TAG, "addBoundaryToCurrentPosition: create marker");
        }
        if (mCurrentMarker == null) {
            Log.i(TAG, "addBoundaryToCurrentPosition: create marker");
            mCurrentMarker = mMap.addMarker(mMarkOption);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected() && mCurrentMarker != null) {
            mGoogleApiClient.connect();
            Log.i(TAG, "onResume: Success");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            Log.i(TAG, "onPause: remove");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            Log.i(TAG, "onStop: disconnnect");
        }
    }
}
