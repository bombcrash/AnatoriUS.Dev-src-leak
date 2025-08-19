package me.alpha432.oyvey.event.impl;

import me.alpha432.oyvey.event.Event;

public class EventMove extends Event {

    private double x, y, z;
    private boolean canceled;

    public EventMove(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.canceled = false;
    }

    // Getter ve setter'lar
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
