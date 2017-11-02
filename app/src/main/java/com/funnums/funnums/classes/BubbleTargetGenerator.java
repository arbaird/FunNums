package com.funnums.funnums.classes;

/**
 * Created by Derek on 10/29/2017.
 */
import android.util.Log;
import java.util.Random;

public class BubbleTargetGenerator {
    public static final String TAG = "BubbleTargetGenerator";

    private final int SIZE = 30;        // number of targets to generate upon intialization
    private int base = 0;               // nitial increase base to be added to target
    private final int RATE = 5;         // ex: the base increase rate will increase by 1 ever 5 targets
    private final int RANDOM_RANGE = 2; // generates between 0 and 1 less than value. 0:1 in this case
    private int target = 3;             // initial target without increase added


    private int[] Targets = new int[SIZE];
    private int nextTargetIndex = 0;
    private int previousTarget = 0; //used for resetting Current to the previous Target, when the target is missed

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
        previousTarget = target;

        nextTargetIndex++;
        return target;
    }

    public int getPreviousTarget() {
        return previousTarget;
    }
}
