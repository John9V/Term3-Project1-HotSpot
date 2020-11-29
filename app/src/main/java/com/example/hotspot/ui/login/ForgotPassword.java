package com.example.hotspot.ui.login;

/**
 * Activity for when you forget your password.
 *
 *  * Might have been https://medium.com/etiya/firebase-authentication-sample-371b5940ba93
 *  * This was the guide I used to write the login page.
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hotspot.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * These login/password pages follow the same pattern. The declare a bunch of navigation buttons
 * at the top of the onCreate method, then write listeners for each widget delcared.
 */
public class ForgotPassword extends AppCompatActivity {
    /**
     * Email address to send recovery link to.
     */
    EditText edtEmail;
    /**
     * Button to request the new password.
     */
    Button btnNewPass;
    /**
     * Reference to the firebase authentication.
     */
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        edtEmail = (EditText) findViewById(R.id.edit_email);
        btnNewPass = (Button) findViewById(R.id.yeniParolaGonder);

        firebaseAuth = FirebaseAuth.getInstance();

        //Listener for new password button.
        btnNewPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(),"Please fill e-mail",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                //Logic for sending a new password email to the user.
                firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            //Inner class
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),
                                            "Password reset link was sent your email address",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Mail sending error",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
    }
}