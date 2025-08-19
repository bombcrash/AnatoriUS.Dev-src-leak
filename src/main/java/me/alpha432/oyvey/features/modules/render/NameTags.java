package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class NameTags extends Module {

    public NameTags() {
        super("NameTags", "Oyuncuların üstünde isim ve sağlık gösterir", Category.RENDER);
    }

    @Override
    public void onRender(MatrixStack matrices, float tickDelta) {

    }

    @Override
    public void onRenderWorldLast(MatrixStack matrices, float tickDelta) {

    }

    @Override
    public void onRender3D(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || player.isInvisible()) continue;
            renderNameTag(matrices, vertexConsumers, player, camPos);
        }
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

    private void renderNameTag(MatrixStack matrices, VertexConsumerProvider vertexConsumers, PlayerEntity player, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;

        matrices.push();

        Vec3d pos = player.getPos().add(0, player.getHeight() + 0.5, 0);
        matrices.translate(
                pos.x - camPos.x,
                pos.y - camPos.y,
                pos.z - camPos.z
        );

        matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);

        String name = Formatting.BOLD + player.getName().getString();
        String health = String.format("%.1f ❤", player.getHealth());
        String full = name + " " + Formatting.RED + health;

        int width = tr.getWidth(full) / 2;

        // Düzeltilmiş draw metodu
        tr.draw(
                Text.literal(full),
                -width,
                0,
                0xFFFFFFFF,
                false,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0,
                15728880
        );

        matrices.pop();
    }
}