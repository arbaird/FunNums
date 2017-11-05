package com.funnums.funnums.maingame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import java.util.Set;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.io.InputStream;

import com.funnums.funnums.minigames.MiniGame;
import com.funnums.funnums.minigames.BubbleGame;
import com.funnums.funnums.minigames.BalloonGame;
import com.funnums.funnums.classes.GameCountdownTimer;
import com.funnums.funnums.uihelpers.*;

import android.os.Handler;
import android.os.Looper;


public class GameView extends SurfaceView implements Runnable {
    //current minigame
    public MiniGame currentGame;

    public String TAG = "Game"; //for debugging

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

    public GameFinishedMenu gameFinishedMenu;

    public String type;

    private long timeLeft;

    private final static int MAX_FPS = 50;
        // maximum number of frames to be skipped
    private final static int MAX_FRAME_SKIPS = 3;
        // the frame period
    private final static int FRAME_PERIOD = 1000 / MAX_FPS;

    //minimum sleep time between frames, used if the updates are occuring so fast that sleep time is negative
    private final static int MIN_SLEEP_TIME = 1000 / (MAX_FPS*10);

    GameView(Context context, String type) {
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
        pauseScreen = new PauseMenu(GameActivity.screenX/4,
                                    offset,
                                    GameActivity.screenX * 3/4,
                                    GameActivity.screenY - offset,
                                    resumeButton,
                                    menuButton);

        this.type = type;

        gameFinishedMenu = new GameFinishedMenu(GameActivity.screenX * 1/8,
                offset,
                GameActivity.screenX * 7/8,
                GameActivity.screenY - offset,
                resumeButton,
                menuButton, 0);


    }

    public void startGame() {
        if(type.equals("bubble"))
            currentGame = new BubbleGame();
        else if(type.equals("balloon"))
            currentGame = new BalloonGame();
        currentGame.init();

    }

    public void setCurrentMiniGame(MiniGame newGame)
    {
        currentGame = newGame;
    }

    @Override
    public void run() {

        //keep track of dela time, that is, how much time has passed in between each iteration of
        //the game loop
        long updateDurationNanos = 0;
        while(playing) {
            long beforeUpdateRender = System.nanoTime();

            //three main functions of game loop
            if(!currentGame.isPaused)
                currentGame.update(updateDurationNanos);
            currentGame.draw(ourHolder, canvas, paint);


            control(updateDurationNanos);
            //sleep(17);
            //update delta time
            updateDurationNanos = (System.nanoTime() - beforeUpdateRender);




        }
    }

    private void control(long updateDurationNanos) {

        double updateDurationMillis = updateDurationNanos /  1000000L; //0.000001;

        int framesSkipped = 0;  // number of frames being skipped
        int sleepTime = (int)(FRAME_PERIOD - updateDurationMillis); // ms to sleep (<0 if we're behind)
        if (sleepTime > 0  )
            sleep(sleepTime);
        else
            sleep(MIN_SLEEP_TIME);

    }

    private void sleep(int sleepTime)
    {
        try {
            //TODO don't hard code 17 in sleep, should be variable based on milis,
            //this acheives approximately 60FPS,
            // 17 milliseconds =  (1000(milliseconds)/60(FPS))
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
        Log.d(VIEW_LOG_TAG, "pause GameView!");
        //if there is a running game timer, paused it
        Log.d(VIEW_LOG_TAG, "currentGame.gameTimer: " + currentGame.gameTimer);
        if(currentGame.gameTimer != null && !currentGame.gameTimer.isPaused) {
            pauseGameTimer();
        }
    }

    public void resumeGameTimer() {
        currentGame.gameTimer.cancel();
        currentGame.gameTimer = null;
        currentGame.gameTimer = new GameCountdownTimer(timeLeft,1000);
        currentGame.gameTimer.start();
    }

    public void pauseGameTimer() {
        currentGame.gameTimer.cancel();
        currentGame.gameTimer.isPaused = true;
        timeLeft = currentGame.gameTimer.getMillisLeft();
    }

    // Make a new thread and start it
    public void resume() {
        playing = true;
        //resume the timer if it was paused becasue of user exiting app, NOT if game pause button was pressed
        if(currentGame.gameTimer != null) {
            if (currentGame.gameTimer.isPaused && !currentGame.isPaused)
                resumeGameTimer();
        }
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //first check if the pause menu should handle the touch
        if(currentGame.isPaused)
            return pauseScreen.onTouch(e);
        if(currentGame.isFinished)
            return gameFinishedMenu.onTouch(e);

        //then, check if the player is touching the pause button
        int x = (int)e.getX();
        int y = (int)e.getY();
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            currentGame.pauseButton.onTouchDown(x, y);
        }
        if (e.getAction() == MotionEvent.ACTION_UP) {
            if (currentGame.pauseButton.isPressed(x, y)) {
                currentGame.pauseButton.cancel();
                currentGame.isPaused = true;

                if(currentGame.gameTimer != null)
                    pauseGameTimer();
            }
            else {
                currentGame.pauseButton.cancel();
            }
        }

        //let the current game handle the touch event
        currentGame.onTouch(e);

        return true;
    }

    public static Bitmap loadBitmap(String filename, boolean transparency) {
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
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

        return bitmap;
    }

    public void updateGameTimer(final long newTime)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                currentGame.gameTimer.cancel();
                currentGame.gameTimer = null;
                currentGame.gameTimer = new GameCountdownTimer(newTime,1000);
                currentGame.gameTimer.start();
            }
        });
    }
}

