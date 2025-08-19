package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Module {

    private final Setting<Float> speedMultiplier = this.register(new Setting<>("Speed", 1.0f, 0.1f, 2.0f));

    public NoSlow() {
        super("NoSlow", "Eşya kullanırken yavaşlamayı engeller ve bakış yönünde hareket sağlar", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) return;

        if (player.isUsingItem()) {
            // Bakış yönünü al
            Vec3d lookVec = player.getRotationVec(1.0F).normalize();

            // Hız çarpanı
            double speed = speedMultiplier.getValue();

            // Bakış yönü XZ'yi kullan, Y eksenini koru
            double motionX = lookVec.x * speed;
            double motionZ = lookVec.z * speed;
            double motionY = player.getVelocity().y; // mevcut Y hızını koru (zıplama, düşme vb)

            // Hızı uygula
            player.setVelocity(motionX, motionY, motionZ);
            player.velocityDirty = true;

            // Sprinti zorla aç
            player.setSprinting(true);
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
