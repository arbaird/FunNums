package com.funnums.funnums.maingame;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Derek on 10/25/2017.
 */

public class SelectGameActivity extends AppCompatActivity {
    static final public String TAG = "Select Game";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Beginning of onCreate");
        super.onCreate(savedInstanceState);
        setContentView(com.funnums.funnums.R.layout.select_game);
        Log.d(TAG, "End of onCreate");
    }

    public void onPressedBubbles(View v) {
        Log.d(TAG, "Bubble game pressed");
        Intent i = new Intent(this, GameActivity.class);
        startActivity(i);
    }


    public void onPressedBalloons(View v) {
        Log.d(TAG, "Bubble game pressed");
        Intent i = new Intent(this, GameActivity.class);
        startActivity(i);
    }


}
