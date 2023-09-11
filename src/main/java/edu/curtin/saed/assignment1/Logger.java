package edu.curtin.saed.assignment1;

import javafx.scene.control.TextArea;

public class Logger {
    private TextArea logger;

    public Logger(TextArea logger) {
        this.logger = logger;
    }

    public void log(String message) {
        logger.appendText(message + "\n");
        // Scroll to the bottom to view the latest log entry
        logger.setScrollTop(Double.MAX_VALUE);
    }
}
