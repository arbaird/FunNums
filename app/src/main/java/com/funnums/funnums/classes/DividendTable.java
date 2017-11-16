package com.funnums.funnums.classes;

import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import java.util.Random;

public class DividendTable {

    // The container for a list of divisors that integrally divide the dividend
    public class DivisorList {
        public int indexOfMaxBigDivisor   = 0; //the index of the biggest divisor < MAX_DIVISOR;
        public int indexOfMaxSmallDivisor = 0; //The index of the biggest divisor < MAX_SMALL_DIVISOR

        private Vector<Integer> divisors = new Vector<>();
        private int nextInsertIndex = 0;

        /* Adds the given divisor into  the list.
         * Before we insert the divisor into the list, we update the indexOfMaxBigDivisor
         * or the indexofMaxSmallDivisor, to maintain where the biggest divisors less than
         * our predefined limit are located. This is so that when we choose a divisor,
         * the number is within our limits.
         */
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

    private final int SMALL_DIVIDEND    = 144; //What number if classified as a small dividend
    public  final int MAX_BIG_DIVISOR   = 25;  //The biggest divisor for divs >  SMALL_DIVIDEND
    public  final int MAX_SMALL_DIVISOR = 12;  //The biggest divisor for divs <= SMALL_DIVIDEND

    private final int MAX_GENERATED_NUM  = 10; //The max number normally generated in +,- operations
    private final int MAX_MULTS_IN_A_ROW = 3;  //The max number of mult ops in a row in an expr.
    private final int MAX_PRODUCT        = (int)Math.pow(MAX_GENERATED_NUM, MAX_MULTS_IN_A_ROW);

    private HashMap<Integer, DivisorList> dividends = new HashMap<>();

    public DividendTable() {
        generateTable(MAX_PRODUCT);
    }

    /* Enters all dividends from 1 to maxProduct into the hashtable where the
     * key are dividends and the values are DivisorList that's a container
     * for a vector of divisors that divide the dividend integrally.
     */
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

    // Gets a divisor depending on how big the dividend is
    public int getDivisor(int dividend) {
        DivisorList divisors = dividends.get(new Integer(dividend));
        if (dividend <= SMALL_DIVIDEND) {
            return getSmallDivisor(divisors);
        }
        return getBigDivisor(divisors);
    }

    /* These functions retrieve an random divisor using the range of the indexofMaxDivisors
     * to limit how big a divisor is returned.
     */
    public int getSmallDivisor(DivisorList divisors) {
        Random rand = new Random();
        int index = rand.nextInt(divisors.indexOfMaxSmallDivisor + 1); //add 1 to get get actual index
        return divisors.get(index);
    }
    public int getBigDivisor(DivisorList divisors) {
        Random rand = new Random();
        int index = rand.nextInt(divisors.indexOfMaxBigDivisor + 1);
        return divisors.get(index);
    }
}
