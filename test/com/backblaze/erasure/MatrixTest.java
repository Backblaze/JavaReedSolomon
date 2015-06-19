/**
 * Unit tests for Matrix
 *
 * Copyright 2015, Backblaze, Inc.  All rights reserved.
 */

package com.backblaze.erasure;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MatrixTest {

    @Test
    public void testIdentity() {
        assertEquals(
                "[[1, 0, 0], [0, 1, 0], [0, 0, 1]]",
                Matrix.identity(3).toString()
        );
    }

    @Test
    public void testBigString() {
        assertEquals("01 00 \n00 01 \n", Matrix.identity(2).toBigString());
    }

    @Test
    public void testMultiply() {
        Matrix m1 = new Matrix(
                new byte [] [] {
                        new byte [] { 1, 2 },
                        new byte [] { 3, 4 }
                });
        Matrix m2 = new Matrix(
                new byte [] [] {
                        new byte [] { 5, 6 },
                        new byte [] { 7, 8 }
                });
        Matrix actual = m1.times(m2);
        // correct answer from java_tables.py
        assertEquals("[[11, 22], [19, 42]]", actual.toString());
    }

    @Test
    public void inverse() {
        Matrix m = new Matrix(
                new byte [] [] {
                    new byte [] { 56, 23, 98 },
                    new byte [] { 3, 100, (byte)200 },
                    new byte [] { 45, (byte)201, 123 }
                });
        assertEquals(
                "[[175, 133, 33], [130, 13, 245], [112, 35, 126]]",
                m.invert().toString()
        );
        assertEquals(
                Matrix.identity(3).toString(),
                m.times(m.invert()).toString()
        );
    }

    @Test
    public void inverse2() {
        Matrix m = new Matrix(
            new byte [] [] {
                new byte [] { 1, 0, 0, 0, 0 },
                new byte [] { 0, 1, 0, 0, 0 },
                new byte [] { 0, 0, 0, 1, 0 },
                new byte [] { 0, 0, 0, 0, 1 },
                new byte [] { 7, 7, 6, 6, 1 }
            }
        );
        assertEquals(
                "[[1, 0, 0, 0, 0]," +
                " [0, 1, 0, 0, 0]," +
                " [123, 123, 1, 122, 122]," +
                " [0, 0, 1, 0, 0]," +
                " [0, 0, 0, 1, 0]]",
                m.invert().toString()
        );
        assertEquals(
                Matrix.identity(5).toString(),
                m.times(m.invert()).toString()
        );
    }
}
