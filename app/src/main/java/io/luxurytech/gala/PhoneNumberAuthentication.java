package io.luxurytech.gala;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

/** Verifies if user is already signed in with phone. If not, let's them sign up. */
public class PhoneNumberAuthentication extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;

    /** Firebase authentication */
    FirebaseAuth auth;

    /** Activity context */
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        // Initialize firebase
        auth = FirebaseAuth.getInstance();

        // TODO: If-statement technically not necessary anymore because it will always be false if this class is called.
        if (auth.getCurrentUser() != null) {
            // already signed in - check if other fields are complete
            goHomeIfUserInfoComplete();

        } else {
            // not signed in
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()
                                    ))
                            .build(),
                    RC_SIGN_IN);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                // Check if they've previously completed the sign in process
                goHomeIfUserInfoComplete();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.e("Login","Login canceled by User");
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.e("Login","No Internet Connection");
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.e("Login","Unknown Error");
                    return;
                }
            }
            Log.e("Login","Unknown sign in response");
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

                        startActivity(new Intent(PhoneNumberAuthentication.this, HomeActivity.class));
                        finish();
                    } else {
                        // If not, go thru process
                        startActivity(new Intent(PhoneNumberAuthentication.this, RecoveryEmail.class));
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

    @Override
    public void onBackPressed() { }

    // TODO: Make a method for starting activity intent
}
