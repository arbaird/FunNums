package com.funnums.funnums.classes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.funnums.funnums.animation.Animation;
import com.funnums.funnums.animation.Frame;

/**
 * Created by austinbaird on 11/2/17.
 */

public class TouchableBalloon extends TouchableNumber {
    // Constructor
    private Fraction frac;

    public Animation anim;

    public Bitmap bottom;

    public boolean popping;

    public float xRadius, yRadius;

    public TouchableBalloon(int screenX, int screenY, int travelAngle, int xRadius,int yRadius, int speed, Fraction frac) {

        super(screenX, screenY, travelAngle, xRadius, speed);
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.frac = frac;

        initAnim();
    }


    public void draw(Canvas canvas, Paint paint) {
        //draw the circle(bubble)
        paint.setColor(Color.argb(255, 255, 255, 255));
        //canvas.drawCircle(x, y, radius, paint);

        //draw the value of the number in the center of the circle(bubble)
        /*paint.setColor(Color.argb(255, 50, 50, 50));
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);*/



        //convert to coords that are positioned correctly when drawn.
        int drawX = (int)x -(int)xRadius;
        int drawY = (int)y -(int)yRadius;
        //scale the image to be the length and width of the diameter of the bubble
        int xDiameter = (int)xRadius*2;
        int yDiameter = (int)yRadius*2;
        //takes x, y coords, then the length and width to scale the image to
        anim.render(canvas, drawX, drawY, paint);//, xDiameter, yDiameter);

        if(!popping) {
            //draw the value of the number in the center of the circle(bubble)
            paint.setTextSize(40);
            paint.setTextAlign(Paint.Align.CENTER);


            canvas.drawText(frac.toString(), x, y, paint);
            canvas.drawBitmap(bottom,x-xRadius, y+(yRadius*24/32), paint);
        }

    }

    //change this to return a fraction!
    public Fraction getValue() {
        return frac;
    }

    /*
       prepare the animation needed for bubble collisions. we can add additional animation for popping as well.
       right now, there is an alien thing inside each bubble that starts a running animation every time
       a bubble collides
    */
    private void initAnim(){
        //get each image for animation
        Bitmap run1 = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/Balloon1.png", false);
        run1= Bitmap.createScaledBitmap(run1, (int) xRadius*2, (int)yRadius*2 ,false);
        bottom  = com.funnums.funnums.maingame.GameView.loadBitmap("BalloonGame/BalloonTail.png", false);
        bottom = Bitmap.createScaledBitmap(bottom, (int) xRadius*2, (int)yRadius*2 ,false);
        //create Frame objects for each frame in animation
        Frame f1 = new Frame(run1, .1f);

        //create animation object
        anim = new Animation(f1);
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
