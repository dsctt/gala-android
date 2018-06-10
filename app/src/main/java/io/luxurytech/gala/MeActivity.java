package io.luxurytech.gala;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    /** Firebase components */
    FirebaseFirestore db;
    FirebaseAuth auth;

    /** The user */
    FirebaseUser authUser;
    String uid;
    String userPhoneNumber;
    int selectedDesiredGender; // desiredGender
    Date selectedBirthday;

    /** Numbers for age */
    Calendar farthestCal, closestCal;

    /** UI */
    ImageButton maleButton;
    ImageButton femaleButton;
    int selectedGender;
    AppCompatImageButton saveButton;
    EditText birthdayEditText;
    SimpleDateFormat simpleDateFormat;

    /** User manager */
    UserManager userManager;

    /** Values from previous registration screens to save in db */
    String regRecoveryEmail;
    String regScreenName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);


        // Set up user manager
        userManager = new UserManager(this);

        // Setup firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        authUser = auth.getCurrentUser();
        if(authUser != null) {
            uid = authUser.getUid();
            userPhoneNumber = authUser.getPhoneNumber();
        }

        // Setup dates
        farthestCal = Calendar.getInstance();
        farthestCal.add(Calendar.YEAR, (-1 * userManager.MAX_AGE));

        closestCal = Calendar.getInstance();
        closestCal.add(Calendar.YEAR, (-1 * userManager.MIN_AGE));

        // Setup UI components
        simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        final Date defaultBirthday = new GregorianCalendar(1995, 00, 01).getTime();
        selectedBirthday = defaultBirthday;
        String defaultBirthdayText = getEmojiByUnicode(Constants.BABY_UNICODE)
                + simpleDateFormat.format(defaultBirthday);
        birthdayEditText = (EditText) findViewById(R.id.birthdayEditText);
        birthdayEditText.setText(defaultBirthdayText);
        birthdayEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDate(1995, 00, 01, R.style.NumberPickerStyle);
            }
        });

        maleButton = (ImageButton) findViewById(R.id.maleButton);
        maleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_male));
        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = userManager.MALE;
                setGenderButtonUI(true);
            }
        });
        femaleButton = (ImageButton) findViewById(R.id.femaleButton);
        femaleButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_female));
        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = userManager.FEMALE;
                setGenderButtonUI(false);
            }
        });
        selectedGender = userManager.FEMALE; // Default
        selectedDesiredGender = userManager.MALE; // Default
        setGenderButtonUI(false); // Default

        saveButton = (AppCompatImageButton) findViewById(R.id.saveButton);
        saveButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_forward_primary));
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        // Get values from sharedPrefs for use in saving to db
        regRecoveryEmail = userManager.getRecoveryEmail();
        regScreenName = userManager.getScreenName();

    }

    /** Saves data to cache and db */
    public void saveData () {

        // Set desired gender
        if(selectedGender == userManager.FEMALE)
            selectedDesiredGender = userManager.MALE;
        else
            selectedDesiredGender = userManager.FEMALE;

        // Set desired age range

        int[] ageArray = userManager.getDefaultAgeRange(selectedBirthday);
        int minAgeFromUserAge = ageArray[0], maxAgeFromUserAge = ageArray[1];

        // Save 'Them' values and clout and phone number and isRegistered to shared prefs
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

        userManager.setDesiredGender(selectedDesiredGender);
        userManager.setDesiredMaxAge(maxAgeFromUserAge);
        userManager.setDesiredMinAge(minAgeFromUserAge);
        userManager.setUserClout(userManager.INITIAL_CLOUT);
        userManager.setUserPhoneNumber(userPhoneNumber);
        userManager.setUserGender(selectedGender);
        userManager.setUserBirthday(sdf.format(selectedBirthday));
        userManager.setUserIsRegistered(true);

        // Add appropriate values to db
        Map<String, Object> dbUser = new HashMap<>();
        dbUser.put(getString(R.string.recoveryEmail), regRecoveryEmail);
        dbUser.put(getString(R.string.screenName), regScreenName);
        dbUser.put(getString(R.string.userBirthday), sdf.format(selectedBirthday));
        dbUser.put(getString(R.string.userGender), selectedGender);
        dbUser.put(getString(R.string.userClout), userManager.INITIAL_CLOUT);
        dbUser.put(getString(R.string.phoneNumber), userPhoneNumber);
        dbUser.put(getString(R.string.userDesiredGender), selectedDesiredGender);
        dbUser.put(getString(R.string.userDesiredMinAge), minAgeFromUserAge);
        dbUser.put(getString(R.string.userDesiredMaxAge), maxAgeFromUserAge);
        dbUser.put(getString(R.string.userFlags), userManager.INITIAL_FLAGS);
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

    public void onDateSet(com.tsongkha.spinnerdatepicker.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        String temp = getEmojiByUnicode(Constants.BABY_UNICODE)
                + simpleDateFormat.format(calendar.getTime());
        birthdayEditText.setText(temp);
        selectedBirthday = calendar.getTime();
    }

    public void showDate(int year, int monthOfYear, int dayOfMonth, int spinnerTheme) {
        Calendar cal = Calendar.getInstance();
        new SpinnerDatePickerDialogBuilder()
                .context(this)
                .callback(this)
                .spinnerTheme(spinnerTheme)
                .defaultDate(year, monthOfYear, dayOfMonth)
                .minDate(farthestCal.get(Calendar.YEAR), farthestCal.get(Calendar.MONTH), farthestCal.get(Calendar.DATE))
                .maxDate(closestCal.get(Calendar.YEAR), closestCal.get(Calendar.MONTH), closestCal.get(Calendar.DATE))
                .showTitle(false)
                .build()
                .show();
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    @Override
    public void onBackPressed() { }
}
