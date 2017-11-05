package com.funnums.funnums.maingame;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;

interface OnGetDataListener {
    //this is for callbacks
    void onSuccess(DataSnapshot dataSnapshot);
    void onStart();
    void onFailure();
}

public class MainMenuActivity extends AppCompatActivity {
    private static final String TAG = "Main Menu";

    String logTag = "MainMenu";

    private boolean waiting = true;

    int connections = 0;

    static SharedPreferences prefs;
    // This is the entry point to our game
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Here we set our UI layout as the view
        setContentView(com.funnums.funnums.R.layout.activity_main_menu);

        //get the stored data on this phone
        prefs = getSharedPreferences("HighScore", MODE_PRIVATE);
        //get the editor so we can update stored data, if needed
        final SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user_name", null);
        editor.commit();

        final String userName = prefs.getString("user_name", null);

        final ProgressDialog progress = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        /*final OnGetDataListener listener = new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

                //got data from database....now you can use the retrieved data
                Log.d(TAG, userName + " already exists");
                waiting = false;

                makeAlert(this, userName + " already exists\nplease enter another username");

            }
            @Override
            public void onStart() {

                progress.setTitle("Loading");
                progress.setMessage("Checking username...");

                //disable dismiss by tapping outside of the dialog
                progress.setCancelable(false);
                progress.show();
                Log.d("ONSTART", "Started");
            }

            @Override
            public void onFailure() {
                waiting = false;
                Log.d(TAG, userName + " does not already exists");
            }
        };*/

        if(!isNetworkAvailable(this)) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("No network connection...\n Relaunch the app with network connection to enter a username so you can compete globally!")
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                }


                            })
                    .setCancelable(false)
                    .create();
            dialog.show();
        }

        //if there is no username, prompt player to enter one
        else if (userName == null) {
            makeAlert("Enter your username!");

        }

        // Prepare to highest Score. We don't need this yet, we can keep it for later when we implement scoring
        // Load fastest time
        // if not available our high score = 1000000
        long highScore = prefs.getLong("HighScore", 1000000);

        // Get a refference to the TextView in our layout
        final TextView textFastestTime = (TextView)findViewById(com.funnums.funnums.R.id.textHiScore);
        // Put the high score in our TextView
        textFastestTime.setText("Your High Score:" + highScore);

    }

    public void checkIfUserExists(/*final OnGetDataListener listener,*/ final String userName)
    {
        final ProgressDialog progress = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        progress.setTitle("Loading");
        progress.setMessage("Checking username...");

        //disable dismiss by tapping outside of the dialog
        progress.setCancelable(false);
        progress.show();
        Log.d("ONSTART", "Started");
        final SharedPreferences.Editor editor = prefs.edit();
        LeaderboardGameActivity.setEndpointToPlayerNames();
        LeaderboardGameActivity.playerScoreCloudEndPoint.child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // TODO: handle the case where the data already exists
                    //listener.onSuccess(snapshot);
                    makeAlert(userName + " already exists\nplease enter another username");
                    progress.dismiss();

                }
                else {
                    // TODO: handle empty strings
                    //listener.onFailure();
                    Log.d(TAG, userName + " does not already exists");
                    editor.putString("user_name", userName);
                    editor.commit();
                    progress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d(TAG, "no connection");
            }

        });

        //while(waiting){}
    }



    public void onPressedSelect(View v) {
        Log.d(TAG, "[SELECT GAME] pressed");
        // Create a new Intent object
        Intent i = new Intent(this, SelectGameActivity.class);
        // Start our GameActivity class via the Intent
        startActivity(i);
    }

    public void onPressedScores(View v) {
        Log.d(TAG, "[SEE LEADEBOARD] pressed");
        // Create a new Intent object
        Intent i = new Intent(this, LeaderboardGameActivity.class);
        // Start our GameActivity class via the Intent
        startActivity(i);
    }

    /*Empty for now*/
    public void onPressedSettings(View v) {
        Log.d(TAG, "[SETTINGS] pressed");
    }


    public static boolean isNetworkAvailable(Context con) {
        try {
            ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void makeAlert(/*final OnGetDataListener listener,*/ String message) {
        //edit text is the text field that the user will enter their name into
        final SharedPreferences.Editor editor = prefs.edit();
        EditText input = new EditText(this);
        input.setId(1000);
        //alert dialog is popup that asks for username, needs the following boiler plate code
        //to store username when it is entered
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(input).setTitle(message)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                EditText theInput = (EditText) ((AlertDialog) dialog)
                                        .findViewById(1000);
                                String enteredText = theInput.getText()
                                        .toString();
                                //if the player entered a name, store it so we don't ask again later
                                if (!enteredText.trim().equals("")) {
                                    checkIfUserExists(/*listener,*/ enteredText);
                                    /*editor.putString("user_name",
                                            enteredText);
                                    editor.commit();*/
                                }
                                else{
                                    makeAlert("Please enter a username");
                                }
                            }


                        })
                .setCancelable(false)
                .create();
        dialog.show();
    }

}