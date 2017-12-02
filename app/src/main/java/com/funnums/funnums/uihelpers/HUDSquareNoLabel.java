package com.funnums.funnums.uihelpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap;

/**
 * Created by austinbaird on 11/30/17.
 */

public class HUDSquareNoLabel extends HUDSquare {
    public HUDSquareNoLabel(float x, float y, float width, float height, String value, Paint paint, Bitmap image) {
        super(x, y, width, height, value, value, paint, image);
        this.image = Bitmap.createScaledBitmap(image, (int)width, (int)height-MARGIN*2 ,false);
    }

    /*
       Draw only the value, no label
    */
    public void draw(Canvas canvas, Paint paint, String val){
        //draw border
        /*paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);*/
        canvas.drawBitmap(image, left, top,  paint);

        //fill the square, less large since we are only drawing the value
        /*paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(left, top, right, bottom-MARGIN*2, paint);*/

        //prepare the text
        paint.setColor(Color.argb(255, 255, 0, 0));
        paint.setTextSize(TEXT_SIZE);
        paint.setTextScaleX(xScale);
        paint.setTextAlign(Paint.Align.CENTER);
        //draw the value
        canvas.drawText(val, (left + right)/2 , (top + bottom)/2, paint);
    }
}
