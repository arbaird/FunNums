package com.funnums.funnums.maingame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.io.InputStream;

import com.funnums.funnums.minigames.MiniGame;
import com.funnums.funnums.minigames.BubbleGame;
import com.funnums.funnums.uihelpers.*;


public class GameView extends SurfaceView implements Runnable
{
    //current minigame
    public MiniGame currentGame;

    public String logTag = "Game"; //for debugging

    public final static int NANOS_TO_SECONDS = 1000000000; //conversion from nanosecs to seconds

    //while playing is true, we keep updating game loop
    public boolean playing;

    //thread for the game
    Thread gameThread = null;

    // For drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder ourHolder;

    public PauseMenu pauseScreen;

    GameView(Context context)
    {
        //set up view properly
        super(context);

        // Initialize our drawing objects
        ourHolder = getHolder();
        paint = new Paint();
        canvas = new Canvas();

        Bitmap resumeDown = loadBitmap("button_resume_down.png", true);
        Bitmap resume = loadBitmap("button_resume.png", true);
        UIButton resumeButton = new UIButton(0,0,0,0, resume, resumeDown);

        Bitmap menuDown = loadBitmap("button_quit_down.png", true);
        Bitmap menu = loadBitmap("button_quit.png", true);
        UIButton menuButton = new UIButton(0,0,0,0, menu, menuDown);

        //Bitmap backdrop = loadBitmap("rounded.png", true);
        int offset = 100;
        pauseScreen = new PauseMenu(GameActivity.screenX/4, offset, GameActivity.screenX * 3/4, GameActivity.screenY - offset, resumeButton, menuButton);

    }

    public void startGame()
    {
        currentGame = new BubbleGame();
        currentGame.init();
    }

    public void setCurrentMiniGame(MiniGame newGame)
    {
        currentGame = newGame;
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
            if(!currentGame.isPaused)
                currentGame.update(updateDurationMillis);
            currentGame.draw(ourHolder, canvas, paint);
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
    public boolean onTouchEvent(MotionEvent e)
    {
        //first check if the pause menu should handle the touch
        if(currentGame.isPaused)
            return pauseScreen.onTouch(e);

        //then, check if the player is touching the pause button
        int x = (int)e.getX();
        int y = (int)e.getY();
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            currentGame.pauseButton.onTouchDown(x, y);
        }
        if (e.getAction() == MotionEvent.ACTION_UP) {
            if (currentGame.pauseButton.isPressed(x, y))
            {
                currentGame.pauseButton.cancel();
                currentGame.isPaused = true;
                //setCurrentState(new PlayState());
            }
            else
            {
                currentGame.pauseButton.cancel();
            }
        }

        //let the current game handle the touch event
        currentGame.onTouch(e);

        return true;
    }

    public static Bitmap loadBitmap(String filename, boolean transparency)
    {
        InputStream inputStream = null;
        try {
            inputStream = GameActivity.assets.open(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (transparency) {
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        } else {
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null,
                options);
        return bitmap;
    }
}

