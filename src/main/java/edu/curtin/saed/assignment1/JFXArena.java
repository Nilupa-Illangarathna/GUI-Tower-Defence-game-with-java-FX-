package edu.curtin.saed.assignment1;

import javafx.scene.canvas.*;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class JFXArena extends Pane {
    private static final String IMAGE_FILE = "robot.png";
    private static final String CITY_IMAGE_FILE = "defence_tower.png";
    private static final String UNBROKEN_WALL = "unbroken_wall.png";
    private static final String HALF_BROKEN_WALL = "half_broken_wall.png";
    private Image robotImage;
    private Image cityImage;
    private Image unbrokenWallImage;
    private Image halfBrokenWallImage;
    @SuppressWarnings("PMD") // because here the hashmap is the most suitable to be used here
    private HashMap<String, Robot> robotRepo;
    public int gridWidth;
    public int gridHeight;
    private int centreX;
    private int centreY;
    private double gridSquareSize;
    private Canvas canvas;
    private List<ArenaListener> listeners = null;
    private ScoreListener scoreListener = null;
    private final Object lock = new Object();
    private int wallCount = 0;

    private int[][] wallGrid;
    private ScheduledExecutorService wallPlacementExecutor = Executors.newScheduledThreadPool(1);
    private long wallPlacementDelay = 2000; // 2000 milliseconds
    private BlockingQueue<WallPlacementRequest> wallPlacementQueue = new LinkedBlockingQueue<>();
    public Logger logger;

    public JFXArena(int gridWidth, int gridHeight, Logger logger) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.centreX = gridWidth / 2;
        this.centreY = gridHeight / 2;
        robotRepo = new HashMap<>();
        wallGrid = new int[gridWidth][gridHeight];
        this.logger = logger;

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(IMAGE_FILE)) {
            if (is != null) {
                robotImage = new Image(is);
            } else {
                throw new AssertionError("Cannot find image file " + IMAGE_FILE);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CITY_IMAGE_FILE)) {
            if (is != null) {
                cityImage = new Image(is);
            } else {
                throw new AssertionError("Cannot find image file " + CITY_IMAGE_FILE);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(UNBROKEN_WALL)) {
            if (is != null) {
                unbrokenWallImage = new Image(is);
            } else {
                throw new AssertionError("Cannot find image file " + UNBROKEN_WALL);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(UNBROKEN_WALL)) {
            if (is != null) {
                unbrokenWallImage = new Image(is);
            } else {
                throw new AssertionError("Cannot find image file " + UNBROKEN_WALL);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(HALF_BROKEN_WALL)) {
            if (is != null) {
                halfBrokenWallImage = new Image(is);
            } else {
                throw new AssertionError("Cannot find image file " + HALF_BROKEN_WALL);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        canvas = new Canvas();
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);

        wallPlacementExecutor.scheduleAtFixedRate(() -> {
            try {
                WallPlacementRequest request = wallPlacementQueue.poll(0, TimeUnit.MILLISECONDS);
                if (request != null) {
                    placeWall(request);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, wallPlacementDelay, TimeUnit.MILLISECONDS);
    }

    public int[][] getGridWallsArray() {
        return wallGrid;
    }

    public void addListener(ArenaListener newListener) {
        if (listeners == null) {
            listeners = new LinkedList<>();
            setOnMouseClicked(event -> {
                int gridX = (int) (event.getX() / gridSquareSize);
                int gridY = (int) (event.getY() / gridSquareSize);

                if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
                    if (wallGrid[gridX][gridY] != 1 && wallGrid[gridX][gridY] != 2 && wallCount < 10) {
                        wallPlacementQueue.offer(new WallPlacementRequest(gridX, gridY));
                        int queuedUpWallCount = 0;
                        if (wallCount == 10) {
                            queuedUpWallCount = 0;
                        } else if (10 - wallCount > wallPlacementQueue.size()) {
                            queuedUpWallCount = wallPlacementQueue.size();
                        } else if (10 - wallCount <= wallPlacementQueue.size()) {
                            queuedUpWallCount = 10 - wallCount;
                        }
                        logger.log("Currently queued up wall count is " + queuedUpWallCount);
                    } else if (wallGrid[gridX][gridY] != 1 && wallGrid[gridX][gridY] == 2) {
                        wallGrid[gridX][gridY] = 0;
                        wallCount--;
                    } else if (wallGrid[gridX][gridY] == 1) {
                        wallGrid[gridX][gridY] = 0;
                        wallCount--;
                    }
                    layoutChildren();
                }
            });
        }
        listeners.add(newListener);
    }

    private void placeWall(WallPlacementRequest request) {
        int gridX = request.gridX;
        int gridY = request.gridY;

        if (wallGrid[gridX][gridY] == 0 && wallCount < 10) {
            wallGrid[gridX][gridY] = 0;
            // wallCount++;
            layoutChildren();

            wallPlacementExecutor.schedule(() -> {
                if (wallCount < 10) {
                    wallGrid[gridX][gridY] = 1;
                }
                wallCount++;
                layoutChildren();

            }, wallPlacementDelay, TimeUnit.MILLISECONDS);
        }
    }

    public void addScoreUpdateListener(ScoreListener newListener) {
        scoreListener = newListener;
    }

    public void updateScoreLabel(int score) {
        scoreListener.scoreUpdated(score);
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();
        GraphicsContext gfx = canvas.getGraphicsContext2D();
        gfx.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

        gridSquareSize = Math.min(getWidth() / (double) gridWidth, getHeight() / (double) gridHeight);

        double arenaPixelWidth = gridWidth * gridSquareSize;
        double arenaPixelHeight = gridHeight * gridSquareSize;

        gfx.setStroke(Color.DARKGREY);
        gfx.strokeRect(0.0, 0.0, arenaPixelWidth - 1.0, arenaPixelHeight - 1.0);

        for (int gridX = 1; gridX < gridWidth; gridX++) {
            double x = (double) gridX * gridSquareSize;
            gfx.strokeLine(x, 0.0, x, arenaPixelHeight);
        }

        for (int gridY = 1; gridY < gridHeight; gridY++) {
            double y = (double) gridY * gridSquareSize;
            gfx.strokeLine(0.0, y, arenaPixelWidth, y);
        }

        drawImage(gfx, cityImage, centreX, centreY);
        synchronized (lock) {
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    if (wallGrid[x][y] == 1) {
                        drawImage(gfx, unbrokenWallImage, x, y);
                    }
                    if (wallGrid[x][y] == 2) {
                        drawImage(gfx, halfBrokenWallImage, x, y);
                    }
                }
            }
            for (Map.Entry<String, Robot> entry : robotRepo.entrySet()) {
                Robot robot = entry.getValue();
                String robotId = robot.getRobotId();
                double robotX = robot.gridPosition().getAnimationX();
                double robotY = robot.gridPosition().getAnimationY();
                if (robotRepo.get(robotId) != null) {
                    drawImage(gfx, robotImage, robotX, robotY);
                    drawLabel(gfx, robotId, robotX, robotY);
                }
            }
        }
    }

    private void drawImage(GraphicsContext gfx, Image image, double gridX, double gridY) {
        double x = (gridX + 0.5) * gridSquareSize;
        double y = (gridY + 0.5) * gridSquareSize;

        double fullSizePixelWidth = image.getWidth();
        double fullSizePixelHeight = image.getHeight();

        double displayedPixelWidth, displayedPixelHeight;
        if (fullSizePixelWidth > fullSizePixelHeight) {
            displayedPixelWidth = gridSquareSize;
            displayedPixelHeight = gridSquareSize * fullSizePixelHeight / fullSizePixelWidth;
        } else {
            displayedPixelHeight = gridSquareSize;
            displayedPixelWidth = gridSquareSize * fullSizePixelWidth / fullSizePixelHeight;
        }

        gfx.drawImage(image, x - displayedPixelWidth / 2.0, y - displayedPixelHeight / 2.0, displayedPixelWidth,
                displayedPixelHeight);
    }

    private void drawLabel(GraphicsContext gfx, String label, double gridX, double gridY) {
        gfx.setTextAlign(TextAlignment.CENTER);
        gfx.setTextBaseline(VPos.TOP);
        gfx.setStroke(Color.BLUE);
        gfx.strokeText(label, (gridX + 0.5) * gridSquareSize, (gridY + 1.0) * gridSquareSize);
    }

    @SuppressWarnings("PMD") // because here a hasmap is the most suitable and convenient to use
    public void updateRobotInfo(HashMap<String, Robot> robotRepo) {
        synchronized (lock) {
            this.robotRepo = robotRepo;
            layoutChildren();
        }
    }

    @SuppressWarnings("PMD") // because here the hashmap is the most suitable to be used here
    public void removeRobotInfo(String robotID, HashMap<String, Robot> drawableRepo, int x, int y) {
        synchronized (lock) {
            System.out.println("before removed " + drawableRepo.size());
            this.robotRepo = drawableRepo;
            System.out.println("before after " + drawableRepo.size());
            if (this.wallGrid[x][y] == 1) {
                this.wallGrid[x][y] = 2;
            } else if (this.wallGrid[x][y] == 2) {
                this.wallGrid[x][y] = 0;
                this.wallCount--;
            } else {
                this.wallGrid[x][y] = 0;
                this.wallCount--;
            }
            layoutChildren();
        }
    }

    private static class WallPlacementRequest {
        public int gridX;
        public int gridY;

        public WallPlacementRequest(int gridX, int gridY) {
            this.gridX = gridX;
            this.gridY = gridY;
        }
    }
}
