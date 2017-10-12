package com.funnums.funnums;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class GameActivity extends Activity
{

    // Our object to handle the View
    private Game game;

    // This is where the "Play" button from MainMenuActivity sends us
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        // Get a Display object to access screen details
        DisplayMetrics display = this.getResources().getDisplayMetrics();


        // Load the screen size into a Point object
        Point size = new Point();

        size.x = display.widthPixels;
        size.y = display.heightPixels;

        // Create an instance of our Game
        game = new Game(this, size.x, size.y);

        // Make our gameView the view for the Activity
        setContentView(game);

    }

    // If the Activity is paused make sure to pause our thread
    @Override
    protected void onPause()
    {
        super.onPause();
        game.pause();
    }

    // If the Activity is resumed make sure to resume our thread
    @Override
    protected void onResume()
    {
        super.onResume();
        game.resume();
    }


}
