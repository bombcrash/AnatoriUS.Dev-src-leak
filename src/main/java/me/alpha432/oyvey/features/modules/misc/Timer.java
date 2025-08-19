package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;

public class Timer extends Module {

    public static Timer INSTANCE;

    private final Setting<Float> speed = register(new Setting<>("Speed", 2.0f, 0.1f, 10.0f));
    private final Setting<Boolean> pulse = register(new Setting<>("Pulse", false));
    private final Setting<Integer> pulseDelay = register(new Setting<>("PulseDelay", 20, 5, 100, v -> pulse.getValue()));
    private final Setting<String> physicsMode = register(new Setting<>("Physics", "Always",
            s -> s.equals("Always") || s.equals("OnlyGround") || s.equals("OnlyAir")));

    private int tickCounter = 0;
    private boolean pulsing = true;

    public Timer() {
        super("Timer", "Changes game tick speed", Category.MISC);
        INSTANCE = this;
    }

    public float getTimerSpeed() {
        if (mc.player == null || mc.world == null) return 1.0f;

        // Physics mode kontrolü
        switch (physicsMode.getValue()) {
            case "OnlyGround":
                if (!mc.player.isOnGround()) return 1.0f;
                break;
            case "OnlyAir":
                if (mc.player.isOnGround()) return 1.0f;
                break;
        }

        // Pulse modu aktifse
        if (pulse.getValue()) {
            tickCounter++;
            if (tickCounter >= pulseDelay.getValue()) {
                pulsing = !pulsing;
                tickCounter = 0;
            }
            return pulsing ? speed.getValue() : 1.0f;
        }

        return speed.getValue();
    }

    public int getExtraTicks() {
        float t = getTimerSpeed();
        return (int) Math.max(0, t - 1);
    }

    @Override
    public void onDisable() {
        tickCounter = 0;
        pulsing = true;
    }

    @Override
    public void onRender(MatrixStack matrices, float tickDelta) {

    }

    @Override
    public void onRenderWorldLast(MatrixStack matrices, float tickDelta) {

    }

    @Override
    public void onRender3D(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, float tickDelta) {

    }

    @Override
    public void onRender3D(float partialTicks) {

    }

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider) {

    }

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, float tickDelta) {

    }

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumers vertexConsumers, float tickDelta) {

    }

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumer vertexConsumer, float tickDelta) {

    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float tickDelta) {

    }

    // Optional: Timer mixin'inde kullanılmak üzere hız getter
    public static float currentSpeed() {
        return INSTANCE != null ? INSTANCE.getTimerSpeed() : 1.0f;
    }

    @Override
    public void onPacketSend(PacketEvent.Send event) {}

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {}

    @Override
    public void onRender() {

    }

    @Override
    public void onRenderWorldLast(MatrixStack matrices) {

    }

    @Override
    public void onRender(DrawContext context) {

    }
}
