package com.funnums.funnums.classes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.funnums.funnums.minigames.OwlGame;

/**
 * Created by Cesar on 11/11/2017.
 */

public class DraggableTile extends DraggableObject{
    private String VIEW_LOG_TAG = "l";

    private String value;

    //Tile is marked as used if it part of the expression
    private boolean used;

    //Used as coordinates when drawing rectangle (Later on they can be erased, for now they make the code easier to read)
    float left, top, right, bottom;

    // Constructor:
    public DraggableTile(int x, int y, int tileLength,  String value) {

        super(x, y, tileLength);
        this.value = value;

        //distance of the left side of rectangular from left side of canvas.
        left = x;
        //Distance of bottom side of rectangle from the top side of canvas
        top = y;
        //distance of the right side of rectangular from left side of canvas.
        right = x + tileLength;
        //Distance of the top side of rectangle from top side of canvas
        bottom = y + tileLength;

        used = false;
    }

    public String getValue() {
        return value;
    }

    public boolean isUsed(){ return used; }

    public void setUsed(boolean used){ this.used = used; }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        //draw the rectangle (tile)
        paint.setColor(Color.argb(255, 245, 228, 118));
        canvas.drawRect(left, top, right, bottom, paint);

        //draw the value of the number in the center of the rectangle (tile)
        paint.setColor(Color.argb(100, 100, 100, 100));
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);

        //For now, I changed number to frac but in the future, we need to separate numbers and fractions
        canvas.drawText(value, x + (length/2), y + (length/2) + 10, paint);
    }

    @Override
    public void setXY(int x, int y) {
        super.setXY(x, y);

        //distance of the left side of rectangular from left side of canvas.
        left = x;
        //Distance of bottom side of rectangle from the top side of canvas
        top = y;
        //distance of the right side of rectangular from left side of canvas.
        right = x + length;
        //Distance of the top side of rectangle from top side of canvas
        bottom = y + length;
    }
}
