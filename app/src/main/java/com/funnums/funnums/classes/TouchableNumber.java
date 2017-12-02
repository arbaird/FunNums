package com.funnums.funnums.classes;
/**
 * Abstract class representing a number on the screen that the player can interact with through touch
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;



public abstract class TouchableNumber {

    private String VIEW_LOG_TAG = "l";

    //coords and radius
    public float x, y, radius;
    //speed of num
    private int speed;

    //individual velocities for x and y directions, calculated based on speed variable
    private float xVelocity, yVelocity;

    //the angle the number will travel at
    public double angle;

    // Constructor
    public TouchableNumber(int screenX, int screenY, int travelAngle, int radius, int speed) {
        x = screenX;
        y = screenY;
        angle = travelAngle;

        this.radius = radius;

        this.speed = speed;


        //Trig to determine the x and y velocities based on speed and angle

        xVelocity = (float) (getSpeed() * Math.cos(Math.toRadians(angle)));
        yVelocity =  (float) -(getSpeed() * Math.sin(Math.toRadians(angle )));

    }

    public void update() {
        move();
    }

    public abstract void draw(Canvas canvas, Paint paint);


    public int getSpeed() {
        return speed;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    void move() {
        x += xVelocity;
        y += yVelocity;
    }




        //bounce the number by swicthing their velocity vectors,
        //since all bubbles are same size (mass) this is all that needs to be done in conservation
        //of momentum inelastic collisions(collisions where things bounce off each other)!
    public void bounceWith(TouchableNumber collidingNum) {
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
    }

    public float getRadius()
    {
        return radius;
    }

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

    public void setRadius(float newRad)
    {
        radius = newRad;
    }

}
