package edu.curtin.saed.assignment1.gameconfigs;

public class GridSizeClass {
    public enum GridSizePresets {
        SMALL(3, 3),
        MEDIUM(6, 6),
        LARGE(9, 9),
        EXTRALARGE(24, 24);

        private final int width;
        private final int height;

        GridSizePresets(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
