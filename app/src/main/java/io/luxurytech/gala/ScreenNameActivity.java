package io.luxurytech.gala;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ScreenNameActivity extends AppCompatActivity {

    /** Firebase components */
//    FirebaseFirestore db;
//    FirebaseAuth auth;

    /** The user */
//    FirebaseUser authUser;
//    String uid;

    /** Screen name Edittext */
    EditText screenNameEditText;

    /** Save button */
    ImageButton saveButton;

    /** Context */
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_name);
        context = this;

        // Setup firebase
//        db = FirebaseFirestore.getInstance();
//        auth = FirebaseAuth.getInstance();
//        authUser = auth.getCurrentUser();
//        if(authUser != null) {
//            uid = authUser.getUid();
//        }

        // Setup UI components
        screenNameEditText = (EditText) findViewById(R.id.screenNameEditText);
        screenNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currEmail = screenNameEditText.getText().toString();
                if(!TextUtils.isEmpty(currEmail)) {
                    setSaveButtonUI(true);
                }

                else {
                    setSaveButtonUI(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        saveButton = (ImageButton) findViewById(R.id.saveButton);
        setSaveButtonUI(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClicked();
            }
        });
    }

    /** Called when the SAVE button is clicked. Adds user to db with UID and recovery email */
    public void saveButtonClicked () {

        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(getString(R.string.screenName), screenNameEditText.getText().toString());
        editor.apply();

        // Go to next screen
        startActivity(new Intent(ScreenNameActivity.this, MeActivity.class));
        overridePendingTransition(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        finish();

//        // Check if there is a user
//        if(authUser == null) {
//            return;
//        }
//
//        // Add screen name value to the db
//        Map<String, Object> dbUser = new HashMap<>();
//        dbUser.put("screenName", screenNameEditText.getText().toString());
//        db.collection("Users")
//                .document(uid)
//                .update(dbUser)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d("FirestoreWrite", "Screen name added");
//                        startActivity(new Intent(ScreenNameActivity.this, MeActivity.class));
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

    /** Sets UI of save button */
    private void setSaveButtonUI(boolean en) {
        if(en) {
            saveButton.setEnabled(true);
            saveButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_forward_primary));
        } else {

            saveButton.setEnabled(false);
            saveButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_arrow_forward_gray));
        }
    }

    @Override
    public void onBackPressed() { }

}
