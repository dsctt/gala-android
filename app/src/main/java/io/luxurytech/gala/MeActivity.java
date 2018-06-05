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
//    FirebaseFirestore db;
//    FirebaseAuth auth;

    /** The user */
//    FirebaseUser authUser;
//    String uid;

    /** Number picker for age */
    NumberPicker ageNumberPicker;

    /** Male and female buttons */
    Button maleButton;
    Button femaleButton;
    int selectedGender;

    /** Context */
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        context = this;

        // Setup firebase
//        db = FirebaseFirestore.getInstance();
//        auth = FirebaseAuth.getInstance();
//        authUser = auth.getCurrentUser();
//        if(authUser != null) {
//            uid = authUser.getUid();
//        }

        // Setup UI components
        ageNumberPicker = (NumberPicker) findViewById(R.id.ageNumberPicker);
        maleButton = (Button) findViewById(R.id.maleButton);
        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = Constants.MALE;
                saveData();
            }
        });
        femaleButton = (Button) findViewById(R.id.femaleButton);
        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGender = Constants.FEMALE;
                saveData();
            }
        });

        ageNumberPicker.setMinValue(Constants.MIN_AGE);
        ageNumberPicker.setMaxValue(Constants.MAX_AGE);

        ageNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                //textview.setText("Selected Value is : " + newVal);
            }
        });

    }

    /** Called when a gender button is clicked. Adds gender and age to user in db. */
    public void saveData () {

        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(getString(R.string.userGender), selectedGender);
        editor.putInt(getString(R.string.userAge), ageNumberPicker.getValue());
        editor.apply();

        // Go to next screen
        startActivity(new Intent(MeActivity.this, ThemActivity.class));
        overridePendingTransition(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        finish();

//        // Check if there is a user
//        if(authUser == null) {
//            return;
//        }
//
//        // Add gender and age to user
//        Map<String, Object> dbUser = new HashMap<>();
//        dbUser.put("userGender", selectedGender);
//        dbUser.put("userAge", ageNumberPicker.getValue());
//        db.collection("Users")
//                .document(uid)
//                .update(dbUser)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d("FirestoreWrite", "Gender and Age added");
//                        startActivity(new Intent(MeActivity.this, ThemActivity.class));
//                        overridePendingTransition(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
//                        finish();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("FirestoreWrite", "Error adding user", e);
//                    }
//                });

    }

    @Override
    public void onBackPressed() { }
}
