package com.funnums.funnums.maingame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.funnums.funnums.classes.PlayerScore;
import com.funnums.funnums.uihelpers.ScoreListAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Activity defined to display Leaderboard. Austin's previously defined functions from MainMenuActivity (Involving Firebase) were transferred to this file.
 */

public class LeaderboardGameActivity extends AppCompatActivity {
    static final public String TAG = "See Leaderboard";

    //reference to firebase database
    private DatabaseReference mDatabase;

    //reference to the playerScore table in our database
    private DatabaseReference playerScoreCloudEndPoint;

    private ArrayList<PlayerScore> playerScoreList;
    private ScoreListAdapter playerScoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Beginning of onCreate");
        super.onCreate(savedInstanceState);
        setContentView(com.funnums.funnums.R.layout.see_leaderboard);
        Log.d(TAG, "End of onCreate");

         /*FireBase stuff*/
        //get reference to database
        mDatabase =  FirebaseDatabase.getInstance().getReference();
        //get reference to the playerScores in our database
        playerScoreCloudEndPoint = mDatabase.child("playerScores");

        //initalize the leader board list view and adapter
        initLeaderBoardListView();

        /*following function call will display the listview with 10 highets scores, left commented
        out for now so we can add it to a button click later
         */
        getHighScores();
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
        Gets the top ten high scores from Firebase and displays them on screen
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
                Log.d(TAG, "ScoreQuery: " + score.toString());
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
                Log.d(TAG, "The user named " + removedScore.name + " has been deleted from leaderboard");

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });

    }

}
