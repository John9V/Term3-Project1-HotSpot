package com.example.hotspot.ui.login;

/**
 * Definitely draws very heavy inspiration from
 * https://medium.com/etiya/firebase-authentication-sample-371b5940ba93
 *
 * New user page logic.
 */

import androidx.annotation.NonNull;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.hotspot.MainActivity;
import com.example.hotspot.R;

public class NewUser extends AppCompatActivity {
    /**
     * Textbok for user to input email.
     */
    EditText edtEmail;
    /**
     * Textbok for user to input password.
     */
    EditText edtPassword;
    /**
     * Button to register instead.
     */
    Button registerButton;
    /**
     * Button to login.
     */
    Button loginButton;
    /**
     * Button which logs the user in.
     */
    FirebaseAuth firebaseAuth;

    /**
     * Called as the activity launches.
     * @param savedInstanceState bundle of data that can be sent across threads.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_user);

        //Declaring all the widgets.
        edtEmail = (EditText) findViewById(R.id.edit_email);
        edtPassword = (EditText) findViewById(R.id.edit_password);
        registerButton = (Button) findViewById(R.id.btn_register);
        loginButton = (Button) findViewById(R.id.btn_login);

        firebaseAuth = FirebaseAuth.getInstance();

        /**
         * On Click listener for the register button.
         */
        registerButton.setOnClickListener(new View.OnClickListener() {
            //Inner class, be mindful of threading
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();

                //Do some validation on input.
                //Email can't be empty.
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields"
                            ,Toast.LENGTH_SHORT).show();
                    return;
                }
                //Password can't be null.
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(),"Please fill in the required fields",
                            Toast.LENGTH_SHORT).show();
                }
                //PW has to be longer than 6 chars.
                if(password.length() < 6 ){
                    Toast.makeText(getApplicationContext(),"Password must be at least 6 " +
                                    "characters", Toast.LENGTH_SHORT).show();
                }

                /**
                 * Logic creating a new user. Goes to the main activity.
                 */
                firebaseAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    startActivity(new Intent(getApplicationContext(),
                                            MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(),"E-mail or "
                                            + "password "
                                            + "is wrong", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        /**
         * Defines the logic for the login button, when clicked. Goes to the main activity.
         */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
    }
}