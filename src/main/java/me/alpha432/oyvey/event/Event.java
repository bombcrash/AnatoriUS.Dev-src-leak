package me.alpha432.oyvey.event;

public class Event {
    private boolean cancelled;

    public boolean isCancelled(boolean b) {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }
}
