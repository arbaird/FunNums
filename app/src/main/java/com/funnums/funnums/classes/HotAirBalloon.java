package com.funnums.funnums.classes;

import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.util.Log;
/**
 * An owl for the owl game.
 */

public class HotAirBalloon extends FloatingObject{



    float minY;
    float maxY;



    /*
        we can tweak the max velociy and fly velocity to make game more playable once this "owl"
        is integrated into the game
     */
    public HotAirBalloon(int x, int y, Bitmap image){
        super(x, y, image);


        gravity = 0.05f;

        minY = y + 25;
        maxY = y - 25;

        maxYVelocity = 0.2f;

        flyVelocity = -0.1f;

    }


    /*
        update the owl by applying gravity to its velocity
     */
    public void update(long deltaTime){
        //convert nanoseconds to seconds
        float delta = deltaTime*1.0f / com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS;
        //apply gravity to the owl's velocity, we can change gravity in OwlGame if needed
        addVelocity(0, gravity * delta);

        /*if((y>minY &&gravity>0)||(y<maxY && gravity <0)){
            gravity = -gravity;
            yVelocity = 0;
        }*/
        if(y>minY &&gravity>0){
            addVelocity(0, flyVelocity * delta);;
        }
        move();
        //Log.d("HOTAIR", y + " " + minY + " " +maxY + " " + gravity);

    }

}
