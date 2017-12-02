package com.funnums.funnums.classes;

import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.util.Log;
/**
 * An Object that floats to the top of the screen
 */

public class FloatingObject {

    //coords
    float x, y;

    //maximum velocity that the object will reach
    public float maxYVelocity;

    //velocity in x and y direction
    public float xVelocity, yVelocity;

    //velocity the object flies at when it increases altitude
    public float flyVelocity;


    public float gravity;

    public Bitmap image;

    /*
        constructor with hardcoded, default values for gravity and velocity
     */
    public FloatingObject(int x, int y, Bitmap image){
        this.x = x;
        this.y = y;

        maxYVelocity = 0.1f;
        flyVelocity = -0.1f;

        gravity = -0.05f;

        this.image = image;
    }

    public FloatingObject (int x, int y, float flyVelocity, float gravity){
        this.x = x;
        this.y = y;

        maxYVelocity = 0.5f;

        this.flyVelocity = flyVelocity;


        this.gravity = gravity;
    }

    /*
        Add velocity to the object. Makes sure velocity does not exceed the max velocity
    */
    public void addVelocity(float x, float y){
        xVelocity += x;
        yVelocity += y;

        //see if velocity is too high and, if it is, set velocity to max value in the correct direction
        //i.e detect if it reached max ascending or descending
        if (Math.abs(yVelocity) > maxYVelocity){
            //set max falling velocity, but flying altitude can be greater than max so owl can fly higher as equations get harder
            if(yVelocity > 0)
                yVelocity = maxYVelocity;
            else
                yVelocity = -maxYVelocity;
        }
    }

    /*
        update the object by applying gravity to its velocity
     */
    public void update(long deltaTime){
        //convert nanoseconds to seconds
        float delta = deltaTime*1.0f / com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS;
        //apply gravity to the owl's velocity, we can change gravity in OwlGame if needed
        addVelocity(0, gravity * delta);
        move();
    }

    /*
        Draw the object
     */
    public void draw(Canvas canvas, Paint paint){
        canvas.drawBitmap(image, (int)x, (int)y, paint);
    }

    /*
        Move the object
     */
    void move() {
        x += xVelocity;
        y += yVelocity;
    }

    public float getY(){
        return y;
    }


}
