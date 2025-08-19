package me.alpha432.oyvey.event.impl;

import me.alpha432.oyvey.event.Event;
import org.lwjgl.glfw.GLFW;

public class EventKeyboardInput extends Event {

    private final int key;
    private final int scancode;
    private final int action;
    private final int mods;

    public EventKeyboardInput(int key, int scancode, int action, int mods) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.mods = mods;
    }

    // Getter metotları
    public int getKey() {
        return key;
    }

    public int getScancode() {
        return scancode;
    }

    public int getAction() {
        return action;
    }

    public int getMods() {
        return mods;
    }

    // Kolay kullanım için tuşun basılı olup olmadığını kontrol eden yardımcı metot
    public boolean isKeyDown() {
        return action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;
    }

    public boolean isKeyPressed() {
        return action == GLFW.GLFW_PRESS;
    }

    public boolean isKeyReleased() {
        return action == GLFW.GLFW_RELEASE;
    }
}
