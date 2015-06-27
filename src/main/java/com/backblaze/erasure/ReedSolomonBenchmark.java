/**
 * Benchmark of Reed-Solomon encoding.
 *
 * Copyright 2015, Backblaze, Inc.  All rights reserved.
 */

package com.backblaze.erasure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Benchmark of Reed-Solomon encoding.
 *
 * Counts the number of bytes of input data that can be processed per
 * second.
 *
 * The set of data the test runs over is twice as big as the L3 cache
 * in a Xeon processor, so it should simulate the case where data has
 * been read in from a socket.
 */
public class ReedSolomonBenchmark {

    private static final int DATA_COUNT = 17;
    private static final int PARITY_COUNT = 3;
    private static final int TOTAL_COUNT = DATA_COUNT + PARITY_COUNT;
    private static final int BUFFER_SIZE = 200 * 1000;
    private static final int PROCESSOR_CACHE_SIZE = 10 * 1024 * 1024;
    private static final int TWICE_PROCESSOR_CACHE_SIZE = 2 * PROCESSOR_CACHE_SIZE;
    private static final int NUMBER_OF_BUFFER_SETS = TWICE_PROCESSOR_CACHE_SIZE / DATA_COUNT / BUFFER_SIZE + 1;

    private static final long MEASUREMENT_DURATION = 2 * 1000;

    private static final Random random = new Random();

    private int nextBuffer = 0;

    public static void main(String [] args) {
        (new ReedSolomonBenchmark()).run();
    }

    public void run() {

        System.out.println("preparing...");
        final BufferSet [] bufferSets = new BufferSet [NUMBER_OF_BUFFER_SETS];
        for (int iBufferSet = 0; iBufferSet < NUMBER_OF_BUFFER_SETS; iBufferSet++) {
            bufferSets[iBufferSet] = new BufferSet();
        }

        List<String> summaryLines = new ArrayList<String>();
        for (CodingLoop codingLoop : CodingLoop.ALL_CODING_LOOPS) {
            System.out.println("\nTEST: " + codingLoop.getName());
            ReedSolomon codec = new ReedSolomon(DATA_COUNT, PARITY_COUNT, codingLoop);
            System.out.println("    warm up...");
            doOneMeasurement(codec, bufferSets);
            doOneMeasurement(codec, bufferSets);
            System.out.println("    testing...");
            Measurement total = new Measurement();
            for (int iMeasurement = 0; iMeasurement < 10; iMeasurement++) {
                total.add(doOneMeasurement(codec, bufferSets));
            }
            System.out.println(String.format("\nAVERAGE: %s", total));
            summaryLines.add(String.format("    %35s %s", codingLoop.getName(), total));
        }

        System.out.println("\nSummary:\n");
        for (String line : summaryLines) {
            System.out.println(line);
        }
    }

    private Measurement doOneMeasurement(ReedSolomon codec, BufferSet [] bufferSets) {
        long bytesEncoded = 0;
        long passesCompleted = 0;
        long encodingTime = 0;
        while (encodingTime < MEASUREMENT_DURATION) {
            BufferSet bufferSet = bufferSets[nextBuffer];
            nextBuffer = (nextBuffer + 1) % bufferSets.length;
            byte[][] shards = bufferSet.buffers;
            long startTime = System.currentTimeMillis();
            codec.encodeParity(shards, 0, BUFFER_SIZE);
            long endTime = System.currentTimeMillis();
            encodingTime += (endTime - startTime);
            bytesEncoded += BUFFER_SIZE * DATA_COUNT;
            passesCompleted += 1;
        }
        double seconds = ((double)encodingTime) / 1000.0;
        double megabytes = ((double)bytesEncoded) / 1000000.0;
        Measurement result = new Measurement(megabytes, seconds);
        System.out.println(String.format("        %s passes, %s", passesCompleted, result));
        return result;
    }

    private static class BufferSet {

        public byte [] [] buffers;

        public byte [] bigBuffer;

        public BufferSet() {
            buffers = new byte [TOTAL_COUNT] [BUFFER_SIZE];
            for (int iBuffer = 0; iBuffer < TOTAL_COUNT; iBuffer++) {
                byte [] buffer = buffers[iBuffer];
                for (int iByte = 0; iByte < BUFFER_SIZE; iByte++) {
                    buffer[iByte] = (byte) random.nextInt(256);
                }
            }

            bigBuffer = new byte [TOTAL_COUNT * BUFFER_SIZE];
            for (int i = 0; i < TOTAL_COUNT * BUFFER_SIZE; i++) {
                bigBuffer[i] = (byte) random.nextInt(256);
            }
        }
    }

    private static class Measurement {
        private double megabytes;
        private double seconds;

        public Measurement() {
            this.megabytes = 0.0;
            this.seconds = 0.0;
        }

        public Measurement(double megabytes, double seconds) {
            this.megabytes = megabytes;
            this.seconds = seconds;
        }

        public void add(Measurement other) {
            megabytes += other.megabytes;
            seconds += other.seconds;
        }

        public double getRate() {
            return megabytes / seconds;
        }

        @Override
        public String toString() {
            return String.format("%5.1f MB/s", getRate());
        }
    }
}
