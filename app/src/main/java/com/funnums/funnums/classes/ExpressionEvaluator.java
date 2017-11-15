package com.funnums.funnums.classes;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;
import android.util.Log;

/* This class takes as input an expression as a space seperated string of numbers and operators
   evaluates it. Currently only +, -, *, /, % are supported. The unary negative operator is not supported.

   Whenever a player adds or removes a 'tile" from the expression, insert() and delete() need to be
   called to maintain ActiveSlots. For the index in insert(), pass in the index where the player
   inserted the num or op: E.g _ _ _ + _ _ _ -> pass in 3 for index. Use isExpr() whenever the player
   adds or removes a tile to automatically check if the expression is legal-- and if it is, use
   evalExpr() to evaluate the expression.
 */
public class ExpressionEvaluator {

    public class ActiveSlots {
        /* exprArr represents the expression, where the val in each index is a num or op
         * typeArr represents the what type of val is at the index of the exprArr

         * The numbers below indicate that at this index in exprArr, the value is:
         *  0 : not active(either deleted or the index has not been used yet)
         *  1 : a number
         * -1 : an operator
         * We intialize the type array with values of 0 so that none of them are active
        */
        private final int MAX_EXPR_SIZE = 7;

        private int activeIndexes = 0; //The number of active indexes in the exprArr
        private String[] exprArr = new String[MAX_EXPR_SIZE];
        private int[]    typeArr = new int   [MAX_EXPR_SIZE];

        private ActiveSlots() {
            Arrays.fill(typeArr, 0);
        }

        /* insert() and delete() are used to maintain which indexes in our expression array are active
         * The type of the value at the index is determined in the try/catch block.
         * If we can parse the value to an int, then it's an int, otherwise it's an op
         */
        public void insert(String token, int index) {
            exprArr[index] = token;
            activeIndexes++;
            try {
                Integer.parseInt(token);
                typeArr[index] = 1;
            }catch(NumberFormatException e) {
                typeArr[index] = -1;
            }
        }

        public void delete(int index) {
            typeArr[index] = 0;
            activeIndexes--;
        }

        public void clearSlots() {
            activeIndexes = 0;
            Arrays.fill(typeArr, 0);
        }
    }

    private static final String TAG = "ExpressionEvaluator";

    private Stack<Integer> numSt = new Stack<>(); //the number stack
    private Stack<String>  opSt  = new Stack<>(); //the operator stack

    public ActiveSlots slots = new ActiveSlots();

    /* Scans through the expression array to test if the values in it represent an expression.
     * If it's not an expression "false" is returned, else the space-separated expression is returned.
     * -If the expr is length 1 or and even length, then return null
     * -If the the scanned index doesn't equal the next type, then return null
     * The nextType is multiplied by -1 each time because the check has to flip between op and num
     */
    public String getUserExpr() {
        String expr  = "";
        int size     = slots.activeIndexes;
        int maxSize  = slots.MAX_EXPR_SIZE;
        int nextType = 1; //The first thing encountered has to be a number

        if (size == 1 || (size % 2 == 0)) {
            return null;
        }
        for (int i=0, count=0; i<maxSize && count<size; i++) {
            if (slots.typeArr[i] == 0)        continue;
            if (slots.typeArr[i] != nextType) return null;

            expr = expr + slots.exprArr[i] + " ";
            count++;
            nextType *= -1;
        }
        return expr;
    }

    /* Evaluates the expression given as a string and returns it.
     * Loops through the tokens.
     * If the tok is a num, then we push it to the num-stack
     * If's an operator and the op-stack is empty, then we push it
     * Else if the tok-op's prec is > than op-stack.top's prec
     * we push the tok-op to the op-stack
     * Else while the op-stack != empty and the tok-op's prec is
     * not > than op-stack.top's prec, we execute the op-stack once
     * When the above condition is finally false, then we push tok-op
     * Once there are no more tokens, if the op-stack is still not empty,
     * then we execute the entire op-stack, fully evaluating the expr.
     */
    public int evalExpr(String expr) {
        if (expr == null) return -9999;

        Scanner sc = new Scanner(expr);
        while(sc.hasNext()) {
            String token = sc.next();
            try {
                int num = Integer.parseInt(token);
                numSt.push(num);
            } catch (NumberFormatException e) {
                if (opSt.empty()) {
                    opSt.push(token);
                }else if (hasGreaterPrec(token, opSt.peek())) {
                    opSt.push(token);
                }else {
                    while(!opSt.empty() &&
                            !hasGreaterPrec(token, opSt.peek())) {
                        executeOp(opSt, numSt);
                    }
                    opSt.push(token);
                }
            }
        }
        while(!opSt.empty()) {
            executeOp(opSt, numSt);
        }
        //Log.d(TAG, "User expr: " +expr + "    = " + numSt.peek());
        return numSt.pop();
    }

    // Tests if operator1 has higher precedence than operator2
    private boolean hasGreaterPrec(String op1, String op2) {
        int opPrec1 = getOpPrec(op1);
        int opPrec2 = getOpPrec(op2);
        return (opPrec1 > opPrec2);
    }

    // Returns the precendence level of the given operator.
    // The higher the number, the higher the precedence.
    // Returns -1 if the op is not recognized.
    private int getOpPrec(String op) {
       switch(op) {
           case "+":
           case "-":
               return 1;
           case "*":
           case "/":
           case "%":
               return 2;
       }
       return -1;
   }

    // Pops an operator, and 2 numbers from the stacks to evaluate the expr
    private void executeOp(Stack<String> opSt, Stack<Integer> numSt) {
        int num2 = numSt.pop();
        int num1 = numSt.pop();
        String op = opSt.pop();

        switch(op) {
            case "+":
                numSt.push(num1 + num2);
                break;
            case "-":
                numSt.push(num1 - num2);
                break;
            case "*":
                numSt.push(num1 * num2);
                break;
            case "/":
                numSt.push(num1 / num2);
                break;
            case "%":
                numSt.push(num1 % num2);
                break;
        }
    }
}