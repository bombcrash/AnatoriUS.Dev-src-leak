package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.event.impl.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import com.google.common.eventbus.Subscribe;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HoleESP extends Module {

    private final List<BlockPos> holes = new ArrayList<>();

    public HoleESP() {
        super("HoleESP", "Highlights safe holes", Category.RENDER);
    }

    @Override
    public void onTick() {
        holes.clear();
        BlockPos playerPos = mc.player.getBlockPos();

        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                for (int y = -3; y <= 2; y++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (isHole(mc.world, pos)) {
                        holes.add(pos);
                    }
                }
            }
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        for (BlockPos pos : holes) {
            Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

            Color color = getHoleColor(mc.world, pos);

            RenderUtil.drawBox(event.getMatrix(), box, color, 0.4f);
            RenderUtil.drawBox(event.getMatrix(), box, color, 1.0f);
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

    private Color getHoleColor(World world, BlockPos pos) {
        Block downBlock = world.getBlockState(pos.down()).getBlock();

        if (downBlock == Blocks.BEDROCK) {
            return new Color(0, 255, 255, 255);
        } else if (downBlock == Blocks.OBSIDIAN) {
            return new Color(255, 0, 255, 255);
        } else {
            return new Color(255, 0, 0, 255);
        }
    }

    private boolean isHole(World world, BlockPos pos) {

        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir() || !world.getBlockState(pos.up(2)).isAir())
            return false;

        Block downBlock = world.getBlockState(pos.down()).getBlock();
        if (downBlock != Blocks.BEDROCK && downBlock != Blocks.OBSIDIAN) return false;

        return isSolidBlock(world, pos.north()) &&
                isSolidBlock(world, pos.south()) &&
                isSolidBlock(world, pos.east()) &&
                isSolidBlock(world, pos.west());
    }

    private boolean isSolidBlock(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block.getDefaultState().isOpaque();
    }
}
