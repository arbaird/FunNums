package com.funnums.funnums.maingame;

//import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import java.util.ArrayList;

import com.funnums.funnums.classes.PlayerScore;
import com.funnums.funnums.uihelpers.ScoreListAdapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ChildEventListener;



public class MainMenuActivity extends AppCompatActivity {
    private static final String TAG = "Main Menu";


    //reference to firebase database
    private DatabaseReference mDatabase;

    //reference to the playerScore table in our database
    private DatabaseReference playerScoreCloudEndPoint;

    String logTag = "MainMenue";

    private ArrayList<PlayerScore> playerScoreList;
    private ScoreListAdapter playerScoreAdapter;

    // This is the entry point to our game
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Here we set our UI layout as the view
        setContentView(com.funnums.funnums.R.layout.activity_main_menu);




        //get the stored data on this phone
        final SharedPreferences prefs = getSharedPreferences("HighScore", MODE_PRIVATE);
        //get the editor so we can update stored data, if needed
        final SharedPreferences.Editor editor = prefs.edit();
        String userName = prefs.getString("user_name", null);
        //if there is no username, prompt player to enter one
        if (userName == null) {
            //edit text is the text field that the user will enter their name into
            EditText input = new EditText(this);
            input.setId(1000);
            //alert dialog is popup that asks for username, needs the following boiler plate code
            //to store username when it is entered
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(input).setTitle("Enter your username!")
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
                                    if (!enteredText.equals("")) {
                                        editor.putString("user_name",
                                                enteredText);
                                        editor.commit();
                                    }
                                }


                            })
                            .setCancelable(false)
                            .create();
            dialog.show();



        }

        // Prepare to highest Score fo rthis user. We don't need this yet, we can keep it for later when we implement scoring

        // Prepare to highest Score. We don't need this yet, we can keep it for later when we implement scoring

        // Load fastest time
        // if not available our high score = 1000000
        long highScore = prefs.getLong("HighScore", 1000000);

        // Get a refference to the TextView in our layout
        final TextView textFastestTime = (TextView)findViewById(com.funnums.funnums.R.id.textHiScore);
        // Put the high score in our TextView
        textFastestTime.setText("Your High Score:" + highScore);

        //initalize the leader board list view and adapter
        initLeaderBoardListView();

        //this code was used to hardcode some user scores into the database to make sure it works.
        //might need it later to debug if we want to add more features to our leaderboard
        /*PlayerScore myScore = new PlayerScore("jimmy", 3000);
        myScore.name = "smitty";
        myScore.hiScore = 1000;*/
        // first, we push an empty value so can get a key to that location,
        // now armed with that key, we can create or update the object that we to save to that location.
        // When the object is ready, we now use the setValue() method to change the value of that location
        // from blank to a real value
        /*String key = playerScoreCloudEndPoint.push().getKey();
        myScore.scoreID = key;
        playerScoreCloudEndPoint.child(key).setValue(myScore);*/

        /*FireBase stuff*/
        //get reference to database
        mDatabase =  FirebaseDatabase.getInstance().getReference();
        //get reference to the playerScores in our database
        playerScoreCloudEndPoint = mDatabase.child("playerScores");

        /*following function call will display the listview with 10 highets scores, left commented
        out for now so we can add it to a button click later
         */
        //getHighScores();
    }

    public void initLeaderBoardListView() {
        //initialize the arralist holing the top ten player scores
        playerScoreList = new ArrayList<PlayerScore>();
        //initialize the adapter that acts as middle man between data in arraylist and listview on the screen
        playerScoreAdapter = new ScoreListAdapter(this, com.funnums.funnums.R.layout.score_list_element, playerScoreList);
        //initalize listview from xml file
        ListView myListView = (ListView) findViewById(com.funnums.funnums.R.id.listView);
        //set the adapter of this listview so when we add to our Arraylist, we can update listview on screen
        myListView.setAdapter(playerScoreAdapter);
        //updates data on the screen
        playerScoreAdapter.notifyDataSetChanged();
    }

    /*
        Gets the top ten high scores from Fierbase and displays them on screen
     */
    public void getHighScores() {
        //empty the arraylist so we don't keep adding ten scores to it everytime we call this function
        playerScoreList.clear();
        //order scores by the ten highest scores
        playerScoreCloudEndPoint.orderByChild("hiScore").limitToLast(10).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                //get a reference to the score being added
                PlayerScore score = dataSnapshot.getValue(PlayerScore.class);
                Log.d(logTag, "ScoreQuery: " + score.toString());
                //add this score to our array list and notify the listview that our list has been updated
                playerScoreList.add(score);
                playerScoreAdapter.notifyDataSetChanged();
            }
            /*
                Following override functions are boilerplate, need to be present even if they don't do anything
             */
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                PlayerScore removedScore = dataSnapshot.getValue(PlayerScore.class);
                Log.d(logTag, "The user named " + removedScore.name + " has been deleted from leaderboard");

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });

    }

    public void onPressedSelect(View v) {
        Log.d(TAG, "[SELECT GAME] pressed");
        // Create a new Intent object
        Intent i = new Intent(this, SelectGameActivity.class);
        // Start our GameActivity class via the Intent
        startActivity(i);
    }


}