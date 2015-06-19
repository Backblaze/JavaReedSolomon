/**
 * Unit tests for Galois
 *
 * Copyright 2015, Backblaze, Inc.
 */
package com.backblaze.erasure;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * This is a totally paranoid test that ensure that the Galois class
 * actually implements a field, with all of the properties that a field
 * must have.
 */
public class GaloisTest {

    @Test
    public void testClosure() {
        // Unlike the Python implementation, there is no need to test
        // for closure.  Because add(), subtract(), multiply(), and
        // divide() all return bytes, there's no way they could
        // possible return something outside the field.
    }

    @Test
    public void testAssociativity() {
        for (int i = -128; i < 128; i++) {
            byte a = (byte) i;
            for (int j = -128; j < 128; j++) {
                byte b = (byte) j;
                for (int k = -128; k < 128; k++) {
                    byte c = (byte) k;
                    assertEquals(
                            Galois.add(a, Galois.add(b, c)),
                            Galois.add(Galois.add(a, b), c)
                    );
                    assertEquals(
                            Galois.multiply(a, Galois.multiply(b, c)),
                            Galois.multiply(Galois.multiply(a, b), c)
                    );
                }
            }
        }
    }

    @Test
    public void testIdentity() {
        for (int i = -128; i < 128; i++) {
            byte a = (byte) i;
            assertEquals(a, Galois.add(a, (byte) 0));
            assertEquals(a, Galois.multiply(a, (byte) 1));
        }
    }

    @Test
    public void testInverse() {
        for (int i = -128; i < 128; i++) {
            byte a = (byte) i;
            {
                byte b = Galois.subtract((byte) 0, a);
                assertEquals(0, Galois.add(a, b));
            }
            if (a != 0) {
                byte b = Galois.divide((byte) 1, a);
                assertEquals(1, Galois.multiply(a, b));
            }
        }
    }

    @Test
    public void testCommutativity() {
        for (int i = -128; i < 128; i++) {
            for (int j = -128; j < 128; j++) {
                byte a = (byte) i;
                byte b = (byte) j;
                assertEquals(Galois.add(a, b), Galois.add(b, a));
                assertEquals(Galois.multiply(a, b), Galois.multiply(b, a));
            }
        }
    }

    @Test
    public void testDistributivity() {
        for (int i = -128; i < 128; i++) {
            byte a = (byte) i;
            for (int j = -128; j < 128; j++) {
                byte b = (byte) j;
                for (int k = -128; k < 128; k++) {
                    byte c = (byte) k;
                    assertEquals(
                            Galois.multiply(a, Galois.add(b, c)),
                            Galois.add(Galois.multiply(a, b), Galois.multiply(a, c))
                    );
                }
            }
        }
    }

    @Test
    public void testExp() {
        for (int i = -128; i < 128; i++) {
            byte a = (byte) i;
            byte power = 1;
            for (int j = 0; j < 256; j++) {
                assertEquals(power, Galois.exp(a, j));
                power = Galois.multiply(power, a);
            }
        }
    }

    @Test
    public void testGenerateLogTable() {
        final short[] logTable = Galois.generateLogTable(Galois.GENERATING_POLYNOMIAL);
        assertArrayEquals(Galois.LOG_TABLE, logTable);

        final byte [] expTable = Galois.generateExpTable(logTable);
        assertArrayEquals(Galois.EXP_TABLE, expTable);

        final Integer [] polynomials = {
                29, 43, 45, 77, 95, 99, 101, 105, 113,
                135, 141, 169, 195, 207, 231, 245
        };
        assertArrayEquals(polynomials, Galois.allPossiblePolynomials());
    }


    @Test
    public void testWithPythonAnswers() {
        // These values were copied output of the Python code.
        assertEquals(12, Galois.multiply((byte)3, (byte)4));
        assertEquals(21, Galois.multiply((byte)7, (byte)7));
        assertEquals(41, Galois.multiply((byte)23, (byte)45));

        assertEquals((byte)   4, Galois.exp((byte) 2, (byte) 2));
        assertEquals((byte) 235, Galois.exp((byte) 5, (byte) 20));
        assertEquals((byte)  43, Galois.exp((byte) 13, (byte) 7));
    }
}
