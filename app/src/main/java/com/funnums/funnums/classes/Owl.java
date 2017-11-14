package com.funnums.funnums.classes;

import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/**
 * An owl for the owl game.
 */

public class Owl {

    //coords
    float x, y;

    //maximum velocity that the owl will reach
    public float maxYVelocity;

    //acceleration, only y is used for now, might be nice to have x values just in case
    float xAcceleration, yAcceleration;
    public float xVelocity, yVelocity;

    //velocity the owl flies at when it increases altitude
    float flyVelocity;

    //size of the owl, jsust used to draw a circle for now
    int size;


    public float gravity;

    /*
        we can tweak the max velociy and fly velocity to make game more playable once this "owl"
        is integrated into the game
     */
    public Owl(int x, int y){
        this.x = x;
        this.y = y;

        maxYVelocity = 1;

        flyVelocity = 2;

        size = 50;

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
    }

    /*
        Right now, just draw a circle where the owl will be
     */
    public void draw(Canvas canvas, Paint paint){
        paint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawCircle(x, y, size, paint);
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

    public float getY(){
        return y;
    }

    public float getSize(){
        return size;
    }



}
