package com.example.hotspot.ui.settings;

/**
 * View for user actions. Unsure why we called it "Notifications?"
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.hotspot.R;
import com.example.hotspot.ui.login.LoginActivity;
import com.example.hotspot.ui.login.NewUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NotificationsFragment extends Fragment {
    /**
     * Seems to be used for error printing.
     */
    private static final String TAG = "MainActivity";

    private TextView textView;
    /**
     * Button to delete the curerntly logged in user.
     */
    private Button btnDeleteUser;
    /**
     * Logs the user out.
     */
    private Button btnLogout;
    /**
     * Shows the remembered data for this user.
     */
    private Button btnViewDatabase;
    /**
     * Unsure what FirebaseAuth is for.
     */
    private FirebaseAuth firebaseAuth;
    /**
     * Listener for the auth state.
     */
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Called every time the user activities screen is opened.
     * @param inflater inflates xml with these views.
     * @param container
     * @param savedInstanceState paddable data between threads.
     * @return ui element for the user activities screen.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        textView = (TextView) view.findViewById(R.id.textView1);
        btnDeleteUser =(Button) view.findViewById(R.id.kullaniciSil);
        btnLogout =(Button) view.findViewById(R.id.cikis_yap);
        btnViewDatabase = (Button) view.findViewById(R.id.view_items_screen);

        firebaseAuth = FirebaseAuth.getInstance();

        /**
         * Displays the current user's email address since that is their public identifier.
         */
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null){
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    //Don't remember why I commented this out, unsure what it does?
                    //finish();
                }
            }
        };

        final FirebaseUser user  = firebaseAuth.getCurrentUser();
        //Displays the current user
        textView.setText("You are logged in as " + user.getEmail());


         //Listener for the user deletion button. Deletes the current user.
        btnDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Make sure the user is not null.
                if(user!=null){
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(getActivity(),"User deleted",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getActivity(), NewUser.class));
                                        //Were these breaking our app?
                                        //finish();
                                    }
                                }
                            });
                }
            }
        });

        //Logs out the current user.
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(getActivity(),LoginActivity.class));
                /*I vaguely remember commenting these out, don't remember why or if whatever I
                was trying worked */
               //finish();
            }
        });

        //Button that sends the user to view a list of their previous locations.
        btnViewDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewDatabase.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(authStateListener!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}