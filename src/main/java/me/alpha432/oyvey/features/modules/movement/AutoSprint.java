package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;

public class AutoSprint extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AutoSprint() {
        super("AutoSprint", "Automatically sprints when moving forward", Category.MOVEMENT);
    }

    // UpdateEvent kullanıyorsan genellikle mod framework'üne göre
    // @Override onUpdate() veya event aboneliği gerekebilir.
    // Burada onUpdate() override edelim, framework buna uygunsa çalışır.

    @Override
    public void onUpdate() {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) return;

        KeyBinding forwardKey = mc.options.forwardKey;

        // İleri tuşu basılıysa ve oyuncu eğilmiyorsa sprint aktif et
        if (forwardKey.isPressed() && !player.isSneaking()) {
            player.setSprinting(true);
        } else {
            player.setSprinting(false);
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
    public void onPacketSend(me.alpha432.oyvey.event.impl.PacketEvent.Send event) {
        // İstersen buraya packet bazlı sprint engelleme kodu koyabilirsin
    }

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {

    }

    @Override
    public void onRender() {
        // Buraya render kodu ekleyebilirsin, ama AutoSprint'te gerek yok
    }

    @Override
    public void onRenderWorldLast(MatrixStack matrices) {

    }

    @Override
    public void onRender(DrawContext context) {

    }
}
