package com.funnums.funnums.classes;

import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.util.Log;
/**
 * Extends FloatingObject to mimic hot air balloon motion of bobbing up and down
 */

public class HotAirBalloon extends FloatingObject{


    //max and min altitudes the balloon will bob between
    float minY;
    float maxY;



    /*
        All hot air balloons bob at same speed in our game
     */
    public HotAirBalloon(int x, int y, Bitmap image){
        super(x, y, image);

        //make gravity positive so balloon falls to bottom of screen
        gravity = 0.05f;

        //set bounds of motion
        minY = y + 25;
        maxY = y - 25;
        //give specific velocity for a hot air balloon
        maxYVelocity = 0.2f;
        flyVelocity = -0.1f;

    }


    /*
        update the hot air balloon by applying gravity to its velocity
     */
    public void update(long deltaTime){
        //convert nanoseconds to seconds
        float delta = deltaTime*1.0f / com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS;
        //apply gravity to the owl's velocity, we can change gravity in OwlGame if needed
        addVelocity(0, gravity * delta);

        //check if balloon has fallen below min altitude, and make it bob upwards if it has
        if(y>minY &&gravity>0){
            addVelocity(0, flyVelocity * delta);;
        }
        move();

    }

}
