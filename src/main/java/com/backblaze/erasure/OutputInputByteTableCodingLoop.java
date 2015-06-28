/**
 * One specific ordering/nesting of the coding loops.
 *
 * Copyright 2015, Backblaze, Inc.  All rights reserved.
 */

package com.backblaze.erasure;

public class OutputInputByteTableCodingLoop extends CodingLoopBase {

    @Override
    public void codeSomeShards(
            byte[][] matrixRows,
            byte[][] inputs, int inputCount,
            byte[][] outputs, int outputCount,
            int offset, int byteCount) {

        final byte [] [] table = Galois.MULTIPLICATION_TABLE;
        for (int iShard = 0; iShard < outputCount; iShard++) {
            final byte [] outputShard = outputs[iShard];
            final byte[] matrixRow = matrixRows[iShard];
            {
                final int iInput = 0;
                final byte [] inputShard = inputs[iInput];
                final byte [] multTableRow = table[matrixRow[iInput] & 0xFF];
                for (int iByte = offset; iByte < offset + byteCount; iByte++) {
                    outputShard[iByte] = multTableRow[inputShard[iByte] & 0xFF];
                }
            }
            for (int iInput = 1; iInput < inputCount; iInput++) {
                final byte [] inputShard = inputs[iInput];
                final byte [] multTableRow = table[matrixRow[iInput] & 0xFF];
                for (int iByte = offset; iByte < offset + byteCount; iByte++) {
                    outputShard[iByte] ^= multTableRow[inputShard[iByte] & 0xFF];
                }
            }
        }
    }
}
