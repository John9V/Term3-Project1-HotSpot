package com.example.hotspot.ui.home;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import com.example.hotspot.data.Risk;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Used to grant permissions for the app to access the device's location.
     */
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    /**
     * Sets the default zoom. Ten is good for a city.
     */
    private static final int DEFAULT_ZOOM = 10;
    /**
     * Provides logic to the home wigit for animating the view, as in when the map moves from point
     * A to point B.
     */
    private HomeViewModel homeViewModel;
    /**
     * The google map object.
     */
    private GoogleMap googleMap;
    /**
     * Location permission starts as false. This must be changed to true to access the device's
     * location.
     */
    private boolean locationPermissionGranted = false;
    /**
     * Used for getting the device's location.
     */
    private FusedLocationProviderClient fusedLocationClient;
    /**
     * The previously recorded location for the device.
     */
    private Location lastKnownLocation;
    /**
     * The default coordinate set, which is Vancouver.
     */
    private LatLng defaultLocation;
    /**
     * Gets an instance of Firebase.
     */
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    /**
     * Instance of the firebase database.
     */
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    /**
     * MyRef is another reference to the database.
     */
    private DatabaseReference myRef;
    /**
     * Will be used to store a reference to the places section of the firebase database (the part
     * that remembers where a user has been).
     */
    private DatabaseReference placesRef;
    /**
     * A reference to the current user.
     */
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    /**
     * A reference to the user's identifier.
     */
    private String userID = user.getUid();
    /**
     * A reference to the user's most recent place.
     */
    /*I guess it's safe to declare these in instance variables, should have been consistent about
    this
     */
    private DatabaseReference mostRecentPlace = database.getReference(userID)
            .child("mostRecentPlace");
    /**
     * Another reference to the places the user has been.
     */
    private DatabaseReference places = database.getReference(userID).child("places");
    /**
     * An arraylist that holds risks.
     */
    public static ArrayList<Risk> risks = new ArrayList<>();
    /**
     * The geocoder object which provides logic for finding the distance between two points.
     */
    private Geocoder geo;
    /**
     * Counter for periodically storing the device's location to the database.
     */
    CountDownTimer countDownTimer;

    /**
     * Called just after the activity is launched.
     * @param inflater Used to link an xml file to the View objects defined here. Mostly used in
     *                 ListViews and RecyclerViews - a ListView in our case.
     * @param container An invisible container that other views and layouts are placed in. Subclass
     *                  of View which specializes in holding groups of Views, like a ListView which
     *                  contains many text views.
     * @param savedInstanceState Subclass far down the polymorphic tree of IBinder, a technology
     *                           which allows objects to be passed between processes.
     * @return The basic UI component for the home fragment.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final MapView mapView = (MapView) root.findViewById(R.id.mapView);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        getLocationPermission();
        periodicallyStoreLocation(); // causes a duplicate store of the location
        final Context thiscontext = container.getContext();
        geo = new Geocoder(thiscontext, Locale.CANADA);

        myRef = mFirebaseDatabase.getReference(userID);
        placesRef = mFirebaseDatabase.getReference("masterSheet");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            //When the database updates
            //Take a snapshot of the user's locations.
            /*
            Enter callback hell. How to avoid this?
             */
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<LatLng> userLocations = getUserLocations(dataSnapshot);
                placesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    //Take a snapshot of the outbreaks.
                    public void onDataChange(DataSnapshot outbreakSnapshot) {
                        ArrayList<LatLng> outbreakLocs = getOutbreakLocations(outbreakSnapshot);
                        //Find the distances between the user's locations and outbreak locations.
                        ArrayList<Double> distances = getDistances(userLocations, outbreakLocs);
                        if(googleMap != null) {
                            //Add the user's locations to the google map, in red.
                            for(LatLng coord : userLocations) {
                                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                        .position(coord).title("User Location"));
                            }
                            //Add the outbreak locations to the google map, in green.
                            for(LatLng coord : outbreakLocs) {
                                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                        .position(coord).title("Outbreak Location"));
                            }
                        }
                        //Popup which shows list of risks to the user.
                        /*
                        Should have made this bigger, text is cut off
                         */
                        Snackbar snackbar = Snackbar
                                .make(mapView, "Exposure risk when you were at "
                                        + risks.get(0).getUserAdd() + " near "
                                        + risks.get(0).getOutbreakAdd(), Snackbar.LENGTH_INDEFINITE)
                                .setAction(("OK"), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) { }
                                });
                        snackbar.show();
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
        //The UI element we just created with all that code above ^
        return root;
    }

    /**
     * Gets the distances between the user and the outbreak locations using a double for loop.
     * @param userLocs the user's locations
     * @param outbreakLocs the outbreak locations.
     * @return an array list of doubles representing locations between each user location and
     * outbreak location.
     */
    private ArrayList<Double> getDistances(ArrayList<LatLng> userLocs,
                                           ArrayList<LatLng> outbreakLocs) {
        ArrayList<Double> distances = new ArrayList<Double>();
        /*
        How to improve? Maps maybe?
         */
        for(int i = 0; i < userLocs.size(); i++) {
            for(int j = 0; j < outbreakLocs.size(); j++) {
                float[] results = new float[1];
                double lat1 = userLocs.get(i).latitude;
                double long1 = userLocs.get(i).longitude;
                double lat2 = outbreakLocs.get(j).latitude;
                double long2 = outbreakLocs.get(j).longitude;
                Location.distanceBetween(lat1, long1, lat2, long2, results);
                distances.add((double) results[0]);
                if(results[0] < 6000) {
                    List<Address> addressList = new ArrayList<>();
                    List<Address> addressList2 = new ArrayList<>();
                    try {
                        addressList = geo.getFromLocation(lat1, long1, 1);
                        addressList2 = geo.getFromLocation(lat2, long2, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Checks risks list for duplicates.
                    if (!risks.contains(new Risk(addressList.get(0).getAddressLine(0),
                            addressList2.get(0).getAddressLine(0)))) {
                        risks.add(new Risk(addressList.get(0).getAddressLine(0),
                                addressList2.get(0).getAddressLine(0)));
                    }
                }
            }
        }
        return distances;
    }

    /**
     * Gets outbreak information from the database.
     * @param dataSnapshot
     * @return
     */
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

    /**
     * Gets user location history from the database.
     * @param dataSnapshot snapshot of user locations from the database.
     * @return list of user locations as coordinates.
     */
    /*
    Should have created location objects of our own that overrode Google's location objects.
    Then we could have added a sense of time more easily.
     */
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

    /*
    Callback interface for when the map is ready to be used, sets the coordinate and a marker
    in the middle of Vancouver.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(googleMap != null) {
            this.googleMap = googleMap;
            LatLng vancouver = new LatLng(49.2827, -123.1207);
            googleMap.addMarker(new MarkerOptions().position(vancouver).title("Vancouver"));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(vancouver, DEFAULT_ZOOM));
        }
    }

    /**
     * Must have copied this from Google documentation.
     */
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {

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

    /**
     * Callback for the result of requesting permissions.
     * @param requestCode Made up code.
     * @param permissions permissions list.
     * @param grantResults unknown.
     */
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
        //Update UI if permissions are granted.
        updateLocationUI();
    }

    /**
     * Also must have been copied from google documentation, probably this one
     * https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial
     */
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

    /**
     * Method for getting the device's last known location and moving the camera there.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                final Task<Location>[] locationResult = new Task[]{fusedLocationClient
                        .getLastLocation()};
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

    /**
     * Store's the device's location periodically.
     */
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

    /**
     * Moves the camera to a different location.
     * @param location the location to move the camera to.
     */
    public void moveCameraTo(Location location) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(),
                        location.getLongitude()), DEFAULT_ZOOM));
    }

    /**
     * Stores the most recent place.
     */
    public void storeRecentPlace() {
        try {
            if (locationPermissionGranted) {
                final Task<Location>[] locationResult = new Task[]{fusedLocationClient
                        .getLastLocation()};
                locationResult[0].addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            //Sets the most recent place
                            mostRecentPlace.setValue(lastKnownLocation);
                            //Adds the most recent place to all places stored
                            DatabaseReference rootRef = FirebaseDatabase.getInstance()
                                    .getReference();
                            final DatabaseReference placesRef = rootRef.child("places");
                            ValueEventListener eventListener = new ValueEventListener() {
                                /*Inner class makes it impossible to access instance variables in
                                this scope*/
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //Adds a location to the recent locations
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