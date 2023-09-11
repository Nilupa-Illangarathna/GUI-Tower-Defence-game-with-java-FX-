package edu.curtin.saed.assignment1;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class GameEngine {
    private Logger logger;
    private final RobotFactory robotFactory;
    private final GameState gameState;
    private final int spawnTimer;
    private ExecutorService executor;
    public JFXArena arena;
    private final int gridWidth;
    private final int gridHeight;
    private int cornerNumber = 2;
    private CustomThreadPoolWrapper threadPoolWrapper;
    public int score = 0;
    // Add a lock for synchronization
    private final ReentrantLock robotCreationLock = new ReentrantLock();

    // Priority queue to manage thread execution based on ThreadCornerNumber
    private PriorityBlockingQueue<SpawnThreadWrapper> spawnQueue = new PriorityBlockingQueue<>();

    public void increaseScore(int scoreInc) {
        score += scoreInc;
    }

    public GameEngine(GameState gameState, Logger logger, int spawnTimer, JFXArena arena, int gridHeight,
            int gridWidth) {
        this.logger = logger;
        this.robotFactory = new RobotFactory(arena, threadPoolWrapper, logger);
        this.spawnTimer = spawnTimer;
        this.gameState = gameState;
        gameState.attachEndGameListener(this::endGame);
        this.arena = arena;
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
    }

    public void startGame() {
        try {
            if (!gameState.isGameFinished()) {
                return; // guard to stop multiple starts
            }
            Thread.sleep(1000); // initial wait to give the player time to react

            executor = Executors.newCachedThreadPool();
            gameState.setGameStart();
            gameState.setGameOverNotPrinted();

            executor.execute(robotFactory);

            // Create and start threads for each corner
            Thread topLeftThread = createSpawnThread(new GridPosition(0, 0), 1);
            Thread topRightThread = createSpawnThread(new GridPosition(gridWidth - 1, 0), 2);
            Thread bottomLeftThread = createSpawnThread(new GridPosition(0, gridHeight - 1), 3);
            Thread bottomRightThread = createSpawnThread(new GridPosition(gridWidth - 1, gridHeight - 1), 4);

            Thread cornerDecider = createCornerChangerThread();

            topLeftThread.start();
            topRightThread.start();
            bottomLeftThread.start();
            bottomRightThread.start();
            cornerDecider.start();

            executor.execute(this::scoreOverTimeCounter);
        } catch (InterruptedException e) {
        }
    }

    private Thread createCornerChangerThread() {
        return new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

            }
            Random random = new Random();
            while (!gameState.isGameFinished()) {
                // Generate a random number between 1 and 3 (inclusive)
                int randomNumber = random.nextInt(4) + 1;
                cornerNumber = randomNumber;
                try {
                    Thread.sleep(1000); // Sleep for 1000 milliseconds
                } catch (InterruptedException e) {
                }
            }
        });
    }

    private Thread createSpawnThread(GridPosition spawnPosition, int threadCornerNumber) {
        Thread spawnThread = new Thread(() -> {
            while (!gameState.isGameFinished()) {
                if (gameState.isValidSpawnPositions()) {
                    // Check if this thread should spawn a robot during this cycle
                    if (threadCornerNumber == cornerNumber) {
                        Robot robot;
                        try {
                            robotCreationLock.lock(); // Acquire the lock
                            robot = robotFactory.getNextRobot();
                            robot.setGridPosition(spawnPosition);
                            robot.attachGameState(gameState);
                            gameState.spawnRobot(robot);
                            gameState.spawnRobot(robot);
                            executor.execute(robot);
                            logger.log("Robot" + robot.getRobotId() + " Spawned: " + spawnPosition.toString());
                        } catch (InterruptedException e) {

                        } finally {
                            robotCreationLock.unlock(); // Release the lock
                        }
                    }
                }
                try {
                    Thread.sleep(spawnTimer);
                } catch (InterruptedException e) {
                    System.out.println("Spawning stopped");
                }
            }
        });

        // Create a wrapper object to associate ThreadCornerNumber with the thread
        SpawnThreadWrapper threadWrapper = new SpawnThreadWrapper(spawnThread, threadCornerNumber);
        spawnQueue.put(threadWrapper); // Add the thread to the priority queue

        return spawnThread;
    }

    private void scoreOverTimeCounter() {
        try {
            while (!gameState.isGameFinished()) {
                Thread.sleep(1000);
                score = gameState.getScore() + 10;
                gameState.setScore(score);
            }
        } catch (InterruptedException e) {
            System.out.println("Score Counter Stopped");
        }
    }

    private void endGame() {
        if (gameState.getGameOverNotPrinted()) {
            // Interrupt and clean up threads
            for (SpawnThreadWrapper threadWrapper : spawnQueue) {
                Thread thread = threadWrapper.thread;
                thread.interrupt(); // Interrupt the thread
            }

            // Stop the executor and clean up
            executor.shutdownNow();
            logger.log("\n====================");
            logger.log("\n\tGAME OVER");
            logger.log("\n\tFinal Score: " + String.valueOf(gameState.getScore()));
            logger.log("\n====================");

            gameState.setGameOverPrinted();
        }
    }

    // Create a class to wrap the threads along with their ThreadCornerNumber
    private class SpawnThreadWrapper implements Comparable<SpawnThreadWrapper> {
        private Thread thread;
        private int threadCornerNumber;

        public SpawnThreadWrapper(Thread thread, int threadCornerNumber) {
            this.thread = thread;
            this.threadCornerNumber = threadCornerNumber;
        }

        @Override
        public int compareTo(SpawnThreadWrapper other) {
            // Compare threads based on their ThreadCornerNumber
            return Integer.compare(this.threadCornerNumber, other.threadCornerNumber);
        }
    }
}
