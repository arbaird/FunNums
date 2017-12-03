package com.funnums.funnums.maingame;


import android.content.Intent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.funnums.funnums.R;
import com.funnums.funnums.classes.HowToPopUp;
import android.content.SharedPreferences;


/**
 *  Activity displaying the mini game selection and how to play each game
 */

public class SelectGameActivity extends AppCompatActivity {
    static final public String TAG = "Select Game";
    SharedPreferences prefs;
    //Create select game activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.funnums.funnums.R.layout.select_game);




        prefs = getSharedPreferences("HighScore", MODE_PRIVATE);
    }

    //Launch mini game activity (Bubble type)
    public void onPressedBubbles(View v) {

        Intent i = new Intent(this, GameActivity.class);

        //setFlag that game we want to create is a bubble game
        i.putExtra("minigame", "bubble");

        startActivity(i);
    }

    //Launch mini game activity (Balloon type)
    public void onPressedBalloons(View v) {

        Intent i = new Intent(this, GameActivity.class);

        //setFlag that game we want to create is a bubble game
        i.putExtra("minigame", "balloon");

        startActivity(i);
    }

    //Launch mini game activity (Owl type)
    public void onPressedOwl(View v) {

        Intent i = new Intent(this, GameActivity.class);

        //setFlag that game we want to create is a bubble game
        i.putExtra("minigame", "owl");

        startActivity(i);
    }


    //Launch pop up activity (How to play bubbles)
    public void onPressedHowToBubbles(View v) {
        Intent i = new Intent(this, HowToPopUp.class);

        i.putExtra("minigame", "bubble");

        startActivity(i);
    }

    //Launch pop up activity (How to play balloons)
    public void onPressedHowToBalloons(View v) {
        Intent i = new Intent(this, HowToPopUp.class);

        i.putExtra("minigame", "balloon");

        startActivity(i);
    }

    //Launch pop up activity (How to play owl)
    public void onPressedHowToOwl(View v) {
        Intent i = new Intent(this, HowToPopUp.class);

        i.putExtra("minigame", "owl");

        startActivity(i);
    }

    /*
        Show the player's high score for a given minigame
     */
    public void showHighScore(String miniGame){
        long score = prefs.getLong(miniGame + "HighScore", 0);
        TextView title;
        switch (miniGame){
            case("owl"):
                title = (TextView) findViewById(R.id.textViewOwlScore);
                break;
            case("bubble"):
                title = (TextView) findViewById(R.id.textViewBubbleScore);
                break;
            case("balloon"):
                title = (TextView) findViewById(R.id.textViewBalloonScore);
                break;
            default:
                return;
        }
        //get
        String scoreText = "High Score: " + String.valueOf(score);
        title.setText(scoreText);
    }

    // If the Activity is resumed make sure to resume our thread
    @Override
    protected void onResume() {
        super.onResume();
        showHighScore("bubble");
        showHighScore("balloon");
        showHighScore("owl");

    }


}
