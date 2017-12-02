package com.funnums.funnums.classes;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import com.funnums.funnums.animation.*;
/**
 * An owl for the owl game.
 */

public class Owl {

    //coords
    float x, y;

    //maximum velocity that the owl will reach
    public float maxYVelocity;

    //velocity, only y is used for now, might be nice to have x values just in case
    public float xVelocity, yVelocity;

    //velocity the owl flies at when it increases altitude
    public float flyVelocity;

    //size of the owl, jsust used to draw a circle for now
    Animation anim;


    public float gravity;

    /*
        only one owl in game, so all its velocity info is hardcoded
     */
    public Owl(int x, int y){

        this.y = y;

        maxYVelocity = 0.5f;
        flyVelocity = 2f;


        gravity = 0.5f;

        initAnim();

        this.x = x - getWidth()/2;
    }

    private void initAnim(){
        //get each image for animation
        Bitmap run1 = com.funnums.funnums.maingame.GameView.loadBitmap("OwlGame/OwlWingUp.png", false);
        Bitmap run2 = com.funnums.funnums.maingame.GameView.loadBitmap("OwlGame/OwlWingOut.png", false);
        Bitmap run3 = com.funnums.funnums.maingame.GameView.loadBitmap("OwlGame/OwlWingDown.png", false);
        Bitmap run4 = com.funnums.funnums.maingame.GameView.loadBitmap("OwlGame/OwlWingOut.png", false);



        //create Frame objects for each frame in animation
        Frame f1 = new Frame(run1, .1f);
        Frame f2 = new Frame(run2, .1f);
        Frame f3 = new Frame(run3, 2f);
        Frame f4 = new Frame(run4, .1f);
        //create animation object
        anim = new Animation(f1, f2, f3, f4);
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
        }
    }

    /*
        update the owl by applying gravity to its velocity
     */
    public void update(long deltaTime){
        //convert nanoseconds to seconds
        anim.update(deltaTime);
        float delta = deltaTime*1.0f / com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS;

        //apply gravity to the owl's velocity, we can change gravity in OwlGame if needed
        addVelocity(0, gravity * delta);

        move();
        correctPos();
    }

    /*
        Right now, just draw a circle where the owl will be
     */
    public void draw(Canvas canvas, Paint paint){
        //paint.setColor(Color.argb(255, 100, 100, 100));
        anim.render(canvas, (int)x, (int)y,paint);
    }

    /*
        Move the owl
     */
    void move() {
        x += xVelocity;
        y += yVelocity;
    }

    void correctPos(){
        if(y + getSize()/2 < 0){
            y -= yVelocity;
        }

    }

    /*
        Call when you want the owl to increase altitude
     */
    public void increaseAltitude(){
        addVelocity(0, -flyVelocity);
        anim.start();
        Log.d("OWL", "FLY!");
    }

    public int getSize(){
        return anim.getCurrentBitmap().getHeight();
    }

    public float getY(){
        return y;
    }

    public int getWidth(){
        return anim.getCurrentBitmap().getWidth();
    }
}
