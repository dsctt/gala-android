package io.luxurytech.gala;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

public class UserManager {

    public final static int MIN_AGE = 18;
    public final static int MAX_AGE = 55;
    public final static int FEMALE = 0;
    public final static int MALE = 1;
    public final static int INITIAL_FLAGS = 0;
    public final static int INITIAL_CLOUT = 1;

    private Context context;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    /** Constructor */
    public UserManager(Context ct){
        context = ct;

        sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    /** Returns user's desired minimum age */
    public int getDesiredMinAge() {
        return sharedPref.getInt(context.getString(R.string.desiredMinAge), MIN_AGE);
    }

    /** Sets user's desired minimum age */
    public void setDesiredMinAge(int age) {
        editor.putInt(context.getString(R.string.desiredMinAge), age);
        editor.apply();
    }



    /** Returns user's desired maximum age */
    public int getDesiredMaxAge() {
        return sharedPref.getInt(context.getString(R.string.desiredMaxAge), MAX_AGE);
    }


    /** Sets user's desired maximum age */
    public void setDesiredMaxAge(int age) {
        editor.putInt(context.getString(R.string.desiredMaxAge), age);
        editor.apply();
    }


    /** Returns user's desired gender */
    public int getDesiredGender() {
        return sharedPref.getInt(context.getString(R.string.desiredGender), FEMALE);
    }

    /** Sets user's desired gender */
    public void setDesiredGender(int gender) {
        editor.putInt(context.getString(R.string.desiredGender), gender);
        editor.apply();
    }



    /** Returns user's recovery email */
    public String getRecoveryEmail() {
        return sharedPref.getString(context.getString(R.string.recoveryEmail), "");
    }

    /** Sets user's desired recovery email */
    public void setRecoveryEmail(String email) {
        editor.putString(context.getString(R.string.recoveryEmail), email);
        editor.apply();
    }



    /** Returns user's clout */
    public int getUserClout() {
        return sharedPref.getInt(context.getString(R.string.userClout), INITIAL_CLOUT);
    }

    /** Sets user's clout */
    public void setUserClout(int clout) {
        editor.putInt(context.getString(R.string.userClout), clout);
        editor.apply();
    }



    /** Returns user's screen name */
    public String getScreenName() {
        return sharedPref.getString(context.getString(R.string.screenName), "");
    }

    /** Sets user's desired screen name */
    public void setScreenName(String name) {
        editor.putString(context.getString(R.string.screenName), name);
        editor.apply();
    }



    /** Returns user's gender */
    public int getUserGender() {
        return sharedPref.getInt(context.getString(R.string.userGender), FEMALE);
    }

    /** Sets user's clout */
    public void setUserGender(int gender) {
        editor.putInt(context.getString(R.string.userGender), gender);
        editor.apply();
    }



    /** Returns user's birthday */
    public String getUserBirthday() {
        return sharedPref.getString(context.getString(R.string.userBirthday), "");
    }

    /** Sets user's birthday */
    public void setUserBirthday(String birthday) {
        editor.putString(context.getString(R.string.userBirthday), birthday);
        editor.apply();
    }



    /** Returns user's phone number */
    public String getUserPhoneNumber() {
        return sharedPref.getString(context.getString(R.string.phoneNumber), "");
    }

    /** Sets user's desired screen name */
    public void setUserPhoneNumber(String number) {
        editor.putString(context.getString(R.string.phoneNumber), number);
        editor.apply();
    }



    /** Returns user's flag count */
    public int getUserFlags() {
        return sharedPref.getInt(context.getString(R.string.userFlags), INITIAL_FLAGS);
    }

    /** Sets user's clout */
    public void setUserFlags(int flags) {
        editor.putInt(context.getString(R.string.userFlags), flags);
        editor.apply();
    }

    /** Sets user's clout */
    public void setUserIsRegistered(boolean registered) {
        editor.putBoolean(context.getString(R.string.userIsRegistered), registered);
        editor.commit();
    }

    /** Returns whether the user is registered or not.
     * Checks Shared Prefs first.
     * If false, double checks with db.
     * Updates Shared Prefs if necessary.
     */
    boolean test = false;
    public boolean isRegistered() {
        // Check shared prefs first
        if(sharedPref.getBoolean(context.getString(R.string.userIsRegistered), false))
            return true;

        // Check the db
        DocumentReference drUser;
        FirebaseFirestore db;
        String uid = null;
        FirebaseUser authUser;
        FirebaseAuth auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        authUser = auth.getCurrentUser();
        if(authUser != null) {
            uid = authUser.getUid();
        }
        drUser = db.collection(context.getString(R.string.DB_COLLECTION_USERS)).document(uid);
        drUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()) {
                        // Cache that the user is registered
                        setUserIsRegistered(true);
                        test = true;

                    } else {
                        // Cache that the user is not registered
                        setUserIsRegistered(false);
                        test = true;
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("FFAILLLL!!!!!!!!!!");
                test = true;
            }
        });

        //while(!test) {System.out.print("lil");}

        return sharedPref.getBoolean(context.getString(R.string.userIsRegistered), false);
    }

    /** Returns a default age range for a user based on their age
     * ageArray[0] = desiredMinAge
     * ageArray[1] = desiredMaxAge
     */
    public int[] getDefaultAgeRange(Date userBirthday) {
        int[] ageArray = new int[2];
        Calendar currCal = Calendar.getInstance();
        Calendar birthdayCal = Calendar.getInstance();
        birthdayCal.setTime(userBirthday);
        int selectedAgeOfUser = currCal.get(Calendar.YEAR) - birthdayCal.get(Calendar.YEAR);
        if(birthdayCal.get(Calendar.MONTH) > currCal.get(Calendar.MONTH) ||
                (birthdayCal.get(Calendar.MONTH) == currCal.get(Calendar.MONTH)
                        && birthdayCal.get(Calendar.DATE) > currCal.get(Calendar.DATE))) {
            selectedAgeOfUser--;
        }


        if(selectedAgeOfUser <= 27)
            ageArray[0] = 18;
        else
            ageArray[0] = selectedAgeOfUser - 10;

        if(selectedAgeOfUser >= 91)
            ageArray[1] = 100;
        else
            ageArray[1] = selectedAgeOfUser + 10;

        return ageArray;
    }

}
