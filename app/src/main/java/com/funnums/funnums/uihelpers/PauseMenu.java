package com.funnums.funnums.uihelpers;

/**
 * Created by austinbaird on 10/19/17.
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

    private Rect fade;

    private UIButton resume, mainMenu;

    private Rect backDropRect;
    private Bitmap bg;

    //space between each button
    private int padding;

    int x, y;

    public PauseMenu(int left, int top, int right, int bottom,
                     UIButton resumeButton,  UIButton menuButton) {

        backDropRect = new Rect(left, top, right, bottom);
        fade = new Rect(0, 0, com.funnums.funnums.maingame.GameActivity.screenX, com.funnums.funnums.maingame.GameActivity.screenY);

        resume = resumeButton;
        mainMenu = menuButton;

        padding = 100;

        resume.setRect(left + padding, top +padding);
        mainMenu.setRect(left + padding, top + padding*2);


    }

    public PauseMenu(int left, int top, int width, int height,
                     UIButton resumeButton,  UIButton menuButton, Bitmap bg) {

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
        //bg = com.funnums.funnums.maingame.GameView.loadBitmap("bubbleBackground.png", false);
        this.bg =  Bitmap.createScaledBitmap(bg, left + width,top + height,true);
        fade = new Rect(0, 0, com.funnums.funnums.maingame.GameActivity.screenX, com.funnums.funnums.maingame.GameActivity.screenY);

        resume = resumeButton;
        mainMenu = menuButton;

        padding = 100;

        resume.setRect(left + padding, top +padding);
        mainMenu.setRect(left + padding, top + padding*2);


    }


    public void draw(Canvas canvas, Paint paint) {

        paint.setColor(Color.argb(126, 0, 0, 0));
        canvas.drawRect(fade, paint);

        //draw the rectangle containing the pasue menu buttons
        paint.setColor(Color.argb(255, 100, 100, 100));
        //canvas.drawRect(backDropRect, paint);

        //TODO uncomment when we have a cool backdrop for menu instead of a grey rectangle
        canvas.drawBitmap(bg, x, y, paint);

        //draw the buttons
        resume.render(canvas, paint);
        mainMenu.render(canvas, paint);
    }

    //handle touches
    public boolean onTouch(MotionEvent e) {

        int x = (int)e.getX();
        int y = (int)e.getY();

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            resume.onTouchDown(x, y);
            mainMenu.onTouchDown(x, y);
        }

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
            else {
                resume.cancel();
                mainMenu.cancel();
            }
        }
        return true;
    }
}
