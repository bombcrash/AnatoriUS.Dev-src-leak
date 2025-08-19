package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FeetPlace extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public FeetPlace() {
        super("FeetPlace", "Places obsidian around your feet", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        BlockPos playerPos = mc.player.getBlockPos();

        BlockPos[] surround = new BlockPos[]{
                playerPos.north(),
                playerPos.south(),
                playerPos.east(),
                playerPos.west()
        };

        for (BlockPos pos : surround) {
            if (mc.world.isAir(pos)) {
                int obsidianSlot = findObsidianSlot();
                if (obsidianSlot == -1) return;

                int prevSlot = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(obsidianSlot);

                placeBlock(pos);

                mc.player.getInventory().setSelectedSlot(prevSlot);
            }
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

    private void placeBlock(BlockPos pos) {
        if (mc.interactionManager != null) {
            Vec3d hitVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        }
    }

    private int findObsidianSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.OBSIDIAN) {
                return i;
            }
        }
        return -1;
    }
}
