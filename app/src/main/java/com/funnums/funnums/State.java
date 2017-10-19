package com.funnums.funnums;

/**
 * Created by austinbaird on 10/19/17.
 */

public abstract class State
{
    public abstract void init();

    public abstract void update(long delta);

    public abstract void draw();

    //public abstract boolean onTouch(MotionEvent e, int scaledX, int scaledY);
}
