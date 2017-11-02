package com.funnums.funnums.uihelpers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * Created by austinbaird on 10/31/17.
 */

public class GameFinishedMenu
{
    public String VIEW_LOG_TAG = "pause";

    //will use when we have a cooler background for pause menu
    private Bitmap backdrop;

    private Rect fade;

    private UIButton resume, mainMenu;

    private Rect backDropRect;

    //space between each button
    private int padding;

    //the score stored as a string, makes drawing easier
    private String score;

    //the message displayed on the game over menu
    private String gameFinishedMessage;

    int screenX;
    int screenY;

    public GameFinishedMenu(int left, int top, int right, int bottom,
                     UIButton resumeButton,  UIButton menuButton, int score) {
        backDropRect = new Rect(left, top, right, bottom);
        fade = new Rect(0, 0, com.funnums.funnums.maingame.GameActivity.screenX, com.funnums.funnums.maingame.GameActivity.screenY);

        resume = resumeButton;
        mainMenu = menuButton;

        padding = backDropRect.height()/8;//100;

        int centeredButtonX = backDropRect.centerX() - resume.getWidth()/2;


        int buttonY = backDropRect.centerY();
        int numButtons = 2;
        int spaceBetweenButtons = (backDropRect.height() - backDropRect.centerY()) / numButtons;

        resume.setRect(centeredButtonX, buttonY);
        //if there were more buttons, each would be placed at buttonY + spaceBetweenButtons*n
        mainMenu.setRect(centeredButtonX, buttonY + spaceBetweenButtons*1);


        gameFinishedMessage = "Great Job! Your Score is ";

        this.score = String.valueOf(score);

        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;
    }

    public void draw(Canvas canvas, Paint paint)
    {
        //draw grey, tanslucent rectangle over entire screen
        paint.setColor(Color.argb(126, 0, 0, 0));
        canvas.drawRect(fade, paint);

        //draw the rectangle containing the pasue menu buttons
        paint.setColor(Color.argb(255, 100, 100, 100));
        canvas.drawRect(backDropRect, paint);

        //TODO uncomment when we have a cool backdrop for menu instead of a grey rectangle
        //canvas.drawBitmap(backdrop, backDropRect.left, backDropRect.top, paint);

        //Draw Current
        paint.setColor(Color.argb(255, 0, 0, 255));
        paint.setTextSize(45);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(gameFinishedMessage, backDropRect.centerX(), backDropRect.top + padding, paint);
        canvas.drawText(score,  backDropRect.centerX(), backDropRect.top + padding*2, paint);

        //draw the buttons
        resume.render(canvas, paint);
        mainMenu.render(canvas, paint);
    }

    //handle touches
    public boolean onTouch(MotionEvent e)
    {
        int x = (int)e.getX();
        int y = (int)e.getY();
        if (e.getAction() == MotionEvent.ACTION_DOWN)
        {
            resume.onTouchDown(x, y);
            mainMenu.onTouchDown(x, y);
        }
        if (e.getAction() == MotionEvent.ACTION_UP)
        {
            if (resume.isPressed(x, y))
            {
                resume.cancel();
                com.funnums.funnums.maingame.GameActivity.gameView.currentGame.isPaused = false;
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
                resume.cancel();
                mainMenu.cancel();
            }
        }
        return true;
    }

}
