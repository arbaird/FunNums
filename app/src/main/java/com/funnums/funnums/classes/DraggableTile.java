package com.funnums.funnums.classes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by Cesar on 11/11/2017.
 *
 * DraggableTile
 */

public class DraggableTile extends DraggableObject{
    private final String TAG = "DraggableTile";
    //Font size inside tile
    float TEXT_SIZE = 50;

    private String value;           /*Character/variable/operand inside the tile*/
    private boolean used;           /*//Tile is marked as used if it part of the expression*/
    private boolean isOperator;

    //Used as coordinates when drawing rectangle (Later on they can be erased
    //for now they make the code easier to read)
    float left, top, right, bottom;

    // Constructor:
    public DraggableTile(float x, float y, float tileLength,  String value) {

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
        isOperator = false;
    }

    //Getter methods
    public String getValue() {
        return value;
    }

    public boolean isUsed(){ return used; }

    public float getLeft(){ return left; }

    public float getTop(){ return top; }

    public float getRight(){ return right; }

    public float getBottom(){ return bottom; }

    //Setter methods
    public void setUsed(boolean used){ this.used = used; }

    //Setter methods
    public void setIsOperator(boolean isOperator){ this.isOperator = isOperator; }

    //Abstract overloaded methods
    @Override
    public void setXY(float x, float y) {
        super.setXY(x, y);

        //distance of the left side of rectangular from left side of canvas.
        left = x;
        //Distance of bottom side of rectangle from the top side of canvas
        top = y;
        //distance of the right side of rectangular from left side of canvas.
        right = x + length;
        //Distance of the bottom side of rectangle from top side of canvas
        bottom = y + length;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {


        //draw the rectangle (tile)
        paint.setColor(Color.argb(255, 245, 228, 118));
        //Different color for operators
        if (isOperator) paint.setColor(Color.argb(255, 245, 191, 0));
        canvas.drawRect(left, top, right, bottom, paint);

        //draw the value of the number in the center of the rectangle (tile)
        paint.setColor(Color.argb(100, 0, 0, 0));
        paint.setTextSize(TEXT_SIZE);
        paint.setTextAlign(Paint.Align.CENTER);

        //Draw Tile text
        canvas.drawText(value, getX() + (length / 2), getY() + (length / 2) + (TEXT_SIZE / 2), paint);
    }


}
