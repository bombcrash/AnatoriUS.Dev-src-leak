package me.alpha432.oyvey.event.impl;

import me.alpha432.oyvey.event.Stage;
import me.alpha432.oyvey.event.Event;

public class UpdateWalkingEvent extends Event {
    private boolean cancelRotate = false;
    private final Stage stage;

    public UpdateWalkingEvent(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public void cancelRotate() {
        this.cancelRotate = true;
    }
    public void setCancelRotate(boolean cancelRotate) {
        this.cancelRotate = cancelRotate;
    }

    public boolean isCancelRotate() {
        return cancelRotate;
    }
}