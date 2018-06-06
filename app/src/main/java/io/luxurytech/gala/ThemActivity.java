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
import android.widget.ImageButton;
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
    String userPhoneNumber;
    int initialUserClout = 1;

    /** Number pickers for age */
    NumberPicker minAgeNumberPicker;
    NumberPicker maxAgeNumberPicker;

    /** Buttons */
    ImageButton maleButton;
    ImageButton femaleButton;
    ImageButton saveButton;
    int selectedGender;

    /** Values from previous registration screens to save in db */
    String regRecoveryEmail;
    String regScreenName;
    int regUserGender;
    int regUserAge;

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
            userPhoneNumber = authUser.getPhoneNumber();
        }

        // Setup UI components
        minAgeNumberPicker = (NumberPicker) findViewById(R.id.minAgeNumberPicker);
        maxAgeNumberPicker = (NumberPicker) findViewById(R.id.maxAgeNumberPicker);
        minAgeNumberPicker.setMinValue(Constants.MIN_AGE);
        minAgeNumberPicker.setMaxValue(Constants.MAX_AGE);

        minAgeNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                // Update minimum max value
                maxAgeNumberPicker.setDisplayedValues(null);
                maxAgeNumberPicker.setMinValue(newVal);
            }
        });

        maxAgeNumberPicker.setMinValue(Constants.MIN_AGE);
        maxAgeNumberPicker.setMaxValue(Constants.MAX_AGE);

        maxAgeNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                // Update maximum min value
                minAgeNumberPicker.setDisplayedValues(null);
                minAgeNumberPicker.setMaxValue(newVal);

            }
        });

        maleButton = (ImageButton) findViewById(R.id.maleButton);
        maleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_male));
        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = Constants.MALE;
                setGenderButtonUI(true);
            }
        });
        femaleButton = (ImageButton) findViewById(R.id.femaleButton);
        femaleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_female));
        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = Constants.FEMALE;
                setGenderButtonUI(false);
            }
        });
        selectedGender = Constants.FEMALE; // Default
        setGenderButtonUI(false); // Default

        // Get values from sharedPrefs for use in saving to db
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        regRecoveryEmail = sharedPref.getString(getString(R.string.recoveryEmail), "");
        regScreenName = sharedPref.getString(getString(R.string.screenName), "");
        regUserGender = sharedPref.getInt(getString(R.string.userGender), Constants.MALE);
        regUserAge = sharedPref.getInt(getString(R.string.userAge), Constants.MIN_AGE);

        saveButton = (ImageButton) findViewById(R.id.saveButton);
        saveButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_forward_primary));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });


    }

    /** Called when a gender button is clicked. Adds all cached items to the db. */
    public void saveData () {

        // Check if there is a user
        if(authUser == null) {
            return;
        }

        // Save 'Them' values and clout and phone number to shared prefs
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.desiredGender), selectedGender);
        editor.putInt(getString(R.string.desiredMaxAge), maxAgeNumberPicker.getValue());
        editor.putInt(getString(R.string.desiredMinAge), minAgeNumberPicker.getValue());
        editor.putInt(getString(R.string.userClout), initialUserClout);
        editor.putString(getString(R.string.phoneNumber), userPhoneNumber);
        editor.apply();

        // Add appropriate values to db
        Map<String, Object> dbUser = new HashMap<>();
        dbUser.put(getString(R.string.recoveryEmail), regRecoveryEmail);
        dbUser.put(getString(R.string.screenName), regScreenName);
        dbUser.put(getString(R.string.userAge), regUserAge);
        dbUser.put(getString(R.string.userGender), regUserGender);
        dbUser.put(getString(R.string.userClout), initialUserClout);
        dbUser.put(getString(R.string.phoneNumber), userPhoneNumber);
        db.collection(getString(R.string.DB_COLLECTION_USERS))
                .document(uid)
                .set(dbUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("FirestoreWrite", "Sucesss");
                        //saveToSharedPrefs();
                        startActivity(new Intent(ThemActivity.this, HomeActivity.class));
                        overridePendingTransition(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FirestoreWrite", "Error", e);
                    }
                });

    }

    /** Checks if user info is already filled in and saves to sharedprefs */
//    private void saveToSharedPrefs() {
//        DocumentReference drUser;
//        drUser = db.collection(getString(R.string.DB_COLLECTION_USERS)).document(uid);
//
//        // Check if user already has full user information
//        // If so, save to shared prefs, and skip to home
//        drUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if(task.isSuccessful()){
//                    DocumentSnapshot doc = task.getResult();
//
//                    String recoveryEmail = doc.get(getString(R.string.recoveryEmail)).toString();
//                    String screenName = doc.get(getString(R.string.screenName)).toString();
//                    String userGender = doc.get(getString(R.string.userGender)).toString();
//                    String userAge = doc.get(getString(R.string.userAge)).toString();
//                    String phoneNumber = doc.get(getString(R.string.phoneNumber)).toString();
//
//
//                    if(recoveryEmail != null && recoveryEmail != "" &&
//                            screenName != null && screenName != "" &&
//                            userGender != null && userGender != "" &&
//                            userAge != null && userAge != "" &&
//                            phoneNumber != null && phoneNumber != ""){
//
//                        SharedPreferences sharedPref = context.getSharedPreferences(
//                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPref.edit();
//                        editor.putString(getString(R.string.desiredGender), desiredGender);
//                        editor.putString(getString(R.string.recoveryEmail), recoveryEmail);
//                        editor.putString(getString(R.string.screenName), screenName);
//                        editor.putString(getString(R.string.userGender), userGender);
//                        editor.putInt(getString(R.string.desiredMaxAge), Integer.parseInt(desiredMaxAge));
//                        editor.putInt(getString(R.string.desiredMinAge), Integer.parseInt(desiredMinAge));
//                        editor.putInt(getString(R.string.userAge), Integer.parseInt(userAge));
//                        editor.putInt(getString(R.string.userClout), initialUserClout);
//                        editor.putString(getString(R.string.phoneNumber), userPhoneNumber);
//                        editor.apply();
//
//                    }
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//            }
//        });
//
//    }

    private void setGenderButtonUI(boolean maleSelected) {
        if(maleSelected) {
            maleButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            femaleButton.setBackgroundColor(getResources().getColor(R.color.lightGray));
        } else {
            maleButton.setBackgroundColor(getResources().getColor(R.color.lightGray));
            femaleButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    @Override
    public void onBackPressed() { }
}
