package me.alpha432.oyvey.features.modules.movement;

import com.google.common.eventbus.Subscribe;
import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.event.impl.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.Random;

public class Scaffold extends Module {

    public Scaffold() {
        super("Scaffold", "Disables all knockback", Category.MOVEMENT);
    }

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Random random = new Random();
    private boolean isPlacing = false;
    private int prevSlot = -1;

    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        BlockPos posBelow = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(posBelow).isReplaceable()) {
            resetSlot();
            return;
        }

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) {
            resetSlot();
            return;
        }

        rotateSpoofSilent(posBelow); // silent spoof
        spoofSlot(blockSlot);

        placeBlock(posBelow);

        if (random.nextInt(5) == 0) sleepRandom(); // Legit gecikme
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block != Blocks.AIR && block != Blocks.WATER && block != Blocks.LAVA) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void spoofSlot(int slot) {
        if (mc.player.getInventory().getSelectedSlot() != slot) {
            prevSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(slot);
        }
    }

    private void resetSlot() {
        if (prevSlot != -1) {
            mc.player.getInventory().setSelectedSlot(prevSlot);
            prevSlot = -1;
        }
    }

    // Silent rotation spoof (kafayı döndürmez, sadece sunucuya gönderir)
    private void rotateSpoofSilent(BlockPos blockPos) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d target = new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
        Vec3d dir = target.subtract(eyes);
        double dist = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        float spoofYaw = (float) (Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90F);
        float spoofPitch = (float) (-Math.toDegrees(Math.atan2(dir.y, dist)));

        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(
                        spoofYaw,
                        spoofPitch,
                        mc.player.isOnGround(),
                        mc.player.isOnGround()
                )
        );
    }

    private void placeBlock(BlockPos pos) {
        BlockHitResult hitResult = new BlockHitResult(
                new Vec3d(pos.getX(), pos.getY(), pos.getZ()),
                Direction.UP,
                pos,
                false
        );
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void sleepRandom() {
        try {
            Thread.sleep(random.nextInt(35) + 15); // 15–50ms legit gecikme
        } catch (InterruptedException ignored) {
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (mc.crosshairTarget instanceof BlockHitResult result) {
            VoxelShape shape = mc.world.getBlockState(result.getBlockPos()).getOutlineShape(mc.world, result.getBlockPos());
            if (shape.isEmpty()) return;
            Box box = shape.getBoundingBox();
            box = box.offset(result.getBlockPos());
            RenderUtil.drawBox(event.getMatrix(), box, Color.red, 1f);
        }
    }

    @Override
    public void onDisable() {
        resetSlot();
    }

    @Override
    public void onEnable() {
        prevSlot = -1;
    }

    public boolean isPlacing() {
        return isPlacing;
    }

    public void setPlacing(boolean placing) {
        isPlacing = placing;
    }

    // Gereksiz render ve packet override'ları boş bırakıldı
    @Override public void onRender(MatrixStack matrices, float tickDelta) {}
    @Override public void onRenderWorldLast(MatrixStack matrices, float tickDelta) {}

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
    public void onRender3D(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta) {}

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumers vertexConsumers, float tickDelta) {

    }

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumer vertexConsumer, float tickDelta) {

    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float tickDelta) {

    }

    @Override public void onPacketSend(PacketEvent.Send event) {}
    @Override public void onPacketReceive(PacketEvent.Receive event) {}

    @Override
    public void onRender() {}

    @Override
    public void onRenderWorldLast(MatrixStack matrices) {

    }

    @Override
    public void onRender(DrawContext context) {

    }

}
