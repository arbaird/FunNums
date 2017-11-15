package com.funnums.funnums.classes;

import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import java.util.Random;

public class DividendTable {

    public class DivisorList {
        public final int MAX_BIG_DIVISOR   = 25; //to limit what the max generated divsor should be
        public final int MAX_SMALL_DIVISOR = 12;
        private Vector<Integer> divisors = new Vector<>();
        private int nextInsertIndex   = 0;

        public int indexOfMaxBigDivisor = 0; //the index of the highest divsor < MAX_DIVISOR;
        public int indexOfMaxSmallDivisor = 0;

        public void add(int divisor) {
            if (divisor <= MAX_BIG_DIVISOR) {
                indexOfMaxBigDivisor = nextInsertIndex;
            }
            if (divisor <= MAX_SMALL_DIVISOR) {
                indexOfMaxSmallDivisor = nextInsertIndex;
            }
            divisors.add( new Integer(divisor) );
            nextInsertIndex++;
        }

        public int get(int index) {
            return divisors.get(index);
        }

        public int size() {
            return divisors.size();
        }

        public Iterator<Integer> iterator() {
            return divisors.iterator();
        }
    }

    private final int MAX_GENERATED_NUM = 1000;
    private final int SMALL_NUM = 144;

    private HashMap<Integer, DivisorList> dividends =
            new HashMap<>();

    public DividendTable() {
        generateTable(MAX_GENERATED_NUM);
    }

    public DividendTable(int maxGeneratedNum) {
        generateTable(maxGeneratedNum);
    }

    private void generateTable(int maxProduct) {
        for (int currentNum=1; currentNum<maxProduct+1; currentNum++) {
            DivisorList divisorList = new DivisorList();

            for (int divisor=1; divisor<maxProduct; divisor++) {
                if (currentNum % divisor == 0) {
                    divisorList.add(divisor);
                }
            }
            dividends.put( new Integer(currentNum), divisorList );
        }
    }

    public int getDivisor(int dividend) {
        DivisorList divisors = dividends.get(new Integer(dividend));
        if (dividend <= SMALL_NUM) {
            return getLimitedDivisor1(divisors);
        }
        return getLimitedDivisor2(divisors);
    }

    public int getLimitedDivisor1(DivisorList divisors) {
        Random rand = new Random();
        int index = rand.nextInt(divisors.indexOfMaxSmallDivisor + 1);
        return divisors.get(index);
    }
    public int getLimitedDivisor2(DivisorList divisors) {
        Random rand = new Random();
        int index = rand.nextInt(divisors.indexOfMaxBigDivisor + 1);
        return divisors.get(index);
    }

    /*
    public void printValues() {
        dividends.forEach((dividend, divisors) -> {
            System.out.print(dividend + "   ");
            Iterator<Integer> iter = divisors.iterator();
            while(iter.hasNext()) {
                System.out.print(" " + iter.next());
            }
            System.out.println();
        });
    }
    */
    public void testGetDivisor(int dividend) {
        System.out.print(dividend + "   ");
        for (int i=0; i<5; i++) {
            System.out.print(" " + getDivisor(dividend));
        }
        System.out.println();
    }
}
