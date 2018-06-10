package io.luxurytech.gala;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RecoveryEmail extends AppCompatActivity {

    /** Firebase components */
      FirebaseFirestore db;
//    FirebaseAuth auth;


    /** The user */
//    FirebaseUser authUser;
//    String uid;

    /** Recovery Email Edittext */
    EditText recoveryEmailEditText;
    TextView existsTextView;

    /** Buttons */
    AppCompatImageButton saveButton;
    Button recoverAccountButton;

    /** Activity Context */
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_email);
        context = this;
        // Setup firebase
          db = FirebaseFirestore.getInstance();
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
                    setSaveButtonUI(true);
                    setRecoverAccountButtonUI(true);
                }

                else {
                    setSaveButtonUI(false);
                    setRecoverAccountButtonUI(false);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        saveButton = (AppCompatImageButton) findViewById(R.id.saveButton);
        setSaveButtonUI(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClicked();
            }
        });

        recoverAccountButton = (Button) findViewById(R.id.recoverAccountButton);
        setRecoverAccountButtonUI(false);
        recoverAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Lower keyboard
                View view = getCurrentFocus();
                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                Snackbar.make(findViewById(R.id.recoveryEmailLayoutID), "Sent!",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        });

        existsTextView = (TextView) findViewById(R.id.existsTextView);
        existsTextView.setVisibility(View.INVISIBLE);

    }

    /** Checks if this is already in the db */
    public void saveButtonClicked() {

        String selectedEmail = recoveryEmailEditText.getText().toString().toLowerCase();


        db.collection(getString(R.string.DB_COLLECTION_USERS))
                .whereEqualTo(getString(R.string.recoveryEmail), selectedEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot qSnap = task.getResult();
                            if (!qSnap.isEmpty()) {
                                // Need to input new screen name
                                existsTextView.setVisibility(View.VISIBLE);
                            } else {
                                // Save and continue
                                saveToSharedPrefs();
                            }
                        }
                    }
                });
    }

    /** Called when the SAVE button is clicked. Adds email to cache */
    public void saveToSharedPrefs () {

        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(getString(R.string.recoveryEmail), recoveryEmailEditText.getText().toString().toLowerCase());
        editor.apply();

        // Go to next screen
        startActivity(new Intent(RecoveryEmail.this, ScreenNameActivity.class));
        overridePendingTransition(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        finish();

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

    /** Sets UI of recover button */
    private void setRecoverAccountButtonUI(boolean en) {
        if(en) {
            recoverAccountButton.setEnabled(true);
            recoverAccountButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            recoverAccountButton.setEnabled(false);
            recoverAccountButton.setTextColor(getResources().getColor(R.color.lightGray));
        }
    }

    @Override
    public void onBackPressed() { }
}
