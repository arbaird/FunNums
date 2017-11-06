package com.funnums.funnums.classes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by austinbaird on 11/2/17.
 */

public class TouchableBalloon extends TouchableNumber {
    // Constructor
    private Fraction frac;

    public TouchableBalloon(int screenX, int screenY, int travelAngle, int radius, int speed, Fraction frac) {

        super(screenX, screenY, travelAngle, radius, speed);
        this.frac = frac;

    }


    public void draw(Canvas canvas, Paint paint) {
        //draw the circle(bubble)
        paint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawCircle(x, y, radius, paint);

        //draw the value of the number in the center of the circle(bubble)
        paint.setColor(Color.argb(255, 50, 50, 50));
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);

        //For now, I changed number to frac but in the future, we need to seperate numbers and fractions
        canvas.drawText(frac.toString(), x, y, paint);
    }

    //change this to return a fraction!
    public Fraction getValue() {
        return frac;
    }

}
