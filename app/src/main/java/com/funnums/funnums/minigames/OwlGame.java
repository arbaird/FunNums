package com.funnums.funnums.minigames;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.funnums.funnums.classes.DraggableTile;
import com.funnums.funnums.classes.ExpressionEvaluator;
import com.funnums.funnums.classes.TileCollisionDetector;
import com.funnums.funnums.uihelpers.GameFinishedMenu;
import com.funnums.funnums.uihelpers.UIButton;

import java.util.ArrayList;
import java.util.Random;


public class OwlGame extends MiniGame {


    class Coordinate{
        int x;
        int y;

        Coordinate(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    class TilePlaceHolder{
        int x;
        int y;
        DraggableTile t;

        TilePlaceHolder(int x, int y){
            this.x = x;
            this.y = y;
            t = null;
        }
    }


    public String TAG = "Owl Game"; //for debugging

    int TILE_LIMIT = 10;
    int EXPR_LIMIT = 7;

    //Ratios based on screen size
    double TILE_LENGTH_RATIO = .10;
    double T_BUFFER_RATIO = .20;
    double E_BUFFER_RATIO = .15;

    //Tile coordinates
    private ArrayList<Coordinate> tileCoordinates = new ArrayList<>();
    //Expression coordinates
    private ArrayList<Coordinate> exprCoordinates = new ArrayList<>();

    //Boolean array specifies if there is an element in said coordinate
    private boolean [] isTileCoordinateUsed;
    private boolean [] isExprCoordinateUsed;


    public final static int NANOS_TO_SECONDS = 1000000000; //conversion from nanosecs to seconds


    // Used to hold touch events so that drawing thread and onTouch thread don't result in concurrent access
    // not likely that these threads would interact, but if they do the game will crash!! which is why
    //we keep events in a separate list to be processed in the game loop
    private ArrayList<MotionEvent> events = new ArrayList<>();

    //dimensions of the sc
    private int screenX;
    private int screenY;

    //this is the amount of space at the top of the screen used for the tiles
    private int tileBuffer;
    private int exprBuffer;

    //running time, used to generate new numbers every few seconds
    //private long runningMilis = 0;

    //player's current sum
    private int sum;
    //target player is trying to sum to
    private int target;

    //TODO initialize target generator
    //The target generator
    // ExpressionGenerator expGenerator = new ExpressionGenerator();

    //For now we use dummy espression
    String [] dummy = {"1", "+", "2", "*", "3", "4", "-", "10", "+", "8"};

    //Evaluates state of current solution
    ExpressionEvaluator expEvaluator = new ExpressionEvaluator();

    //list of all the touchable tiles on screen
    ArrayList<DraggableTile> tileList = new ArrayList<>();

    //!!Pointer used by event listener switch to init later on
    //SparseArray<DraggableTile> mTilePointer = new SparseArray<>(TILE_LIMIT);

    // For drawing
    //private Paint paint;
    //private Canvas canvas;
    //private SurfaceHolder ourHolder;

    //!!generates random positions for us
    private Random r;

    //used to animate text, i.e show +3 when a 3 is touched
    //ArrayList<TextAnimator> scoreAnimations = new ArrayList<>();

    //Optimal tile length/width radius
    private int tLength;

    //Counter of tiles in use
    int numberOfTiles;
    int numberOfExpr;

    //game over menu
    private GameFinishedMenu gameFinishedMenu;

    public void init() {

        //game only finished when owl has died :P
        isFinished = false;

        //initialize random generator
        r = new Random();

        //TODO get a target from the target generator
        //target = targetGen.nextTarget();
        //!!For now refer to dummy

        //TODO set values according to the target generated
        numberOfTiles = 10;
        numberOfExpr = 7;

        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;

        //Set appropriate sizes based on screen
        tLength = (int) (screenX * TILE_LENGTH_RATIO);
        tileBuffer = (int) (screenY * T_BUFFER_RATIO);
        exprBuffer = (int) (screenY * E_BUFFER_RATIO);

        //Generate tile coordinates
        generateTileCoordinates();
        generateExprCoordinates();

        //Generate tiles
        generateTiles();

        //Timer can still be used to determine how much longer can the owl stay afloat
        //Initialize timer to 61 seconds, update after 1 sec interval
        //gameTimer = new GameCountdownTimer(61000, 1000);
        //gameTimer.start();


        //set up the pause button
        int offset = 100;
        Bitmap pauseImgDown = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("pause_down.png", true);
        Bitmap pauseImg = com.funnums.funnums.maingame.GameActivity.gameView.loadBitmap("pause.png", true);
        pauseButton = new UIButton(screenX *3/4, 0, screenX, offset, pauseImg, pauseImgDown);

        Log.d(TAG, "init pauseButton: " + pauseButton);

        Bitmap resumeDown = com.funnums.funnums.maingame.GameView.loadBitmap("button_resume_down.png", true);
        Bitmap resume = com.funnums.funnums.maingame.GameView.loadBitmap("button_resume.png", true);
        UIButton resumeButton = new UIButton(0,0,0,0, resume, resumeDown);

        Bitmap menuDown = com.funnums.funnums.maingame.GameView.loadBitmap("button_quit_down.png", true);
        Bitmap menu = com.funnums.funnums.maingame.GameView.loadBitmap("button_quit.png", true);
        UIButton menuButton = new UIButton(0,0,0,0, menu, menuDown);

        gameFinishedMenu = new GameFinishedMenu(screenX * 1/8,
                offset,
                screenX * 7/8,
                screenY - offset,
                resumeButton,
                menuButton, sum);


    }

    private void generateTileCoordinates(){
        int x, y;
        Coordinate coord;

        //for now we pretend that we always use 10 tiles we can change it later
        y = screenY - tileBuffer - exprBuffer + (int)(.15 * tileBuffer);
        x = (int) (.05 * screenX);

        coord = new Coordinate (x, y);
        tileCoordinates.add(coord);

        for(int i = 1; i < 5; i++){

            x += (int) (.1 * screenX) + tLength;

            coord = new Coordinate (x, y);
            tileCoordinates.add(coord);
        }

        y +=  (int)(.45 * tileBuffer);
        x = (int) (.05 * screenX);

        coord = new Coordinate (x, y);
        tileCoordinates.add(coord);

        for(int i = 6; i < 10; i++){
            x += (int) (.1 * screenX) + tLength;

            coord = new Coordinate (x, y);
            tileCoordinates.add(coord);
        }

        isTileCoordinateUsed = new boolean [numberOfTiles];
    }

    private void generateExprCoordinates(){
        int x, y;
        Coordinate coord;

        //for now we pretend that we always use 10 tiles we can change it later
        y = screenY - exprBuffer + (int)(.20 * exprBuffer);
        x = (int) (.05 * screenX);

        coord = new Coordinate (x, y);
        exprCoordinates.add(coord);

        for(int i = 1; i < 10; i++){

            x += tLength;

            coord = new Coordinate (x, y);
            exprCoordinates.add(coord);
        }

        isExprCoordinateUsed = new boolean [numberOfExpr];

    }

    public void update(long delta) {
        if (isPaused)
            return;

        //detect and handle collisions
        //findCollisions();

        for (DraggableTile tile : tileList) {
            //update the number
            tile.update();

            /*Checkif location is inside fixed spot, if so fix it, use boolean to know if it has been dropped*/
            /*if((num.getX() > screenX - num.getRadius() && num.getXVelocity() > 0)
                    || (num.getX() < 0 && num.getXVelocity() < 0) )
                num.setXVelocity(-num.getXVelocity()); //bounced off vertical edge

            if ((num.getY() > screenY - num.getRadius() && num.getYVelocity() > 0)
                    || (num.getY() < topBuffer + num.getRadius() && num.getYVelocity() < 0))
                num.setYVelocity(-num.getYVelocity()); //bounce off horizontal edge*/

        }

        /*runningMilis += delta;
        //generate a new number every 1/2 second if there are less than the max amount of numbers on the screen
        if (runningMilis > 0.5 * NANOS_TO_SECONDS && numberList.size() < maxNumsOnScreen) {
            generateNumber();
            runningMilis = 0;

        }*/

        //process all touch events
        processEvents();

    }

    /*
    Generates a draggable tile on screen
     */
    private void generateTiles() {

        int x, y;
        String value;
        Coordinate coord;
        DraggableTile til;

        for (int i = 0; i < numberOfTiles; i++){
            coord = tileCoordinates.get(i);
            x = coord.x;
            y = coord.y;

            //TODO change from dummy to actual new expression
            value = dummy[i];

            til = new DraggableTile (x, y, tLength, value);
            tileList.add(til);

            isTileCoordinateUsed[i] = true;
        }

        //} while(findCollisions(x,y));
        //while this new coordinate causes collisions, keep generating a new coordinates until
        //it finds coordinates in a place without collisions

        //angle is direction number travels, max and min are the max and min angles for a number
        //determined by which quadrant the number spawns in. i.e if it spawns in bottom right corner,
        //we want it to travel up and to the left (min = 90 max = 180)
        //int angle, max, min;

        //determine the quadrant the number will spawn in to plan the angle
        /*if (x >= screenX/2) {
            if (y >= screenY / 2) {
                //lower right quadrant
                max = 180;
                min = 91;
            }
            else {
                //upper right quadrant
                max = 270;
                min = 181;
            }
        }
        else {
            if (y >= screenY / 2) {
                //lower left quadrant
                max = 90;
                min = 1;
            }
            else {
                //upper left quadrant
                max = 360;
                min = 270;
            }
        }

        //make angles more diagonal
        max -= 25;
        min += 25;

        angle = r.nextInt(max - min) + min; //get random angle between max and min angles
*/

    }


    //Process the touch events

   private void processEvents() {

       for(MotionEvent e : events) {
           int x = (int) e.getX();
           int y = (int) e.getY();

           checkTouchedTile(x, y);
       }

       events.clear();

        /*DraggableTile touchedTile;
        int xTouch;
        int yTouch;
        int pointerId;
        int actionIndex;

        for(MotionEvent e : events) {

            actionIndex = e.getActionIndex();

            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // it's the first pointer, so clear all existing pointers data
                    mTilePointer.clear();

                    xTouch = (int) e.getX(0);
                    yTouch = (int) e.getY(0);

                    // check if we've touched inside some circle
                    touchedTile = checkTouchedTile(xTouch, yTouch);
                    //touchedCircle.centerX = xTouch;
                    //touchedCircle.centerY = yTouch;
                    if (touchedTile != null) {
                        mTilePointer.put(e.getPointerId(0), touchedTile);
                    }

                    //invalidate();
                   // handled = true;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.w(TAG, "Pointer down");
                    // It secondary pointers, so obtain their ids and check circles
                    pointerId = e.getPointerId(actionIndex);

                    xTouch = (int) e.getX(actionIndex);
                    yTouch = (int) e.getY(actionIndex);

                    // check if we've touched inside some circle
                    touchedTile = checkTouchedTile(xTouch, yTouch);

                    if (touchedTile != null) {
                        mTilePointer.put(e.getPointerId(0), touchedTile);
                    }
                    //touchedCircle.centerX = xTouch;
                    //touchedCircle.centerY = yTouch;
                    //invalidate();
                    //handled = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    final int pointerCount = e.getPointerCount();

                    Log.w(TAG, "Move");

                    for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                        // Some pointer has moved, search it by pointer id
                        pointerId = e.getPointerId(actionIndex);

                        xTouch = (int) e.getX(actionIndex);
                        yTouch = (int) e.getY(actionIndex);

                        touchedTile = mTilePointer.get(pointerId);

                        if (null != touchedTile) {
                            //touchedCircle.centerX = xTouch;
                            //touchedCircle.centerY = yTouch;
                        }
                    }
                    //invalidate();
                    //handled = true;
                    break;

                case MotionEvent.ACTION_UP:
                    mTilePointer.clear();
                    //invalidate();
                    //handled = true;
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    // not general pointer was up
                    pointerId = e.getPointerId(actionIndex);

                    mTilePointer.remove(pointerId);
                    //invalidate();
                    //handled = true;
                    break;

                case MotionEvent.ACTION_CANCEL:
                    //handled = true;
                    break;

                default:
                    // do nothing
                    break;
            }


            //checkTouchArea(x, y);
        }
        events.clear();

            //public boolean onTouchEvent(final MotionEvent event) {
            //boolean handled = false;



            // get touch event coordinates and make transparent circle from it


            //return super.onTouchEvent(event) || handled;*/
    }



   //Check if where the player touched the screen is on a touchable tile, if so return it,otherwise null

    /*private DraggableTile getTouchedTile(int x, int y) {

        for (DraggableTile t : tileList) {

            if (x >= t.getX() && x <= (t.getX() + tLength)) {
                if (y >= t.getY() && y <= (t.getY() + tLength)) {
                    return t;
                }
            }
        }

        return null;

    }

    */

    private void checkTouchedTile(int x, int y) {

        for (DraggableTile t : tileList) {

            //TODO fix touch sensitivity
            if (x >= t.getX() && x <= (t.getX() + tLength)) {
                if (y >= (t.getY()+60) && y <= (t.getY() + tLength+60)) {

                    if (t.isUsed()){
                        moveToTiles(t);
                    } else {
                        moveToExpr(t);
                    }
                    break;
                }
            }
        }

    }

   private void moveToExpr(DraggableTile tile) {
       int x, y;
       Coordinate coord;

       int currentCoordIndex = 0;

       //Find your own current cordinate index
       for (int index = 0; index < numberOfTiles; index++) {
           coord = tileCoordinates.get(index);

           if (coord.x == tile.getX() && coord.y == tile.getY()){
               currentCoordIndex = index;
               break;
           }

       }


       //Find an open coordinate
       for (int index = 0; index < numberOfExpr; index++) {

           if (!isExprCoordinateUsed[index]) {
               coord = exprCoordinates.get(index);

               x = coord.x;
               y = coord.y;

               tile.setXY(x, y);
               tile.setUsed(true);
               isExprCoordinateUsed[index] = true;
               isTileCoordinateUsed[currentCoordIndex] = false;

               break;
           }

       }




   }


    private void moveToTiles(DraggableTile tile){
        int x, y;
        Coordinate coord;

        int currentCoordIndex = 0;

        //Find your own current cordinate index
        for (int index = 0; index < numberOfExpr; index++) {
            coord = exprCoordinates.get(index);

            if (coord.x == tile.getX() && coord.y == tile.getY()){
                currentCoordIndex = index;
                break;
            }

        }

        //Find an open coordinate
        for (int index = 0; index < numberOfTiles; index++) {

            if (!isTileCoordinateUsed[index]) {
                coord = tileCoordinates.get(index);

                x = coord.x;
                y = coord.y;

                tile.setXY(x, y);
                tile.setUsed(false);
                isTileCoordinateUsed[index] = true;
                isExprCoordinateUsed[currentCoordIndex] = false;
                break;
            }

        }

    }




    /*
       When a number is touched, call this function. It will update the current Sum and check it
       player has reached the target, in which case we make a new target. Else, if the target is
       exceeded, for now we tell the player they exceeded the target and reset the game

       Also if the target is reached add 5 seconds or if the target is exceeded take away 5 seconds

    private void processScore(TouchableBubble num) {

        sum += num.getValue();
        score = sum;
        TextAnimator textAnimator = new TextAnimator("+" + String.valueOf(num.getValue()), num.getX(), num.getY(), 0, 255, 0);
        scoreAnimations.add(textAnimator);
        if (sum == target) {
            makeNewTarget();
            long newTime = 1000;
            com.funnums.funnums.maingame.GameActivity.gameView.updateGameTimer(newTime);

        } else if (sum > target) {
            resetGame();

            long newTime = -1000;
            com.funnums.funnums.maingame.GameActivity.gameView.updateGameTimer(newTime);
        }
    }
*/


    /*
       Create a new target

    private void makeNewTarget() {
        //text, x, y, r, g, b, interval, size
        TextAnimator textAnimator = new TextAnimator("New Target!", screenX/2, screenY/2, 44, 185, 185, 1.25, 50);
        scoreAnimations.add(textAnimator);

        previousTarget = target;
        target = targetGen.nextTarget();
        numGen.setAbsoluteTarget(target - previousTarget); //used for scaling the numbers generated
    }
*/


    /*
        For now, tell player they missed the target and reset the target and current sum

    private void resetGame() {
        //text, x, y, r, g, b, interval, size
        TextAnimator message1 = new TextAnimator("Target missed!", screenX/2, screenY/2, 185, 44, 44, 1.25, 60);
        TextAnimator message2 = new TextAnimator("Current reset", screenX/2, screenY/2 + 60, 185, 44, 44, 1.25, 50);
        scoreAnimations.add(message1);
        scoreAnimations.add(message2);

        //target = r.nextInt(3)+5;
        //sum = 0;
        //score = 0;

        sum = previousTarget; //reset the current sum to the previous target


        //if we want game to stop, make playing false here
        //   playing = false;
    }
*/
    /*


    /*
        Detect collisions for all our tiles on screen move them somewhere else

    private void findTileCollisions() {
        //this double for loop set up is so we don't check 0 1 and then 1 0 later, since they would have the same result
        //a bit of a micro optimization, but can be useful if there are a lot of numbers on screen
        for(int i = 0; i < tileList.size(); i++)
            for(int j = i+1; j < tileList.size(); j++)
                if(TileCollisionDetector.isCollision(tileList.get(i), tileList.get(j))) {
                    //tileList.get(i).bounceWith(numberList.get(j));
                    //reject the move******
                }
    }*/

    /*
        Overloaded to take an x and y coordinate as arguments.
        Return true if a given coordinate will cause a collision with numbers on screen, false otherwise
     *//*
    private boolean findCollisions(int x, int y) {
        //this double for loop set up is so we don't check 0 1 and then 1 0 later, since they would have the same result
        //a bit of a micro optimization, but can be useful if there are a lot of numbers on screen

        //allow a little extra space for new appearing numbers
        int buffer = bRadius / 2;
        for(int i = 0; i < numberList.size(); i++)
            if(CollisionDetector.isCollision(numberList.get(i), x, y, bRadius + buffer))
                return true;

        return false;
    }
*/
    public void draw(SurfaceHolder ourHolder, Canvas canvas, Paint paint) {

        if (ourHolder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();

            // Rub out the last frame
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            //draw tile buffer
            paint.setColor(Color.argb(255, 100, 150, 155));
            canvas.drawRect( (float)0, (float)(screenY-tileBuffer - exprBuffer), (float)screenX,
                    (float)screenY - exprBuffer, paint);

            //draw expr buffer
            paint.setColor(Color.argb(255, 150, 150, 155));
            canvas.drawRect( (float)0, (float)(screenY - exprBuffer), (float)screenX,
                    (float)screenY, paint);

            //draw all the numbers
            for(DraggableTile num : tileList)
                num.draw(canvas, paint);

            //Draw pause button
            /*if(pauseButton != null)
                pauseButton.render(canvas, paint);

            //draw pause menu, if paused
            if(isPaused)
                com.funnums.funnums.maingame.GameActivity.gameView.pauseScreen.draw(canvas, paint);
            //game finished stuff
            if(isFinished)
                com.funnums.funnums.maingame.GameActivity.gameView.gameFinishedMenu.draw(canvas, paint);*/



            //draw all text animations
            //for(TextAnimator score : scoreAnimations)
             //   score.render(canvas, paint);

            //Draw Current
            /*paint.setColor(Color.argb(255, 0, 0, 255));
            paint.setTextSize(45);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Current", screenX * 1/4, topBuffer - offset, paint);
            canvas.drawText(String.valueOf(sum),  screenX * 1/4, topBuffer, paint);
            //Draw Target
            canvas.drawText("Target", screenX * 3/4, topBuffer - offset, paint);
            canvas.drawText(String.valueOf(target),  screenX * 3/4, topBuffer, paint);
            //draw timer
            canvas.drawText("Timer", screenX * 1/2, offset, paint);
            canvas.drawText(String.valueOf(gameTimer.toString()),  screenX *  1/2, offset*2, paint);
            //Draw pause button
            if(pauseButton != null)
                pauseButton.render(canvas, paint);

            //draw pause menu, if paused
            if(isPaused)
                com.funnums.funnums.maingame.GameActivity.gameView.pauseScreen.draw(canvas, paint);
            //game finished stuff
            if(isFinished)
                com.funnums.funnums.maingame.GameActivity.gameView.gameFinishedMenu.draw(canvas, paint);
*/
            ourHolder.unlockCanvasAndPost(canvas);
        }


    }


    public boolean onTouch(MotionEvent e) {
        //add touch event to eventsQueue rather than processing it immediately. This is because
        //onTouchEvent is run in a separate thread by Android and if we touch and delete a number
        //in this touch UI thread while our game thread is accessing that same number, the game crashes
        //because two threads are accessing same memory being removed. We could do mutex but this setup
        //is pretty standard I believe.

        events.add(e);
        Log.d(TAG, "Touch event added");
        return true;
    }



}
