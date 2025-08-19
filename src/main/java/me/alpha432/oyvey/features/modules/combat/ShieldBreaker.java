package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class ShieldBreaker extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private int prevSlot = -1;
    private boolean didBreak = false;

    public ShieldBreaker() {
        super("ShieldBreaker", "Kalkan kırmak için baltaya geçer ve sonra kapanır", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (didBreak) {
            disable();
            return;
        }

        // Eğer hedef yoksa çık
        if (!(mc.crosshairTarget instanceof EntityHitResult entityHit)) return;

        if (!(entityHit.getEntity() instanceof PlayerEntity target)) return;

        // Hedef kalkan çektiyse baltaya geç ve saldır
        if (target.isUsingItem() && target.getActiveItem().getItem().toString().contains("shield")) {
            int axeSlot = findAxeInHotbar();
            if (axeSlot == -1) return;

            if (prevSlot == -1) {
                prevSlot = mc.player.getInventory().getSelectedSlot();
            }

            mc.player.getInventory().setSelectedSlot(axeSlot);

            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);

            didBreak = true;
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

    @Override
    public void onDisable() {
        if (prevSlot != -1) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
        }
        prevSlot = -1;
        didBreak = false;
    }

    private int findAxeInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }
}
