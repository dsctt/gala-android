package io.luxurytech.gala;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ScreenNameActivity extends AppCompatActivity {

    /** Firebase components */
      FirebaseFirestore db;
//    FirebaseAuth auth;

    /** The user */
//    FirebaseUser authUser;
//    String uid;

    /** Screen name */
    EditText screenNameEditText;
    TextView existsTextView;

    /** Save button */
    AppCompatImageButton saveButton;

    /** Context */
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_name);
        context = this;

        // Setup firebase
          db = FirebaseFirestore.getInstance();
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
        saveButton = (AppCompatImageButton) findViewById(R.id.saveButton);
        setSaveButtonUI(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClicked();
            }
        });

        existsTextView = (TextView) findViewById(R.id.existsTextView);
        existsTextView.setVisibility(View.INVISIBLE);
    }

    /** Check if screen name already exists */
    private void saveButtonClicked() {

        String selectedScreenName = screenNameEditText.getText().toString().toLowerCase();


        db.collection(getString(R.string.DB_COLLECTION_USERS))
                .whereEqualTo(getString(R.string.screenName), selectedScreenName)
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
    /** Called when the screen name is confirmed valid */
    public void saveToSharedPrefs () {

        String selectedScreenName = screenNameEditText.getText().toString().toLowerCase();

        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(getString(R.string.screenName), selectedScreenName);
        editor.apply();

        // Go to next screen
        startActivity(new Intent(ScreenNameActivity.this, MeActivity.class));
        overridePendingTransition(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        finish();

    }

    /** Sets UI of save button */
    private void setSaveButtonUI(boolean en) {
        if(en) {
            saveButton.setEnabled(true);
            saveButton.setImageResource(R.drawable.baseline_arrow_forward_primary);
        } else {

            saveButton.setEnabled(false);
            saveButton.setImageResource(R.drawable.baseline_arrow_forward_gray);
        }
    }


    @Override
    public void onBackPressed() { }

}
