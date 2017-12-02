package com.funnums.funnums.classes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.funnums.funnums.animation.*;

/**
 * Extends touchable number to more closely imitate a moving bubble
 */

public class TouchableBubble extends TouchableNumber {
    private String VIEW_LOG_TAG = "l";

    //flag indicating if bubble is currently popping
    public boolean popping = false;
    //value inside the bubble
    private int value;
    //animation for collisions
    public Animation anim;

    // Constructor
    public TouchableBubble(int screenX, int screenY, int travelAngle, int radius, int speed, int value) {

        super(screenX, screenY, travelAngle, radius, speed);
        this.value = value;

        initAnim();

    }

    /*
        prepare the animation needed for bubble collisions. we can add additional animation for popping as well.
        right now, there is an alien thing inside each bubble that starts a running animation every time
        a bubble collides
     */
    private void initAnim(){
        //get each image for animation
        Bitmap run1 = com.funnums.funnums.maingame.GameView.loadBitmap("BubbleTestmdpi.png", false);
        Bitmap run2 = com.funnums.funnums.maingame.GameView.loadBitmap("Bubble small right ripplemdpi.png", false);
        Bitmap run3 = com.funnums.funnums.maingame.GameView.loadBitmap("Bubble right ripplemdpi.png", false);
        Bitmap run4 = com.funnums.funnums.maingame.GameView.loadBitmap("Bubble right ripplemdpi.png", false);

        //create Frame objects for each frame in animation
        Frame f1 = new Frame(run1, .1f);
        Frame f4 = new Frame(run2, .1f);
        Frame f5 = new Frame(run3, .1f);
        Frame f6 = new Frame(run4, .1f);
        //create animation object
        anim = new Animation(f1, f4, f5, f6);
    }

    public int getValue() {
        return value;
    }

    public void update(long delta){
        if(!popping)
            super.update();
        anim.update(delta);
    }

    /*
        update bouncing bubble physics and also start animation for the bubbles that collided
     */
    public void bounceWith(TouchableBubble collidingNum){
        if(popping || collidingNum.popping)
            return;

        super.bounceWith(collidingNum);
        animateCollision();
        collidingNum.animateCollision();
    }

    /*
        play collision, or restart it if it is already playing
     */
    private void animateCollision(){
        if(anim.playing)
            anim.restart();
        else
            anim.start();
    }

    public void pop(){
        Bitmap run1 = com.funnums.funnums.maingame.GameView.loadBitmap("Bubble pop larger groupingmdpi.png", false);
        Bitmap run2 = com.funnums.funnums.maingame.GameView.loadBitmap("Bubble pop smaller groupingmdpi.png", false);
        //create Frame objects for each frame in animation
        Frame f1 = new Frame(run1, .1f);
        Frame f2 = new Frame(run2, .1f);
        //create animation object
        anim = new Animation(f1, f2);
        anim.start();
        //set flag to indicate this bubble is currently popping
        popping = true;
    }


    public void draw(Canvas canvas, Paint paint) {

        paint.setColor(Color.argb(255, 255, 255, 255));


        //convert to coords that are positioned correctly when drawn.
        int drawX = (int)x -(int)radius;
        int drawY = (int)y -(int)radius;
        //scale the image to be the length and width of the diameter of the bubble
        int diameter = (int)radius*2;

        //takes x, y coords, then the length and width to scale the image to
        anim.render(canvas, paint, drawX, drawY, diameter, diameter);

        if(!popping) {
            //draw the value of the number in the center of the circle(bubble)
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);

            canvas.drawText(String.valueOf(value), x, y, paint);
        }
    }


}
