package com.funnums.funnums.uihelpers;

/**
 *  Adapted from James Cho's "Beginner's Guide to Android Game Development
 *  Pause menu displayed when user pauses the game
 */
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.view.MotionEvent;

public class PauseMenu {

    public String VIEW_LOG_TAG = "pause";

    //will use when we have a cooler background for pause menu
    private Bitmap backdrop;

    //translucent rect to fade the entire screen behind the pause menu
    private Rect fade;

    //buttons to select
    private UIButton resume, mainMenu;

    //backdrop image
    private Bitmap bg;

    //space between each button
    private int padding;

    //coords and dimensions
    int x, y;
    int width, height;


    public PauseMenu(int left, int top, int width, int height,
                     UIButton resumeButton,  UIButton menuButton, Bitmap bg) {

        x = left;
        y = top;
        this.width = width;
        this.height = height;
        //create a scaled version of the given background image for the board;
        this.bg =  Bitmap.createScaledBitmap(bg, left + width,top + height,true);
        //create translucent Rect to draw over game behind the menu
        fade = new Rect(0, 0, com.funnums.funnums.maingame.GameActivity.screenX, com.funnums.funnums.maingame.GameActivity.screenY);

        //create the buttons the player can use
        resume = new UIButton(0,0,0,0, resumeButton.getImg(), resumeButton.getImgDown());
        mainMenu = new UIButton(0,0,0,0, menuButton.getImg(), menuButton.getImgDown());

        //determine the space between buttons
        padding = height/4;

        //set region corresponding to button clicks
        resume.setRect(left + width/2 - resume.getWidth()/4, top +padding);
        mainMenu.setRect(left + width/2- resume.getWidth()/4, top + padding*2);
    }


    public void draw(Canvas canvas, Paint paint) {

        paint.setColor(Color.argb(126, 0, 0, 0));
        //fade the game behind the pause menu
        canvas.drawRect(fade, paint);

        //draw the rectangle containing the pasue menu buttons
        paint.setColor(Color.argb(255, 100, 100, 100));
        //draw the backdrop for menu
        canvas.drawBitmap(bg, x, y, paint);

        //draw the buttons
        resume.render(canvas, paint);
        mainMenu.render(canvas, paint);
    }

    //handle touches
    public boolean onTouch(MotionEvent e) {

        int x = (int)e.getX();
        int y = (int)e.getY();
        //if user touches down, change button appearances so they look pressed
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            resume.onTouchDown(x, y);
            mainMenu.onTouchDown(x, y);
        }
        //check if either button is pressed down, and perform appropriate actions for each button if they are
        if (e.getAction() == MotionEvent.ACTION_UP) {
            if (resume.isPressed(x, y)) {
                resume.cancel();
                com.funnums.funnums.maingame.GameActivity.gameView.currentGame.isPaused = false;
                if(com.funnums.funnums.maingame.GameActivity.gameView.currentGame.gameTimer != null)
                    com.funnums.funnums.maingame.GameActivity.gameView.resumeGameTimer();
            }
            else if(mainMenu.isPressed(x, y)) {
                mainMenu.cancel();
                Intent i = new Intent(com.funnums.funnums.maingame.GameActivity.gameView.getContext(), com.funnums.funnums.maingame.MainMenuActivity.class);
                // Start our GameActivity class via the Intent
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                com.funnums.funnums.maingame.GameActivity.gameView.getContext().startActivity(i);
            }
            //otherwise make buttons appear unselected
            else {
                resume.cancel();
                mainMenu.cancel();
            }
        }
        return true;
    }

    public void setBackDrop(Bitmap bg){
        this.bg =  Bitmap.createScaledBitmap(bg, x + width,y + height,true);
    }
}
