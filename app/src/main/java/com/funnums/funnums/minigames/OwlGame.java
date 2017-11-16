package com.funnums.funnums.minigames;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.funnums.funnums.classes.GameCountdownTimer;
import com.funnums.funnums.classes.Owl;
import com.funnums.funnums.classes.Cloud;
import com.funnums.funnums.uihelpers.UIButton;

public class OwlGame extends MiniGame {

    Cloud cloud1;
    Cloud cloud2;

    Owl owl;
    int screenY;
    int screenX;

    public synchronized void init() {
        //place owl at top of screen, we can change the spawn point in the future
        owl = new Owl(100, 100);

        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;

        Log.d("OWL", "INIT CLOUDS");
        cloud1 = new Cloud(screenX/2, screenY/8, screenY/8, screenY *3/8, 75);
        cloud2 = new Cloud(screenX*3/2, screenY *3/4, screenY /2, screenY *3/4, 75);
        Log.d("OWL", "Clouds initialized");
        //cloud3 = new Cloud(screenX * 3/2, 300);

        //we don't use a gametimer in this game, make sure that any left over timer from another game
        //isn't used for this one
        if(gameTimer != null)
            gameTimer.cancel();
        gameTimer = null;



        //set up the pause button
        int offset = 100;
        Bitmap pauseImgDown = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("pause_down.png", true);
        Bitmap pauseImg = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("pause.png", true);
        pauseButton = new UIButton(screenX *3/4, 0, screenX, offset, pauseImg, pauseImgDown);


    }

    /*
        Update the game logic
     */
    public synchronized void update(long delta){
        owl.update(delta);
        //if the owl reached the bottom of the screen, the game is over
        if(owl.getY() > screenY - owl.getSize()){
            GameCountdownTimer.completeGame();
        }
        //if owl is at top of screen, make sure it won't go off the screen
        else if(owl.getY() < owl.getSize()){
            owl.yVelocity = 0;
        }

        cloud1.update(delta);
        cloud2.update(delta);
    }

    public synchronized void draw(SurfaceHolder ourHolder, Canvas canvas, Paint paint){
        if (ourHolder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();

            // Rub out the last frame
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            //draw the owl
            owl.draw(canvas, paint);

            //draw the clouds
            cloud1.draw(canvas, paint);
            cloud2.draw(canvas, paint);

            paint.setColor(Color.argb(255, 0, 0, 255));
            //Draw pause button
            if(pauseButton != null)
                pauseButton.render(canvas, paint);

            //draw pause menu, if paused
            if(isPaused)
                com.funnums.funnums.maingame.GameActivity.gameView.pauseScreen.draw(canvas, paint);
            //game finished stuff
            if(isFinished)
                com.funnums.funnums.maingame.GameActivity.gameView.gameFinishedMenu.draw(canvas, paint);

            ourHolder.unlockCanvasAndPost(canvas);
        }

    }

    /*
        Right now, touching the screen makes the owl fly higher. We will update this later
     */
    public synchronized boolean onTouch(MotionEvent e){
        //only make owl fly if it won't go off screen
        if(e.getAction() == MotionEvent.ACTION_UP) {
            if (!(owl.getY() < owl.getSize()))
                owl.increaseAltitude();
        }
        return true;
    }
}
