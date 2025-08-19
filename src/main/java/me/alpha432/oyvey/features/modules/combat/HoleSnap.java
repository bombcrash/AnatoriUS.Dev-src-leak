package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class HoleSnap extends Module {

    private final Setting<Double> range = register(new Setting<>("Range", 4.0, 1.0, 8.0));
    private final Setting<Boolean> onlyBedrock = register(new Setting<>("OnlyBedrock", false));
    private final Setting<Boolean> smooth = register(new Setting<>("Smooth", true));
    private final Setting<Double> speed = register(new Setting<>("Speed", 0.2, 0.05, 1.0));

    private BlockPos targetHole = null;

    public HoleSnap() {
        super("HoleSnap", "Smoothly slides to a safe hole", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }

        targetHole = findNearestHole();
        if (targetHole == null) {
            disable();
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || targetHole == null) {
            disable();
            return;
        }

        Vec3d playerPos = mc.player.getPos();
        Vec3d holeCenter = new Vec3d(targetHole.getX() + 0.5, targetHole.getY(), targetHole.getZ() + 0.5);
        double distance = playerPos.distanceTo(holeCenter);

        if (distance < 0.1) {
            mc.player.setVelocity(0, 0, 0);
            disable();
            return;
        }

        if (smooth.getValue()) {
            Vec3d direction = holeCenter.subtract(playerPos).normalize().multiply(speed.getValue());
            mc.player.setVelocity(direction);
        } else {
            mc.player.setPosition(holeCenter.x, holeCenter.y, holeCenter.z);
            mc.player.setVelocity(0, 0, 0);
            disable();
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

    private BlockPos findNearestHole() {
        BlockPos playerPos = mc.player.getBlockPos();
        List<BlockPos> holes = new ArrayList<>();

        int r = range.getValue().intValue();
        for (int x = -r; x <= r; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (isHole(pos)) holes.add(pos);
                }
            }
        }

        BlockPos nearest = null;
        double closestDistance = Double.MAX_VALUE;

        for (BlockPos hole : holes) {
            double dist = mc.player.getPos().squaredDistanceTo(hole.getX() + 0.5, hole.getY(), hole.getZ() + 0.5);
            if (dist < closestDistance) {
                closestDistance = dist;
                nearest = hole;
            }
        }

        return nearest;
    }

    private boolean isHole(BlockPos pos) {
        if (!mc.world.getBlockState(pos).isAir()
                || !mc.world.getBlockState(pos.up()).isAir()
                || !mc.world.getBlockState(pos.up(2)).isAir())
            return false;

        BlockPos down = pos.down();
        BlockPos north = pos.north();
        BlockPos south = pos.south();
        BlockPos east = pos.east();
        BlockPos west = pos.west();

        boolean downSolid = isSafe(down);
        boolean northSolid = isSafe(north);
        boolean southSolid = isSafe(south);
        boolean eastSolid = isSafe(east);
        boolean westSolid = isSafe(west);

        return downSolid && northSolid && southSolid && eastSolid && westSolid;
    }

    private boolean isSafe(BlockPos pos) {
        if (onlyBedrock.getValue()) {
            return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
        }
        return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK
                || mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN;
    }
}
