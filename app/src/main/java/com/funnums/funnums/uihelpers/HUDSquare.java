package com.funnums.funnums.uihelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import android.util.Log;
/**
 * Created by austinbaird on 11/19/17.
 */

public class HUDSquare {


    float TEXT_SIZE = 45;

    int BORDER = 10;
    public int MARGIN = 10;
    int OFFSET = 50;

    //float specificTextDimension = 55;

    private String msg, value;           /*Character/variable/operand inside the tile*/
    private boolean used;           /*//Tile is marked as used if it part of the expression*/

    //Used as coordinates when drawing rectangle (Later on they can be erased
    //for now they make the code easier to read)
    public float left, top, right, bottom;

    public float x, y, height, width;
    public float xScale;

    public Rect backdrop;
    // Constructor:
    public HUDSquare(float x, float y,  String msg, String value, Paint paint) {

        //float tileLength = (msg.length() * specificTextDimension)/2;

        this.x = x;
        this.y = y-MARGIN;
        //this.length = tileLength;
        this.msg = msg;
        this.value = value;

        //distance of the left side of rectangular from left side of canvas.
        left = x;//-tileLength/2;
        //Distance of bottom side of rectangle from the top side of canvas
        top = this.y-TEXT_SIZE/2 - MARGIN;
        //distance of the right side of rectangular from left side of canvas.
        right = x; //+ tileLength/2;
        //Distance of the top side of rectangle from top side of canvas
        bottom = this.y + TEXT_SIZE*2 - MARGIN- BORDER - BORDER/2;

        used = false;


        Rect bounds = new Rect();
        Rect b = new Rect();
        String strWidth = "";
        for(int i = 0; i < msg.length(); i++)
            strWidth += 'c';

        paint.setTextSize(TEXT_SIZE);
        paint.getTextBounds(msg, 0, msg.length(), bounds);
        paint.getTextBounds("0", 0, 1, b);

        right = x + bounds.width()/2;
        bottom = y + b.height() + b.height() - MARGIN - MARGIN;
        left = x -  bounds.width()/2;
    }

    public HUDSquare(float x, float y, float width, float height, String msg, String value, Paint paint) {
        left = x;
        top = y;

        this.height = height;
        this.width = width;

        bottom = y + height /*- BORDER/2*/;
        right = x + width;
        backdrop = new Rect((int)left,(int) top, (int)right,(int) bottom);

        this.msg = msg;
        this.value = value;

        adjustTextSize(paint, height, msg);
        adjustTextScale(paint, height, msg);

    }

        //Getter methods
    public String getValue() {
        return value;
    }

    public boolean isUsed(){ return used; }

    public float getLeft(){ return left; }

    public float getTop(){ return top; }

    public float getRight(){ return right; }

    public void setValue(String newVal){
        value = newVal;
    }

    //Setter methods
    public void setUsed(boolean used){ this.used = used; }


    public void drawBetter(Canvas canvas, Paint paint, String val){
        // border
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);
        canvas.drawRect(left, top, right, bottom, paint);

        // fill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(left, top, right, bottom, paint);

        paint.setColor(Color.argb(255, 255, 0, 0));
        paint.setTextSize(TEXT_SIZE);
        paint.setTextScaleX(xScale);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(msg, (left + right)/2 , (top + bottom)/2 - height/8, paint);
        canvas.drawText(val, (left + right)/2 , bottom -  height/8, paint);


    }

    public void draw(Canvas canvas, Paint paint, String val) {


        // border
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);
        canvas.drawRect(left, top, right, bottom, paint);




        // fill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(left, top, right, bottom, paint);

        //Draw Current
        paint.setColor(Color.argb(255, 255, 0, 0));
        paint.setTextSize(TEXT_SIZE);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(msg, (left + right)/2 , y + MARGIN, paint);
        canvas.drawText(val, (left + right)/2 , y+OFFSET + MARGIN, paint);


        //draw the rectangle (tile)
        //paint.setColor(Color.argb(255, 245, 228, 118));
        //canvas.drawRect(left, top, right, bottom, paint);

        //draw the value of the number in the center of the rectangle (tile)
        //paint.setColor(Color.argb(100, 100, 100, 100));
        //paint.setTextSize(TEXT_SIZE);
        //paint.setTextAlign(Paint.Align.CENTER);

        //Draw Tile text
        //canvas.drawText(value, x + (length / 2), y + (length / 2) + (TEXT_SIZE / 2), paint);
    }

    public void drawBetterNoLabel(Canvas canvas, Paint paint, String val){
        // border
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);
        canvas.drawRect(left, top, right, bottom-MARGIN*2, paint);




        // fill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(left, top, right, bottom-MARGIN*2, paint);

        //Draw Current
        paint.setColor(Color.argb(255, 255, 0, 0));
        paint.setTextSize(TEXT_SIZE);
        paint.setTextScaleX(xScale);
        paint.setTextAlign(Paint.Align.CENTER);
        //canvas.drawText(val, (left + right)/2 , y+OFFSET + MARGIN, paint);
        canvas.drawText(val, (left + right)/2 , (top + bottom)/2, paint);
    }

    void adjustTextSize(Paint paint, float height, String text) {
        paint.setTextSize(100);
        paint.setTextScaleX(1.0f);
        Rect bounds = new Rect();
        // ask the paint for the bounding rect if it were to draw this
        // text
        paint.getTextBounds(text, 0, text.length(), bounds);
        // get the height that would have been produced
        int h = (bounds.bottom - bounds.top)*2;
        // make the text text up 70% of the height
        float target = (float)height *.8f;
        // figure out what textSize setting would create that height
        // of text
        float size = ((target/h)*100f);
        // and set it into the paint
        paint.setTextSize(size);
        TEXT_SIZE = size;
    }

    void adjustTextScale(Paint paint, float height, String text) {
        // do calculation with scale of 1.0 (no scale)
        paint.setTextScaleX(1.0f);
        Rect bounds = new Rect();
        // ask the paint for the bounding rect if it were to draw this
        // text.
        paint.getTextBounds(text, 0, text.length(), bounds);
        // determine the width
        int w = bounds.right - bounds.left;
        // calculate the baseline to use so that the
        // entire text is visible including the descenders
        int text_h = bounds.bottom-bounds.top;
        //mTextBaseline=bounds.bottom+((height-text_h)/2);
        // determine how much to scale the width to fit the view
        float xscale = ((float) (width/*-getPaddingLeft()-getPaddingRight()*/)) / w;
        // set the scale for the text paint
        paint.setTextScaleX(xscale);

        this.xScale = xscale;
        Log.d("Scale", ""+ xScale);
    }
}
