package com.funnums.funnums.classes;

/**
 * Created by Derek on 10/29/2017.
 */
import android.util.Log;
import java.util.Random;

public class BubbleTargetGenerator {
    public static final String TAG = "BubbleTargetGenerator";

    private final int SIZE = 30;        //number of targets to generate upoon intialization
    private final int RANDOM_RANGE = 2; //generates 0:1
    private final int INIT_TARGET = 3;  //initial target without increase added
    private final int INIT_BASE = 0;    //initial increase base to be added to target
    private final int RATE = 5;         //ex: the base increase rate will increase by 1 ever 5 targets

    private int[] Targets = new int[SIZE];
    private int nextTargetIndex = 0;

    private int target = INIT_TARGET;
    private int base = INIT_BASE;
    private Random r = new Random();

    public BubbleTargetGenerator() {
        generateTargets();
    }

    private void generateTargets() {
        for (int i=0; i<SIZE; i++) {
            int increase = base + base/RATE + r.nextInt(RANDOM_RANGE);
            target += increase;
            Targets[i] = target;
            base++;
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
