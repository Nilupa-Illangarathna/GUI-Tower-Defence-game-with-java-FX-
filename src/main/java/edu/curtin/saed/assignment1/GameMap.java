package edu.curtin.saed.assignment1;

import java.util.ArrayList;
import java.util.Random;

public class GameMap {
    private final GridSqaure[][] gameMap;
    private final GridPosition topLeft;
    private final GridPosition topRight;
    private final GridPosition bottomLeft;
    private final GridPosition bottomRight;
    private final GridPosition centre;
    private final int gridWidth;
    private final int gridHeight;

    public GameMap(int gridWidth, int gridHeight) {
        this.gridWidth = gridWidth - 1;
        this.gridHeight = gridHeight - 1;
        this.gameMap = new GridSqaure[gridWidth][gridHeight];
        topLeft = new GridPosition(0, 0);
        topRight = new GridPosition(gridWidth - 1, 0);
        bottomLeft = new GridPosition(0, gridHeight - 1);
        bottomRight = new GridPosition(gridWidth - 1, gridHeight - 1);
        centre = new GridPosition(gridWidth / 2, gridHeight / 2);

        // instantiate map with empty cells
        for (int row = 0; row < gridWidth; row++) {
            for (int column = 0; column < gridHeight; column++) {
                gameMap[row][column] = new GridSqaure();
            }
        }
    }

    public boolean isPositionOccupied(GridPosition position) {
        return gameMap[position.getGridX()][position.getGridY()].isOccupied();
    }

    public GridPosition getNextRandomValidSpawnPosition() {
        ArrayList<GridPosition> validPositions = new ArrayList<>();
        GridPosition gridPosition;

        if (!isPositionOccupied(topLeft)) {
            validPositions.add(topLeft);
        }
        if (!isPositionOccupied(topRight)) {
            validPositions.add(topRight);
        }
        if (!isPositionOccupied(bottomLeft)) {
            validPositions.add(bottomLeft);
        }
        if (!isPositionOccupied(bottomRight)) {
            validPositions.add(bottomRight);
        }

        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(validPositions.size());
        gridPosition = validPositions.get(index);
        return gridPosition;
    }

    public boolean isValidSpawnPosition() {
        return !isPositionOccupied(topLeft) ||
                !isPositionOccupied(topRight) ||
                !isPositionOccupied(bottomLeft) ||
                !isPositionOccupied(bottomRight);
    }

    @SuppressWarnings("PMD") // because here an arraylist is the most suitable to be used rather than an
                             // interface which is more easier and convenient
    public ArrayList<GridPosition> getValidMoveListFromPosition(GridPosition gridPosition) {
        ArrayList<GridPosition> moveList = new ArrayList<>();

        validateMoveUpFromPosition(moveList, gridPosition);
        validateMoveDownFromPosition(moveList, gridPosition);
        validateMoveLeftFromPosition(moveList, gridPosition);
        validateMoveRightFromPosition(moveList, gridPosition);

        return moveList;
    }

    @SuppressWarnings("PMD") // because here an arraylist is the most suitable to be used rather than an
                             // interface which is more easier and convenient
    private void validateMoveRightFromPosition(ArrayList<GridPosition> moveList, GridPosition currentGridPosition) {
        if (currentGridPosition.getGridX() + 1 <= gridWidth) {
            GridPosition positionToAttempt = new GridPosition(currentGridPosition.getGridX() + 1,
                    currentGridPosition.getGridY());
            if (!isPositionOccupied(positionToAttempt)) {
                moveList.add(positionToAttempt);
            }
        }
    }

    @SuppressWarnings("PMD") // because here an arraylist is the most suitable to be used rather than an
                             // interface which is more easier and convenient
    private void validateMoveLeftFromPosition(ArrayList<GridPosition> moveList, GridPosition currentGridPosition) {
        if (currentGridPosition.getGridX() - 1 >= 0) {
            GridPosition positionToAttempt = new GridPosition(currentGridPosition.getGridX() - 1,
                    currentGridPosition.getGridY());
            if (!isPositionOccupied(positionToAttempt)) {
                moveList.add(positionToAttempt);
            }
        }
    }

    @SuppressWarnings("PMD") // because here an arraylist is the most suitable to be used rather than an
                             // interface which is more easier and convenient
    private void validateMoveDownFromPosition(ArrayList<GridPosition> moveList, GridPosition currentGridPosition) {
        if (currentGridPosition.getGridY() + 1 <= gridHeight) {
            GridPosition positionToAttempt = new GridPosition(currentGridPosition.getGridX(),
                    currentGridPosition.getGridY() + 1);
            if (!isPositionOccupied(positionToAttempt)) {
                moveList.add(positionToAttempt);
            }
        }
    }

    @SuppressWarnings("PMD") // because here an arraylist is the most suitable to be used rather than an
                             // interface which is more easier and convenient
    private void validateMoveUpFromPosition(ArrayList<GridPosition> moveList, GridPosition currentGridPosition) {
        if (currentGridPosition.getGridY() - 1 >= 0) {
            GridPosition positionToAttempt = new GridPosition(currentGridPosition.getGridX(),
                    currentGridPosition.getGridY() - 1);
            if (!isPositionOccupied(positionToAttempt)) {
                moveList.add(positionToAttempt);
            }
        }
    }

    public void moveRobotIntoPosition(Robot robot) {
        int gridPositionX = robot.gridPosition().getGridX();
        int gridPositionY = robot.gridPosition().getGridY();
        gameMap[gridPositionX][gridPositionY].setOccupant(robot);
    }

    public void moveRobotOutOfOldPosition(GridPosition oldPosition) {
        int gridPositionX = oldPosition.getGridX();
        int gridPositionY = oldPosition.getGridY();
        gameMap[gridPositionX][gridPositionY].unOccupy();
    }

    public boolean isCentralSquareOccupied() {
        return isPositionOccupied(centre);
    }

    public Robot getRobotFromPosition(GridPosition firingPosition) {
        int gridX = firingPosition.getGridX();
        int gridY = firingPosition.getGridY();
        return gameMap[gridX][gridY].getOccupant();
    }
}
