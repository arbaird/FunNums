package com.funnums.funnums.classes;

import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.util.Log;
/**
 * An owl for the owl game.
 */

public class FloatingObject {

    //coords
    float x, y;

    float minY;
    float maxY;

    //maximum velocity that the owl will reach
    public float maxYVelocity;

    //velocity, only y is used for now, might be nice to have x values just in case
    public float xVelocity, yVelocity;

    //velocity the owl flies at when it increases altitude
    public float flyVelocity;

    //size of the owl, jsust used to draw a circle for now
    int size;


    public float gravity;

    public Bitmap image;

    /*
        we can tweak the max velociy and fly velocity to make game more playable once this "owl"
        is integrated into the game
     */
    public FloatingObject(int x, int y, Bitmap image){
        this.x = x;
        this.y = y;

        maxYVelocity = 0.1f;

        flyVelocity = -0.1f;

        size = 100;

        gravity = -0.05f;

        this.image = image;
    }

    public FloatingObject (int x, int y, float flyVelocity, float gravity){
        this.x = x;
        this.y = y;

        maxYVelocity = 0.5f;

        flyVelocity = 2.0f;

        size = 100;

        gravity = 0.5f;
    }

    /*
        Add velocity to the owl. Makes sure velocity does not exceed the max velocity
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
        update the owl by applying gravity to its velocity
     */
    public void update(long deltaTime){
        //convert nanoseconds to seconds
        float delta = deltaTime*1.0f / com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS;
        //apply gravity to the owl's velocity, we can change gravity in OwlGame if needed
        addVelocity(0, gravity * delta);
        move();
        //Log.d("HOTAIR", y + " " + minY + " " +maxY + " " + gravity);

    }

    /*
        Right now, just draw a circle where the owl will be
     */
    public void draw(Canvas canvas, Paint paint){
        //paint.setColor(Color.argb(255, 100, 100, 100));
        canvas.drawBitmap(image, (int)x, (int)y, paint);
    }

    /*
        Move the owl
     */
    void move() {
        x += xVelocity;
        y += yVelocity;
    }

    /*
        Call when you want the owl to increase altitude
     */
    public void increaseAltitude(){
        addVelocity(0, -flyVelocity);
    }

    public void increaseFlyVelocity(float addVelocity){
        flyVelocity += addVelocity;
    }

    public float getFlyVelocty(){
        return flyVelocity;
    }

    public float getY(){
        return y;
    }

    public float getSize(){
        return size;
    }

}
