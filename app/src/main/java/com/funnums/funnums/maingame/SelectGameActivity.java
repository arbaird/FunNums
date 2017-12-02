package com.funnums.funnums.maingame;


import android.content.Intent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.funnums.funnums.classes.HowToPopUp;

/**
 *  Activity displaying the mini game selection and how to play each game
 */

public class SelectGameActivity extends AppCompatActivity {
    static final public String TAG = "Select Game";

    //Create select game activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.funnums.funnums.R.layout.select_game);

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


}
