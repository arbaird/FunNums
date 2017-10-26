package com.funnums.funnums.maingame;

//import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainMenuActivity extends AppCompatActivity {
    private static final String TAG = "Main Menu";

    // This is the entry point to our game
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Here we set our UI layout as the view
        setContentView(com.funnums.funnums.R.layout.activity_main_menu);

        // Prepare to highest Score. We don't need this yet, we can keep it for later when we implement scoring
        SharedPreferences prefs;
        SharedPreferences.Editor editor;
        prefs = getSharedPreferences("HighScore", MODE_PRIVATE);

        // Load fastest time
        // if not available our high score = 1000000
        long highScore = prefs.getLong("HighScore", 1000000);

        // Get a refference to the TextView in our layout
        final TextView textFastestTime = (TextView)findViewById(com.funnums.funnums.R.id.textHiScore);
        // Put the high score in our TextView
        textFastestTime.setText("Your High Score:" + highScore);

    }

    public void onPressedSelect(View v) {
        Log.d(TAG, "[SELECT GAME] pressed");
        // Create a new Intent object
        Intent i = new Intent(this, SelectGameActivity.class);
        // Start our GameActivity class via the Intent
        startActivity(i);
    }

}