package io.luxurytech.gala;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {


    /** Firebase components */
    FirebaseFirestore db;
    FirebaseAuth auth;
    boolean isFirebaseSetup;


    /** The user */
    FirebaseUser authUser;
    String uid;

    /** UI components */
    Button exitButton, changeRecoveryEmailButton;
    EditText recoveryEmailEditText;

    /** Shared Prefs */
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

        // Setup UI
        exitButton = (Button) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
            }
        });
        changeRecoveryEmailButton = (Button) findViewById(R.id.changeRecoveryEmailButton);
        changeRecoveryEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRecoveryEmail();
            }
        });
        changeRecoveryEmailButton.setEnabled(false);
        recoveryEmailEditText = (EditText) findViewById(R.id.recoveryEmailEditText);
        recoveryEmailEditText.setText(sharedPref.getString("recoveryEmail", "hbn"));
        recoveryEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changeRecoveryEmailButton.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void changeRecoveryEmail() {
        // Setup firebase
        setupFirebase();


        // Check if there is a user
        if(authUser == null) {
            return;
        }

        // Get new value
        final String newRecoveryEmail = recoveryEmailEditText.getText().toString();

        // Change recovery email in firebase
        Map<String, Object> dbUser = new HashMap<>();
        dbUser.put("recoveryEmail", newRecoveryEmail);
        db.collection("Users")
                .document(uid)
                .update(dbUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Settings Activity", "Recovery Email added");
                        changeRecoveryEmailButton.setEnabled(false);
                        // Update shared pref
                        sharedPrefEditor.putString("recoveryEmail", newRecoveryEmail);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Settings Activity", "Error adding recovery email", e);
                    }
                });
    }

    /** Set up firebase (since it will not be used in every instance of SettingsActivity
     *  this will be called when it is necessary)
     */
    private void setupFirebase(){
        if(isFirebaseSetup)
            return;

        // Setup firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        authUser = auth.getCurrentUser();
        if(authUser != null) {
            uid = authUser.getUid();
        }

        isFirebaseSetup = true;
    }

}
