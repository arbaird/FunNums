package com.funnums.funnums.maingame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.io.InputStream;
import android.os.Handler;
import android.os.Looper;

import com.funnums.funnums.R;
import com.funnums.funnums.minigames.MiniGame;
import com.funnums.funnums.minigames.BubbleGame;
import com.funnums.funnums.minigames.BalloonGame;
import com.funnums.funnums.minigames.OwlGame;
import com.funnums.funnums.classes.GameCountdownTimer;
import com.funnums.funnums.uihelpers.*;
import com.funnums.funnums.classes.GameCountdownTimer;

import android.media.MediaPlayer;


import android.os.Handler;
import android.os.Looper;

/*
    The view used to display the current mingame.  Allows us to do drawing on a spearate thread
    so we don't kill the phone's CPU
 */
public class GameView extends SurfaceView implements Runnable {
    //current minigame
    public MiniGame currentGame;

    public String TAG = "Game"; //for debugging

    public final static int NANOS_TO_SECONDS = 1000000000; //conversion from nanosecs to seconds

    //while playing is true, we keep updating game loop
    public boolean playing;

    //thread for the game
    public Thread gameThread = null;

    //For sound effects
    private static SoundPool soundPool;
    private int pauseId;
    private int timeUpId;
    public float volume;

    // For drawing
    public Paint paint;
    public Canvas canvas;
    public SurfaceHolder ourHolder;

    //pause menu
    public PauseMenu pauseScreen;
    //display when the game is over
    public GameFinishedMenu gameFinishedMenu;
    //type of minigame being played
    public String gameType;
    //timeleft, used for when the player pauses the game
    private long timeLeft;

    //Max FPS,used to control frame rate
    private final static int MAX_FPS = 50;
    // the frame period
    private final static int FRAME_PERIOD = 1000 / MAX_FPS;

    //minimum sleep time between frames, used if the updates are occuring so fast that sleep time is negative
    private final static int MIN_SLEEP_TIME = 1000 / (MAX_FPS*10);
    //context used so we can still access shared preferences from outside an Activity
    public Context context;
    SharedPreferences prefs;

    GameView(Context context, String type) {
        //set up view properly
        super(context);
        this.context = context;
        //store which minigame type player selected
        this.gameType = type;
        //get custom font
        Typeface tf =Typeface.createFromAsset(GameActivity.assets,"fonts/Cendol_Pulut.ttf");

        // Initialize our drawing objects
        ourHolder = getHolder();
        paint = new Paint();
        canvas = new Canvas();
        //set the font
        paint.setTypeface(tf);

        //set up buttons for game finished menu and pause screen
        Bitmap resumeDown = loadBitmap("Shared/resumebuttonDown.png", true);
        Bitmap resume = loadBitmap("Shared/resumebutton.png", true);
        UIButton resumeButton = new UIButton(0,0,0,0, resume, resumeDown);

        Bitmap menuDown = loadBitmap("Shared/quitbuttonDown.png", true);
        Bitmap menu = loadBitmap("Shared/quitbutton.png", true);
        UIButton menuButton = new UIButton(0,0,0,0, menu, menuDown);

        //get the stored data on this phone
        prefs = context.getSharedPreferences("HighScore", Context.MODE_PRIVATE);

        //set up backdrop for menus, give it bubblegame backdrop to start with, will be changed by each minigame
        Bitmap backdrop = loadBitmap("BubbleGame/BubbleMenuBoard.png", true);

        //set up sound effects
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC,0);
        pauseId = soundPool.load(context, R.raw.pause,1);
        timeUpId = soundPool.load(context, R.raw.timesup,1);

        //get the volume float from sharedPreferences. Returns 1 (max volume) if no volume is stored.
        volume=prefs.getFloat("volume", 1);


        //magic number, but seems to be good spacing across different sized phones
        int offset = 100;
        //initialize pause menu
        pauseScreen = new PauseMenu(GameActivity.screenX*1/8,
                                    offset,
                                    GameActivity.screenX * 5/8,
                                    GameActivity.screenY - offset*3,
                                    resumeButton,
                                    menuButton,
                                    backdrop);
        //initialize game finished menu
        gameFinishedMenu = new GameFinishedMenu(GameActivity.screenX*1/8,
                offset,
                GameActivity.screenX * 5/8,
                GameActivity.screenY - offset*3,
                resumeButton,
                menuButton, backdrop, paint);
    }

    /*
        Start a minigame type based on intent to Game Activity from Select Game Activity
     */
    public void startGame() {
        if(gameType.equals("bubble"))
            currentGame = new BubbleGame();
        else if(gameType.equals("balloon"))
            currentGame = new BalloonGame();
        else if(gameType.equals("owl"))
            currentGame = new OwlGame();
        currentGame.init();
        //clean up memory used from previous game, or anything unused from main menu
        System.gc();

    }


    public void setCurrentMiniGame(MiniGame newGame)
    {
        currentGame = newGame;
    }

    @Override
    public synchronized void run() {

        //keep track of delta time, that is, how much time has passed in between each iteration of
        //the game loop
        long updateDurationNanos = 0;
        while(playing) {
            long beforeUpdateRender = System.nanoTime();

            //three main functions of game loop
            //update game if game is not paused
            if(!currentGame.isPaused)
                currentGame.update(updateDurationNanos);
            //draw the scene
            currentGame.draw(ourHolder, canvas, paint);
            //control frame rate
            control(updateDurationNanos);

            updateDurationNanos = (System.nanoTime() - beforeUpdateRender);
        }
    }

    //restart the current minigame
    public void restart(){
        //clean up the running thread
        playing = false;
        try {
            gameThread.join();
        }
        catch (InterruptedException e) {
            Log.e(TAG, "Error joining gameThread\n" + e.getStackTrace());
        }
        //start new thread and begin the current game
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
        startGame();

    }

    //control framerate
    private void control(long updateDurationNanos) {

        double updateDurationMillis = updateDurationNanos /  1000000L;

        int sleepTime = (int)(FRAME_PERIOD - updateDurationMillis); // ms to sleep (<0 if we're behind)
        //make the thread sleep if sleep Time is positive, else, make it sleep minimum sleep time (2milis)
        if (sleepTime > 0  )
            sleep(sleepTime);
        else
            sleep(MIN_SLEEP_TIME);
    }

    //abstraction to make code cleaner, i.e less try blocks inside if statements
    private void sleep(int sleepTime)
    {
        try {
            gameThread.sleep(sleepTime);
        }
        catch (InterruptedException e) {
            Log.e(TAG, "Error causing thread to sleep\n" + e.getStackTrace());
        }
    }

    // Clean up our thread if the game is interrupted or the player quits
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        }
        catch (InterruptedException e) {
            Log.e(TAG, "Error joining gameThread\n" + e.getStackTrace());
        }
        //if there is a running game timer, paused it
        if(currentGame.gameTimer != null && !currentGame.gameTimer.isPaused) {
            pauseGameTimer();
        }
    }

    //resume the timer using the stored time left
    public void resumeGameTimer() {
        currentGame.gameTimer.cancel();
        currentGame.gameTimer = null;
        currentGame.gameTimer = new GameCountdownTimer(timeLeft,1000);
        currentGame.gameTimer.start();
    }

    //cancel timer and store timer left so we can reset it properly
    public void pauseGameTimer() {
        currentGame.gameTimer.cancel();
        currentGame.gameTimer.isPaused = true;
        timeLeft = currentGame.gameTimer.getTime();
    }

    // Make a new thread and start it
    public void resume() {
        playing = true;
        //resume the timer if it was paused becasue of user exiting app, NOT if game pause button was pressed.
        //this is handle by resumeGameTimer()
        if(currentGame.gameTimer != null) {
            if (currentGame.gameTimer.isPaused && !currentGame.isPaused)
                resumeGameTimer();
        }
        gameThread = new Thread(this);
        gameThread.start();
        gameThread.setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //first check if the pause menu should handle the touch
        if(currentGame.isPaused)
            return pauseScreen.onTouch(e);
        //then check if game finished menu should handle the touch
        if(currentGame.isFinished) {
            return gameFinishedMenu.onTouch(e);
        }

        //then, check if the player is touching the pause button
        int x = (int)e.getX();
        int y = (int)e.getY();
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            //change the button appearance to a pressed button
            if(currentGame.pauseButton.onTouchDown(x, y))
                return true;

        }
        if (e.getAction() == MotionEvent.ACTION_UP) {
            //if pause button is pressed, then pause the game
            if (currentGame.pauseButton.isPressed(x, y)) {
                //pause the timer
                currentGame.pauseButton.cancel();
                currentGame.isPaused = true;

                //play pause sound
                soundPool.play(pauseId,volume,volume,1,0,1);
                //make sure current game has a timer before pausing it (Owl game doesn't use a timer)

                if(currentGame.gameTimer != null)
                    pauseGameTimer();
                return true;
            }
            //else, change pause button's appearance so it looks unselected
            else {
                currentGame.pauseButton.cancel();
            }
        }


        //if none of these menus or pause button is being touched,let the current game handle the touch event
        currentGame.onTouch(e);
        return true;
    }

    /*
        Load a bitmap from the assets folder
     */
    public static Bitmap loadBitmap(String filename, boolean transparency) {
        InputStream inputStream = null;
        //open file from assets folder
        try {
            inputStream = GameActivity.assets.open(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        //set transparency, as specified
        if (transparency) {
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        } else {
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }
        //get the actual bitmap and return it
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        return bitmap;
    }

    /*
        Add a given amount of time to the current game timer
     */
    public void updateGameTimer(final long timeToAdd)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //make sure time doesn't become negative if time was deducted
                long newTime = Math.max(0, currentGame.gameTimer.getTime() + timeToAdd);
                //cancel the timer and start a new one
                currentGame.gameTimer.cancel();
                currentGame.gameTimer = null;

                currentGame.gameTimer = new GameCountdownTimer(newTime, 1000);
                currentGame.gameTimer.start();
            }
        });
    }

    /*
        Sets the image for the menu backdrop, should vary based on current game
     */
    public void setMenuBackdrop(String fileName){
        Bitmap backdrop = com.funnums.funnums.maingame.GameView.loadBitmap(fileName, false);
        pauseScreen.setBackDrop(backdrop);
        gameFinishedMenu.setBackDrop(backdrop);
    }

}

