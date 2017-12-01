package com.funnums.funnums.minigames;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.funnums.funnums.R;
import com.funnums.funnums.classes.DraggableTile;
import com.funnums.funnums.classes.ExpressionEvaluator;
import com.funnums.funnums.classes.ExpressionGenerator;
import com.funnums.funnums.maingame.GameActivity;
import com.funnums.funnums.uihelpers.GameFinishedMenu;
import com.funnums.funnums.uihelpers.UIButton;
import com.funnums.funnums.classes.GameCountdownTimer;
import com.funnums.funnums.classes.Owl;
import com.funnums.funnums.classes.TilePlaceHolder;

import com.funnums.funnums.classes.Cloud;
import com.funnums.funnums.uihelpers.UIButton;


public class OwlGame extends MiniGame {
    public String TAG_OWL = "Owl Game"; //for debugging

    //TODO find out the source of the mysterious power that this number holds
    int HOLY_MAGIC_NUMBER = 60;

    /**
     * Private TitlePlaceHolder class holds the coordinates for a tile to be placed
     * and a reference to the tile that holds that position
     * */

    private final int TILE_LIMIT = 10;
    private final int EXPR_LIMIT =  7;

    //Ratios based on screen size
    private double TILE_LENGTH_RATIO = .10;     /*10% of the screen width*/
    private double T_BUFFER_RATIO    = .20;     /*20% of the screen length*/
    private double E_BUFFER_RATIO    = .15;     /*15% of the screen length*/

    //Dimensions of the screen
    private int screenX;
    private int screenY;

    //This is the amount of space at the top of the screen used for the tiles
    private float tileBuffer;
    private float exprBuffer;

    /* Used to hold touch events so that drawing thread and onTouch thread don't result in concurrent
     * access not likely that these threads would interact, but if they do the game will crash!!
     * which is why we keep events in a separate list to be processed in the game loop
     */
    private ArrayList<MotionEvent> events = new ArrayList<>();

    //clouds to draw
    Cloud cloud1;
    Cloud cloud2;
    //our master, the owl
    Owl owl;

    //Tile coordinates
    private ArrayList<TilePlaceHolder> tileSpaces = new ArrayList<>();
    //Expression coordinates
    private ArrayList<TilePlaceHolder> exprSpaces = new ArrayList<>();

    // List of all the touchable tiles on screen
    private ArrayList<DraggableTile> tileList = new ArrayList<>();

    private ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private ExpressionGenerator generator = new ExpressionGenerator();

    //The shuffled expression as a string
    private String [] expr;

    // Target player is trying to sum to
    private int target;
    // The current number of targets that the player has reached
    private int targetsReached = 0;

    //Optimal tile length/width radius
    private float tLength;

    //Counter of tiles
    private int numberOfTiles;
    private int numberOfExprSpaces;

    //Counter or tile spaces in use
    private int numberOfTileSpacesUsed;
    private int numberOfExprSpacesUsed;

    //game over menu
    private GameFinishedMenu gameFinishedMenu;

    //Separate tile objects
    private DraggableTile targetTile;
    private DraggableTile equalsTile;

    //Current tile being dragged
    private DraggableTile currentDraggTIle;
    private boolean isSingleTouch;
    private int countTouches;

    //sound effects
    private SoundPool soundPool;
    private float volume;
    private int clickId;
    private int drownId;

    public synchronized void init() {

        //Game only finished when owl has died :P
        isFinished = false;

        currentDraggTIle = null;
        isSingleTouch = false;
        countTouches = 0;

        numberOfTiles = TILE_LIMIT;
        numberOfExprSpaces = EXPR_LIMIT;

        //No tiles are present currently
        numberOfExprSpacesUsed = 0;
        numberOfTileSpacesUsed = 0;

        //Get x and Y values of the Screen
        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;

        //Set appropriate sizes based on screen
        tLength = (float) (screenX * TILE_LENGTH_RATIO);
        tileBuffer = (float) (screenY * T_BUFFER_RATIO);
        exprBuffer = (float) (screenY * E_BUFFER_RATIO);

        //initialize soundPool to load sound effects
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        clickId = soundPool.load(context, R.raw.click,1);
        volume = GameActivity.gameView.volume;

        //Generate tile coordinates
        generateTileSpaceHolders();
        generateExprSpaceHolders();

        //Generate initial target and expression
        makeNewTargetAndExpr();

        //Generate tiles
        generateTiles();
        generateTargetTile();

        //place owl at top of screen, we can change the spawn point in the future
        owl = new Owl(screenX/2, 100);

        screenX = com.funnums.funnums.maingame.GameActivity.screenX;
        screenY = com.funnums.funnums.maingame.GameActivity.screenY;

        Log.d("OWL", "INIT CLOUDS");
        int cloudSize = 75;
        //arguments for new cloud are initial x, initial y, min spawn point, max span paint, size)
        cloud1 = new Cloud(screenX/2, screenY/8, screenY/8, screenY *3/8, cloudSize);
        cloud2 = new Cloud(screenX*3/2,  screenY -tileBuffer - exprBuffer - cloudSize/2, screenY /2, (int)(screenY -tileBuffer - exprBuffer- cloudSize/2), cloudSize);
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

        Bitmap backdrop = com.funnums.funnums.maingame.GameView.loadBitmap("MenuBoard.png", true);

        GameActivity.gameView.pauseScreen.setBackDrop(backdrop);
        GameActivity.gameView.gameFinishedMenu.setBackDrop(backdrop);

    }

    //Update method to be called by game loop
    public synchronized void update(long delta) {
        if (isPaused)
            return;

        owl.update(delta);
        //if the owl reached the bottom of the screen, the game is over
        if(owl.getY() - owl.getSize() > screenY -tileBuffer - exprBuffer){
            GameCountdownTimer.completeGame();
        }
        //if owl is at top of screen, make sure it won't go off the screen
        else if(owl.getY() < owl.getSize()){
            owl.yVelocity = 0;
        }
        cloud1.update(delta);
        cloud2.update(delta);

        processEvents();
    }

    //Draw method
    public synchronized void draw(SurfaceHolder ourHolder, Canvas canvas, Paint paint) {
        if (ourHolder.getSurface().isValid()) {
            //First we lock the area of memory we will be drawing to
            canvas = ourHolder.lockCanvas();

            // Rub out the last frame
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            //draw the clouds
            cloud1.draw(canvas, paint);
            cloud2.draw(canvas, paint);

            //draw the owl
            owl.draw(canvas, paint);

            paint.setColor(Color.argb(255, 0, 0, 255));

            //draw tile buffer
            paint.setColor(Color.argb(255, 100, 150, 155));
            canvas.drawRect( (float)0, (float)(screenY-tileBuffer - exprBuffer), (float)screenX,
                    (float)screenY - exprBuffer, paint);

            //draw expr buffer
            paint.setColor(Color.argb(255, 150, 150, 155));
            canvas.drawRect( (float)0, (float)(screenY - exprBuffer), (float)screenX,
                    (float)screenY, paint);

            //Draw all the tiles
            for(DraggableTile t : tileList)
                t.draw(canvas, paint);

            //Draw Target and equals tile
            equalsTile.draw(canvas, paint);
            targetTile.draw(canvas, paint);

            //Draw all the tile spots
            for(TilePlaceHolder ph : exprSpaces)
                ph.draw(canvas, paint);

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

    /**Process the touch events WITHOUT Drag and Drop feature
    private void processEvents() {

        for(MotionEvent e : events) {

            //Prevents double/multiple touch action
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {

                float x = e.getX();
                float y = e.getY();

                checkTouchedTile(x, y);
            }
        }

        events.clear();

    }*/

    //Process the touch events WITH Drag and Drop feature
    private void processEvents() {
        int COUNT_TO_DRAGGING_ACTION = 5;

        DraggableTile t;
        float x, y;

        for(MotionEvent e : events) {

            x = e.getX();
            y = e.getY();


            if  (e.getActionMasked() == MotionEvent.ACTION_DOWN){       /*First touch*/
                //Log.d(TAG_OWL, "DOWN event" );

                //Get a reference to the Tile in that coordinate, otherwise null
                t = getTouchedTile(x, y);
                //Set the t to be the tile that is the target of a dragging action
                currentDraggTIle = t;

                //Set the action to be a single touch
                isSingleTouch = true;
                countTouches = 0;


            }else if (e.getActionMasked() == MotionEvent.ACTION_MOVE){      /*Dragging action*/
                //Log.d(TAG_OWL, "MOVE event" );

                //Check that a tile has been set as target of the dragging action
                if (currentDraggTIle != null) {

                    //Update the coordinate of the tile to match the touch
                    currentDraggTIle.setXY(x - (tLength / 2), y - 60 - (tLength / 2));

                    /*Dragging only takes place after a certain number of ACTION_MOVE events have occurred
                    * Otherwise the event is considered a single touch. Without this, even a single touch could be considered
                     * as a dragging instead of a single touch. This is the case because even though we think
                     * that human touch is instantaneous, the program could register more than one touch action.*/
                    if (countTouches++ >= COUNT_TO_DRAGGING_ACTION)
                        isSingleTouch = false;


                }

            } else if (e.getActionMasked() == MotionEvent.ACTION_UP){           /*Final touch/End of dragging action*/
                //Log.d(TAG_OWL, "UP event" );

                //If it is a single touch, continue as before dragging was implemented
                if (isSingleTouch) {
                    checkTouchedTile(x,y);

                } else if (currentDraggTIle != null){
                    //Otherwise there is a dragging action and need to check if the dragged tile needs its position to be updated
                    findPlaceHolder(x,y);
                }

                //Clear the pointer to dragged tile
                currentDraggTIle = null;
                //Reset boolean and count
                isSingleTouch = false;
                countTouches = 0;
            }
        }

        events.clear();

    }

    // Function is called after the end of a dragging action that ends at coordinates x and y
    // Using x and y the appropriate position that the dragged object must take
    public void findPlaceHolder(float x, float y){

        //If is not part of the expression
        if (!currentDraggTIle.isUsed()) {
            dragCheckExprSpaceHolder(x, y);
        } else{
            dragCheckIsOutsideExpression(x, y);
        }

    }

    //Touch handler
    public synchronized boolean onTouch(MotionEvent e) {
        //add touch event to eventsQueue rather than processing it immediately. This is because
        //onTouchEvent is run in a separate thread by Android and if we touch and delete a number
        //in this touch UI thread while our game thread is accessing that same number, the game crashes
        //because two threads are accessing same memory being removed. We could do mutex but this setup
        //is pretty standard I believe.
        events.add(e);
        Log.d(TAG_OWL, "Touch event added");

        return true;

    }

    // Generates TileSpaceHolders to be used by the tiles, initially no actual tiles
    // are being held inside the place holders
    private synchronized void generateTileSpaceHolders(){
        double SPACING_TOP_PERCENTAGE = .15;
        double SPACING_LEFT_PERCENTAGE = .05;
        double SPACING_MIDDLE_PERCENTAGE = .45;
        double SPACING_BETWEEN_PERCENTAGE = .1;


        float x, y;
        TilePlaceHolder space;

        //Y value starts at the top of the tileBuffer + 15% of the overall length of tileBuffer
        y = screenY - tileBuffer - exprBuffer + (int)(SPACING_TOP_PERCENTAGE * tileBuffer);
        //X leaves 5% spacing
        x = (int) (SPACING_LEFT_PERCENTAGE * screenX);

        space = new TilePlaceHolder (x, y, tLength);
        tileSpaces.add(space);

        for(int i = 1; i < 5; i++){

            x += (int) (SPACING_BETWEEN_PERCENTAGE * screenX) + tLength;

            space = new TilePlaceHolder (x, y, tLength);
            tileSpaces.add(space);
        }

        //Create second row

        //Y now adds 30% for the tile space and 15% for extra space
        y +=  (int)(SPACING_MIDDLE_PERCENTAGE * tileBuffer);
        //Reset X
        x = (int) (SPACING_LEFT_PERCENTAGE * screenX);

        space = new TilePlaceHolder (x, y, tLength);
        tileSpaces.add(space);

        for(int i = 6; i < 10; i++){
            x += (int) (SPACING_BETWEEN_PERCENTAGE * screenX) + tLength;

            space = new TilePlaceHolder (x, y, tLength);
            tileSpaces.add(space);
        }
    }

    // Generates TileSpaceHolders to be used by the tiles in an expression
    private synchronized void generateExprSpaceHolders(){
        double SPACING_TOP_PERCENTAGE = .25;
        double SPACING_LEFT_PERCENTAGE = .05;

        float x, y;
        TilePlaceHolder space;

        //Y starts at top of exprBuffer + 20% spacing
        y = screenY - exprBuffer + (int)(SPACING_TOP_PERCENTAGE * exprBuffer);
        x = (int) (SPACING_LEFT_PERCENTAGE * screenX);

        space = new TilePlaceHolder (x, y, tLength);
        exprSpaces.add(space);

        for(int i = 1; i < 9; i++){

            x += tLength;

            space = new TilePlaceHolder (x, y, tLength);
            exprSpaces.add(space);
        }

    }

    // Generates a draggable tiles on screen
    private synchronized void generateTiles() {

        float x, y;
        String value;
        TilePlaceHolder space;
        DraggableTile til;

        for (int i = 0; i < numberOfTiles; i++){
            space = tileSpaces.get(i);
            x = space.x;
            y = space.y;

            value = expr[i];

            til = new DraggableTile (x, y, tLength, value);
            tileList.add(til);

            //Set to operator type
            if ( value == "+"|| value == "-" || value == "/" || value == "*"){
                til.setIsOperator(true);
            }

            space.setTile(til);
        }
        
        numberOfTileSpacesUsed = numberOfTiles;

    }

    //Generate  a tile to represent the target as well as equal sign
    private void generateTargetTile(){
        float x, y;
        String value;
        TilePlaceHolder space;

        //Generate equal sign
        space = exprSpaces.get(numberOfExprSpaces);

        x = space.x;
        y = space.y;
        value = "=";

        equalsTile = new DraggableTile (x, y, tLength, value);
        space.setTile(equalsTile);

        //Generate target tile
        space = exprSpaces.get(numberOfExprSpaces+1);

        x = space.x;
        y = space.y;
        value = String.valueOf(target);

        targetTile = new DraggableTile (x, y, tLength, value);
        space.setTile(targetTile);
    }

    //Check if there is a tile in the touch coordinates, and if so,
    //move tile to corresponding space
    private void checkTouchedTile(float x, float y) {
        boolean touchInXRange, touchInYRange;

        for (DraggableTile t : tileList) {
            touchInXRange = ( x >= t.getLeft() && x <= t.getRight() );
            touchInYRange = ( y >= (t.getTop()+HOLY_MAGIC_NUMBER) && y <= (t.getBottom()+HOLY_MAGIC_NUMBER) );

            // If there is a hit
            if (touchInXRange && touchInYRange) {
                Log.d(TAG_OWL, "Tile Pressed: " + t.getValue());
                if (t.isUsed()){
                    moveToTiles(t);
                    Log.d(TAG_OWL, "moveToTiles");
                } else {
                    moveToExpr(t);
                    Log.d(TAG_OWL, "moveToExpr");
                }

                //play click sound
                soundPool.play(clickId,volume,volume,2,0,1);

                if (evaluatesToTarget()) {
                    handleOnCorrect();
                }
                break;
            }
        }
    }

    //Check if there is a tile in the touch coordinates, and if so,
    //return it, otherwise return null
    private DraggableTile getTouchedTile(float x, float y) {
        boolean touchInXRange, touchInYRange;

        for (DraggableTile t : tileList) {

            touchInXRange = ( x >= t.getLeft() && x <= t.getRight() );
            touchInYRange = ( y >= (t.getTop()+HOLY_MAGIC_NUMBER) && y <= (t.getBottom()+HOLY_MAGIC_NUMBER) );

            // If there is a hit
            if (touchInXRange && touchInYRange) {
                return t;
            }
        }

        return null;
    }

    //If there is a slot available in the expression
    // 1) Free your current spot
    // 2) Find the next open available space in the expression
    private void moveToExpr(DraggableTile tile) {
        float x, y;
        int index = 0;

        //If there is space in the expression
        if (numberOfExprSpacesUsed < numberOfExprSpaces){


            //Free your spot
            freeTileSpaceHolder(tile);

            //Find an open spot in the expression
            for (TilePlaceHolder p : exprSpaces) {

                if (p.getTile() == null ){
                    x = p.x;
                    y = p.y;
                    tile.setXY(x, y);

                    tile.setUsed(true);
                    p.setTile(tile);

                    //Insert token to evaluate
                    evaluator.slots.insert(tile.getValue(), index);

                    break;
                }

                index++;
            }

            //Update values accordingly
            numberOfExprSpacesUsed++;
            numberOfTileSpacesUsed--;
        }

    }

    // 1) Free your current spot in the expression
    // 2) Find the next open available space in the overall tile space
    private void moveToTiles(DraggableTile tile){
        float x, y;
        int index = 0;

        //Free your spot in the expression
        for (TilePlaceHolder p : exprSpaces) {

            if (p.getTile() == tile) {
                p.setTile(null);

                //Insert token in evaluator
                evaluator.slots.delete(index);
                break;
            }

            index++;

        }

        //Find an open spot in the overall tile space
        for (TilePlaceHolder p : tileSpaces) {

            if (p.getTile() == null ){
                x = p.x;
                y = p.y;
                tile.setXY(x, y);

                tile.setUsed(false);
                p.setTile(tile);

                break;
            }

        }

        //Update values accordingly
        numberOfExprSpacesUsed--;
        numberOfTileSpacesUsed++;

    }

    /* Calls getUserExpr() to check if the current user expression is valid, and if so, we call
     * evalExpr() to check the value of it. Returns true if the expression evaluates to the target.
     */
    public boolean evaluatesToTarget() {
        String expr = evaluator.getUserExpr();
        Log.d(TAG_OWL, "User Expr: "+expr);
        if (expr == null) {
            Log.d(TAG_OWL, "Expr is null, returning false");
            return false;
        }
        Log.d(TAG_OWL, "Expr Length: " + expr.length());
        int userNumber = evaluator.evalExpr(expr);
        Log.d(TAG_OWL, "User Expr: "+expr+" " + "UserValue: "+userNumber +" Target: " + target);
        if (userNumber != target) {
            return false;
        }
        return true;
    }

    public void handleOnCorrect() {
        //Give the Owl a push!
        if(!(owl.getY() < owl.getSize()))
            owl.increaseAltitude();

        targetsReached++;
        score += getPoints();
        makeNewTargetAndExpr();
        setupNewTiles();
    }

    /* Retrieves the difficulty of the last expr from the generator and updates our score accordingly.
     */
    public int getPoints() {
        final int EASY   = 1;
        final int MEDIUM = 2;
        final int HARD   = 3;

        int difficulty = generator.getDifficulty();
        switch (difficulty) {
            case EASY:
                return 1;
            case MEDIUM:
                return 5;
            case HARD:
                return 10;
        }
        return -1;
    }

    /* Generates a new shuffled expression and sets a new target
     * A proper target can only be retrieved after getNewExpr() is called inside
     * getShuffledExpression, which is why these 2 calls are grouped as a single function.
     */
    private void makeNewTargetAndExpr() {
        expr = getShuffledExpression();
        target = generator.getTarget();
    }

    /* Can be modified depending on balance. The first 13 targets are computed from a expression
     * with only 1 operator, each operator +, -, *, / getting ~3 iterations to ease the player in.
     * After the initial 13 targets, every 4 targets generates an expression using 3 ops
     * Otherwise we generate an expression using 2 operators. getNewExpr() also sets the target.
     */
    private String[] getShuffledExpression() {
        if (targetsReached < 3)      return generator.getNewExpr(new String[] {"+"});
        if (targetsReached < 6)      return generator.getNewExpr(new String[] {"-"});
        if (targetsReached < 10)     return generator.getNewExpr(new String[] {"*"});
        if (targetsReached < 13)     return generator.getNewExpr(new String[] {"/"});
        if (targetsReached % 4 == 0) return generator.getNewExpr(3);
                                     return generator.getNewExpr(2);
    }

    /* Removes all references of current tiles from the exprHolder ArrayList
     * and from the tileList ArrayList.
     * Then new tiles are generated and stored in the the tileHolder ArrayList
     */
    private void setupNewTiles() {
        numberOfTileSpacesUsed = 0;
        numberOfExprSpacesUsed = 0;
        evaluator.slots.clearSlots();
        clearTilesInExprHolder();
        clearTilesInTopHolder();
        tileList.clear();           //old tiles need to be cleared
        generateTiles();
        generateTargetTile();
    }

    // Removes the reference to the tile from each holder in TopHolder
    private void clearTilesInTopHolder() {
        for (TilePlaceHolder placeHolder: tileSpaces) {
            placeHolder.setTile(null);
        }
    }

    // Removes the reference to the tile from each holder in ExprHolder
    private void clearTilesInExprHolder() {
        for (TilePlaceHolder placeHolder: exprSpaces) {
            placeHolder.setTile(null);
        }

    }

    //Free your current TilePlaceHolder
    private void freeTileSpaceHolder(DraggableTile t) {

        //Return to original position in the expression
        if ( t.isUsed() ){

            for (TilePlaceHolder p : exprSpaces){
                if (p.getTile() == t){
                    p.setTile(null);
                    break;
                }
            }

        } else {
            //Return to original position in tile buffer

            for (TilePlaceHolder p : tileSpaces){
                if (p.getTile() == t){
                    p.setTile(null);
                    break;
                }
            }

        }

    }

    //Checks if the current dragged object is in one of the slots of the expression,
    // if so add it to expression, otherwise return to tile buffer
    private void dragCheckExprSpaceHolder(float x, float y){
        int index = 0;
        boolean spotFound = false;
        boolean touchInXRange = false, touchInYRange = false;

        //Find if the tile is placed in a placeHolder that is part of the expression
        for (TilePlaceHolder p : exprSpaces) {

            //Boolean check of touch
            touchInXRange = (x >= p.left && x <= p.right);
            touchInYRange = (y >= (p.top + HOLY_MAGIC_NUMBER) && y <= (p.bottom + HOLY_MAGIC_NUMBER));

            spotFound = touchInXRange && touchInYRange;

            // If there is a hit
            if (spotFound) {

                //No tile is there
                if (p.getTile() == null ){

                    //Free your spot
                    freeTileSpaceHolder(currentDraggTIle);

                    //Change your position
                    currentDraggTIle.setXY(p.x, p.y);

                    //Add tile to expression
                    currentDraggTIle.setUsed(true);
                    p.setTile(currentDraggTIle);

                    //Insert token to evaluate
                    evaluator.slots.insert(currentDraggTIle.getValue(), index);

                    //Update values accordingly
                    numberOfExprSpacesUsed++;
                    numberOfTileSpacesUsed--;

                    //play click sound
                    soundPool.play(clickId,volume,volume,2,0,1);

                    break;

                } else { //there is a tile there, no valid change of position
                    spotFound = false;
                }


            }

            index++;
        }

        //No hit, return to position
        if (!spotFound) {
            dragTileToOriginalPosition();
        }

        //Evaluate
        if (evaluatesToTarget()) {
            handleOnCorrect();
        }

    }

    //Checks if the current dragged object is outside of the boundaries of the expression buffer,
    // if so return it to the next available position in the tile buffer
    private void dragCheckIsOutsideExpression(float x, float y){
        boolean touchInYRange = false;

        //Boolean check of touch
        touchInYRange = (y <= screenY - exprBuffer + HOLY_MAGIC_NUMBER);

        // If there is a hit
        if (touchInYRange) {
            moveToTiles(currentDraggTIle);
        } else {
            dragTileToOriginalPosition();
        }

        //Evaluate current state of expression
        if (evaluatesToTarget()) {
            handleOnCorrect();
        }

    }

    // Return a tile to its original position before dragging
    private void dragTileToOriginalPosition(){

        //Return to original position in the expression
        if ( currentDraggTIle.isUsed() ){

            for (TilePlaceHolder p : exprSpaces){
                if (p.getTile() == currentDraggTIle){
                    currentDraggTIle.setXY(p.x, p.y);
                    break;
                }
            }

        } else {
            //Return to original position in tile buffer

            for (TilePlaceHolder p : tileSpaces){
                if (p.getTile() == currentDraggTIle){
                    currentDraggTIle.setXY(p.x, p.y);
                    break;
                }
            }

        }

    }



}
