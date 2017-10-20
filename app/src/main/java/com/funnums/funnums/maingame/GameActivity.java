package com.funnums.funnums.maingame;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.content.res.AssetManager;



public class GameActivity extends Activity
{

    // Our object to handle the View
    public static GameView gameView;

    //used to load bitmaps
    public static AssetManager assets;

    public static int screenX;
    public static int screenY;

    // This is where the "Play" button from MainMenuActivity sends us
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get a Display object to access screen details
        DisplayMetrics display = this.getResources().getDisplayMetrics();
        assets = getAssets();

        screenX = display.widthPixels;
        screenY = display.heightPixels;

        // Create an instance of our Game
        gameView = new GameView(this);
        gameView.startGame();

        // Make our gameView the view for the Activity
        setContentView(gameView);
    }

    // If the Activity is paused make sure to pause our thread
    @Override
    protected void onPause()
    {
        super.onPause();
        gameView.pause();
    }

    // If the Activity is resumed make sure to resume our thread
    @Override
    protected void onResume()
    {
        super.onResume();
        gameView.resume();
    }
}
