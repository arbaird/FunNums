package com.funnums.funnums;

/**
 * Created by austinbaird on 10/6/17.
 */



import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Iterator;




public class Game extends SurfaceView implements Runnable
{

    public State currentState;

    public String logTag = "Game"; //for debugging

    public final static int NANOS_TO_SECONDS = 1000000000; //conversion from nanosecs to seconds


    // Used to hold touch events so that drawing thread and onTouch thread don't result in concurrent access
    // not likely that these threads would interact, but if they do the game will crash!! which is why
    //we keep events in a separate list to be processed in the game loop
    private ArrayList<MotionEvent> events = new ArrayList<>();

    private boolean gameEnded;

    //for drawing
    private Context context;

    //dimensions of the sc
    private int screenX;
    private int screenY;

    private int topBuffer = 200;

    //while playing is true, we keep updating game loop
    public boolean playing;

    //thread for the game
    Thread gameThread = null;

    // For drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder ourHolder;


    Game(Context context, int x, int y)
    {
        //set up view properly
        super(context);
        this.context  = context;


        screenX = x;
        screenY = y;

        Log.d(VIEW_LOG_TAG, String.valueOf(x) + ", " + String.valueOf(y));

        // Initialize our drawing objects
        ourHolder = getHolder();
        paint = new Paint();
        canvas = new Canvas();

        currentState = new BubbleGameState();
        currentState.init(x, y);

    }





    public void setCurrentState(State newState)
    {
        currentState = newState;
    }

    @Override
    public void run()
    {

        //keep track of dela time, that is, how much time has passed in between each iteration of
        //the game loop
        long updateDurationMillis = 0;
        while(playing)
        {
            long beforeUpdateRender = System.nanoTime();

            //three main functions of game loop
            /*update(updateDurationMillis);
            draw();*/
            currentState.update(updateDurationMillis);
            currentState.draw(ourHolder,canvas,paint);
            control();

            //update delta time
            updateDurationMillis = (System.nanoTime() - beforeUpdateRender);
        }
    }





    private void control() {
        try
        {
            //TODO don't hard code 17 in sleep, should be variable based on milis,
            //this acheives approximately 60FPS,
            // 17 milliseconds =  (1000(milliseconds)/60(FPS))
            gameThread.sleep(17);
        }
        catch (InterruptedException e)
        {
            Log.e(logTag, "Error causing thread to sleep\n" + e.getStackTrace());
        }
    }



    // Clean up our thread if the game is interrupted or the player quits
    public void pause()
    {
        playing = false;
        try
        {
            gameThread.join();
        }
        catch (InterruptedException e)
        {
            Log.e(logTag, "Error joining gameThread\n" + e.getStackTrace());
        }
    }

    // Make a new thread and start it
    public void resume()
    {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent)
    {
        //add touch event to eventsQueue rather than processing it immediately. This is because
        //onTouchEvent is run in a separate thread by Android and if we touch and delete a number
        //in this touch UI thread while our game thread is accessing that same number, the game crashes
        //because two threads are accessing same memory being removed. We could do mutex but this setup
        //is pretty standard I believe.


        currentState.onTouch(motionEvent);

        return true;
    }


}

