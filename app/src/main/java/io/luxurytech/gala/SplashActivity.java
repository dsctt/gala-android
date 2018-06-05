package io.luxurytech.gala;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    /** Firebase authentication */
    FirebaseAuth auth;

    /** Activity context */
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        // Initialize firebase
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            // already signed in - check if other fields are complete
            goHomeIfUserInfoComplete();
        } else {
            // If not, go to phone auth
            startActivity(new Intent(SplashActivity.this, PhoneNumberAuthentication.class));
            finish();
        }

    }

    /** Checks if user info is already filled in and then takes the user Home if true
     * Also updates SharedPreferences
     */
    private void goHomeIfUserInfoComplete() {
        DocumentReference drUser;
        FirebaseFirestore db;
        String uid = null;
        FirebaseUser authUser;

        db = FirebaseFirestore.getInstance();
        authUser = auth.getCurrentUser();
        if(authUser != null) {
            uid = authUser.getUid();
        }
        drUser = db.collection(getString(R.string.DB_COLLECTION_USERS)).document(uid);

        // Check if user already has full user information
        // If so, save to shared prefs, and skip to home
        drUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();


                    if(doc.get(getString(R.string.recoveryEmail))!= null && doc.get(getString(R.string.recoveryEmail)).toString() != "" &&
                            doc.get(getString(R.string.screenName)) != null && doc.get(getString(R.string.screenName)).toString() != "" &&
                            doc.get(getString(R.string.userGender)) != null && doc.get(getString(R.string.userGender)).toString() != "" &&
                            doc.get(getString(R.string.userAge)) != null && doc.get(getString(R.string.userAge)).toString() != "" &&
                            doc.get(getString(R.string.phoneNumber)) != null && doc.get(getString(R.string.phoneNumber)).toString() != "") {

                        String recoveryEmail = doc.get(getString(R.string.recoveryEmail)).toString();
                        String screenName = doc.get(getString(R.string.screenName)).toString();
                        String userGender = doc.get(getString(R.string.userGender)).toString();
                        String userAge = doc.get(getString(R.string.userAge)).toString();
                        String phoneNumber = doc.get(getString(R.string.phoneNumber)).toString();
                        int userClout = Integer.parseInt(doc.get(getString(R.string.userClout)).toString());

                        SharedPreferences sharedPref = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.recoveryEmail), recoveryEmail);
                        editor.putString(getString(R.string.screenName), screenName);
                        editor.putString(getString(R.string.userGender), userGender);
                        editor.putString(getString(R.string.phoneNumber), phoneNumber);
                        editor.putInt(getString(R.string.userAge), Integer.parseInt(userAge));
                        editor.putInt(getString(R.string.userClout), userClout);
                        editor.apply();

                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        // If not, go thru process
                        startActivity(new Intent(SplashActivity.this, RecoveryEmail.class));
                        finish();
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }
}
