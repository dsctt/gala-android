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

public class RecoveryEmail extends AppCompatActivity {

    /** Firebase components */
//    FirebaseFirestore db;
//    FirebaseAuth auth;


    /** The user */
//    FirebaseUser authUser;
//    String uid;

    /** Recovery Email Edittext */
    EditText recoveryEmailEditText;

    /** Save button */
    Button saveButton;

    /** Activity Context */
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_email);
        context = this;
        // Setup firebase
//        db = FirebaseFirestore.getInstance();
//        auth = FirebaseAuth.getInstance();
//        authUser = auth.getCurrentUser();
//        if(authUser != null) {
//            uid = authUser.getUid();
//        }

        // Setup UI components
        recoveryEmailEditText = (EditText) findViewById(R.id.recoveryEmailEditText);
        recoveryEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Check if valid email
                String currEmail = recoveryEmailEditText.getText().toString();
                if(!TextUtils.isEmpty(currEmail) && android.util.Patterns.EMAIL_ADDRESS.matcher(currEmail).matches()) {
                    saveButton.setEnabled(true);
                }

                else {
                    saveButton.setEnabled(false);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClicked();
            }
        });
    }

    /** Called when the SAVE button is clicked. Adds email to cache */
    public void saveButtonClicked () {

        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(getString(R.string.recoveryEmail), recoveryEmailEditText.getText().toString());
        editor.apply();

        // Go to next screen
        startActivity(new Intent(RecoveryEmail.this, ScreenNameActivity.class));
        overridePendingTransition(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        finish();



        // Check if there is a user
//        if(authUser == null) {
//            return;
//        }
//
//        // Change recovery email in firebase
//        Map<String, Object> dbUser = new HashMap<>();
//        dbUser.put("recoveryEmail", recoveryEmailEditText.getText().toString());
//        db.collection("Users")
//                .document(uid)
//                .set(dbUser)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d("RecoveryEmail", "Recovery Email added");
//                        goToNextScreen();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("RecoveryEmail", "Error adding recovery email", e);
//                    }
//                });

    }

    @Override
    public void onBackPressed() { }
}
