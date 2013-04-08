package com.pzelnip.assnw4pa1;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Solves the ping/pong problem for week 4 using Java Lock objects, and the
 * associated Condition objects which provide wait/notify mechanisms.
 * 
 * @author aparkin
 * 
 */
public class Program {
    /** How many times to print Ping/Pong to the console */
    public static final int NUMBER_OF_TIMES_TO_PINGPONG = 3;

    public static void main(String[] args) throws InterruptedException {
        // lock to synchronize on
        Lock lock = new ReentrantLock();
        // condition variable to ensure alternating ping/pong
        Condition hasPrinted = lock.newCondition();

        // Fire up two threads, one for ping and one for pong.
        Thread ping = new Thread(new PingPongWithLock("Ping!", lock,
                hasPrinted, NUMBER_OF_TIMES_TO_PINGPONG));
        Thread pong = new Thread(new PingPongWithLock("Pong!", lock,
                hasPrinted, NUMBER_OF_TIMES_TO_PINGPONG));

        // start them up & wait for them to finish
        ping.start();
        pong.start();
        ping.join();
        pong.join();
        System.out.println("Done!");
    }

    /**
     * Thread class which encapsulates printing one of PING or PONG to the
     * console
     * 
     */
    private static class PingPongWithLock implements Runnable {
        Lock lock;
        Condition printed;
        int iterations;
        String message;

        /**
         * Initialize this thread to use the given lock & condition, repeat
         * numIterations times, and print the given message.
         * 
         * @param message
         *            the message to output to the console
         * @param lock
         *            the lock to synchronize on
         * @param printed
         *            the Condition variable to use for notifying other threads
         *            that this thread has printed
         * @param iterations
         *            how many times to print the message to the console
         */
        public PingPongWithLock(String message, Lock lock, Condition printed,
                int iterations) {
            this.message = message;
            this.lock = lock;
            this.printed = printed;
            this.iterations = iterations;
        }

        @Override
        public void run() {
            for (int numPrints = 0; numPrints < iterations; numPrints++) {
                lock.lock();
                try {
                    System.out.println(message);
                    // notify other threads
                    printed.signal();
                    // immediately wait for other threads to print, note that
                    // this implicitly also releases the lock
                    printed.await();
                } catch (InterruptedException e) {
                    System.out.println(message + " -- InterruptedException");
                } finally {
                    lock.unlock();
                }
            }
            
            // clean up, ensure any threds waiting on this thread to complete
            // are notified
            lock.lock();
            printed.signal();
            lock.unlock();
        }
    }
}
