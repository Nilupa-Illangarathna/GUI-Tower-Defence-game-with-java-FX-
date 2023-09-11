package edu.curtin.saed.assignment1;

public class GridPosition {
    public int gridX;
    public int gridY;
    public double animationX;
    public double animationY;

    public GridPosition() {

    }

    public GridPosition(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        animationX = gridX;
        animationY = gridY;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void incrementAnimationX() {
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {

        }
        animationX = animationX + 0.1;
    }

    public void incrementAnimationY() {
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {

        }
        animationY = animationY + 0.1;
    }

    public void decrementAnimationX() {
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {

        }

        animationX = animationX - 0.1;
    }

    public void decrementAnimationY() {
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {

        }
        animationY = animationY - 0.1;
    }

    public double getAnimationX() {
        return animationX;
    }

    public double getAnimationY() {
        return animationY;
    }

    public void setAnimationX(double animationX) {
        this.animationX = animationX;
    }

    public void setAnimationY(double animationY) {
        this.animationY = animationY;
    }

    @Override
    public String toString() {
        return "(" + gridX + "," + gridY + ")";
    }
}
