package edu.curtin.saed.assignment1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import edu.curtin.saed.assignment1.gameconfigs.GridSizeClass;
import edu.curtin.saed.assignment1.globlesettings.GlobalSettings;

public class Graphic extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Robot Grumble");
        int spawnTimer = 2000;
        GridSizeClass.GridSizePresets gridSize = GridSizeClass.GridSizePresets.valueOf(GlobalSettings.getGridSize());
        int gridWidth = gridSize.getWidth();
        int gridHeight = gridSize.getHeight();

        TextArea textArea = new TextArea();
        Logger logger = new Logger(textArea);
        ToolBar toolbar = new ToolBar();
        JFXArena arena = new JFXArena(gridWidth, gridHeight, logger);
        GameMap gameMap = new GameMap(gridWidth, gridHeight);
        GameState gameState = new GameState(gameMap, arena);
        GameEngine gameEngine = new GameEngine(gameState, logger, spawnTimer, arena, gridHeight, gridWidth);

        Label label = new Label("Score: 0");
        Button startButton = new Button("Start Game");

        arena.addListener((x, y) -> {
            System.out.println("Arena click at (" + x + "," + y + ")");
        });
        arena.addScoreUpdateListener((score) -> {
            label.setText("Score: " + score);
        });

        startButton.setOnAction((event) -> {
            logger.log("~~~~~~ Starting Game ~~~~~~");
            gameEngine.startGame();
            startButton.setDisable(true);
        });

        toolbar.getItems().addAll(label, startButton);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(arena, textArea);
        arena.setMinWidth(300.0);

        BorderPane contentPane = new BorderPane();
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);

        Scene scene = new Scene(contentPane, 800, 800);
        stage.setScene(scene);
        stage.show();
    }
}