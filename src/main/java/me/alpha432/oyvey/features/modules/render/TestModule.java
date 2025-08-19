package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.traits.Util;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;

import static me.alpha432.oyvey.manager.RotationManager.prePitch;
import static me.alpha432.oyvey.manager.RotationManager.preYaw;

public class TestModule extends Module implements Util {
    private VertexConsumer vertexConsumer;

    public TestModule() {
        super("TestModule", "", Category.CLIENT);
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


    @Override
    public void onEnable(){
        int preYaw = (int) mc.player.getYaw();
        int prePitch = (int) mc.player.getPitch();


    }

    @Override
    public void onDisable(){
        mc.player.setYaw(preYaw);
        mc.player.setPitch(prePitch);
    }
}

