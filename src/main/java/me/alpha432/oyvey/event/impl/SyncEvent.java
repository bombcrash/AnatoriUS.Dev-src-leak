package me.alpha432.oyvey.event.impl;

import me.alpha432.oyvey.event.Event;
import me.alpha432.oyvey.event.Stage;

public class SyncEvent extends Event {
    private final Stage stage;

    public SyncEvent() {
        this.stage = Stage.PRE; // Event aşaması (stage), varsayılan olarak PRE kullanılır
    }

    public Stage getStage() {
        return stage;
    }
}
