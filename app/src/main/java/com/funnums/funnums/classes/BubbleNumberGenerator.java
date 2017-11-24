package com.funnums.funnums.classes;
/**
 * Created by Derek on 10/30/2017.
 */
import android.util.Log;
import java.util.Random;
import java.util.HashMap;
import java.lang.Math;

public class BubbleNumberGenerator {
    public static final String TAG = "BubbleNumberGenerator";

    private final int MAX_UNCHECKED_NUM = 2;
    private final int MAX_VALUE_REPETITIONS = 0;

    private HashMap<Integer, Integer> CurrentValues = new HashMap<>();
    private Random r = new Random();

    private int absTarget; //the initial absolute difference between Current and Target

    /* We generate a number from 1 to the (current absolute target - 1)
       If the number is not 1 or 2, we check if it's in the hastable, and if it is,
       then we generate a another number - we do this to maintain variety in the numbers
       generated, but we don't check for 1 or 2 because those are the smallest prime numbers
       which are useful for reaching the target.
     */
    public int nextNum() {
        int maxNum = absTarget - 1;

        int newNum = r.nextInt(maxNum + 1); //to inclusively generate maxNum, we add 1

        while (newNum > MAX_UNCHECKED_NUM         &&
               CurrentValues.get(newNum) != null  &&
               CurrentValues.get(newNum) > MAX_VALUE_REPETITIONS)   {

            newNum = r.nextInt(maxNum + 1);
        }
        return Math.max(newNum, 1);
    }

    public void increment(int value) {
        if (!CurrentValues.containsKey(value)) {
            CurrentValues.put(value, 0);
        }
        int oldCount = CurrentValues.get(value);
        CurrentValues.put(value, oldCount + 1);
    }

    public void decrement(int value) {
        int oldCount = CurrentValues.get(value);
        CurrentValues.put(value, oldCount - 1);
    }

    public void setAbsoluteTarget(int val) {
        absTarget = val;
    }
}

