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

public class TouchableNumber
{

    //use bitmap when we add in our own images
    //private Bitmap bitmap;
    private int x, y, radius;
    private int speed;


    private static Random r = new Random();
    // A hit box for collision detection

    //the actual value of this number
    private int number;

    private int xVelocity, yVelocity;

    //the angle the number will travel at
    private int angle;



    // Constructor
    public TouchableNumber(Context context, int screenX, int screenY, int travelAngle)
    {
        x = screenX;
        y = screenY;
        angle = travelAngle;

        xVelocity = 0;
        yVelocity = 5;

        radius = 100;

        speed = 5;

        //make number random.
        //TODO, change constructor so we pass random number as argument so we can check if the
        //number is already on the screen before we generate it, this way we can prevent a bunch
        //of the same number appearing over and over
        number = r.nextInt(4) + 1;


    }

    public void update()
    {

        //Trig! I looked this up on StackOverflow

        xVelocity = (int) (getSpeed() * Math.cos(Math.toRadians(angle)));
        yVelocity =  (int) -(getSpeed() * Math.sin(Math.toRadians(angle )));

        move();
    }

    public void draw(Canvas canvas, Paint paint)
    {
        //draw the circle(bubble)
        paint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawCircle(x, y, radius, paint);

        //draw the value of the number in the center of the circle(bubble)
        paint.setColor(Color.argb(100, 100, 100, 100));
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(number), x, y, paint);
    }


    public int getSpeed()
    {

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



    public int getValue()
    {

        return number;
    }

    //bounce the number by reversing its travel angle
    public void bounce()
    {

        // Reverse the travelling angle
        if(angle >= 180)
            angle -= 180;
        else
            angle += 180;


        // Reverse velocity because occasionally they get stuck

        x -= (xVelocity);
        y -=(yVelocity);
    }

    public int getRadius()
    {
        return radius;
    }

}
