package com.funnums.funnums.classes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.funnums.funnums.maingame.GameActivity;

import java.util.Random;

/**
 * Created by austinbaird on 11/14/17.
 */

public class Cloud {
    public float x, y;
    private static final int VEL_X = -2;

    private static Random r = new Random();

    public int maxYSpawn;
    public int minYSpawn;

    public int size;


    public Cloud(float x, float y, int minY, int maxY, int size) {
        this.x = x;
        this.y = y;

        Log.d("CLOUD", "X: " + x + " Y: " + y);

        maxYSpawn = GameActivity.screenY/2;
        minYSpawn = GameActivity.screenY/8;

        this.minYSpawn = minY;
        this.maxYSpawn = maxY;

        this.size = size;
    }

    public void update(long deltaTime) {

        //float delta = deltaTime*1.0f / com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS;

        x += VEL_X; //* delta;
        if (x <= -200) {
            // wrap x so that cloud respawns on right side of the screen
            x += GameActivity.screenX + 400;
            // new y spawn point is between the given max and min span points
            y = r.nextInt(maxYSpawn - minYSpawn) + minYSpawn;
        }
    }

    public void draw(Canvas canvas, Paint paint){
        paint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawCircle(x, y, size, paint);
    }
}
