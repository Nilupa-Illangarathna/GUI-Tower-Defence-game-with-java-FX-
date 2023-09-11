package edu.curtin.saed.assignment1;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;

public class GameState {
    private boolean isGameFinished;
    private boolean gameOvered;
    private final GameMap gameMap;
    @SuppressWarnings("PMD") // because here an arraylist is the most suitable to be used
    private final HashMap<String, Robot> robotRepo;
    public JFXArena arena;
    public final Object lock = new Object();
    private int gameScore;
    private EndGameListener endGameListener = null;

    public GameState(GameMap gameMap, JFXArena arena) {
        this.isGameFinished = true;
        this.gameMap = gameMap;
        robotRepo = new HashMap<>();
        this.arena = arena;
        gameScore = 0;
    }

    public void increaseScore(int scoreInc) {
        gameScore += scoreInc;
    }

    public int getScore(int scoreInc) {
        return gameScore;
    }

    public void attachEndGameListener(EndGameListener endGameListener) {
        this.endGameListener = endGameListener;
    }

    public void setGameStart() {
        isGameFinished = false;
    }

    public void setGameOverNotPrinted() {
        gameOvered = true;
    }

    public void setGameOverPrinted() {
        gameOvered = false;
    }

    public boolean getGameOverNotPrinted() {
        return gameOvered;
    }

    public boolean isGameFinished() {
        synchronized (lock) {
            return isGameFinished;
        }
    }

    public boolean isValidSpawnPositions() {
        synchronized (lock) {
            return gameMap.isValidSpawnPosition();
        }
    }

    public GridPosition getNextRandomSpawnPosition() {
        synchronized (lock) {
            return gameMap.getNextRandomValidSpawnPosition();
        }
    }

    public void spawnRobot(Robot robot) {
        synchronized (lock) {
            gameMap.moveRobotIntoPosition(robot);
            updateRobotRepo(robot);
        }
    }

    public void handleRobotMovementToNewPosition(Robot robot) {
        synchronized (lock) {
            updateRobotRepo(robot);
        }
    }

    private void checkLoseCondition() {
        synchronized (lock) {
            if (gameMap.isCentralSquareOccupied()) {
                isGameFinished = true;
                endGameListener.endGame();
            }
        }
    }

    @SuppressWarnings("PMD") // because here an arraylist is the most suitable to be used rather than an
                             // interface which is more easier and convenient
    public ArrayList<GridPosition> getValidMoveListFromPosition(GridPosition gridPosition) {
        synchronized (lock) {
            return gameMap.getValidMoveListFromPosition(gridPosition);
        }
    }

    public void updateRobotRepo(Robot robot) {
        robotRepo.put(robot.getRobotId(), robot);
        HashMap<String, Robot> drawableRepo = new HashMap<>();
        robotRepo.forEach(drawableRepo::put);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                arena.updateRobotInfo(drawableRepo);
            }
        });
    }

    public int getScore() {
        synchronized (lock) {
            return gameScore;
        }
    }

    public void setScore(int gameScore) {
        synchronized (lock) {
            this.gameScore = gameScore;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    arena.updateScoreLabel(gameScore);
                }
            });
        }
    }

    public boolean isRobotInGridPosition(GridPosition firingPosition) {
        synchronized (lock) {
            return gameMap.isPositionOccupied(firingPosition);
        }
    }

    public Robot getRobotFromPosition(GridPosition firingPosition) {
        synchronized (lock) {
            return gameMap.getRobotFromPosition(firingPosition);
        }
    }

    public void removeRobotFromRepo(String robotId, int x, int y) {
        synchronized (lock) {
            robotRepo.remove(robotId);
            HashMap<String, Robot> drawableRepo = new HashMap<>();
            robotRepo.forEach(drawableRepo::put);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    arena.removeRobotInfo(robotId, drawableRepo, x, y);
                    System.out.println(robotId + " trying to remove");
                }
            });
        }
    }

    public void removeDeadRobotFrimPosition(Robot robot) {
        synchronized (lock) {
            gameMap.moveRobotIntoPosition(robot);
        }
    }

    public void moveRobotOutOfOldPosition(GridPosition oldPosition) {
        synchronized (lock) {
            gameMap.moveRobotOutOfOldPosition(oldPosition);
            checkLoseCondition();
        }
    }

    public void removeRobotOutOfOldPosition(GridPosition oldPosition) {
        synchronized (lock) {
            gameMap.moveRobotOutOfOldPosition(oldPosition);
        }
    }
}