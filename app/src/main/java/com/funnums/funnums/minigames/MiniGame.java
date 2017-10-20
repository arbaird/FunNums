package com.funnums.funnums.minigames;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.MotionEvent;

import com.funnums.funnums.uihelpers.UIButton;


/**
 * Created by austinbaird on 10/19/17.
 */

public abstract class MiniGame
{
    public boolean isPaused;

    public UIButton pauseButton;


    public void setCurrentMiniGame(MiniGame newGame)
    {
        com.funnums.funnums.maingame.GameActivity.gameView.setCurrentMiniGame(newGame);
    }

    public abstract void init();

    public abstract void update(long delta);

    public abstract void draw(SurfaceHolder ourHolder, Canvas canvas, Paint paint);

    public abstract boolean onTouch(MotionEvent e);
}
