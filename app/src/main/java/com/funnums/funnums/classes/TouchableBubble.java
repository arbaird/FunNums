package com.funnums.funnums.classes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.Random;

/**
 * Created by austinbaird on 11/2/17.
 */

public class TouchableBubble extends TouchableNumber {
    private String VIEW_LOG_TAG = "l";


    private int value;

    // Constructor
    public TouchableBubble(int screenX, int screenY, int travelAngle, int radius, int speed, int value) {

        super(screenX, screenY, travelAngle, radius, speed);
        this.value = value;

    }

    public int getValue() {
        return value;
    }


    public void draw(Canvas canvas, Paint paint) {
        //draw the circle(bubble)
        paint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawCircle(x, y, radius, paint);

        //draw the value of the number in the center of the circle(bubble)
        paint.setColor(Color.argb(100, 100, 100, 100));
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);

        //For now, I changed number to frac but in the future, we need to seperate numbers and fractions
        canvas.drawText(String.valueOf(value), x, y, paint);
    }


}
