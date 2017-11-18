package com.funnums.funnums.classes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by metal on 11/18/2017.
 */

public class TilePlaceHolder{
    public float x, y;
    public float left, top, right, bottom;
    DraggableTile t;

    public TilePlaceHolder(float x, float y, float length){
        this.x = x;
        this.y = y;
        t = null;

        //distance of the left side of rectangular from left side of canvas.
        left = x;
        //Distance of bottom side of rectangle from the top side of canvas
        top = y;
        //distance of the right side of rectangular from left side of canvas.
        right = x + length;
        //Distance of the top side of rectangle from top side of canvas
        bottom = y + length;
    }

    public boolean isOccupied(){
        return (t != null);
    }

    public DraggableTile getTile(){
        return t;
    }

    public float getX(){ return x; }

    public float getY(){ return y; }

    public float getLeft(){ return left; }

    public float getTop(){ return top; }

    public float getRight(){ return right; }

    public float getBottom(){ return bottom; }

    public void setTile(DraggableTile t){
        this.t = t;
    }

    public void draw(Canvas canvas, Paint paint) {

        //draw the rectangle (tile space)
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(255, 0, 0, 0));
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setStyle(Paint.Style.FILL);

    }
}
