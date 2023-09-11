package edu.curtin.saed.assignment1;

import java.util.ArrayList;
import java.util.Random;

public class Robot implements Runnable {
    private int maxMoveDelay = 2000; // A delay value d, randomly chosen from between 500–2000 milliseconds
    private int minMoveDelay = 500; // A delay value d, randomly chosen from between 500–2000 milliseconds

    private final String robotId;
    private final int delay;
    private GridPosition gridPosition;
    private GameState gameState;
    private boolean isAlive;
    private boolean isBuried; // This makes the robot removal process a success.
    private Random randomGenerator = new Random();
    private JFXArena arena;
    private CustomThreadPoolWrapper threadpool;
    private Logger logger;

    public Robot(String robotId, JFXArena arena, CustomThreadPoolWrapper threadpool, Logger logger) {
        this.robotId = robotId;
        this.delay = getRandomDelay();
        this.gridPosition = new GridPosition();
        isAlive = true;
        isBuried = false;
        this.arena = arena;
        this.threadpool = threadpool;
        this.logger = logger;
    }

    public String getRobotId() {
        return robotId;
    }

    public int getDelay() {
        return delay;
    }

    public GridPosition gridPosition() {
        return gridPosition;
    }

    public void setGridPosition(GridPosition gridPosition) {
        this.gridPosition = gridPosition;
    }

    private int getRandomDelay() {
        int delay;
        delay = (int) ((Math.random() * (maxMoveDelay - minMoveDelay)) + minMoveDelay);
        return delay;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isBuried() {
        return isBuried;
    }

    public void attachGameState(GameState gameState) {
        this.gameState = gameState;
    }

    private void attemptMove() throws InterruptedException {
        ArrayList<GridPosition> validMoves = gameState.getValidMoveListFromPosition(gridPosition);

        if (validMoves.size() > 0 && isAlive) {
            GridPosition oldPosition = gridPosition();
            int index = randomGenerator.nextInt(validMoves.size());
            GridPosition newGridPosition = validMoves.get(index);
            newGridPosition.setAnimationX(oldPosition.getGridX());
            newGridPosition.setAnimationY(oldPosition.getGridY());

            setGridPosition(newGridPosition);
            gameState.removeDeadRobotFrimPosition(this);

            for (int i = 0; i < 10; i++) {

                if (oldPosition.getGridX() < newGridPosition.getGridX()) {
                    newGridPosition.incrementAnimationX();
                }
                if (oldPosition.getGridX() > newGridPosition.getGridX()) {
                    newGridPosition.decrementAnimationX();
                }
                if (oldPosition.getGridY() < newGridPosition.getGridY()) {
                    newGridPosition.incrementAnimationY();
                }
                if (oldPosition.getGridY() > newGridPosition.getGridY()) {
                    newGridPosition.decrementAnimationY();
                }
                setGridPosition(newGridPosition);
                if (isAlive) {
                    gameState.handleRobotMovementToNewPosition(this);
                }
                Thread.sleep(50);
            }

            int[][] wallGrid = arena.getGridWallsArray();
            if (wallGrid[newGridPosition.getGridX()][newGridPosition.getGridY()] == 1
                    || wallGrid[newGridPosition.getGridX()][newGridPosition.getGridY()] == 2) {
                logger.log("Robot " + robotId + " was hit on a wall and destroyed");
                if (wallGrid[newGridPosition.getGridX()][newGridPosition.getGridY()] == 1) {
                    logger.log(
                            "Robot hit the wall and it was partially damaged, and the total score was increased by 100.");
                } else if (wallGrid[newGridPosition.getGridX()][newGridPosition.getGridY()] == 2) {
                    logger.log(
                            "Robot hit the partially damaged wall and it was fully destroyed, and the total score was increased by 100.");
                }
                gameState.increaseScore(100);
                kill(oldPosition, newGridPosition);
            }

            gameState.moveRobotOutOfOldPosition(oldPosition);
        }
    }

    @Override
    public String toString() {
        return "Robot{robotId[" + robotId + "], delay[" + delay + "], gridPosition:[" + gridPosition.toString() + "]}";
    }

    @Override
    public void run() {
        try {
            while (isAlive() && !gameState.isGameFinished()) {
                Thread.sleep(getDelay());
                if (!isBuried) {
                    attemptMove();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Robot:" + robotId + " shutdown");
            // Clean up the thread by setting isAlive to false
            isAlive = false;
        }
    }

    public void kill(GridPosition oldPosition, GridPosition newGridPosition) {
        isAlive = false; // This has to be re-implemented. With this, the robot disappears but it is
                         // on the grid. So the blocking queue restricts other robots from entering this
                         // place.
        isBuried = true;
        maxMoveDelay = 0;
        minMoveDelay = 0;
        int x = newGridPosition.gridX;
        int y = newGridPosition.gridY;

        gameState.moveRobotOutOfOldPosition(newGridPosition);
        gameState.removeRobotFromRepo(robotId, x, y);
        gameState.removeRobotOutOfOldPosition(oldPosition);
        gameState.removeRobotOutOfOldPosition(newGridPosition);
        threadpool.returnThreadToPool(Thread.currentThread());

        ///////////////////////////////////////////////////////////////////////////
        // Clean up the thread by interrupting it
        Thread.currentThread().interrupt();
    }
}
