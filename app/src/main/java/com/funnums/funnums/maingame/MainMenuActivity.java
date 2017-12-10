package com.funnums.funnums.maingame;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import android.content.Context;

import 	android.os.Message;


import com.funnums.funnums.classes.PlayerScore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.MutableData;



public class MainMenuActivity extends AppCompatActivity {
    private static final String TAG = "Main Menu";

    String logTag = "MainMenu";

    //shared prefrences for our app, basically stored data that persists after app is closed
    static SharedPreferences prefs;
    Context context;
    //flag indicating if the username is being reset, false if it is the first time the user enters their name
    boolean resetting;
    // This is the entry point to our game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //context used for creating alerts
        context = this;
        //used to indicate if username is being reset
        resetting = false;
        //Here we set our UI layout as the view
        setContentView(com.funnums.funnums.R.layout.activity_main_menu);

        //get the stored data on this phone
        prefs = getSharedPreferences("HighScore", MODE_PRIVATE);
        //get the editor so we can update stored data, if needed
        final SharedPreferences.Editor editor = prefs.edit();
        //uncomment to test adding new usernames
        /*editor.putString("user_name", null);
        editor.commit();*/

        String userName = prefs.getString("user_name", null);

        //if there is no username, prompt user to create one
        if (userName == null){
            makeAlertWithConfirmedConnection("Enter your username!", resetting);
        }
    }

    /*
        Check if a username has already been claimed
     */
    public void checkIfUserExists(final String userName)
    {
        //prepare loading bar
        final ProgressDialog progress = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
        progress.setTitle("Loading");
        progress.setMessage("Checking username...");

        //disable dismiss by tapping outside of the dialog
        progress.setCancelable(true);
        progress.show();

        //get reference to editor so we can store the username in our app so we don't ask for
        //username everytime player opens funnums
        final SharedPreferences.Editor editor = prefs.edit();
        //set table to playerNames so we can search if chosen username already exists
        LeaderboardGameActivity.setEndpointToPlayerNames();
        //listen for response from firebase
        LeaderboardGameActivity.playerScoreCloudEndPoint.child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //if given key already exists, reprompt user for new name
                    makeAlertWithConfirmedConnection(userName + " already exists\nplease enter another username", resetting);
                    //dismiss progress bar
                    progress.dismiss();
                }
                else {
                    //if name is being reset, update the scoreboards
                    if(resetting) {
                        removeOldName();
                        String oldName = prefs.getString("user_name", "");
                        updateAllGames(oldName, userName);
                        displayNewName(userName);
                    }
                    //store unique user name on app
                    editor.putString("user_name", userName);
                    editor.commit();
                    //store username in Firebase
                    LeaderboardGameActivity.setEndpointToPlayerNames();
                    PlayerScore newPlayer = new PlayerScore(userName, 0);
                    LeaderboardGameActivity.playerScoreCloudEndPoint.child(userName).setValue(newPlayer);
                    //dismiss progress bar
                    progress.dismiss();

                }
            }
            //boilerplate
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d(TAG, "no connection");
            }

        });
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

    public void onPressedSettings(View v) {
        Log.d(TAG, "[SETTINGS] pressed");
        Intent i = new Intent(this, SettingsActivity.class);
        // Start our GameActivity class via the Intent
        startActivity(i);
    }


    /*
        Make alert prompting user for username. Can be given different strings for message, so we can inform
        user if name already exists, or just ask for username on first alert
     */
    public void makeAlert(String message) {
        //edit text is the text field that the user will enter their name into

        final SharedPreferences.Editor editor = prefs.edit();
        EditText input = new EditText(context);
        input.setId(1000);
        //alert dialog is popup that asks for username, needs the following boiler plate code
        //to store username when it is entered
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(input).setTitle("FunNums").setMessage(message)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                EditText theInput = (EditText) ((AlertDialog) dialog)
                                        .findViewById(1000);
                                String enteredText = theInput.getText()
                                        .toString();
                                //if the player entered a name, check if name already exists in database
                                if (!enteredText.trim().equals("")) {
                                    checkIfUserExists(enteredText);
                                }
                                //if user entered nothing, remprompt for username
                                else{
                                    makeAlertWithConfirmedConnection("Please enter a username", resetting);
                                }
                            }


                        })
                .setCancelable(true)
                .create();
        dialog.show();
    }

    /*
        Creates an alert to enter username once connection to firebase has been established so we can check
        if a username is already taken
     */
    public void makeAlertWithConfirmedConnection(final String message, final boolean resetingName) {

        //get reference to database to check connection
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                //check if user entered username yet, since we might reconnect while entering name
                final String userName;
                //always set name to null if resetting the name, so user is reprompted if they entered an existing username
                if(resetingName)
                    userName = null;
                else
                    userName = prefs.getString("user_name", null);
                //if connected and still no username, prompt user for username
                if (connected && userName == null) {
                    makeAlert(message);
                }
                //else, do not prompt for name, since we cannot check if it is unique
                else {
                    System.out.println("not connected to firebase");

                }
            }

            //boilerplate
            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
    }

    /*
        set the context to be used when making alert dialogs, so that these methods can be called in
        the Settings menu as well as from teh main menu
     */
    public void setContext(Context c){
        context = c;
        resetting = true;
    }

    /*
        remove the oldname from the list of usernames on Firebase
     */
    public void removeOldName(){
        final String oldName = prefs.getString("user_name", "");
        LeaderboardGameActivity.setEndpointToPlayerNames();
        LeaderboardGameActivity.playerScoreCloudEndPoint.child(oldName).removeValue();
    }

    /*
        updates the names on every scoreboard, replacing the old username with the new one
     */
    public void  updateAllGames(final String oldName, final String newName){
        updateScoreNames("bubble",oldName, newName);
        updateScoreNames("balloon",oldName, newName);
        updateScoreNames("owl",oldName, newName);
    }
    /*
        update names on the score board for a given game, removing the old name and replacing it with the new username
     */
    public void updateScoreNames(final String game, final String oldName, final String newName){
        LeaderboardGameActivity.mDatabase.child(game+"Scores").child(oldName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //if given key already exists, reprompt user for new name
                    PlayerScore score = snapshot.getValue(PlayerScore.class);
                    score.name = newName;
                    LeaderboardGameActivity.mDatabase.child(game+"Scores").child(newName).setValue(score);

                    LeaderboardGameActivity.mDatabase.child(game+"Scores").child(oldName).removeValue();
                }
            }
            //boilerplate
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d(TAG, "no connection");
            }

        });
    }
    /*
        called after all names are updaetd on leaderboards to dispaly the new username in the
        Settings menu
     */
    public void displayNewName(String userName){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_name", userName);
        editor.commit();

        SettingsActivity s = (SettingsActivity) context;
        s.setUserNameString();
    }

    //couldn't get this thing to work, apparently its not necessary anyway
    /*public void transaction(final String oldName, final String newName){
        LeaderboardGameActivity.mDatabase.child("playerScores").child(oldName).runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {


                        Log.d("WHAT", "Try toget key");
                        Log.d("WHAT", mutableData+"");
                        Log.d("WHAT",  mutableData.getKey());

                        MutableData oldData = mutableData.child("playerScores").child(oldName);
                        oldData.setValue(null);

                        MutableData newData = mutableData.child("playerScores").child(newName);
                        newData.setValue(new PlayerScore(newName, 0));


                        String[] games = {"bubble", "balloon", "owl"};

                        PlayerScore score1 = mutableData.child("bubbleScores").child(oldName).getValue(PlayerScore.class);
                        score1.name = newName;
                        MutableData bubbleScore = mutableData.child("bubbleScores").child(newName);
                        bubbleScore.setValue(score1);

                        MutableData oldBubble = mutableData.child("bubble"+"Scores").child(oldName);
                        oldBubble.setValue(null);

                        PlayerScore score2 = mutableData.child("balloonScores").child(oldName).getValue(PlayerScore.class);
                        score2.name = newName;
                        MutableData balloonScore = mutableData.child("balloonScores").child(newName);
                        balloonScore.setValue(score2);

                        MutableData oldBalloon = mutableData.child("balloon"+"Scores").child(oldName);
                        oldBalloon.setValue(null);

                        PlayerScore score3 = mutableData.child("owlScores").child(oldName).getValue(PlayerScore.class);
                        score3.name = newName;
                        MutableData owlScore = mutableData.child("owlScores").child(newName);
                        owlScore.setValue(score3);

                        MutableData oldOwl = mutableData.child("owl"+"Scores").child(oldName);
                        oldOwl.setValue(null);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                        if(databaseError != null)
                            Log.d("error", databaseError.getDetails());
                        PlayerScore newScore = (PlayerScore)dataSnapshot.child("playerScores").child(newName).getValue();
                        Log.d("COMPLETE", b + " " + newScore);
                        displayNewName(newName);
                    }
                });
    }*/

}