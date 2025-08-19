package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static me.alpha432.oyvey.features.commands.Command.sendMessage;
import static net.minecraft.block.Blocks.BEDROCK;
import static net.minecraft.block.Blocks.OBSIDIAN;

public class TestAC extends Module {

    private final Setting<Float> placeRange = register(new Setting<>("PlaceRange", 5.5f, 0f, 6f));
    private final Setting<Float> breakRange = register(new Setting<>("BreakRange", 5.5f, 0f, 6f));
    private final Setting<Float> minDamage = register(new Setting<>("MinDamage", 6.0f, 0f, 20f));
    private final Setting<Float> maxSelfDamage = register(new Setting<>("MaxSelfDamage", 8.0f, 0f, 20f));
    private final Setting<Boolean> rotate = register(new Setting<>("Rotate", true));
    private final Setting<Boolean> silentRotate = register(new Setting<>("SilentRotate", true));

    private long lastPlaceTime = 0;
    private long lastBreakTime = 0;
    private final int placeDelay = 100; // ms
    private final int breakDelay = 100; // ms

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TestAC() {
        super("TestAC", "Basic crystal aura with async placement and breaking", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        sendMessage("Test enabled");
    }

    @Override
    public void onDisable() {
        executor.shutdownNow();
        sendMessage("Test disabled");
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();

        if (now - lastBreakTime >= breakDelay) {
            breakCrystals();
        }

        if (now - lastPlaceTime >= placeDelay) {
            placeCrystalAsync();
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

    private void breakCrystals() {
        List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(EndCrystalEntity.class,
                mc.player.getBoundingBox().expand(breakRange.getValue()),
                e -> mc.player.squaredDistanceTo(e) <= breakRange.getValue() * breakRange.getValue())
                .stream()
                .collect(Collectors.toList());

        if (crystals.isEmpty()) return;

        EndCrystalEntity target = crystals.get(0); // Basitçe ilkini kır

        if (rotate.getValue()) {
            if (silentRotate.getValue()) silentRotateToEntity(target);
            else rotateToEntity(target);
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        lastBreakTime = System.currentTimeMillis();
    }

    private void placeCrystalAsync() {
        executor.submit(() -> {
            if (mc.player == null || mc.world == null) return;

            Hand crystalHand;
            if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) crystalHand = Hand.OFF_HAND;
            else if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) crystalHand = Hand.MAIN_HAND;
            else return;

            for (PlayerEntity target : mc.world.getPlayers()) {
                if (target == mc.player || target.isDead() || target.isSpectator()) continue;

                BlockPos targetPos = target.getBlockPos();

                // Basit 3x3x3 alan araması
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos basePos = targetPos.add(x, y, z);

                            if (mc.player.getPos().distanceTo(Vec3d.of(basePos)) > placeRange.getValue()) continue;

                            if (!isValidBaseBlock(basePos)) continue;
                            if (!mc.world.getBlockState(basePos.up()).isAir()) continue;
                            if (!mc.world.getBlockState(basePos.up(2)).isAir()) continue;

                            Vec3d crystalPos = Vec3d.of(basePos).add(0.5, 1.0, 0.5);

                            float targetDamage = calculateDamage(target, crystalPos);
                            float selfDamage = calculateDamage(mc.player, crystalPos);

                            if (targetDamage < minDamage.getValue()) continue;
                            if (selfDamage > maxSelfDamage.getValue()) continue;

                            mc.execute(() -> {
                                if (rotate.getValue()) {
                                    if (silentRotate.getValue()) silentRotateToPos(crystalPos);
                                    else rotateToPos(crystalPos);
                                }

                                BlockHitResult hitResult = new BlockHitResult(crystalPos, Direction.UP, basePos, false);
                                mc.interactionManager.interactBlock(mc.player, crystalHand, hitResult);
                                mc.player.swingHand(crystalHand);
                                lastPlaceTime = System.currentTimeMillis();
                            });
                            return; // Sadece bir tane yerleştir
                        }
                    }
                }
            }
        });
    }

    private boolean isValidBaseBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == BEDROCK || mc.world.getBlockState(pos).getBlock() == OBSIDIAN;
    }

    private float calculateDamage(Entity target, Vec3d crystalPos) {
        // Basit mesafeye göre ters orantılı hasar hesaplama
        double distance = target.getPos().distanceTo(crystalPos);
        float baseDamage = 12.0f;

        float damage = (float) (baseDamage - distance * 2.0);
        return Math.max(damage, 0);
    }

    // Döndürme işlemleri (rotate ve silent rotate)

    private void rotateToEntity(Entity entity) {
        Vec3d eyesPos = mc.player.getEyePos();
        Vec3d targetPos = entity.getPos().add(0, entity.getHeight() / 2.0, 0);
        float[] rotations = calculateLookAt(targetPos, eyesPos);
        mc.player.setYaw(rotations[0]);
        mc.player.setPitch(rotations[1]);
    }

    private void silentRotateToEntity(Entity entity) {
        // Paketle oyuncunun bakış açısını değiştir (opsiyonel)
        // Şimdilik boş bırakıldı
    }

    private void rotateToPos(Vec3d pos) {
        Vec3d eyesPos = mc.player.getEyePos();
        float[] rotations = calculateLookAt(pos, eyesPos);
        mc.player.setYaw(rotations[0]);
        mc.player.setPitch(rotations[1]);
    }

    private void silentRotateToPos(Vec3d pos) {
        // Paketle oyuncunun bakış açısını değiştir (opsiyonel)
        // Şimdilik boş bırakıldı
    }

    private float[] calculateLookAt(Vec3d target, Vec3d position) {
        double diffX = target.x - position.x;
        double diffY = target.y - position.y;
        double diffZ = target.z - position.z;

        double distXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, distXZ));

        return new float[]{yaw, pitch};
    }
}
