package com.funnums.funnums.classes;

/**
 * Created by Derek on 10/29/2017.
 */
import android.util.Log;
import java.util.Random;

public class BubbleTargetGenerator {
    public static final String TAG = "BubbleTargetGenerator";

    private final int SIZE = 30;        //number of targets to generate upoon intialization
    private final int RANDOM_RANGE = 3; //generates 0:2
    private final int INIT_BASE = 3;    //initial increase base to be added to target
    private final int INIT_IBASE = 0;   //initial ibase used for ibase/RATE
    private final int RATE = 3;         //the increase rate will increase by 1 evert 5 targets

    private int[] Targets = new int[SIZE];
    private int nextTargetIndex = 0;

    private int target = 0;         //target starts at 0
    private int base   = INIT_BASE;
    private int ibase  = INIT_IBASE;
    private Random r = new Random();

    public BubbleTargetGenerator() {
        generateTargets();
    }

    private void generateTargets() {
        for (int i=0; i<SIZE; i++) {
            int increase = base + ibase/RATE + r.nextInt(RANDOM_RANGE);
            target += increase;
            Targets[i] = target;
            ++ibase;
        }
    }

    public void printTargets() {
        for (int i=0; i<SIZE; i++) {
            Log.d(TAG, "Target "+i+" : "+Integer.toString(Targets[i]));
        }
    }

    //Checks if we need to generate more targets before we return the next target
    public int nextTarget() {
        if (nextTargetIndex == SIZE) {
            nextTargetIndex = 0;
            generateTargets();
        }
        int target = Targets[nextTargetIndex];

        nextTargetIndex++;
        return target;
    }
}
