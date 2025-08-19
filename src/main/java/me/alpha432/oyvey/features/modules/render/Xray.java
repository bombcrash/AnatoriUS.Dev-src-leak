package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.event.impl.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static me.alpha432.oyvey.util.render.RenderUtil.drawBox;

public class Xray extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Map<Block, Color> oreColors = new HashMap<>();
    private final Set<BlockPos> foundOres = new HashSet<>();
    private int tickCounter = 0;

    static {
        oreColors.put(Blocks.DIAMOND_ORE, new Color(0, 128, 255, 120));
        oreColors.put(Blocks.DIAMOND_BLOCK, new Color(0, 128, 255, 120));
        oreColors.put(Blocks.IRON_ORE, new Color(180, 180, 180, 120));
        oreColors.put(Blocks.GOLD_ORE, new Color(255, 215, 0, 120));
        oreColors.put(Blocks.COAL_ORE, new Color(30, 30, 30, 120));
        oreColors.put(Blocks.LAPIS_ORE, new Color(33, 66, 201, 120));
        oreColors.put(Blocks.EMERALD_ORE, new Color(0, 255, 0, 120));
        oreColors.put(Blocks.REDSTONE_ORE, new Color(255, 0, 0, 120));
        oreColors.put(Blocks.NETHER_QUARTZ_ORE, new Color(255, 255, 255, 120));
        oreColors.put(Blocks.NETHER_GOLD_ORE, new Color(255, 215, 0, 120));
        // Yeni madenler eklendi:
        oreColors.put(Blocks.COPPER_ORE, new Color(184, 115, 51, 120));
        oreColors.put(Blocks.DEEPSLATE_DIAMOND_ORE, new Color(0, 128, 255, 120));
        oreColors.put(Blocks.DEEPSLATE_IRON_ORE, new Color(180, 180, 180, 120));
        oreColors.put(Blocks.DEEPSLATE_GOLD_ORE, new Color(255, 215, 0, 120));
        oreColors.put(Blocks.DEEPSLATE_COAL_ORE, new Color(30, 30, 30, 120));
        oreColors.put(Blocks.DEEPSLATE_LAPIS_ORE, new Color(33, 66, 201, 120));
        oreColors.put(Blocks.DEEPSLATE_EMERALD_ORE, new Color(0, 255, 0, 120));
        oreColors.put(Blocks.DEEPSLATE_REDSTONE_ORE, new Color(255, 0, 0, 120));
        oreColors.put(Blocks.ANCIENT_DEBRIS, new Color(139, 69, 19, 120));
    }

    public Xray() {
        super("Xray", "Highlights ores through walls", Category.RENDER);
    }

    @Override
    public void onEnable() {
        foundOres.clear();
    }

    private void scanNearbyOres() {
        if (mc.world == null || mc.player == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int radius = 12;

        // Sadece belli aralıklarla tarama yap
        // Böylece xray koruma server paketlerini biraz azaltabiliriz
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (oreColors.containsKey(block)) {
                        // Bazen küçük pozisyon değişikliği ile koruma atlatılabilir
                        foundOres.add(pos.toImmutable());
                    }
                }
            }
        }
    }

    @Override
    public void onTick() {
        tickCounter++;
        if (tickCounter >= 20) { // Her 20 tickte bir tara
            tickCounter = 0;
            foundOres.clear();  // Eskileri temizle ki chunk yenilendiğinde güncel olsun
            scanNearbyOres();
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        for (BlockPos pos : foundOres) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!oreColors.containsKey(block)) continue;

            VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
            if (shape.isEmpty()) continue;

            Box box = shape.getBoundingBox().offset(pos);

            Color color = oreColors.get(block);

            // Basit animasyon veya opaklık varyasyonu atlatma için örnek:
            float alphaOffset = (float) ((Math.sin(System.currentTimeMillis() / 500.0 + pos.getX()) + 1) / 2 * 50);
            Color animatedColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)Math.min(255, color.getAlpha() + alphaOffset));

            drawBox(event.getMatrix(), box, animatedColor, 3.0f);
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
