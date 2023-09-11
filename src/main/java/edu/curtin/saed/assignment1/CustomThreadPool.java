package edu.curtin.saed.assignment1;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CustomThreadPool {
    private BlockingQueue<Thread> pool;
    private int initialThreadCount;

    public CustomThreadPool(int poolSize) {
        pool = new LinkedBlockingQueue<>(poolSize);
        initialThreadCount = 5; // Initial number of threads
        createInitialThreads();
    }

    private void createInitialThreads() {
        for (int i = 0; i < initialThreadCount; i++) {
            Thread thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        // Wait for a task to execute
                        Runnable task = pool.take();
                        task.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            thread.start();
            pool.offer(thread);
        }
    }

    @SuppressWarnings("PMD") // because here run() is more convienet to be used here
    public void execute(Runnable task) {
        if (pool.isEmpty()) {
            // Create a new thread if the pool is empty
            Thread newThread = new Thread(task);
            newThread.start();
        } else {
            // Reuse an available thread from the pool
            Thread existingThread = pool.poll();
            existingThread.interrupt(); // Interrupt the thread to release it from its current task
            existingThread.run(); // Reuse the existing thread
        }
    }

    public void returnThreadToPool(Thread thread) {
        thread.interrupt();
        pool.offer(thread);
    }

    public int getAvailableThreadCount() {
        return pool.size();
    }

    public void interruptShutdown() {
        // Interrupt all threads in the pool to release them from their tasks
        for (Thread thread : pool) {
            thread.interrupt();
        }
        // Clear the pool
        pool.clear();
    }
}
