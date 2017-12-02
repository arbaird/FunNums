package com.funnums.funnums.maingame;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.funnums.funnums.classes.HowToPopUp;

/**
 *  Activity displaying the mini game selection and how to play each game
 */

public class SelectGameActivity extends AppCompatActivity {
    static final public String TAG = "Select Game";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.funnums.funnums.R.layout.select_game);

    }


    public void onPressedBubbles(View v) {

        Intent i = new Intent(this, GameActivity.class);

        //setFlag that game we want to create is a bubble game
        i.putExtra("minigame", "bubble");

        startActivity(i);
    }

    public void onPressedBalloons(View v) {

        Intent i = new Intent(this, GameActivity.class);

        //setFlag that game we want to create is a bubble game
        i.putExtra("minigame", "balloon");

        startActivity(i);
    }

    public void onPressedOwl(View v) {

        Intent i = new Intent(this, GameActivity.class);

        //setFlag that game we want to create is a bubble game
        i.putExtra("minigame", "owl");

        startActivity(i);
    }

    public void onPressedHowToBubbles(View v) {
        Log.d(TAG, "How To Bubble game pressed");
        Intent i = new Intent(this, HowToPopUp.class);

        i.putExtra("minigame", "bubble");

        startActivity(i);
    }

    public void onPressedHowToBalloons(View v) {
        Log.d(TAG, "How To Balloon game pressed");
        Intent i = new Intent(this, HowToPopUp.class);

        i.putExtra("minigame", "balloon");

        startActivity(i);
    }

    public void onPressedHowToOwl(View v) {
        Log.d(TAG, "How To Owl game pressed");
        Intent i = new Intent(this, HowToPopUp.class);

        i.putExtra("minigame", "owl");

        startActivity(i);
    }


}
