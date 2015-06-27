/**
 * One specific ordering/nesting of the coding loops.
 *
 * Copyright 2015, Backblaze, Inc.  All rights reserved.
 */

package com.backblaze.erasure;

public class IndexShardInputExpCodingLoop implements CodingLoop {

    @Override
    public String getName() {
        return "index/shard/input (exp table)";
    }

    @Override
    public void codeSomeShards(
            byte[][] matrixRows,
            byte[][] inputs, int inputCount,
            byte[][] outputs, int outputCount,
            int offset, int byteCount) {

        // This is the inner loop.  It needs to be fast.  Be careful
        // if you change it.
        //
        // Note that dataShardCount is final in the class, so the
        // compiler can load it just once, before the loop.  Explicitly
        // adding a local variable does not make it faster.
        //
        // I have tried inlining Galois.multiply(), but it doesn't
        // make things any faster.  The JIT compiler is known to inline
        // methods, so it's probably already doing so.
        //
        // This method has been timed and compared with a C implementation.
        // This Java version is only about 10% slower than C.

        for (int iByte = offset; iByte < offset + byteCount; iByte++) {
            for (int iRow = 0; iRow < outputCount; iRow++) {
                byte [] matrixRow = matrixRows[iRow];
                int value = 0;
                for (int c = 0; c < inputCount; c++) {
                    value ^= Galois.multiply(matrixRow[c], inputs[c][iByte]);
                }
                outputs[iRow][iByte] = (byte) value;
            }
        }
    }

    @Override
    public boolean checkSomeShards(
            byte[][] matrixRows,
            byte[][] inputs, int inputCount,
            byte[][] toCheck, int checkCount,
            int offset, int byteCount) {

        // This is the inner loop.  It needs to be fast.  Be careful
        // if you change it.
        //
        // Note that dataShardCount is final in the class, so the
        // compiler can load it just once, before the loop.  Explicitly
        // adding a local variable does not make it faster.
        //
        // I have tried inlining Galois.multiply(), but it doesn't
        // make things any faster.  The JIT compiler is known to inline
        // methods, so it's probably already doing so.
        //
        // This method has been timed and compared with a C implementation.
        // This Java version is only about 10% slower than C.

        for (int iByte = offset; iByte < offset + byteCount; iByte++) {
            for (int iRow = 0; iRow < checkCount; iRow++) {
                byte [] matrixRow = matrixRows[iRow];
                int value = 0;
                for (int c = 0; c < inputCount; c++) {
                    value ^= Galois.multiply(matrixRow[c], inputs[c][iByte]);
                }
                if (toCheck[iRow][iByte] != (byte) value) {
                    return false;
                }
            }
        }
        return true;
    }
}
