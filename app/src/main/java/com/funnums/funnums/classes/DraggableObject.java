package com.funnums.funnums.classes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by Cesar on 11/11/2017.
 */

public abstract class DraggableObject {

    private String VIEW_LOG_TAG = "l";

    //use bitmap when we add in our own images
    //private Bitmap bitmap;
    public float x, y, length;

    // Constructor
    public DraggableObject(int screenX, int screenY, int length) {
        x = screenX;
        y = screenY;
        this.length = length;
    }

    public void update() {
        move();
    }

    public abstract void draw(Canvas canvas, Paint paint);

    public float getX() {
        return x;
    }

    public float getY() { return y; }

    public void setXY(int x, int y){
        this.x = x;
        this.y = y;
    }


    //Either pass speed and angle or new coordinates
    void move(/*int x, int y*/) {
        //this.x = x;
        //this.y = y;

        //Might need this later on
        /*this.x += x;
        this.y += y;

        xVelocity = (float) (getSpeed() * Math.cos(Math.toRadians(angle)));
        yVelocity =  (float) -(getSpeed() * Math.sin(Math.toRadians(angle )));*/
    }

    //bounce the number by swicthing their velocity vectors,
    //since all bubbles are same size (mass) this is all that needs to be done in conservation
    //of momentum inelastic collisions(collisions where things bounce off each other)!
   /* public void bounceWith(TouchableNumber collidingNum) {
        //move both circles so they no longer overlap each other
        CollisionDetector.correctCircleOverlap(this, collidingNum);

        float tempX = xVelocity;
        float tempY = yVelocity;

        //hold onto the values being swapped, a little redundant but makes it easier to track
        //which variables are being stored where, since there are 2 variable swaps instad of 1
        //(one swap x velocity and one for y velocity)
        float x1velocity = tempX;
        float y1velocity = tempY;
        float x2velocity = collidingNum.getXVelocity();
        float y2velocity = collidingNum.getYVelocity();

        //the x swap
        x1velocity  = x2velocity;
        x2velocity = tempX;

        //the y swap
        y1velocity  = y2velocity;
        y2velocity = tempY;

        //set this TouchableNumber's x velocity to the new value
        setXVelocity(x1velocity);
        setYVelocity(y1velocity);

        //set the bubble this number is colliding with to its new velocity
        collidingNum.setXVelocity(x2velocity);
        collidingNum.setYVelocity(y2velocity);
    }*/

    public float getLength() {
        return length;
    }
/*
    public void setXVelocity(float velocity)
    {
        xVelocity = velocity;
    }

    public float getXVelocity()
    {
        return xVelocity;
    }

    public void setYVelocity(float velocity)
    {
        yVelocity = velocity;
    }

    public float getYVelocity()
    {
        return yVelocity;
    }

    public void fixAngle()
    {

        angle = Math.toDegrees(Math.atan2(-yVelocity, xVelocity));
        xVelocity = (float) (getSpeed() * Math.cos(Math.toRadians(angle)));
        yVelocity =  (float) -(getSpeed() * Math.sin(Math.toRadians(angle )));
    }
*/

}