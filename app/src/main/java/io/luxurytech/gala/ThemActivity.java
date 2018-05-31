package io.luxurytech.gala;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ThemActivity extends AppCompatActivity {

    /** Firebase components */
    FirebaseFirestore db;
    FirebaseAuth auth;

    /** The user */
    FirebaseUser authUser;
    String uid;

    /** Number pickers for age */
    NumberPicker minAgeNumberPicker;
    NumberPicker maxAgeNumberPicker;

    /** Male and female buttons */
    Button maleButton;
    Button femaleButton;
    String selectedGender;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them);
        context = this;

        // Setup firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        authUser = auth.getCurrentUser();
        if(authUser != null) {
            uid = authUser.getUid();
        }

        // Setup UI components
        minAgeNumberPicker = (NumberPicker) findViewById(R.id.minAgeNumberPicker);
        maxAgeNumberPicker = (NumberPicker) findViewById(R.id.maxAgeNumberPicker);
        maleButton = (Button) findViewById(R.id.maleButton);
        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = "male";
                saveData();
            }
        });
        femaleButton = (Button) findViewById(R.id.femaleButton);
        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = "female";
                saveData();
            }
        });

        minAgeNumberPicker.setMinValue(13);
        minAgeNumberPicker.setMaxValue(100);

        minAgeNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                //textview.setText("Selected Value is : " + newVal);
            }
        });

        maxAgeNumberPicker.setMinValue(13);
        maxAgeNumberPicker.setMaxValue(100);

        maxAgeNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                //textview.setText("Selected Value is : " + newVal);
            }
        });

    }

    /** Called when a gender button is clicked. Adds desired gender and age range to user in db. */
    public void saveData () {

        // Check if there is a user
        if(authUser == null) {
            return;
        }

        // Add desired gender and age range to user
        Map<String, Object> dbUser = new HashMap<>();
        dbUser.put("desiredGender", selectedGender);
        dbUser.put("desiredMinAge", minAgeNumberPicker.getValue());
        dbUser.put("desiredMaxAge", maxAgeNumberPicker.getValue());
        db.collection("Users")
                .document(uid)
                .update(dbUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("FirestoreWrite", "Gender and Age added");
                        saveToSharedPrefs();
                        startActivity(new Intent(ThemActivity.this, HomeActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FirestoreWrite", "Error adding user", e);
                    }
                });

    }

    /** Checks if user info is already filled in and saves to sharedprefs */
    private void saveToSharedPrefs() {
        DocumentReference drUser;
        drUser = db.collection("Users").document(uid);

        // Check if user already has full user information
        // If so, save to shared prefs, and skip to home
        drUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();

                    String desiredGender = doc.get("desiredGender").toString();
                    String recoveryEmail = doc.get("recoveryEmail").toString();
                    String screenName = doc.get("screenName").toString();
                    String userGender = doc.get("userGender").toString();
                    String desiredMaxAge = doc.get("desiredMaxAge").toString();
                    String desiredMinAge = doc.get("desiredMinAge").toString();
                    String userAge = doc.get("userAge").toString();


                    if(desiredGender != null && desiredGender != "" &&
                            recoveryEmail != null && recoveryEmail != "" &&
                            screenName != null && screenName != "" &&
                            userGender != null && userGender != "" &&
                            desiredMaxAge != null && desiredMaxAge != "" &&
                            desiredMinAge != null && desiredMinAge != "" &&
                            userAge != null && userAge != ""){

                        SharedPreferences sharedPref = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("desiredGender", desiredGender);
                        editor.putString("recoveryEmail", recoveryEmail);
                        editor.putString("screenName", screenName);
                        editor.putString("userGender", userGender);
                        editor.putInt("desiredMaxAge", Integer.parseInt(desiredMaxAge));
                        editor.putInt("desiredMinAge", Integer.parseInt(desiredMinAge));
                        editor.putInt("userAge", Integer.parseInt(userAge));
                        editor.commit();

                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }
}
