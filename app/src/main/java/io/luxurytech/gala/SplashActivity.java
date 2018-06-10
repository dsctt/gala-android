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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

public class SplashActivity extends AppCompatActivity {

    /** Firebase */
    FirebaseAuth auth;

    /** User Manager */
    UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);

        // Set up user manager
        userManager = new UserManager(this);

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
            if(userManager.isRegistered())
                goHome();
            else
                goToRecoveryEmail();
        } else {
            // If not, go to phone auth
            goToPhoneAuth();
        }
    }

    /** Check if connected to the internet
     * If yes, begins sign in flow
     */
    private void checkForInternet() {
        if(!isOnline()){

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Add the message
            builder.setTitle("Uh oh!");
            builder.setMessage(getString(R.string.internetMessage));
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

    /** Send to home screen */
    private void goHome() {
        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /** Send to recovery email screen */
    private void goToRecoveryEmail() {
        startActivity(new Intent(SplashActivity.this, RecoveryEmail.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /** Send to phone auth screen */
    private void goToPhoneAuth() {
        startActivity(new Intent(SplashActivity.this, PhoneNumberAuthentication.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
