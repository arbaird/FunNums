package com.funnums.funnums.classes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap;

import com.funnums.funnums.maingame.GameActivity;

import java.util.Random;

/**
 * Created by austinbaird on 11/14/17.
 */

public class Cloud {
    public float x, y;
    private static final int VEL_X = -1;

    private static Random r = new Random();

    public int maxYSpawn;
    public int minYSpawn;

    public int size;

    private Bitmap image;


    public Cloud(float x, float y, String imageName) {
        this.x = x;
        this.y = y;

        this.image =  com.funnums.funnums.maingame.GameView.loadBitmap(imageName, false);
    }

    public void update() {

        //float delta = deltaTime*1.0f / com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS;

        x += VEL_X; //* delta;
        if (x <= -image.getWidth()) {
            // wrap x so that cloud respawns on right side of the screen
            x += GameActivity.screenX + image.getWidth();
        }
    }

    public void draw(Canvas canvas, Paint paint){
        canvas.drawBitmap(image, x, y,  paint);
    }

    public int getWidth(){
        return image.getWidth();
    }
}
