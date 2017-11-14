package com.funnums.funnums.classes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.funnums.funnums.animation.*;

/**
 * Created by austinbaird on 11/2/17.
 */

public class TouchableBubble extends TouchableNumber {
    private String VIEW_LOG_TAG = "l";


    private int value;
    private Animation anim;

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
        Bitmap run1 = com.funnums.funnums.maingame.GameView.loadBitmap("BubbleTestmdpi.png", true);
        /*Bitmap run2 = com.funnums.funnums.maingame.GameView.loadBitmap("run_anim2.png", true);
        Bitmap run3 = com.funnums.funnums.maingame.GameView.loadBitmap("run_anim3.png", true);
        Bitmap run4 = com.funnums.funnums.maingame.GameView.loadBitmap("run_anim4.png", true);
        Bitmap run5 = com.funnums.funnums.maingame.GameView.loadBitmap("run_anim5.png", true);*/
        //create Frame objects for each frame in animation
        Frame f1 = new Frame(run1, .1f);
        /*Frame f2 = new Frame(run2, .1f);
        Frame f3 = new Frame(run3, .1f);
        Frame f4 = new Frame(run4, .1f);
        Frame f5 = new Frame(run5, .1f);*/
        //create animation object
        anim = new Animation(f1);
    }

    public int getValue() {
        return value;
    }

    public void update(long delta){
        super.update();
        anim.update(delta*1.0f/ com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS);
    }

    /*
        update bouncing bubble physics and also start animation for the bubbles that collided
     */
    public void bounceWith(TouchableBubble collidingNum){
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


    public void draw(Canvas canvas, Paint paint) {
        //draw the circle(bubble)
        paint.setColor(Color.argb(255, 255, 255, 255));
        //canvas.drawCircle(x, y, radius, paint);


        //convert to coords that are positioned correctly when drawn.
        int drawX = (int)x -(int)radius;
        int drawY = (int)y -(int)radius;
        //scale the image to be the length and width of the diameter of the bubble
        int diameter = (int)radius*2;

        //takes x, y coords, then the length and width to scale the image to
        anim.render(canvas, paint, drawX, drawY, diameter, diameter);

        //draw the value of the number in the center of the circle(bubble)
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);


        canvas.drawText(String.valueOf(value), x, y, paint);
    }


}
