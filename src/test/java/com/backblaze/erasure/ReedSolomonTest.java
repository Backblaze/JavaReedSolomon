/**
 * Unit tests for ReedSolomon
 *
 * Copyright 2015, Backblaze, Inc.  All rights reserved.
 */

package com.backblaze.erasure;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the ReedSolomon class.
 *
 * Most of the test cases were copied from the Python code to
 * make sure that the Java code does the same thing.
 */
public class ReedSolomonTest {

    /**
     * Zero size encode.
     */
    @Test
    public void testZeroSizeEncode() {
        ReedSolomon codec = ReedSolomon.create(2, 1);
        byte [] [] shards = new byte [3] [0];
        codec.encodeParity(shards, 0, 0);
    }

    /**
     * Make sure that the results on a tiny encoding match what
     * the prototype Python code did, and that all of the different
     * coding loops produce the same answers.
     */
    @Test
    public void testOneEncode() {
        for (CodingLoop codingLoop : CodingLoop.ALL_CODING_LOOPS) {
            ReedSolomon codec = new ReedSolomon(5, 5, codingLoop);
            byte[][] shards = new byte[10][];
            shards[0] = new byte[]{0, 1};
            shards[1] = new byte[]{4, 5};
            shards[2] = new byte[]{2, 3};
            shards[3] = new byte[]{6, 7};
            shards[4] = new byte[]{8, 9};
            shards[5] = new byte[2];
            shards[6] = new byte[2];
            shards[7] = new byte[2];
            shards[8] = new byte[2];
            shards[9] = new byte[2];
            codec.encodeParity(shards, 0, 2);
            assertArrayEquals(new byte[]{12, 13}, shards[5]);
            assertArrayEquals(new byte[]{10, 11}, shards[6]);
            assertArrayEquals(new byte[]{14, 15}, shards[7]);
            assertArrayEquals(new byte[]{90, 91}, shards[8]);
            assertArrayEquals(new byte[]{94, 95}, shards[9]);

            assertTrue(codec.isParityCorrect(shards, 0, 2));
            shards[8][0] += 1;
            assertFalse(codec.isParityCorrect(shards, 0, 2));
        }
    }

    /**
     * Checks that all of the coding loops produce the same results.
     */
    @Test
    public void testCodingLoopsProduceSameAnswers() {

    }

    /**
     * Encodes a set of data shards, and then tries decoding
     * using all possible subsets of the encoded shards.
     *
     * Uses 5+5 coding, so there must be 5 input data shards.
     */
    private void runEncodeDecode(byte[][] dataShards) {

        final int shardLength = dataShards[0].length;

        // Make the list of data and parity shards.
        assertEquals(5, dataShards.length);
        final int dataLength = dataShards[0].length;
        byte [] [] allShards = new byte [10] [];
        for (int i = 0; i < 5; i++) {
            allShards[i] = Arrays.copyOf(dataShards[i], dataLength);
        }
        for (int i = 5; i < 10; i++) {
            allShards[i] = new byte [dataLength];
        }

        // Encode.
        ReedSolomon codec = ReedSolomon.create(5, 5);
        codec.encodeParity(allShards, 0, dataLength);

        // Make a copy to decode with.
        byte [] [] testShards = new byte [10] [];
        boolean [] shardPresent = new boolean [10];
        for (int i = 0; i < 10; i++) {
            testShards[i] = Arrays.copyOf(allShards[i], shardLength);
            shardPresent[i] = true;
        }

        // Decode with 0, 1, ..., 5 shards missing.
        for (int numberMissing = 0; numberMissing < 6; numberMissing++) {
            tryAllSubsetsMissing(codec, allShards, testShards, shardPresent, numberMissing);
        }
    }

    private void tryAllSubsetsMissing(ReedSolomon codec,
                                      byte [] [] allShards, byte [] [] testShards,
                                      boolean [] shardPresent, int numberMissing) {
        final int shardLength = allShards[0].length;
        List<int []> subsets = allSubsets(numberMissing, 0, 10);
        for (int [] subset : subsets) {
            // Get rid of the shards specified by this subset.
            for (int missingShard : subset) {
                clearBytes(testShards[missingShard]);
                shardPresent[missingShard] = false;
            }

            // Reconstruct the missing shards
            codec.decodeMissing(testShards, shardPresent, 0, shardLength);

            // Check the results.  After checking, the contents of testShards
            // is ready for the next test, the next time through the loop.
            checkShards(allShards, testShards);

            // Put the "present" flags back
            for (int i = 0; i < codec.getTotalShardCount(); i++) {
                shardPresent[i] = true;
            }
        }
    }

    private void clearBytes(byte [] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    private void checkShards(byte[][] expectedShards, byte[][] actualShards) {
        assertEquals(expectedShards.length, actualShards.length);
        for (int i = 0; i < expectedShards.length; i++) {
            assertArrayEquals(expectedShards[i], actualShards[i]);
        }
    }

    /**
     * Returns a list of arrays with all possible sets of
     * unique values where (min <= n < max).
     *
     * This is NOT EFFICIENT, because it allocates lots of
     * temporary arrays, but it's OK for these tests.
     *
     * To avoid duplicates that are in a different order,
     * each subset is generated with elements in increasing
     * order.
     *
     * Given (n=2, min=1, max=4), returns:
     *    [1, 2]
     *    [1, 3]
     *    [1, 4]
     *    [2, 3]
     *    [2, 4]
     *    [3, 4]
     */
    private List<int []> allSubsets(int n, int min, int max) {
        List<int []> result = new ArrayList<int[]>();
        if (n == 0) {
            result.add(new int [0]);
        }
        else {
            for (int i = min; i < max - n; i++) {
                int [] prefix = { i };
                for (int [] suffix : allSubsets(n - 1, i + 1, max)) {
                    result.add(appendIntArrays(prefix, suffix));
                }
            }
        }
        return result;
    }

    private int [] appendIntArrays(int [] a, int [] b) {
        int [] result = new int[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
