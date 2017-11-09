package com.funnums.funnums.animation;

/**
 * Created by austinbaird on 11/7/17.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class Animation {
    private Frame[] frames;
    private double[] frameEndTimes;
    private int currentFrameIndex = 0;
    private double totalDuration = 0;
    private double currentTime = 0;

    //boolean to control if animation is playing or not
    public boolean playing;

    public Animation(Frame... frames) {
        this.frames = frames;
        //initially animatins are static, unchanging
        playing = false;
        frameEndTimes = new double[frames.length];
        //get a recode of frame end times so we know when to change frames
        for (int i = 0; i < frames.length; i++) {
            Frame f = frames[i];
            totalDuration += f.getDuration();
            frameEndTimes[i] = totalDuration;
        }
    }

    public void start(){
        playing = true;
    }


    public synchronized void update(float increment) {
        //if animation is not playing, do nothing
        if(!playing)
            return;
        currentTime += increment;
        //if current time excceds total duration of this animation, reset to first frame and stop playing
        //we can make this more configurable for different animations, i.e if we want animation to loop
        //or want it to stop on last frame without resetting
        if (currentTime > totalDuration) {
            wrapAnimation();
            playing = false;
        }
        //if enough time has passed, move to next frame in animation
        while (currentTime > frameEndTimes[currentFrameIndex]) {
            currentFrameIndex++;
        }
    }

    /*
        restart the animation from first frame
     */
    public synchronized void restart(){
        currentFrameIndex = 0;
        currentTime = 0;
        playing = true;
    }

    /*
        move current frame to first frame
     */
    private synchronized void wrapAnimation() {
        currentFrameIndex = 0;
        currentTime %= totalDuration;
    }

    public synchronized void render(Canvas g, int x, int y, Paint p) {
        g.drawBitmap(frames[currentFrameIndex].getImage(), x, y, p);
    }

    /*
        render a scaled image, given a width and height for scaling
     */
    public synchronized void render(Canvas canvas, Paint p, int x, int y, int width,
                                    int height) {
        Bitmap currentFrameImage = frames[currentFrameIndex].getImage();
        Rect srcRect = new Rect();
        srcRect.set(0, 0, currentFrameImage.getWidth(), currentFrameImage.getHeight());
        //get rect for scaled image, based on given width and height
        Rect dstRect = new Rect();

        dstRect.set(x, y, x + width, y + height);
        canvas.drawBitmap(currentFrameImage, srcRect, dstRect, p);
    }
}