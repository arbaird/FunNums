package com.funnums.funnums.uihelpers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Paint;

public class UIButton {

    private Rect buttonRect;
    private boolean buttonDown = false;
    private Bitmap buttonImage, buttonDownImage;

    public UIButton(int left, int top, int right, int bottom,
                    Bitmap buttonImage, Bitmap buttonPressedImage) {

        buttonRect = new Rect(left, top, right, bottom);
        this.buttonImage = buttonImage;
        this.buttonDownImage = buttonPressedImage;
    }

    public int getWidth()
    {
        return buttonImage.getWidth();
    }

    public void setRect(int left, int top) {
        buttonRect = new Rect(left, top, left + buttonImage.getWidth(),top + buttonImage.getHeight());
    }

    public void render(Canvas g, Paint p) {
        p.setColor(Color.argb(255, 255, 255, 255));
        Bitmap currentButtonImage = buttonDown ? buttonDownImage : buttonImage;
        g.drawBitmap(currentButtonImage, buttonRect.left, buttonRect.top, p);
    }

    //might use this to scale buttons rather than always draw them as the size of the .png file
    public void drawImage(Canvas canvas, Paint p, int width, int height) {
        //get rect for actual size of image
        Bitmap currentButtonImage = buttonDown ? buttonDownImage : buttonImage;
        Rect srcRect = new Rect();
        srcRect.set(0, 0, currentButtonImage.getWidth(), currentButtonImage.getHeight());
        //get rect for scaled image, based on given width and height
        Rect dstRect = new Rect();

        int x = buttonRect.left;
        int y = buttonRect.top;
        dstRect.set(x, y, x + width, y + height);
        canvas.drawBitmap(currentButtonImage, srcRect, dstRect, p);
    }


    //repsond when user touches button
    public boolean onTouchDown(int touchX, int touchY) {
        if (buttonRect.contains(touchX, touchY)) {
            buttonDown = true;
        } else {
            buttonDown = false;
        }
        return buttonDown;
    }

    //user lifted finger from button
    public void cancel() {
        buttonDown = false;
    }

    //true when button is pressed
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
