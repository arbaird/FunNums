package com.funnums.funnums.uihelpers;

/**
 * Created by austinbaird on 10/10/17.
 */
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;


//make canvas text fade, move, etc
//TODO, make text that can move across screen, if we ned that
public class TextAnimator {
    //text to display
    String text;
    //current elapsed time
    float currentElapsed;
    //interval thhat we want to update alpha, move text, etc.
    double interval;
    //current alpha of the text, i.e how faded the text is
    public int alpha;
    //coors
    float x, y;
    //color values
    int r,g,b;
    //size of text
    int size;

    public TextAnimator(String text, float x, float y, int r, int g, int b) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
        this.b = b;

        //start text as being fully visible, i.e not faded at all
        alpha = 255;

        //if no interval is given, default to updating every tenth of a second
        interval = com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS * 0.1;
        size = 40;
    }

    public TextAnimator(String text, int x, int y, int r, int g, int b, double interval, int size) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
        this.b = b;
        this.size = size;

        alpha = 255;

        //convert interval to NanoSceonds
        this.interval = com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS * interval;
    }



    /*
        If the current elapsed time exceeds the interval, fade the text a little
     */
    public void update(float increment) {
        currentElapsed += increment;
        if(currentElapsed > interval) {
            alpha = Math.max(0, alpha -= 10);
        }
    }

    /*
        Draw the text
     */
    public void render(Canvas canvas, Paint paint) {
        paint.setTextSize(size);
        paint.setColor(Color.argb(alpha, r, g, b));
        canvas.drawText(text, x, y, paint);
    }

}
