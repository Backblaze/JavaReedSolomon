/**
 * One specific ordering/nesting of the coding loops.
 *
 * Copyright 2015, Backblaze, Inc.  All rights reserved.
 */

package com.backblaze.erasure;

public class InputShardIndexTableCodingLoop implements CodingLoop {

    @Override
    public String getName() {
        return "input/shard/index (mult table)";
    }

    @Override
    public void codeSomeShards(
            byte[][] matrixRows,
            byte[][] inputs, int inputCount,
            byte[][] outputs, int outputCount,
            int offset, int byteCount) {

        final byte [] [] table = Galois.MULTIPLICATION_TABLE;

        {
            final int iInput = 0;
            final byte[] inputShard = inputs[iInput];
            for (int iShard = 0; iShard < outputCount; iShard++) {
                final byte[] outputShard = outputs[iShard];
                final byte[] matrixRow = matrixRows[iShard];
                final byte[] multTableRow = table[matrixRow[iInput] & 0xFF];
                for (int iByte = offset; iByte < offset + byteCount; iByte++) {
                    outputShard[iByte] = multTableRow[inputShard[iByte] & 0xFF];
                }
            }
        }

        for (int iInput = 1; iInput < inputCount; iInput++) {
            final byte[] inputShard = inputs[iInput];
            for (int iShard = 0; iShard < outputCount; iShard++) {
                final byte[] outputShard = outputs[iShard];
                final byte[] matrixRow = matrixRows[iShard];
                final byte[] multTableRow = table[matrixRow[iInput] & 0xFF];
                for (int iByte = offset; iByte < offset + byteCount; iByte++) {
                    outputShard[iByte] ^= multTableRow[inputShard[iByte] & 0xFF];
                }
            }
        }
    }

    @Override
    public boolean checkSomeShards(
            byte[][] matrixRows,
            byte[][] inputs, int inputCount,
            byte[][] toCheck, int checkCount,
            int offset, int byteCount) {

        // TODO: structure like the coding loop (use a temporary buffer?)
        //
        // Checking loop doesn't match the coding loop.  Need to figure
        // out how to do it.  The problem is that the coding loop uses
        // the output shards as temporary working space, so the original
        // value isn't there to check after computing the new value.

        byte [] [] table = Galois.MULTIPLICATION_TABLE;
        for (int iByte = offset; iByte < offset + byteCount; iByte++) {
            for (int iRow = 0; iRow < checkCount; iRow++) {
                byte [] matrixRow = matrixRows[iRow];
                int value = 0;
                for (int c = 0; c < inputCount; c++) {
                    value ^= table[matrixRow[c] & 0xFF][inputs[c][iByte] & 0xFF];
                }
                if (toCheck[iRow][iByte] != (byte) value) {
                    return false;
                }
            }
        }
        return true;
    }
}
