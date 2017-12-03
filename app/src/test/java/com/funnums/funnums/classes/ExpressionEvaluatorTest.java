package com.funnums.funnums.classes;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by austinbaird on 12/2/17.
 */
public class ExpressionEvaluatorTest {
    @Test
    public void getUserExpr() throws Exception {
        ExpressionEvaluator ex = new ExpressionEvaluator();

    }

    @Test
    public void testAddition() throws Exception {
        ExpressionEvaluator ex = new ExpressionEvaluator();
        assertEquals(10, ex.evalExpr("8 + 2"));
    }

    @Test
    public void testDivision() throws Exception {
        ExpressionEvaluator ex = new ExpressionEvaluator();
        assertEquals(4, ex.evalExpr("8 / 2"));
    }

    @Test
    public void testSubtraction() throws Exception {
        ExpressionEvaluator ex = new ExpressionEvaluator();
        assertEquals(6, ex.evalExpr("8 - 2"));
    }

    @Test
    public void testMultiplication() throws Exception {
        ExpressionEvaluator ex = new ExpressionEvaluator();
        assertEquals(16, ex.evalExpr("8 * 2"));
    }

    @Test
    public void testLongExpression() throws Exception {
        ExpressionEvaluator ex = new ExpressionEvaluator();
        assertEquals(22, ex.evalExpr("8 + 2 + 3 + 4 + 5"));
    }

    @Test
    public void testPrecedence() throws Exception {
        ExpressionEvaluator ex = new ExpressionEvaluator();
        assertEquals(23, ex.evalExpr("3 + 4 * 5"));
    }


}