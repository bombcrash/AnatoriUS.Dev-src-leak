package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.event.impl.Render2DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.combat.AutoCrystal;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HudModule extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final Setting<Boolean> rainbow = this.register(new Setting<>("Rainbow", true, "RGB animasyonlu renk"));
    private final Setting<Integer> speed = this.register(new Setting<>("Speed", 10, 1, 100, "RGB geçiş hızı"));
    private final Setting<Integer> red = this.register(new Setting<>("Red", 255, 0, 255, "Kırmızı renk"));
    private final Setting<Integer> green = this.register(new Setting<>("Green", 0, 0, 255, "Yeşil renk"));
    private final Setting<Integer> blue = this.register(new Setting<>("Blue", 0, 0, 255, "Mavi renk"));

    public HudModule() {
        super("Hud", "Ekranda aktif modları gösterir", Category.CLIENT);
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        int color = rainbow.getValue()
                ? Color.HSBtoRGB((System.currentTimeMillis() % (360L * speed.getValue())) / (360.0f * speed.getValue()), 1, 1)
                : new Color(red.getValue(), green.getValue(), blue.getValue()).getRGB();

        // Başlık
        event.getContext().drawTextWithShadow(mc.textRenderer, "Anatorius 0.6", 3, 1, color);

        // Aktif modlar
        List<Module> active = OyVey.moduleManager.getEnabledModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())))
                .collect(Collectors.toList());

        int y = 2;
        for (Module m : active) {
            String name = m.getName();

            if (m instanceof AutoCrystal ac && ac.isEnabled()) {
                String tgt = ac.getTarget() != null ? ac.getTarget().getName().getString() : "None";
                name += " [" + tgt + " | " + ac.getCPS() + " CPS]";
            }

            int x = mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(name) - 3;
            event.getContext().drawTextWithShadow(mc.textRenderer, name, x, y, color);
            y += 10;
        }
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

    @Override
    public void onPacketSend(PacketEvent.Send event) {

    }

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {

    }

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
