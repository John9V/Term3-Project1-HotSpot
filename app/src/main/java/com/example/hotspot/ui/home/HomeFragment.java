package com.example.hotspot.ui.home;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 10;
    private HomeViewModel homeViewModel;
    private GoogleMap googleMap;
    private boolean locationPermissionGranted = false;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;
    private LatLng defaultLocation;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference myRef;
    private DatabaseReference placesRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userID = user.getUid();
    private DatabaseReference mostRecentPlace = database.getReference(userID).child("mostRecentPlace");
    private DatabaseReference places = database.getReference(userID).child("places");
    private ArrayList<Pair<String, String>> risks = new ArrayList<Pair<String, String>>();
//    private static GeoApiContext context = new GeoApiContext.Builder().apiKey("AIzaSyBZKhlW5rencQBYPaDXRsYqkFrKeyNxnlw").build();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        MapView mapView = (MapView) root.findViewById(R.id.mapView);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        getLocationPermission();
        periodicallyStoreLocation(); // causes a duplicate store of the location

        myRef = mFirebaseDatabase.getReference(userID);
        placesRef = mFirebaseDatabase.getReference("masterSheet");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<LatLng> userLocations = getUserLocations(dataSnapshot);
                placesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot outbreakSnapshot) {
                        ArrayList<LatLng> outbreakLocs = getOutbreakLocations(outbreakSnapshot);
                        ArrayList<Double> distances = getDistances(userLocations, outbreakLocs);
                        if(googleMap != null) {

                            for(LatLng coord : userLocations) {
                                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(coord).title("User Location"));
                            }

                            for(LatLng coord : outbreakLocs) {
                                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(coord).title("Outbreak Location"));
                            }
                        }
                        System.out.println(distances.size() + " " + distances);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {}});
        System.out.println("risks: " + risks);
        return root;
    }

    private ArrayList<Double> getDistances(ArrayList<LatLng> userLocs, ArrayList<LatLng> outbreakLocs) {
        ArrayList<Double> distances = new ArrayList<Double>();
        System.out.println("userlocs size: " + userLocs.size());
        System.out.println("outbreakLocs size: " + outbreakLocs.size());
        int counter = 0;
        for(int i = 0; i < userLocs.size(); i++) {
            for(int j = 0; j < outbreakLocs.size(); j++) {
                float[] results = new float[1];
                double lat1 = userLocs.get(i).latitude;
                double long1 = userLocs.get(i).longitude;
                double lat2 = outbreakLocs.get(j).latitude;
                double long2 = outbreakLocs.get(j).longitude;
                Location.distanceBetween(lat1, long1, lat2, long2, results);
                distances.add(Double.valueOf(results[0]));
                if(results[0] < 6000) {
                    Geocoder geo = new Geocoder(getActivity(), Locale.CANADA);
                    List<Address> addressList = new ArrayList<>();
                    List<Address> addressList2 = new ArrayList<>();
                    try {
                        addressList = geo.getFromLocation(lat1, long1, 1);
                        addressList2 = geo.getFromLocation(lat2, long2, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    risks.add(new Pair<>(addressList.get(0).getAddressLine(0), addressList2.get(0).getAddressLine(0)));
                    Toast mToastToShow = Toast.makeText(getActivity(), "Exposure risk when you were at " + addressList.get(0).getAddressLine(0) + " near "
                            + addressList2.get(0).getAddressLine(0), Toast.LENGTH_LONG);
                    mToastToShow.show();
                }
            }
        }
        return distances;
    }

    private ArrayList<LatLng> getOutbreakLocations(DataSnapshot dataSnapshot) {
        ArrayList<LatLng> array  = new ArrayList<>();

        for(DataSnapshot place : dataSnapshot.getChildren()) {
            double lat = (double) place.child("1").getValue();
            double lng = (double) place.child("2").getValue();
            LatLng newLatLng = new LatLng(lat, lng);
            array.add(newLatLng);
        }
        return array;
    }

    private ArrayList<LatLng> getUserLocations(DataSnapshot dataSnapshot) {
        ArrayList<LatLng> array  = new ArrayList<>();
        for(DataSnapshot place : dataSnapshot.child("places").getChildren()) {
            HashMap location =(HashMap) place.getValue();
            double lat = (double) location.get("latitude");
            double lng = (double) location.get("longitude");
            LatLng newLatLng = new LatLng(lat, lng);
            array.add(newLatLng);
        }
        return array;
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
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000 * 60 * 60) {

            // This is called after every given interval.
            public void onTick(long millisUntilFinished) {
                getDeviceLocation();
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
                            final DatabaseReference placesRef = rootRef.child("places");
                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // adds a location to the recent locations
                                    String id = places.push().getKey();
                                    places.child(id).setValue(lastKnownLocation);
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