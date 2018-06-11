package io.luxurytech.gala;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.appyvet.materialrangebar.RangeBar;
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
    Button exitButton, signOutButton;
    AppCompatImageButton changeRecoveryEmailButton;
    AppCompatImageButton maleButton;
    AppCompatImageButton femaleButton;
    EditText recoveryEmailEditText;
    TextView cloutTextView;

    /** Determines whether we need to save new information upon exit of Activity
     *  (not applicable to recoveryEmail)
     */
    boolean desiredAgeHasChanged = false, desiredGenderHasChanged = false;
    int newDesiredMinAge, newDesiredMaxAge;
    int newDesiredGender;

    /** Range bar for age */
    RangeBar ageRangeBar;

    /** User manager */
    UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        // Set up user manager
        userManager = new UserManager(this);

        // Setup UI
        exitButton = (Button) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitButtonClicked();
            }
        });

        changeRecoveryEmailButton = (AppCompatImageButton) findViewById(R.id.changeRecoveryEmailButton);
        changeRecoveryEmailButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_gray));
        changeRecoveryEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRecoveryEmail();
            }
        });
        setChangeRecoveryEmailButtonUI(false);

        recoveryEmailEditText = (EditText) findViewById(R.id.recoveryEmailEditText);
        recoveryEmailEditText.setText(userManager.getRecoveryEmail());
        recoveryEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Check if valid email
                String currEmail = recoveryEmailEditText.getText().toString();
                if(!TextUtils.isEmpty(currEmail) && android.util.Patterns.EMAIL_ADDRESS.matcher(currEmail).matches()) {
                    setChangeRecoveryEmailButtonUI(true);
                }

                else {
                    setChangeRecoveryEmailButtonUI(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ageRangeBar = (RangeBar) findViewById(R.id.ageRangeBar);
        ageRangeBar.setTickStart(userManager.MIN_AGE);
        ageRangeBar.setTickEnd(55);
        ageRangeBar.setRangePinsByValue(userManager.getDesiredMinAge(), userManager.getDesiredMaxAge());
        ageRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                // Update variables
                desiredAgeHasChanged = true;
                newDesiredMinAge = Integer.parseInt(leftPinValue);
                newDesiredMaxAge = Integer.parseInt(rightPinValue);
            }
        });

        maleButton = (AppCompatImageButton) findViewById(R.id.maleButton);
        maleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_male));
        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desiredGenderHasChanged = true;
                newDesiredGender = userManager.MALE;
                setGenderButtonUI(true);
            }
        });
        femaleButton = (AppCompatImageButton) findViewById(R.id.femaleButton);
        femaleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_female));
        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desiredGenderHasChanged = true;
                newDesiredGender = userManager.FEMALE;
                setGenderButtonUI(false);
            }
        });
        if(userManager.getDesiredGender() == userManager.MALE){
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
        cloutTextView.setText(getEmojiByUnicode(Constants.PURPLE_HEART_UNICODE) + userManager.getUserClout());

        ageRangeBar.setRangePinsByValue(userManager.getDesiredMinAge(),
                userManager.getDesiredMaxAge());
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
                        setChangeRecoveryEmailButtonUI(false);
                        // Update shared pref
                        userManager.setRecoveryEmail(newRecoveryEmail);
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
        if (desiredAgeHasChanged) {
            userManager.setDesiredMinAge(newDesiredMinAge);
            userManager.setDesiredMaxAge(newDesiredMaxAge);
        }
        if (desiredGenderHasChanged) {
            userManager.setDesiredGender(newDesiredGender);
        }

        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    /** Sets UI for gender buttons based on which is selected */
    private void setGenderButtonUI(boolean maleSelected) {
        if(maleSelected) {
            maleButton.setBackgroundResource(R.color.colorPrimary);
            femaleButton.setBackgroundResource(R.color.lightGray);
        } else {
            maleButton.setBackgroundResource(R.color.lightGray);
            femaleButton.setBackgroundResource(R.color.colorPrimary);
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

    /** Sets UI of save button */
    private void setChangeRecoveryEmailButtonUI(boolean en) {
        if(en) {
            changeRecoveryEmailButton.setEnabled(true);
            changeRecoveryEmailButton.setImageResource(R.drawable.ic_check_primary);
        } else {

            changeRecoveryEmailButton.setEnabled(false);
            changeRecoveryEmailButton.setImageResource(R.drawable.ic_check_gray);
        }
    }


}
