package com.funnums.funnums.classes;
/**
 * Created by Derek on 10/30/2017.
 */
import android.util.Log;
import java.util.*;
import java.util.Random;
import java.lang.Math;

public class BubbleNumberGenerator {
    public static final String TAG = "BubbleNumberGenerator";

    /* There are 4 bubble types: Mini, Small, Medium, and Large.
       Each Type corresponds to how large relatively the bubble's number is .
       The CHANCES correspond to the chance for each bubble type to be generated,
       and must add up to 100.
       The FACTORS relate to what fraction of absTarget we generate,
       so a larger FACTOR will mean a smaller generated number.
    */
    private int MINI_CHANCE   = 20;
    private int SMALL_CHANCE  = 35;
    private int MEDIUM_CHANCE = 30;
    private int LARGE_CHANCE  = 15;

    private int MINI_FACTOR   = 5;
    private int SMALL_FACTOR  = 4;
    private int MEDIUM_FACTOR = 3;
    private int LARGE_FACTOR  = 2;

    private int absTarget;

    private Random r = new Random();


    /* We generate a number that is a fraction of the current absolute target,
       and the fraction is determined by the bubble type generated.
     */
    public int nextNum() {
        int num = absTarget;
        int bubbleType = r.nextInt(100); // generates [0:99]
        Log.d(TAG, "Bubble Type Chance: "+bubbleType);
        if (bubbleType < MINI_CHANCE) {
            Log.d(TAG, "Generating MINI bubble");
            num = num/MINI_FACTOR;

        }else if (bubbleType < SMALL_CHANCE) {
            Log.d(TAG, "Generating SMALL bubble");
            num = num/SMALL_FACTOR;

        }else if (bubbleType < MEDIUM_CHANCE) {
            Log.d(TAG, "Generating MEDIUM bubble");
            num = num/MEDIUM_FACTOR;

        }else { //It's a large bubble
            Log.d(TAG, "Generating LARGE bubble");
            num = num/LARGE_FACTOR;
        }
        //We want to return a number that's from 1-2 at minimum
        int min = Math.max(r.nextInt(3), 1);
        return Math.max(num, min);
    }

    public void setAbsoluteTarget(int val) {
        absTarget = val;
    }

}
