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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/** Verifies if user is already signed in with phone. If not, let's them sign up. */
public class PhoneNumberAuthentication extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;

    /** Firebase authentication */
    FirebaseAuth auth;

    /** Activity context */
    Context context;

    /** User manager */
    UserManager userManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up user manager
        userManager = new UserManager(this);

        // Initialize firebase
        auth = FirebaseAuth.getInstance();

        // TODO: If-statement technically not necessary anymore because it will always be false if this class is called.
        if (auth.getCurrentUser() != null) {
            // already signed in - check if other fields are complete
            if(userManager.isRegistered())
                goHome();
            else
                goToRecoveryEmail();

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
                if(userManager.isRegistered())
                    goHome();
                else
                    goToRecoveryEmail();
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

    /** Send to home screen */
    private void goHome() {
        // Need to make sure everything is cached
//        DocumentReference drUser;
//        FirebaseFirestore db;
//        String uid = null;
//        FirebaseUser authUser;
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//
//        db = FirebaseFirestore.getInstance();
//        authUser = auth.getCurrentUser();
//        if(authUser != null) {
//            uid = authUser.getUid();
//        }
//        drUser = db.collection(context.getString(R.string.DB_COLLECTION_USERS)).document(uid);
//        drUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if(task.isSuccessful()){
//                    DocumentSnapshot doc = task.getResult();
//
//                    userManager.setRecoveryEmail(doc.get(getString(R.string.recoveryEmail)).toString());
//                    userManager.setUserBirthday(doc.get(getString(R.string.userBirthday)).toString());
//                    userManager.setUserGender(
//                            Integer.parseInt(doc.get(getString(R.string.userGender)).toString()));
//                    userManager.setUserPhoneNumber(doc.get(getString(R.string.userPhoneNumber)).toString());
//                    userManager.setScreenName(doc.get(getString(R.string.screenName)).toString());
//                    userManager.setUserClout(
//                            Integer.parseInt(doc.get(getString(R.string.userClout)).toString()));
//                    userManager.setUserFlags(
//                            Integer.parseInt(doc.get(getString(R.string.userFlags)).toString()));
//                    userManager.setDesiredMinAge(
//                            Integer.parseInt(doc.get(getString(R.string.userDesiredMinAge)).toString()));
//                    userManager.setDesiredMaxAge(
//                            Integer.parseInt(doc.get(getString(R.string.userDesiredMaxAge)).toString()));
//                    userManager.setDesiredGender(
//                            Integer.parseInt(doc.get(getString(R.string.userDesiredGender)).toString()));
//                }
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//            }
//        });


        startActivity(new Intent(PhoneNumberAuthentication.this, HomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /** Send to recovery email screen */
    private void goToRecoveryEmail() {
        startActivity(new Intent(PhoneNumberAuthentication.this, RecoveryEmail.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() { }

}
