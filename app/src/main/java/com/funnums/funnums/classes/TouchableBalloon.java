package com.funnums.funnums.classes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.Random;

import com.funnums.funnums.animation.Animation;
import com.funnums.funnums.animation.Frame;

/**
 * Extends TouchableNumber to more closely mimic a floating balloon
 */

public class TouchableBalloon extends TouchableNumber {
    // Fraction value for the balloon
    private Fraction frac;
    //animation for balloon
    public Animation anim;
    //the bottom of balloon(the tail), drawn separately to make checking the balloon coords more simple
    public Bitmap bottom;
    //flag indicating if balloon is currently being pooped
    public boolean popping;
    //x and y radius, to allow ellipse shape
    public float xRadius, yRadius;

    //the color of the next balloon, used to load a balloon image of different colors
    public static int color = 1;

    public TouchableBalloon(int screenX, int screenY, int travelAngle, int xRadius,int yRadius, int speed, Fraction frac) {

        super(screenX, screenY, travelAngle, xRadius, speed);
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.frac = frac;

        initAnim();
    }


    public void draw(Canvas canvas, Paint paint) {

        paint.setColor(Color.argb(255, 255, 255, 255));

        //convert to coords that are positioned correctly when drawn.
        int drawX = (int)x -(int)xRadius;
        int drawY = (int)y -(int)yRadius;

        //takes x, y coords, then the length and width to scale the image to
        anim.render(canvas, drawX, drawY, paint);

        //if balloon is popping, draw the fraction value
        if(!popping) {
            //draw the value of the number in the center of the circle(bubble)
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);

            canvas.drawText(frac.toString(), x, y, paint);
            //position the fraction to be drawn
            canvas.drawBitmap(bottom,x-xRadius, y+(yRadius*3/4), paint);
        }

    }

    //change this to return a fraction!
    public Fraction getValue() {
        return frac;
    }

    /*
       prepare the animations needed for balloons. we can add additional animation for popping as well.
    */
    private void initAnim(){
        //get number from 1-3 to load new color every time new balloon is generated
        int num = ((color++)%3) +1;

        //get each image for top of balloon
        Bitmap top = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/Balloon" +num+".png", false);
        top= Bitmap.createScaledBitmap(top, (int) xRadius*2, (int)yRadius*2 ,false);
        //get image for bottom of balloon
        bottom  = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/BalloonTail"+num+".png", false);
        bottom = Bitmap.createScaledBitmap(bottom, (int) xRadius*2, (int)yRadius*2 ,false);
        //only one frame for now, no animation, but keep this set up in case we want to add an animation later
        Frame f1 = new Frame(top, .1f);
        //create animation object
        anim = new Animation(f1);
    }

    public void pop(){
        //initialize animation for popping
        Bitmap run1 = com.funnums.funnums.maingame.GameView.loadBitmap("Bubble pop larger groupingmdpi.png", false);
        Bitmap run2 = com.funnums.funnums.maingame.GameView.loadBitmap("Bubble pop smaller groupingmdpi.png", false);
        //create Frame objects for each frame in animation
        Frame f1 = new Frame(run1, .1f);
        Frame f2 = new Frame(run2, .1f);
        //create animation object
        anim = new Animation(f1, f2);
        anim.start();
        //set flag to indicate this balloon is currently popping
        popping = true;
    }

    public void update(long delta){
        if(!popping)
            super.update();
        anim.update(delta/**1.0f/ com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS*/);
    }

    /*
        update bouncing balloon physics and also start animation for the bubbles that collided
     */
    public void bounceWith(TouchableBalloon collidingNum) {
        if (popping || collidingNum.popping)
            return;

        super.bounceWith(collidingNum);
    }


}
