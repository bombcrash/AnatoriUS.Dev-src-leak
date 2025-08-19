package me.alpha432.oyvey.event.impl;

import me.alpha432.oyvey.event.Event;

public class EventMouse extends Event {

    public static final int BUTTON_LEFT = 0;
    public static final int BUTTON_RIGHT = 1;
    public static final int BUTTON_MIDDLE = 2;

    private final int button;
    private final int action;
    private final int mods;
    private boolean canceled;

    /**
     * @param button Mouse tuşu (0: sol, 1: sağ, 2: orta)
     * @param action GLFW aksiyonu (0: bırakma, 1: basma, 2: tekerlek kaydırma)
     * @param mods  Mod tuşları (shift, ctrl vb.)
     */
    public EventMouse(int button, int action, int mods) {
        this.button = button;
        this.action = action;
        this.mods = mods;
        this.canceled = false;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public int getMods() {
        return mods;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
