package com.funnums.funnums.animation;

/**
 * Info for an individual frame. Just holds the image for a frame and how long the frame lasts in animation
 */



import android.graphics.Bitmap;

public class Frame {
    private Bitmap image;
    private double duration;

    public Frame(Bitmap image, double duration) {
        this.image = image;
        this.duration = duration* com.funnums.funnums.maingame.GameView.NANOS_TO_SECONDS;//duration;
    }

    public double getDuration() {
        return duration;
    }

    public Bitmap getImage() {
        return image;
    }
}