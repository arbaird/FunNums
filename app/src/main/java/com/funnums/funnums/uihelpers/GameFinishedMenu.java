package com.funnums.funnums.uihelpers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.funnums.funnums.maingame.GameActivity;

/**
 *  Adapted from James Cho's "Beginner's Guide to Android Game Development
 *  Menu displayed when the game is over
 */

public class GameFinishedMenu
{
    public String VIEW_LOG_TAG = "gameFinished";

    //back ground (board) for the menu
    private Bitmap bg;
    //fade the current game behind this menu
    private Rect fade;
    //buttons for the player to choose from
    private UIButton playAgain, mainMenu;

    //space between each button
    private int padding;

    //the score stored as a string, makes drawing easier
    private String score;

    //the message displayed on the game over menu
    private String gameFinishedMessage;

    //coords, along with dimensions
    int x;
    int y;
    int width, height;
    //scale of the text size so it fits inside the menu
    float xScale;

    float TEXT_SIZE = 60;

    public GameFinishedMenu(int left, int top, int width, int height,
                     UIButton resumeButton,  UIButton menuButton, Bitmap bg, Paint paint) {


        x = left;
        y = top;
        this.width = width;
        this.height = height;
        //create a scaled version of the given background image for the board;
        this.bg =  Bitmap.createScaledBitmap(bg, left + width,top + height,true);
        //create translucent Rect to draw over game behind the menu
        fade = new Rect(0, 0, com.funnums.funnums.maingame.GameActivity.screenX, com.funnums.funnums.maingame.GameActivity.screenY);

        //create the buttons the player can use
        playAgain = new UIButton(0,0,0,0, resumeButton.getImg(), resumeButton.getImgDown());
        mainMenu = new UIButton(0,0,0,0, menuButton.getImg(), menuButton.getImgDown());

        //magic number for spacing, but seems to look good across different sized phones
        padding = 100;
        //Y coord for the buttons
        int buttonY = y + height *5/8;
        //in case we add more buttons, spacing between buttons will already be handled
        int numButtons = 2;
        //calculate space between buttons based on number of buttons
        //if there were more buttons, each would be placed at buttonY + spaceBetweenButtons*n
        int spaceBetweenButtons = (height - buttonY) / numButtons;
        int centeredButtonX = GameActivity.screenX/2 - playAgain.getWidth()/2;

        //set region corresponding to button clicks
        playAgain.setRect(centeredButtonX, buttonY);
        mainMenu.setRect(centeredButtonX, buttonY + spaceBetweenButtons*1);
        //give message for game over screen
        gameFinishedMessage = "Great Job! Your Score is ";
        //adjust the size of the text so it fits inside the menu
        //adjustTextSize(paint, gameFinishedMessage);
        adjustTextScale(paint, gameFinishedMessage);
    }

    public void setScore(int score)
    {
        this.score = String.valueOf(score);
    }

    public void draw(Canvas canvas, Paint paint)
    {
        //draw grey, tanslucent rectangle over entire screen
        paint.setColor(Color.argb(126, 0, 0, 0));
        canvas.drawRect(fade, paint);

        paint.setColor(Color.argb(255, 100, 100, 100));
        //draw the board
        canvas.drawBitmap(bg, x, y, paint);

        //center of screen
        int centeredX = GameActivity.screenX/2;
        //Set up the text appropriately
        paint.setColor(Color.argb(255, 0, 0, 255));
        paint.setTextSize(TEXT_SIZE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextScaleX(xScale);
        //draw the text
        canvas.drawText(gameFinishedMessage, centeredX, y + height/3, paint);
        canvas.drawText(score,  centeredX, y + + height/3 + padding, paint);

        //draw the buttons
        playAgain.render(canvas, paint);
        mainMenu.render(canvas, paint);
    }

    //handle touches
    public boolean onTouch(MotionEvent e)
    {
        int x = (int)e.getX();
        int y = (int)e.getY();
        //if user touches down, change button appearances so they look pressed
        if (e.getAction() == MotionEvent.ACTION_DOWN)
        {
            playAgain.onTouchDown(x, y);
            mainMenu.onTouchDown(x, y);
        }
        if (e.getAction() == MotionEvent.ACTION_UP)
        {
            //check if either button is pressed down, and perform appropriate actions for each button if they are
            if (playAgain.isPressed(x, y))
            {
                playAgain.cancel();
                com.funnums.funnums.maingame.GameActivity.gameView.restart();
            }
            else if(mainMenu.isPressed(x, y))
            {
                mainMenu.cancel();
                Intent i = new Intent(com.funnums.funnums.maingame.GameActivity.gameView.getContext(), com.funnums.funnums.maingame.MainMenuActivity.class);
                // Start our GameActivity class via the Intent
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                com.funnums.funnums.maingame.GameActivity.gameView.getContext().startActivity(i);
            }
            //otherwise make buttons appear unselected
            else
            {
                playAgain.cancel();
                mainMenu.cancel();
            }
        }
        return true;
    }

    /*
        Adjust the x scale of the text so it fits inside the menu
     */
    void adjustTextScale(Paint paint, String text) {
        // do calculation with scale of 1.0 (no scale)
        paint.setTextScaleX(1.0f);
        Rect bounds = new Rect();
        // ask the paint for the bounding rect if it were to draw this text.
        paint.setTextSize(TEXT_SIZE);
        paint.getTextBounds(text, 0, text.length(), bounds);
        // determine the width
        int w = bounds.right - bounds.left;
        // determine how much to scale the width to fit the view
        float xscale = ((float) width) / w;
        // set the scale for the text paint
        paint.setTextScaleX(xscale);

        this.xScale = xscale;
    }

    /*
        Change the backdrop image
     */
    public void setBackDrop(Bitmap bg){
        this.bg =  Bitmap.createScaledBitmap(bg, x + width,y + height,true);
    }

    /*
        Adjust text size, in Y direction
     */
    void adjustTextSize(Paint paint, String text) {
        //start with large size
        paint.setTextSize(100);
        paint.setTextScaleX(1.0f);
        Rect bounds = new Rect();
        // ask the paint for the bounding rect if it were to draw this large text
        paint.getTextBounds(text, 0, text.length(), bounds);
        // get the height that would have been produced
        int w = bounds.right - bounds.left;
        // make the text text up 80% of the height
        float target = (float)width *.8f;
        // figure out what textSize setting would create that height of text
        float size = ((target/w)*100f);
        // and set it into the paint
        paint.setTextSize(size);
        TEXT_SIZE = size;
    }

}
