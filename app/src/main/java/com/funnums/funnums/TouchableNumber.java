package com.funnums.funnums;

/**
 * Created by austinbaird on 10/6/17.
 */



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.Random;

public class TouchableNumber {
    private Bitmap bitmap;
    private int x, y, radius;
    private int speed;

    private int shieldStrength;
    private boolean boosting;

    private final int GRAVITY = -12;

    // Stop ship leaving the screen
    private int maxY;
    private int minY;

    private final int MIN_SPEED = 1;
    private final int MAX_SPEED = 20;

    private static Random r = new Random();
    // A hit box for collision detection

    //the actual value of this number
    private int number;

    private int xVelocity, yVelocity;



    // Constructor
    public TouchableNumber(Context context, int screenX, int screenY) {
        boosting = false;
        x = screenX;
        y = screenY;

        xVelocity = 0;
        yVelocity = 5;

        radius = 100;

        shieldStrength = 10000;
        speed = 1;
        //bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ship);

        //maxY = screenY - bitmap.getHeight();
        minY = 0;

        number = r.nextInt(4) + 1;

        // Initialize the hit box
//        hitBox = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());

    }

    public void update() {

        // Don't let ship stray off screen
        /*if (y < minY) {
            y = minY;
        }
        if (y > maxY) {
            y = maxY;
        }*/

        // Refresh hit box location
        move();
    }

    public void draw(Canvas canvas, Paint paint)
    {
        paint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawCircle(x, y, radius, paint);


        paint.setColor(Color.argb(100, 100, 100, 100));
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(number), x, y, paint);
    }


    public void setBoosting() {

        boosting = true;
    }

    public void stopBoosting() {

        boosting = false;
    }

    //Getters
    public Bitmap getBitmap() {

        return bitmap;
    }

    public int getSpeed() {

        return speed;
    }

    public int getX() {

        return x;
    }

    public int getY() {

        return y;
    }

    void move()
    {
        x += xVelocity;
        y += yVelocity;
    }



    public int getValue() {

        return number;
    }

    public void reduceShieldStrength(){
        shieldStrength --;
    }

    public int getRadius()
    {
        return radius;
    }

}
