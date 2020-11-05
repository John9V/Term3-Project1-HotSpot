package com.example.hotspot.ui.home;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.hotspot.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private int highestIndex = 0;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 10;
    private HomeViewModel homeViewModel;
    private GoogleMap googleMap;
    private boolean locationPermissionGranted = false;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;
    private LatLng defaultLocation;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mostRecentPlace = database.getReference("mostRecentPlace");
    private DatabaseReference places = database.getReference("places");
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        MapView mapView = (MapView) root.findViewById(R.id.mapView);
//        final TextView textView = root.findViewById(R.id.text_home);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        getLocationPermission();
        getDeviceLocation();
        periodicallyStoreLocation();

//        // Write a message to the database

        DatabaseReference myRef = database.getReference("message");
        myRef.setValue("Hello, World!");


        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });
        return root;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(googleMap != null) {
            this.googleMap = googleMap;
            LatLng vancouver = new LatLng(49.2827, -123.1207);
            googleMap.addMarker(new MarkerOptions().position(vancouver).title("Vancouver"));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(vancouver, DEFAULT_ZOOM));
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                final Task<Location>[] locationResult = new Task[]{fusedLocationClient.getLastLocation()};
                locationResult[0].addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                                lastKnownLocation = task.getResult();
                                moveCameraTo(lastKnownLocation);
                                storeRecentPlace();
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            googleMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }

                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }

    }

    CountDownTimer countDownTimer;
    public void periodicallyStoreLocation() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 10000) {

            // This is called after every given interval.
            public void onTick(long millisUntilFinished) {
                storeRecentPlace();
            }

            public void onFinish() {
                start();
            }
        }.start();
    }

    public void moveCameraTo(Location location) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(),
                        location.getLongitude()), DEFAULT_ZOOM));
    }

    public void storeRecentPlace() {
        try {
            if (locationPermissionGranted) {
                final Task<Location>[] locationResult = new Task[]{fusedLocationClient.getLastLocation()};
                locationResult[0].addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // sets the most recent place
                            mostRecentPlace.setValue(lastKnownLocation);

                            // adds the most recent place to all places stored
                            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference placesRef = rootRef.child("places");
                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    // finds the last index of a stored location
                                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                        highestIndex = Integer.parseInt(ds.getKey());
                                    }
                                    // adds a a location
                                    places.child(String.valueOf(highestIndex+1)).setValue(lastKnownLocation);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            };
                            placesRef.addListenerForSingleValueEvent(eventListener);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            googleMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }

                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }







}