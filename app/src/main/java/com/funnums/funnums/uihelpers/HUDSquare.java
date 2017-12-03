package com.funnums.funnums.uihelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import android.util.Log;
import android.graphics.Bitmap;
/**
 * Squared used in minigame HUDs to display score information, time left, etc.
 */

public class HUDSquare {


    float TEXT_SIZE = 45;


    //space between label and value
    public int MARGIN = 10;

    //float specificTextDimension = 55;
    //Message for the square, and value, i.e "Score" "50"
    private String msg, value;

    //Used as coordinates when drawing rectangle (Later on they can be erased
    //for now they make the code easier to read)
    public float left, top, right, bottom;

    //coords and dimensions
    public float x, y, height, width;
    //scale of the text, so it fits insode the square
    public float xScale;

    //backdrop of the square
    public Rect backdrop;

    public Bitmap image;

    public HUDSquare(float x, float y, float width, float height, String msg, String value, Paint paint) {
        this.left = x;
        this.top = y;
        this.height = height;
        this.width = width;

        //calculate the bottom and right of the quare based on dimensions given
        bottom = y + height;
        right = x + width;
        //initialize the square drawn
        backdrop = new Rect((int)left,(int) top, (int)right,(int) bottom);

        //set message and value
        this.msg = msg;
        this.value = value;

        //adjust the text size and scale
        adjustTextSize(paint, height, msg);
        adjustTextScale(paint, height, msg);

        this.image = com.funnums.funnums.maingame.GameView.loadBitmap("Shared/HUD element.png", false);
        this.image = Bitmap.createScaledBitmap(image, (int)width, (int)height ,false);


    }


        //Getter methods
    public String getValue() {
        return value;
    }

    public float getLeft(){ return left; }

    public float getTop(){ return top; }

    public float getRight(){ return right; }

    public void setValue(String newVal){
        value = newVal;
    }


    /*
        draw the square with message and value
     */
    public void draw(Canvas canvas, Paint paint, String val){

        canvas.drawBitmap(image, left, top,  paint);

        //prepare the text
        paint.setColor(Color.argb(255, 0, 0, 0));
        paint.setTextSize(TEXT_SIZE);
        paint.setTextScaleX(xScale);
        paint.setTextAlign(Paint.Align.CENTER);
        //draw the message and value underneath the message
        canvas.drawText(msg, (left + right)/2 , (top + bottom)/2 - height/8, paint);
        canvas.drawText(val, (left + right)/2 , bottom -  height/8, paint);


    }

    /*
        Adjust text size, in Y direction
     */
    void adjustTextSize(Paint paint, float height, String text) {
        //start with large size
        paint.setTextSize(100);
        paint.setTextScaleX(1.0f);
        Rect bounds = new Rect();
        // ask the paint for the bounding rect if it were to draw this large text
        paint.getTextBounds(text, 0, text.length(), bounds);
        // get the height that would have been produced
        int h = (bounds.bottom - bounds.top)*2;
        // make the text text up 80% of the height
        float target = (float)height *.8f;
        // figure out what textSize setting would create that height of text
        float size = ((target/h)*100f);
        // and set it into the paint
        paint.setTextSize(size);
        TEXT_SIZE = size;
    }

    /*
        Adjust the scale of the Text in X direction
     */
    void adjustTextScale(Paint paint, float height, String text) {
        // do calculation with scale of 1.0 (no scale)
        paint.setTextScaleX(1.0f);
        Rect bounds = new Rect();
        // ask the paint for the bounding rect if it were to draw this text.
        paint.getTextBounds(text, 0, text.length(), bounds);
        // determine the width
        int w = bounds.right - bounds.left;
        // determine how much to scale the width to fit the view
        float xscale = width / w;
        // set the scale for the text paint
        paint.setTextScaleX(xscale);
        //set the scale
        this.xScale = xscale;
    }
}
