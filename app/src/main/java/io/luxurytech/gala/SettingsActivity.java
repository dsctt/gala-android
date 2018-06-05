package io.luxurytech.gala;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

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
    boolean isFirebaseSetup = false;


    /** The user */
    FirebaseUser authUser;
    String uid;

    /** UI components */
    Button exitButton, changeRecoveryEmailButton, signOutButton;
    Button maleButton;
    Button femaleButton;
    EditText recoveryEmailEditText;
    TextView cloutTextView;

    /** Determines whether we need to save new information upon exit of Activity
     *  (not applicable to recoveryEmail)
     */
    boolean desiredMinAgeHasChanged = false, desiredMaxAgeHasChanged = false, desiredGenderHasChanged = false;
    int newDesiredMinAge, newDesiredMaxAge;
    int newDesiredGender;


    /** Shared Prefs */
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;

    /** Number pickers for age */
    NumberPicker minAgeNumberPicker;
    NumberPicker maxAgeNumberPicker;



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
                exitButtonClicked();
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
        changeRecoveryEmailButton.setTextColor(getResources().getColor(R.color.darkGray));

        recoveryEmailEditText = (EditText) findViewById(R.id.recoveryEmailEditText);
        recoveryEmailEditText.setText(sharedPref.getString(getString(R.string.recoveryEmail), ""));
        recoveryEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Check if valid email
                String currEmail = recoveryEmailEditText.getText().toString();
                if(!TextUtils.isEmpty(currEmail) && android.util.Patterns.EMAIL_ADDRESS.matcher(currEmail).matches()) {
                    changeRecoveryEmailButton.setEnabled(true);
                    changeRecoveryEmailButton.setTextColor(getResources().getColor(R.color.white));
                }

                else {
                    changeRecoveryEmailButton.setEnabled(false);
                    changeRecoveryEmailButton.setTextColor(getResources().getColor(R.color.darkGray));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        minAgeNumberPicker = (NumberPicker) findViewById(R.id.minAgeNumberPicker);
        minAgeNumberPicker.setMinValue(Constants.MIN_AGE);
        minAgeNumberPicker.setMaxValue(sharedPref.getInt(this.getString(R.string.desiredMaxAge), Constants.MAX_AGE));
        minAgeNumberPicker.setValue(sharedPref.getInt(this.getString(R.string.desiredMinAge), Constants.MIN_AGE));

        minAgeNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                // Update variables
                desiredMinAgeHasChanged = true;
                newDesiredMinAge = newVal;

                // Update minimum max value
                maxAgeNumberPicker.setDisplayedValues(null);
                maxAgeNumberPicker.setMinValue(newVal);
            }
        });


        maxAgeNumberPicker = (NumberPicker) findViewById(R.id.maxAgeNumberPicker);
        maxAgeNumberPicker.setMinValue(sharedPref.getInt(this.getString(R.string.desiredMinAge), Constants.MAX_AGE));
        maxAgeNumberPicker.setMaxValue(Constants.MAX_AGE);
        maxAgeNumberPicker.setValue(sharedPref.getInt(this.getString(R.string.desiredMaxAge), Constants.MAX_AGE));

        maxAgeNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                // Update variables
                desiredMaxAgeHasChanged = true;
                newDesiredMaxAge = newVal;

                // Update maximum min value
                minAgeNumberPicker.setDisplayedValues(null);
                minAgeNumberPicker.setMaxValue(newVal);

            }
        });

        maleButton = (Button) findViewById(R.id.maleButton);
        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desiredGenderHasChanged = true;
                newDesiredGender = Constants.MALE;
                setGenderButtonUI(true);
            }
        });
        femaleButton = (Button) findViewById(R.id.femaleButton);
        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desiredGenderHasChanged = true;
                newDesiredGender = Constants.FEMALE;
                setGenderButtonUI(false);
            }
        });
        if(sharedPref.getInt(getString(R.string.desiredGender), Constants.FEMALE) == Constants.MALE){
            setGenderButtonUI(true);
        }
        else {
            setGenderButtonUI(false);
        }

        signOutButton = (Button) findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert(getString(R.string.signOutMessage));
            }
        });

        cloutTextView = (TextView) findViewById(R.id.cloutTextView);
        cloutTextView.setText(getEmojiByUnicode(Constants.PURPLE_HEART_UNICODE) + sharedPref.getInt(getString(R.string.userClout), 1));

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
        dbUser.put(getString(R.string.recoveryEmail), newRecoveryEmail);
        db.collection("Users")
                .document(uid)
                .update(dbUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Settings Activity", "Recovery Email added");
                        changeRecoveryEmailButton.setEnabled(false);
                        changeRecoveryEmailButton.setTextColor(getResources().getColor(R.color.darkGray));
                        // Update shared pref
                        sharedPrefEditor.putString(getString(R.string.recoveryEmail), newRecoveryEmail);
                        sharedPrefEditor.apply();
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

    /** Updates Shared Prefs if necessary, before closing */
    private void exitButtonClicked(){


        // Update shared prefs

        if (desiredMinAgeHasChanged) {
            sharedPrefEditor.putInt(getString(R.string.desiredMinAge), newDesiredMinAge);
        }
        if (desiredMaxAgeHasChanged) {
            sharedPrefEditor.putInt(getString(R.string.desiredMaxAge), newDesiredMaxAge);
        }
        if (desiredGenderHasChanged) {
            sharedPrefEditor.putInt(getString(R.string.desiredGender), newDesiredGender);
        }

        sharedPrefEditor.apply();
        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /** Sets UI for gender buttons based on which is selected */
    private void setGenderButtonUI(boolean maleButtonIsChosen) {
        if(maleButtonIsChosen) {
            maleButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            femaleButton.setTextColor(getResources().getColor(R.color.white));
        }
        else {
            maleButton.setTextColor(getResources().getColor(R.color.white));
            femaleButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        }

    }

    /** Gets emoji from unicode value */
    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    /** Pops up an alert with message */
    private void showAlert(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);


        // Add the message
        builder.setMessage(message);
        // Add the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setupFirebase();
                auth.signOut();
                startActivity(new Intent(SettingsActivity.this, PhoneNumberAuthentication.class));
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        // Make sure everything gets saved
        exitButtonClicked();
    }

}
