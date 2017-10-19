package com.funnums.funnums;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.MotionEvent;

/**
 * Created by austinbaird on 10/19/17.
 */

public abstract class State
{
    public void setCurrentState(State newState) {
        GameActivity.game.setCurrentState(newState);
    }

    public abstract void init(int x, int y);

    public abstract void update(long delta);

    public abstract void draw(SurfaceHolder ourHolder, Canvas canvas, Paint paint);

    public abstract boolean onTouch(MotionEvent e);
}
