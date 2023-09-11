package edu.curtin.saed.assignment1;

import java.util.concurrent.SynchronousQueue;

public class RobotFactory implements Runnable {
    private SynchronousQueue<Robot> queue = new SynchronousQueue<>();
    private int idCounter = 1;
    public JFXArena arena;
    private CustomThreadPoolWrapper threadPoolWrapper;
    private Logger logger;

    public RobotFactory(JFXArena arena, CustomThreadPoolWrapper threadPoolWrapper, Logger logger) {
        this.arena = arena;
        this.threadPoolWrapper = threadPoolWrapper;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Produce Robots
                Thread.sleep(1500); // Every 1500 milliseconds, a new robot appears randomly
                int robotId = idCounter;
                Robot robot = new Robot(String.valueOf(robotId), arena, threadPoolWrapper, logger);

                queue.put(robot);
                idCounter++;
            }
        } catch (InterruptedException e) {
            System.out.println("Robot Factory Interrupted, shutting down");
        }
    }

    public Robot getNextRobot() throws InterruptedException {
        return queue.take();
    }
}