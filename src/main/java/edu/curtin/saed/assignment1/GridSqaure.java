package edu.curtin.saed.assignment1;

public class GridSqaure {
    public boolean occupied;
    public Robot robot;

    public GridSqaure() {
        occupied = false;
        robot = null;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public Robot getOccupant() {
        return robot;
    }

    public void setOccupant(Robot robot) {
        this.robot = robot;
        this.occupied = true;
    }

    public void unOccupy() {
        occupied = false;
        robot = null;
    }
}