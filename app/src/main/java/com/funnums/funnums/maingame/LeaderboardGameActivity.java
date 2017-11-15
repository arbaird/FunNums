package com.funnums.funnums.maingame;

import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.funnums.funnums.R;
import com.funnums.funnums.classes.PlayerScore;
import com.funnums.funnums.uihelpers.ScoreListAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.ProgressDialog;

import java.util.ArrayList;

import static com.funnums.funnums.maingame.MainMenuActivity.*;

/**
 * Activity defined to display Leaderboard. Austin's previously defined functions from MainMenuActivity (Involving Firebase) were transferred to this file.
 */

public class LeaderboardGameActivity extends AppCompatActivity {
    static final public String TAG = "See Leaderboard";

    //reference to firebase database
    public static DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    //reference to the playerScore table in our database
    public static DatabaseReference playerScoreCloudEndPoint = mDatabase.child("playerScores");

    private ArrayList<PlayerScore> playerScoreList;
    private ScoreListAdapter playerScoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.funnums.funnums.R.layout.see_leaderboard);

        //initalize the leader board list view and adapter
        initLeaderBoardListView();

        //following function call will display the listview with 10 highest scores
        getHighScores("bubble");

        //start out displaying bubble game leaderboard
        Button bubbleButton= (Button) findViewById(R.id.buttonBubbleScores);
        bubbleButton.setEnabled(false);
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
    public void getHighScores(String game) {
        //empty the arraylist so we don't keep adding ten scores to it everytime we call this function
        playerScoreList.clear();
        playerScoreAdapter.notifyDataSetChanged();

        playerScoreCloudEndPoint = mDatabase.child(game + "Scores");
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

    /*
        store given high score, if it is higher than current high score
     */
    public static void storeHighScore(long score) {

        SharedPreferences prefs = MainMenuActivity.prefs;

        if(prefs == null){
            Context context = com.funnums.funnums.maingame.GameActivity.gameView.context;
            prefs = context.getSharedPreferences("HighScore", MODE_PRIVATE);
        }

        //get the editor so we can update stored data, if needed
        final SharedPreferences.Editor editor = prefs.edit();

        //get current minigame to store score for
        String currentMiniGame = com.funnums.funnums.maingame.GameActivity.gameView.gameType;

        long currentHighScore = prefs.getLong(currentMiniGame+ "HighScore", 0);

        if(currentHighScore > score)
            return;
        else{
            editor.putLong(currentMiniGame + "HighScore", score);
            editor.apply();
        }
        //if database hasn't been initialized yet, initialize it!
        if(mDatabase == null)
        {
            mDatabase =  FirebaseDatabase.getInstance().getReference();
            playerScoreCloudEndPoint = mDatabase.child("playerScores");
        }

        //get reference to table for current minigame scores
        playerScoreCloudEndPoint = mDatabase.child(currentMiniGame + "Scores");

        String userName = prefs.getString("user_name", null);
        //if user still hasn't entered their username, no score to store
        if(userName == null)
            return;

        //store new high score in database
        PlayerScore myScore = new PlayerScore(userName, score);
        playerScoreCloudEndPoint.child(userName).setValue(myScore);

    }

    /*
        Method called when a button is clicked ot display keaderboard for the given game
     */
    public void chooseGameScores(View v){
        int clickedButtonId = v.getId(); //get the android id of the clicked button

        String game = "";
        //id of button
        int titleStringId = 0;

        //find the actual button number from the android id
        switch (clickedButtonId)
        {
            case(R.id.buttonBubbleScores) :
                game = "bubble";
                titleStringId = R.string.bubble_leaderboard;
                break;
            case(R.id.buttonBalloonScores) :
                game = "balloon";
                titleStringId = R.string.balloon_leaderboard;
                break;
            case(R.id.buttonOwlScores) :
                game = "owl";
                titleStringId = R.string.owl_leaderboard;
                break;

        }
        //display high scores for selected game
        getHighScores(game);

        //set current game button as unselectable, and all others as selectable
        int[] buttonIds = {R.id.buttonBubbleScores, R.id.buttonBalloonScores, R.id.buttonOwlScores};
        for(int i = 0; i < buttonIds.length; i++) {
            Button button= (Button) findViewById(buttonIds[i]);
            if(buttonIds[i] == clickedButtonId)
                button.setEnabled(false);
            else
                button.setEnabled(true);
        }

        //update textview to show current mini game name
        TextView title = (TextView) findViewById(R.id.textLeaderboard);
        title.setText(titleStringId);

    }

    public static void setEndpointToPlayerNames(){
        playerScoreCloudEndPoint = mDatabase.child("playerScores");
    }

}



