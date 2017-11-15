package com.funnums.funnums.classes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Cesar on 11/11/2017.
 *
 * Abstract Class defines a features of a Draggable object
 * -Have an X/Y position
 * -Length (Assuming a Square object)
 * -Be able to draw itself
 */

public abstract class DraggableObject {

    //use bitmap when we add in our own images
    //private Bitmap bitmap;
    public float x, y, length;

    // Constructor
    public DraggableObject(float screenX, float screenY, float length) {
        x = screenX;
        y = screenY;
        this.length = length;
    }

    //Getter methods
    public float getX() {
        return x;
    }

    public float getY() { return y; }

    public float getLength(){ return length; }

    //Position setter
    public void setXY(float x, float y){
        this.x = x;
        this.y = y;
    }

    //Draw method
    public abstract void draw(Canvas canvas, Paint paint);

    //Could be used later on when dragging
    void move() {}

    //Could be used later on when dragging
    public void update() {
        move();
    }

}