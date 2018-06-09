package io.luxurytech.gala;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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

import java.io.IOException;

public class SplashActivity extends AppCompatActivity {

    /** Firebase authentication */
    FirebaseAuth auth;

    /** Activity context */
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        // Check if connected to the internet
        checkForInternet();

    }

    /** Handle user sign in */
    private void handleUserSignIn() {
        // Initialize firebase
        auth = FirebaseAuth.getInstance();

        // Check if user is already signed in
        if (auth.getCurrentUser() != null) {
            // already signed in - check if other fields are complete
            goHomeIfUserInfoComplete();
        } else {
            // If not, go to phone auth
            startActivity(new Intent(SplashActivity.this, PhoneNumberAuthentication.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    /** Check if connected to the internet */
    private void checkForInternet() {
        if(!isOnline()){

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Add the message
            builder.setTitle("Uh oh!");
            builder.setMessage("Please make sure you are connected to the internet.");
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    checkForInternet();
                }
            });

            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            handleUserSignIn();
        }
    }

    /** Check if the user can connect online */
    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    /** Checks the server to see if Lockout
     * This could be:
     * Inactive Hours
     * Banned
     * Old version number
     */
    private void checkServerForLockout() {

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

                    if(doc != null) {
//                    if(doc.get(getString(R.string.recoveryEmail))!= null && doc.get(getString(R.string.recoveryEmail)).toString() != "" &&
//                            doc.get(getString(R.string.screenName)) != null && doc.get(getString(R.string.screenName)).toString() != "" &&
//                            doc.get(getString(R.string.userGender)) != null && doc.get(getString(R.string.userGender)).toString() != "" &&
//                            doc.get(getString(R.string.userBirthday)) != null && doc.get(getString(R.string.userBirthday)).toString() != "" &&
//                            doc.get(getString(R.string.phoneNumber)) != null && doc.get(getString(R.string.phoneNumber)).toString() != "") {
//
//                        String recoveryEmail = doc.get(getString(R.string.recoveryEmail)).toString();
//                        String screenName = doc.get(getString(R.string.screenName)).toString();
//                        String userGender = doc.get(getString(R.string.userGender)).toString();
//                        String userBirthday = doc.get(getString(R.string.userBirthday)).toString();
//                        String phoneNumber = doc.get(getString(R.string.phoneNumber)).toString();
//                        int userClout = Integer.parseInt(doc.get(getString(R.string.userClout)).toString());
//
//                        SharedPreferences sharedPref = context.getSharedPreferences(
//                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPref.edit();
//                        editor.putString(getString(R.string.recoveryEmail), recoveryEmail);
//                        editor.putString(getString(R.string.screenName), screenName);
//                        editor.putString(getString(R.string.userGender), userGender);
//                        editor.putString(getString(R.string.phoneNumber), phoneNumber);
//                        editor.putString(getString(R.string.userBirthday), userBirthday);
//                        editor.putInt(getString(R.string.userClout), userClout);
//                        editor.apply();

                        // checkForLockout

                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        // If not, go thru process
                        startActivity(new Intent(SplashActivity.this, RecoveryEmail.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
