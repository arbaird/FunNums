package com.funnums.funnums.classes;

/**
 * Created by austinbaird on 10/6/17.
 */



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.util.Random;

public class TouchableNumber
{

    private String VIEW_LOG_TAG = "l";
    //use bitmap when we add in our own images
    //private Bitmap bitmap;
    private float x, y, radius;
    private int speed;


    private static Random r = new Random();
    // A hit box for collision detection

    //the actual value of this number
    private int number;

    private float xVelocity, yVelocity;

    //the angle the number will travel at
    public double angle;



    // Constructor
    public TouchableNumber(int screenX, int screenY, int travelAngle, int value, int radius)
    {
        x = screenX;
        y = screenY;
        angle = travelAngle;

        xVelocity = 0;
        yVelocity = 6;

        this.radius = radius;

        speed = 10;

        number = value;

        //Trig! I looked this up on StackOverflow

        xVelocity = /*(int)*/(float) (getSpeed() * Math.cos(Math.toRadians(angle)));
        yVelocity =  /*(int)*/(float) -(getSpeed() * Math.sin(Math.toRadians(angle )));

        Log.d(VIEW_LOG_TAG, "INITAL: " + String.valueOf(x + ", " + y ));


    }

    public void update()
    {


        move();
    }

    public void draw(Canvas canvas, Paint paint)
    {
        //draw the circle(bubble)
        paint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawCircle(x, y, radius, paint);

        //draw the value of the number in the center of the circle(bubble)
        paint.setColor(Color.argb(100, 100, 100, 100));
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(number), x, y, paint);
    }


    public int getSpeed()
    {

        return speed;
    }

    public float getX() {

        return x;
    }

    public float getY() {

        return y;
    }

    void move()
    {
        x += xVelocity;
        y += yVelocity;
    }



    public int getValue()
    {

        return number;
    }

    //bounce the number by reversing its travel angle
    public void bounceWith(TouchableNumber collidingNum)
    {

        float tempX = xVelocity;
        float tempY = yVelocity;

        float x1velocity = tempX;
        float y1velocity = tempY;
        float x2velocity = collidingNum.getXVelocity();
        float y2velocity = collidingNum.getYVelocity();

        x1velocity  = x2velocity;
        x2velocity = tempX;

        y1velocity  = y2velocity;
        y2velocity = tempY;

        setXVelocity(x1velocity);
        setYVelocity(y1velocity);

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

    public void fixAngle()
    {
        angle = Math.toDegrees(Math.atan2(yVelocity, xVelocity));
    }

    public void setRadius(float newRad)
    {
        radius = newRad;
    }

}
