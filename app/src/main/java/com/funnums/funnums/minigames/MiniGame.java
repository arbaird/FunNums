package com.funnums.funnums.minigames;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.MotionEvent;

import com.funnums.funnums.classes.GameCountdownTimer;
import com.funnums.funnums.uihelpers.UIButton;
import com.funnums.funnums.classes.GameCountdownTimer;


/**
 * Created by austinbaird on 10/19/17.
 */

public abstract class MiniGame
{
    public boolean isPaused;
    public boolean isFinished;

    public UIButton pauseButton;


    public int score;

    public static GameCountdownTimer gameTimer;


    public void setCurrentMiniGame(MiniGame newGame)
    {
        com.funnums.funnums.maingame.GameActivity.gameView.setCurrentMiniGame(newGame);
    }

    public abstract void init();

    public abstract void update(long delta);

    public abstract void draw(SurfaceHolder ourHolder, Canvas canvas, Paint paint);

    public abstract boolean onTouch(MotionEvent e);
}
