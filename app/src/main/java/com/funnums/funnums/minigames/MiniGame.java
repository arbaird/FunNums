package com.funnums.funnums.minigames;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.SurfaceHolder;
import android.view.MotionEvent;

import com.funnums.funnums.R;
import com.funnums.funnums.classes.GameCountdownTimer;
import com.funnums.funnums.maingame.GameActivity;
import com.funnums.funnums.uihelpers.UIButton;
import com.funnums.funnums.classes.GameCountdownTimer;


/**
 * Abstract class for a minigame, every minigame must provide a method to initialize the game,
 * update the game logic, draw the game, and handle touch events
 */

public abstract class MiniGame {
    public boolean isPaused;
    public boolean isFinished;

    public UIButton pauseButton;


    public int score = 0;

    public static GameCountdownTimer gameTimer;

    //gets the context and volume to be used for sound effects(required in soundPool)
    public Context context = com.funnums.funnums.maingame.GameActivity.gameView.context;
    float volume = GameActivity.gameView.volume;
    public SoundPool soundPool;
    public int gameOverSoundId;

    public MiniGame(){
        //set up the pause button


        Bitmap pauseImg = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("Shared/pauseButton.png", true);
        Bitmap pauseImgDown = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("Shared/pauseButtonDown.png", true);

        //resize the pause button
        int size = (int)(com.funnums.funnums.maingame.GameActivity.screenY * 0.084459);
        pauseImg = Bitmap.createScaledBitmap(pauseImg, size, size,false);
        pauseImgDown = Bitmap.createScaledBitmap(pauseImgDown, size, size,false);

        int offset = pauseImg.getHeight();
        pauseButton = new UIButton(com.funnums.funnums.maingame.GameActivity.screenX - pauseImg.getWidth(), 0, com.funnums.funnums.maingame.GameActivity.screenX, offset, pauseImg, pauseImgDown);
    }

    public void setCurrentMiniGame(MiniGame newGame) {
        com.funnums.funnums.maingame.GameActivity.gameView.setCurrentMiniGame(newGame);
    }

    public abstract void init();

    public abstract void update(long delta);

    public abstract void draw(SurfaceHolder ourHolder, Canvas canvas, Paint paint);

    public abstract boolean onTouch(MotionEvent e);

    public synchronized void initTimer(int time) {
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
        gameTimer = new GameCountdownTimer(time + 1000, 1000);
        gameTimer.context = com.funnums.funnums.maingame.GameActivity.gameView.context;
        gameTimer.start();
    }


    public void onFinish(){
        GameActivity.gameView.currentGame.playGameOverSound();
        isFinished = true;
        com.funnums.funnums.maingame.GameActivity.gameView.gameFinishedMenu.setScore(score);
        //update high score, if new one is achieved
        com.funnums.funnums.maingame.LeaderboardGameActivity.storeHighScore(score);
    }


    public void playGameOverSound(){
        soundPool.play(gameOverSoundId,volume,volume,1,0,1);
    }



}
