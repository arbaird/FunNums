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
 * Created by austinbaird on 10/31/17.
 */

public class GameFinishedMenu
{
    public String VIEW_LOG_TAG = "gameFinished";

    //will use when we have a cooler background for pause menu
    private Bitmap bg;

    private Rect fade;

    private UIButton playAgain, mainMenu;

    private Rect backDropRect;

    //space between each button
    private int padding;

    //the score stored as a string, makes drawing easier
    private String score;

    //the message displayed on the game over menu
    private String gameFinishedMessage;

    int x;
    int y;
    int width, height;

    float xScale;

    public GameFinishedMenu(int left, int top, int right, int bottom,
                     UIButton resumeButton,  UIButton menuButton, int score) {
        backDropRect = new Rect(left, top, right, bottom);
        fade = new Rect(0, 0, com.funnums.funnums.maingame.GameActivity.screenX, com.funnums.funnums.maingame.GameActivity.screenY);

        playAgain = resumeButton;
        mainMenu = menuButton;

        padding = backDropRect.height()/8;//100;

        int centeredButtonX = backDropRect.centerX() - playAgain.getWidth()/2;


        int buttonY = backDropRect.centerY();
        int numButtons = 2;
        int spaceBetweenButtons = (backDropRect.height() - backDropRect.centerY()) / numButtons;

        playAgain.setRect(centeredButtonX, buttonY);
        //if there were more buttons, each would be placed at buttonY + spaceBetweenButtons*n
        mainMenu.setRect(centeredButtonX, buttonY + spaceBetweenButtons*1);


        gameFinishedMessage = "Great Job! Your Score is ";

        this.score = String.valueOf(score);

    }

    public GameFinishedMenu(int left, int top, int width, int height,
                     UIButton resumeButton,  UIButton menuButton, Bitmap bg, Paint paint) {

        /*
        background = com.funnums.funnums.maingame.GameView.loadBitmap("bubbleBackground.png", false);
        background = Bitmap.createScaledBitmap(background, screenX,screenY/2,true);


new PauseMenu(GameActivity.screenX/4,
                                    offset,
                                    GameActivity.screenX * 3/4,
                                    GameActivity.screenY - offset,
                                    resumeButton,
                                    menuButton);

         */
        x = left;
        y = top;
        this.width = width;
        this.height = height;
        //bg = com.funnums.funnums.maingame.GameView.loadBitmap("bubbleBackground.png", false);
        this.bg =  Bitmap.createScaledBitmap(bg, left + width,top + height,true);
        fade = new Rect(0, 0, com.funnums.funnums.maingame.GameActivity.screenX, com.funnums.funnums.maingame.GameActivity.screenY);

        //UIButton menuButton = new UIButton(0,0,0,0, menu, menuDown);
        playAgain = new UIButton(0,0,0,0, resumeButton.getImg(), resumeButton.getImgDown());
        mainMenu = new UIButton(0,0,0,0, menuButton.getImg(), menuButton.getImgDown());

        padding = 100;

        int buttonY = y + height/2;//.centerY();
        int numButtons = 2;
        int spaceBetweenButtons = (height - buttonY) / numButtons;
        int centeredButtonX = (x+width/2) - playAgain.getWidth()/4;

        playAgain.setRect(centeredButtonX, buttonY);
        //if there were more buttons, each would be placed at buttonY + spaceBetweenButtons*n
        mainMenu.setRect(centeredButtonX, buttonY + spaceBetweenButtons*1);

        gameFinishedMessage = "Great Job! Your Score is ";
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

        //draw the rectangle containing the pasue menu buttons
        //paint.setColor(Color.argb(255, 100, 100, 100));
        //canvas.drawRect(backDropRect, paint);

        paint.setColor(Color.argb(255, 100, 100, 100));
        //TODO uncomment when we have a cool backdrop for menu instead of a grey rectangle
        canvas.drawBitmap(bg, x, y, paint);

        int centeredX = GameActivity.screenX/2;//(x+x+width)/2;
        //Draw Current
        paint.setColor(Color.argb(255, 0, 0, 255));
        paint.setTextSize(45);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextScaleX(xScale);
        canvas.drawText(gameFinishedMessage, centeredX, y + padding*2, paint);
        canvas.drawText(score,  centeredX, y + padding*3, paint);

        //draw the buttons
        playAgain.render(canvas, paint);
        mainMenu.render(canvas, paint);
    }

    //handle touches
    public boolean onTouch(MotionEvent e)
    {
        int x = (int)e.getX();
        int y = (int)e.getY();
        if (e.getAction() == MotionEvent.ACTION_DOWN)
        {
            playAgain.onTouchDown(x, y);
            mainMenu.onTouchDown(x, y);
        }
        if (e.getAction() == MotionEvent.ACTION_UP)
        {
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
            else
            {
                playAgain.cancel();
                mainMenu.cancel();
            }
        }
        return true;
    }

    void adjustTextScale(Paint paint, String text) {
        // do calculation with scale of 1.0 (no scale)
        paint.setTextScaleX(1.0f);
        Rect bounds = new Rect();
        // ask the paint for the bounding rect if it were to draw this
        // text.
        paint.setTextSize(45);
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
    }

    public void setBackDrop(Bitmap bg){
        this.bg =  Bitmap.createScaledBitmap(bg, x + width,y + height,true);
    }
}
