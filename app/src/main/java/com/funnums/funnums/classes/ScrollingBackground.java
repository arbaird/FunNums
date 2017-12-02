package com.funnums.funnums.classes;

/**
 * Created by austinbaird on 12/1/17.
 */

import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class ScrollingBackground {

    Bitmap bitmap;
    Bitmap bitmapReversed;

    int width;
    int height;
    boolean reversedFirst;
    float speed;

    float xClip;
    int startY;
    int endY;

    int fps = 1;

    public ScrollingBackground(int screenWidth, int screenHeight, String bitmapName, int sY, int eY, float s) {

        // Load the bitmap using the fileName
        bitmap = com.funnums.funnums.maingame.GameView.loadBitmap(bitmapName, false);

        // Which version of background (reversed or regular)
        // is currently drawn first (on left)
        reversedFirst = false;

        //Initialise animation variables.

        // Where to clip the bitmaps
        // Starting at the first pixel
        xClip = 0;

        //Position the background vertically
        startY = sY; //* (screenHeight / 100);
        endY = eY;// * (screenHeight / 100);
        speed = s;

        // Create the bitmap
        bitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, (endY - startY), true);

        // Save the width and height for later use
        width = bitmap.getWidth();
        height = bitmap.getHeight();



        //Create a mirror image of the background (horizontal flip)
        Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        bitmapReversed = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);


    }

    public void update(){

        // Move the clipping position and reverse if necessary
        xClip -= speed;
        if (xClip >= width) {
            xClip = 0;
            reversedFirst = !reversedFirst;
        } else if (xClip <= 0) {
            xClip = width;
            reversedFirst = !reversedFirst;

        }
    }

    public void draw(Canvas canvas, Paint paint) {

        // define what portion of images to capture and
        // what coordinates of screen to draw them at

        // For the regular bitmap
        Rect fromRect1 = new Rect(0, 0, (int)(width - xClip), height);
        Rect toRect1 = new Rect((int)xClip, startY, width, endY);

        // For the reversed background
        Rect fromRect2 = new Rect((int)(width - xClip), 0, width, height);
        Rect toRect2 = new Rect(0, startY, (int)xClip, endY);

        //draw the two background bitmaps
        if (!reversedFirst) {
            canvas.drawBitmap(bitmap, fromRect1, toRect1, paint);
            canvas.drawBitmap(bitmapReversed, fromRect2, toRect2, paint);
        } else {
            canvas.drawBitmap(bitmap, fromRect2, toRect2, paint);
            canvas.drawBitmap(bitmapReversed, fromRect1, toRect1, paint);
        }

    }
}
