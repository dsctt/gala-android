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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MeActivity extends AppCompatActivity {

    /** Firebase components */
    FirebaseFirestore db;
    FirebaseAuth auth;

    /** The user */
    FirebaseUser authUser;
    String uid;
    String userPhoneNumber;
    int initialUserClout = 1;
    int selectedDesiredGender; // desiredGender

    /** Number picker for age */
    NumberPicker ageNumberPicker;

    /** UI */
    ImageButton maleButton;
    ImageButton femaleButton;
    int selectedGender;
    ImageButton saveButton;

    /** Context */
    Context context;

    /** Values from previous registration screens to save in db */
    String regRecoveryEmail;
    String regScreenName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
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
        ageNumberPicker = (NumberPicker) findViewById(R.id.ageNumberPicker);

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
        selectedDesiredGender = Constants.MALE; // Default
        setGenderButtonUI(false); // Default

        ageNumberPicker.setMinValue(Constants.MIN_AGE);
        ageNumberPicker.setMaxValue(Constants.MAX_AGE);

        ageNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                //textview.setText("Selected Value is : " + newVal);
            }
        });

        saveButton = (ImageButton) findViewById(R.id.saveButton);
        saveButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_forward_primary));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        // Get values from sharedPrefs for use in saving to db
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        regRecoveryEmail = sharedPref.getString(getString(R.string.recoveryEmail), "");
        regScreenName = sharedPref.getString(getString(R.string.screenName), "");

    }

    /** Called when a gender button is clicked. Adds gender and age to user in db. */
    public void saveData () {

        // Set desired gender
        if(selectedGender == Constants.FEMALE)
            selectedDesiredGender = Constants.MALE;
        else
            selectedDesiredGender = Constants.FEMALE;

        // Set desired age range
        int selectedAgeOfUser = ageNumberPicker.getValue();
        int minAgeFromUserAge, maxAgeFromUserAge;

        if(selectedAgeOfUser <= 27)
            minAgeFromUserAge = 18;
        else
            minAgeFromUserAge = selectedAgeOfUser - 10;

        if(selectedAgeOfUser >= 91)
            maxAgeFromUserAge = 100;
        else
            maxAgeFromUserAge = selectedAgeOfUser + 10;

        // Save 'Them' values and clout and phone number to shared prefs
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.desiredGender), selectedDesiredGender);
        editor.putInt(getString(R.string.desiredMaxAge), maxAgeFromUserAge);
        editor.putInt(getString(R.string.desiredMinAge), minAgeFromUserAge);
        editor.putInt(getString(R.string.userClout), initialUserClout);
        editor.putString(getString(R.string.phoneNumber), userPhoneNumber);
        editor.putInt(getString(R.string.userGender), selectedGender);
        editor.putInt(getString(R.string.userAge), selectedAgeOfUser);
        editor.apply();

        // Add appropriate values to db
        Map<String, Object> dbUser = new HashMap<>();
        dbUser.put(getString(R.string.recoveryEmail), regRecoveryEmail);
        dbUser.put(getString(R.string.screenName), regScreenName);
        dbUser.put(getString(R.string.userAge), selectedAgeOfUser);
        dbUser.put(getString(R.string.userGender), selectedGender);
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
                        startActivity(new Intent(MeActivity.this, HomeActivity.class));
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
