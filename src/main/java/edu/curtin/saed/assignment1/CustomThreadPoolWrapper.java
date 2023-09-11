package edu.curtin.saed.assignment1;

public class CustomThreadPoolWrapper {
    public CustomThreadPool threadPool;
    private int threadCount;

    public CustomThreadPoolWrapper(int poolSize) {
        threadPool = new CustomThreadPool(poolSize);
        threadCount = 0;
    }

    public void execute(Runnable task) {
        if (threadPool.getAvailableThreadCount() > 0) {
            // Use the custom thread pool if threads are available
            threadPool.execute(task);
        } else {
            // Create a new thread as a fallback when the pool is empty
            Thread newThread = new Thread(task);
            newThread.start();
            threadCount++;
        }
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void returnThreadToPool(Thread currentThread) {
        threadPool.returnThreadToPool(currentThread);
    }

    @SuppressWarnings("PMD") // because here the thread must be of thread group type
    public void interruptShutdown() {
        threadPool.interruptShutdown();

        ThreadGroup group = Thread.currentThread().getThreadGroup();
        Thread[] threads = new Thread[group.activeCount()];
        group.enumerate(threads);
        for (Thread thread : threads) {
            if (thread != null && !thread.isDaemon()) {
                thread.interrupt();
            }
        }
    }
}
