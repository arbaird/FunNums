package com.funnums.funnums.classes;

import java.util.Scanner;
import java.util.Stack;
import java.util.NoSuchElementException;
import android.util.Log;

/* This class takes as input an expression as a space seperated string of numbers and operators
   evaluates it. Currently only +, -, *, /, % are supported. The unary negative operator is not supported.
 */
public class ExpressionEvaluator {
    private static final String TAG = "ExpressionEvaluator";

    private Stack<Integer> numSt = new Stack<>(); //the number stack
    private Stack<String>  opSt  = new Stack<>(); //the operator stack

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
        Log.d(TAG, expr + "=" + numSt.peek());
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