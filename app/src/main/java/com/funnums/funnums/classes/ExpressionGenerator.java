package com.funnums.funnums.classes;

import android.nfc.Tag;
import android.util.Log;

import java.util.Arrays;
import java.util.Random;
import java.util.Hashtable;
import java.util.List;
import java.util.Collections;

public class ExpressionGenerator {
    private final String TAG = "ExpressionGenerator";
    //The range of numbers we want generated from 0 to RANGE inclusive
    private final int RANGE = 10;
    private final int MAX_NUMBER_OF_TILE_CHOICES = 10;

    private Random rand = new Random();
    private Hashtable<Integer, Integer> dividends = new Hashtable<>();
    private DividendTable divTable = new DividendTable();
    private ExpressionEvaluator evaluator = new ExpressionEvaluator();

    private int currentExprDifficulty = -1;
    private int currentTarget = -1;

    private int generateNum() {
        //the rand call generates -> 0:RANGE-1, so we add 1 -> 1:RANGE
        int num = rand.nextInt(RANGE) + 1;
        return num;
    }

    /* Takes the number of ops to generate in the expression, generates the ops, then uses
     * the ops the generate the expression. Then the expression is evaluated and saved as the
     * currentTarget. The difficulty of the expression is now calculated to be later used
     * increasing the score. Then we fill in the rest of the expression array with numbers,
     * and shuffle the array so that the expression is out of order and has unnecessary numbers.
     */
    public String[] getNewExpr(int numOps) {
        String[] ops  = generateOps(numOps);
        String[] expr = generateExpr(ops);

        currentTarget = evaluator.evalExpr(expr.toString());
        setExprDifficulty(ops);

        int exprLength = 2 * ops.length + 1;
        int tilesToFill = MAX_NUMBER_OF_TILE_CHOICES - exprLength;
        expr = fillAndShuffleExpr(expr, tilesToFill);
        return expr;
    }

    /* This function is the same as the above one, but instead of generated a number of random
     * operators, it takes in an array of operators to use. Useful for beginning levels when
     * the types of operators should be controlled.
     */
    public String[] getNewExpr(String[] ops) {
        String[] expr = generateExpr(ops);
        Log.d(TAG, "to stringed:" + toString(expr));
        currentTarget = evaluator.evalExpr(toString(expr));
        Log.d(TAG, "target set to:" + Integer.toString(currentTarget));
        setExprDifficulty(ops);

        int exprLength = 2 * ops.length + 1;
        int tilesToFill = MAX_NUMBER_OF_TILE_CHOICES - exprLength;
        expr = fillAndShuffleExpr(expr, tilesToFill);
        return expr;
    }

    // Returns a number of randomly generated operators
    public String[] generateOps(int numOps) {
        String[] ops = new String[numOps];
        for (int i=0; i<numOps; i++) {
            ops[i] = generateOp();
        }
        return ops;
    }

    public String generateOp() {
        int opType = rand.nextInt(4);
        switch (opType) {
            case 0:
                return "+";
            case 1:
                return "-";
            case 2:
                return "*";
            case 3:
                return "/";
        }
        return null;
    }

    public String[] generateExpr(String[] ops) {
        String[] expr = new String[MAX_NUMBER_OF_TILE_CHOICES];

        int dividend = -1; //used for keeping track of the mul/div computation

        // Insert the first number first, as we generate only the second number of each op
        expr[0] = Integer.toString(generateNum());
        int nextInd = 1;
        for (int i=0; i<ops.length; i++) {
            switch(ops[i]) {
                case "+":
                    dividend = -1;
                    expr[nextInd] = "+";
                    nextInd++;
                    expr[nextInd] = Integer.toString(generateNum());
                    nextInd++;
                    break;
                case "-":
                    dividend = -1;
                    expr[nextInd] = "-";
                    nextInd++;
                    expr[nextInd] = Integer.toString(generateNum());
                    nextInd++;
                    break;
                case "*":
                    int num = generateNum();
                    if (dividend == -1) {
                        //use nextInd-1 if we maintain dividend before inserting
                        int prevNum = Integer.parseInt(expr[nextInd-1]);
                        dividend = prevNum * num;
                    }else {
                        dividend *= num;
                    }
                    expr[nextInd] = "*";
                    nextInd++;
                    expr[nextInd] = Integer.toString(num);
                    nextInd++;
                    break;
                case "/":
                    if (dividend == -1) {
                        int prevNum = Integer.parseInt(expr[nextInd-1]);
                        num = divTable.getDivisor(prevNum);
                        dividend = prevNum / num;
                    }else {
                        num = divTable.getDivisor(dividend);
                        dividend /= num;
                    }
                    expr[nextInd] = "/";
                    nextInd++;
                    expr[nextInd] = Integer.toString(num);
                    nextInd++;
                    break;
            }
        }
        return expr;
    }

    /* Fills in expression with random numbers to trick the player, then converts it to a list
     * to use the shuffle() method to shuffle the expression, converts the list back into an array
     * and returns it.
     */
    String[] fillAndShuffleExpr(String[] expr, int tilesToFill) {
        int startFillingIndex = expr.length - tilesToFill + 1;
        for (int i=startFillingIndex; i<expr.length; i++) {
            int randomNum = generateNum();
            expr[i] = Integer.toString(randomNum);
        }
        List<String> exprList = Arrays.asList(expr);
        Collections.shuffle(exprList);
        expr = exprList.toArray(expr);
        return expr;
    }

    //Currently sets difficulty to the number of operations used to generate the expression
    private void setExprDifficulty(String [] ops) {
        currentExprDifficulty = ops.length;
    }

    public int getDifficulty() {
        return currentExprDifficulty;
    }

    public int getTarget() {
        return currentTarget;
    }

    //Converts the array expression into a space separated string
    public String toString(String[] expr) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<expr.length; i++) {
            if (expr[i] != null) {
                stringBuilder.append(expr[i]);
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }
}
