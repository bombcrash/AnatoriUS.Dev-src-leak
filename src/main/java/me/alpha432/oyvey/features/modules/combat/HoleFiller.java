package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import me.alpha432.oyvey.util.models.Timer;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static me.alpha432.oyvey.util.InventoryUtil.findHotbarBlock;
import static me.alpha432.oyvey.util.InventoryUtil.switchToSlot;

public class HoleFiller extends Module {
    protected String display = "";

    protected void setDisplayInfo(String str) {
        this.display = str;
    }

    private final Setting<Double> range = this.register(new Setting<>("Range", 4.8, 1.0, 6.0));
    private final Setting<Integer> delay = this.register(new Setting<>("Delay", 2, 0, 20));
    private final Setting<Boolean> render = this.register(new Setting<>("Render", true));

    private final Timer timer = new Timer();
    private final List<BlockPos> holes = new ArrayList<>();
    private BlockPos targetHole = null;
    private PlayerEntity target = null;

    public HoleFiller() {
        super("HoleFiller", "Fills holes near enemies", Category.COMBAT);

        // Register tick event (Fabric)
        ClientTickEvents.END_CLIENT_TICK.register(client -> onUpdate());

        // Register render event (Fabric)
        WorldRenderEvents.LAST.register(context -> onRender(context.matrixStack()));
    }

    public void onUpdate() {
        if (fullNullCheck()) {
            setDisplayInfo("");
            return;
        }

        target = null;
        double closestDistance = range.getValue();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity)) continue;
            if (entity == mc.player) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist <= closestDistance) {
                closestDistance = dist;
                target = (PlayerEntity) entity;
            }
        }

        if (target == null) {
            setDisplayInfo("");
            return;
        }

        setDisplayInfo(target.getName().getString());

        if (!timer.passedMs(delay.getValue() * 50)) return;

        findHoles(mc.player, range.getValue().floatValue());
        findTargetHole();

        if (targetHole != null) {
            int obbySlot = findHotbarBlock(Blocks.OBSIDIAN);
            if (obbySlot == -1) return;

            switchToSlot(obbySlot, true);

            BlockUtil.placeBlock(targetHole);

            timer.reset();
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

    private void onRender(MatrixStack matrixStack) {
        if (render.getValue() && targetHole != null) {
            VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();
            RenderUtil.drawBox(matrixStack, vertexConsumers, targetHole, new java.awt.Color(255, 0, 0, 128), 0.5f, 0.5f);
        }
    }

    private void findHoles(PlayerEntity player, float range) {
        holes.clear();
        BlockPos playerPos = player.getBlockPos();
        int rangeInt = (int) range;

        for (int x = -rangeInt; x <= rangeInt; x++) {
            for (int y = -rangeInt; y <= rangeInt; y++) {
                for (int z = -rangeInt; z <= rangeInt; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (player.getBlockPos().isWithinDistance(pos, range) && BlockUtil.isHole(pos)) {
                        holes.add(pos);
                    }
                }
            }
        }
    }

    private void findTargetHole() {
        targetHole = holes.stream()
                .min(Comparator.comparingDouble(pos -> mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)))
                .orElse(null);
    }

    public boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }
}
