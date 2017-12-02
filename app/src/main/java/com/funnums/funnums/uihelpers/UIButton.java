package com.funnums.funnums.uihelpers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Paint;

/*
    Adapted from James Cho's "Beginner's Guide to Android Game Development
    Buttons we can use inside the game's SurfaceView, since we cannot use Android buttons inside
    a SurfaceView
 */
public class UIButton {

    //region of the screen corresponding to where this button can be touched
    private Rect buttonRect;
    private boolean buttonDown = false;
    //imaged for when the button is pressed and not pressed
    private Bitmap buttonImage, buttonDownImage;

    public UIButton(int left, int top, int right, int bottom,
                    Bitmap buttonImage, Bitmap buttonPressedImage) {
        //create dimensions for the button touch region
        buttonRect = new Rect(left, top, right, bottom);
        //set the images for the button
        this.buttonImage = buttonImage;
        this.buttonDownImage = buttonPressedImage;
    }

    public int getWidth()
    {
        return buttonImage.getWidth();
    }

    /*
        Set new touch area
     */
    public void setRect(int left, int top) {
        buttonRect = new Rect(left, top, left + buttonImage.getWidth(),top + buttonImage.getHeight());
    }

    /*
        Draw the button
     */
    public void render(Canvas g, Paint p) {
        p.setColor(Color.argb(255, 255, 255, 255));
        Bitmap currentButtonImage = buttonDown ? buttonDownImage : buttonImage;
        g.drawBitmap(currentButtonImage, buttonRect.left, buttonRect.top, p);
    }

    /*
        respond when user touches button by changing the appearance of the button and setting flag
        that button is pressed
     */
    public boolean onTouchDown(int touchX, int touchY) {
        if (buttonRect.contains(touchX, touchY)) {
            buttonDown = true;
        } else {
            buttonDown = false;
        }
        return buttonDown;
    }

    /*
        user lifted finger from button
     */
    public void cancel() {
        buttonDown = false;
    }

    /*
        true when button is pressed
     */
    public boolean isPressed(int touchX, int touchY) {
        return buttonDown && buttonRect.contains(touchX, touchY);
    }

    public Bitmap getImg(){
        return buttonImage;
    }
    public Bitmap getImgDown(){
        return buttonDownImage;
    }
}
