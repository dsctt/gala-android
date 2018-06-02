package io.luxurytech.gala;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    FirebaseFirestore db;
    FirebaseAuth auth;


    /** The user */
    FirebaseUser authUser;
    String uid;

    /** Recovery Email Edittext */
    EditText recoveryEmailEditText;

    /** Save button */
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_email);

        // Setup firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        authUser = auth.getCurrentUser();
        if(authUser != null) {
            uid = authUser.getUid();
        }

        // Setup UI components
        recoveryEmailEditText = (EditText) findViewById(R.id.recoveryEmailEditText);
        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClicked();
            }
        });
    }

    /** Called when the SAVE button is clicked. Adds user to db with UID and recovery email */
    public void saveButtonClicked () {

        // Check if there is a user
        if(authUser == null) {
            return;
        }

        // Change recovery email in firebase
        Map<String, Object> dbUser = new HashMap<>();
        dbUser.put("recoveryEmail", recoveryEmailEditText.getText().toString());
        db.collection("Users")
                .document(uid)
                .set(dbUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("RecoveryEmail", "Recovery Email added");
                        goToNextScreen();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("RecoveryEmail", "Error adding recovery email", e);
                    }
                });

    }

    public void goToNextScreen(){
        startActivity(new Intent(RecoveryEmail.this, ScreenNameActivity.class));
        finish();
    }
}
