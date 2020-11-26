package com.example.hotspot.ui.settings;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotspot.R;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewDatabase extends AppCompatActivity {
    private static final String TAG = "ViewDatabase";

    TextView textViewTitle;

//    Add Firebase stuff
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference myRef;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String userID;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_risks);

        mListView = (ListView) findViewById(R.id.listview);

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be usable.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        myRef = mFirebaseDatabase.getReference(userID);

        textViewTitle =  findViewById(R.id.textViewTitle);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    toastMessage("Successfully signed in with: " + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessage("Successfully signed out.");
                }
            }
        };

        // this doesn't apparently break it either
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                 This method is called once with the initial value and again whenever data at this location is updated.
//                 showData is breaking the app
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
    } // onCreate ending

    // this method is breaking the app
    private void showData(DataSnapshot dataSnapshot) {

       for(DataSnapshot place : dataSnapshot.child("places").getChildren()){

           HashMap location =(HashMap) place.getValue();
            UserInformation uInfo = new UserInformation();
            uInfo.setAccuracy((long)location.get("accuracy"));
            uInfo.setComplete((boolean)location.get("complete"));

            ArrayList<Object> array  = new ArrayList<>();

            array.add(uInfo.getAccuracy());
            array.add(uInfo.isComplete());

            ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,array);
            mListView.setAdapter(adapter);
        }
    } // showData ending

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}
