package com.pzelnip.assnw4pa2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Solve the dining philosophers problem.  My solution to the problem was to
 * have a lock for each chopstick, and have each philosopher make use of
 * Java Lock's tryLock() method to test for acquiring the lock.
 * 
 * @author aparkin
 * 
 */
public class Program {
    public static final int NUM_TIMES_TO_EAT = 5;
    public static final int NUM_PHILOSOPHERS = 5;
    public static final int NUM_CHOPSTICKS = 5;

    public static void main(String[] args) throws InterruptedException {
        new Program().runme(); // get out of static context
    }

    void runme() throws InterruptedException {
        System.out.println("Dinner is starting!");

        List<Thread> philosophers = new ArrayList<Thread>();
        List<Lock> chopsticks = new ArrayList<Lock>();

        // create a lock for each chopstick
        for (int x = 0; x < NUM_CHOPSTICKS; x++) {
            chopsticks.add(new ReentrantLock());
        }

        for (int x = 0; x < NUM_PHILOSOPHERS; x++) {
            philosophers.add(new Thread(new Philosopher(x + 1, pickLeft(x,
                    chopsticks), pickRight(x, chopsticks))));
        }

        for (Thread philosopher : philosophers) {
            philosopher.start();
        }

        for (Thread philosopher : philosophers) {
            philosopher.join();
        }

        System.out.println("Dinner is over!");
    }

    /**
     * Pick the left chopstick lock for the given philosopher
     * 
     * @param id
     *            the id of the philosopher
     * @param locks
     *            the array of locks for each chopstick
     * @return the lock for the left chopstick of the given philosopher
     */
    Lock pickLeft(int id, List<Lock> locks) {
        return locks.get(id);
    }

    /**
     * Pick the right chopstick lock for the given philosopher
     * 
     * @param id
     *            the id of the philosopher
     * @param locks
     *            the array of locks for each chopstick
     * @return the lock for the right chopstick of the given philosopher
     */
    Lock pickRight(int id, List<Lock> locks) {
        int idx = ((id - 1) + NUM_PHILOSOPHERS) % NUM_PHILOSOPHERS;
        return locks.get(idx);
    }

    private static class Philosopher implements Runnable {
        int id;
        Lock left;
        Lock right;

        /**
         * Create a philosopher with the given id value, and a lock for his/her
         * left and right chopsticks
         * 
         * @param id
         *            an arbitrary id value for this philosopher
         * @param left
         *            the lock for this philosopher's left chopstick
         * @param right
         *            the lock for this philosopher's right chopstick
         */
        public Philosopher(int id, Lock left, Lock right) {
            this.id = id;
            this.left = left;
            this.right = right;
        }

        /**
         * Try to acquire both chopstick locks. If this method returns
         * <code>true</code> then both locks are held. If this method returns
         * <code>false</code> then <b>no locks</b> are held.
         * 
         * @return <code>true</code> if both chopstick locks were obtained, and
         *         <code>false</code> otherwise.
         */
        boolean trySticks() {
            boolean l = left.tryLock();
            if (!l)
                return false;

            System.out.println("Philosopher " + id + " picks up left chopstick.");

            // assert: if here, left lock is held

            boolean r = right.tryLock();
            if (!r) {
                System.out.println("Philosopher " + id
                                + " puts down down left chopstick because he could not acquire his right stick.");
                left.unlock();
                return false;
            }

            System.out.println("Philosopher " + id + " picks up right chopstick.");
            return true;
        }

        @Override
        public void run() {
            for (int x = 0; x < NUM_TIMES_TO_EAT; x++) {
                int delay = 10;
                while (!trySticks()) {
                    try {
                        System.out.println("Philosopher " + id
                                        + " failed to get both sticks and is waiting for "
                                        + delay + " ms");
                        Thread.sleep(delay);
                        delay *= 2;
                    } catch (InterruptedException e) {
                        // swallow intentionally, Thread.sleep requires
                        // InterruptedException to be caught
                        System.err.println("Philosopher " + id
                                        + " caught an InterruptedException while sleeping");
                    }
                }

                System.out.println("Philosopher " + id + " eats.");

                right.unlock();
                System.out.println("Philosopher " + id
                        + " puts down down right chopstick.");

                left.unlock();
                System.out.println("Philosopher " + id
                        + " puts down down left chopstick.");
            }
        }
    }
}
