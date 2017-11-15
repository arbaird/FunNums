package com.funnums.funnums.classes;

import java.util.Random;
import java.util.Hashtable;

public class ExpressionGenerator {

    private DividendTable divTable = new DividendTable();
    private Random rand = new Random();
    private Hashtable<Integer, Integer> dividends =
            new Hashtable<>();

    private int generateNum() {
        //the rand call generates 0:9
        int num = rand.nextInt(10) + 1;
        return num;
    }

    public String[] getExpr(String[] ops) {
        int exprLen = 2 * ops.length + 1;
        String[] expr = new String[exprLen];

        int dividend = -1; //used for keeping track of the mul/div compatiation

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

    /*
    public void printExpr(String[] expr) {
        System.out.print("expr =   ");
        for (int i=0; i<expr.length; i++) {
            System.out.print(expr[i] + ' ');
        }
        System.out.println();
    }
    */

}
