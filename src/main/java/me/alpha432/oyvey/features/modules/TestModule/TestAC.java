package me.alpha432.oyvey.features.modules.TestModule;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.commands.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.minecraft.block.Blocks.BEDROCK;
import static net.minecraft.block.Blocks.OBSIDIAN;

public class TestAC extends Module {

    private final Setting<Float> placeRange = this.register(new Setting<>("PlaceRange", 6.0f, 0.0f, 6.0f));
    private final Setting<Float> attackRange = this.register(new Setting<>("AttackRange", 6.0f, 0.0f, 6.0f));
    private final Setting<Float> minDamage = this.register(new Setting<>("MinDamage", 6.0f, 0.0f, 20.0f));
    private final Setting<Float> maxSelfDamage = this.register(new Setting<>("MaxSelfDamage", 10.0f, 0.0f, 36.0f));
    private final Setting<Boolean> renderDebug = this.register(new Setting<>("RenderDebug", true));
    private final Setting<Integer> red = this.register(new Setting<>("DebugRed", 200, 0, 255, v -> renderDebug.getValue()));
    private final Setting<Integer> green = this.register(new Setting<>("DebugGreen", 10, 0, 255, v -> renderDebug.getValue()));
    private final Setting<Integer> blue = this.register(new Setting<>("DebugBlue", 230, 0, 255, v -> renderDebug.getValue()));
    private final Setting<Integer> breakSpeed = this.register(new Setting<>("BreakSpeed", 50, 0, 1000));
    private final Setting<Integer> placeSpeed = this.register(new Setting<>("PlaceSpeed", 50, 0, 1000));
    private final Setting<Boolean> rotate = this.register(new Setting<>("Rotate", false));
    private final Setting<Boolean> silentRotate = this.register(new Setting<>("SilentRotate", true));
    private final Setting<Boolean> prioritizeHealth = this.register(new Setting<>("PrioritizeHealth", true));
    private final Setting<Boolean> prioritizeArmor = this.register(new Setting<>("PrioritizeArmor", false));
    private final Setting<Boolean> prioritizeDistance = this.register(new Setting<>("PrioritizeDistance", true));
    private final Setting<Boolean> strictPlacement = this.register(new Setting<>("StrictPlacement", false));

    private BlockPos lastPlacedCrystalPos = null;
    private long lastBreakTime = 0;
    private long lastPlaceTime = 0;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TestAC() {
        super("TestAC", "Improved crystal aura with damage calc, rotation, target prioritization and async.", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        Command.sendMessage(this.getName() + " was toggled " + Formatting.GREEN + "on" + Formatting.GRAY + ".");
    }

    @Override
    public void onDisable() {
        Command.sendMessage(this.getName() + " was toggled " + Formatting.RED + "off" + Formatting.GRAY + ".");
        executor.shutdownNow();
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastBreakTime >= breakSpeed.getValue()) {
            breakCrystal();
        }

        if (currentTime - lastPlaceTime >= placeSpeed.getValue()) {
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

    private void breakCrystal() {
        List<EndCrystalEntity> crystals = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e instanceof EndCrystalEntity && e.squaredDistanceTo(mc.player) <= attackRange.getValue() * attackRange.getValue())
                .map(e -> (EndCrystalEntity) e)
                .toList();

        if (crystals.isEmpty()) return;

        EndCrystalEntity targetCrystal = null;
        double bestScore = Double.MAX_VALUE;

        for (EndCrystalEntity crystal : crystals) {
            PlayerEntity targetPlayer = getPrioritizedTarget(crystal.getPos());
            if (targetPlayer == null) continue;

            float damage = calculateDamage(targetPlayer, crystal.getPos());
            if (damage < minDamage.getValue()) continue;

            double score = 0;
            if (prioritizeDistance.getValue()) {
                score += mc.player.squaredDistanceTo(crystal);
            }
            if (prioritizeHealth.getValue()) {
                score += targetPlayer.getHealth();
            }
            if (prioritizeArmor.getValue()) {
                score += getArmorValue(targetPlayer) * 2;
            }

            if (score < bestScore) {
                bestScore = score;
                targetCrystal = crystal;
            }
        }

        if (targetCrystal != null) {
            if (rotate.getValue()) {
                if (silentRotate.getValue()) silentRotateToEntity(targetCrystal);
                else rotateToEntity(targetCrystal);
            }

            mc.interactionManager.attackEntity(mc.player, targetCrystal);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastBreakTime = System.currentTimeMillis();
        }
    }

    private PlayerEntity getPrioritizedTarget(Vec3d fromPos) {
        List<PlayerEntity> players = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && !p.isDead() && !p.isSpectator() && !p.isInvisible())
                .collect(Collectors.toList());

        PlayerEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (PlayerEntity player : players) {
            if (player.getHealth() <= 0) continue;

            double score = 0;
            if (prioritizeDistance.getValue()) score += player.getPos().squaredDistanceTo(fromPos);
            if (prioritizeHealth.getValue()) score += player.getHealth();
            if (prioritizeArmor.getValue()) score += getArmorValue(player) * 2;

            if (score < bestScore) {
                bestScore = score;
                best = player;
            }
        }

        return best;
    }

    private int getArmorValue(PlayerEntity player) {
        // Burada Minecraft 1.21.1 API'sine göre zırh değeri hesaplanmalı.
        // Örnek olarak basit toplam zırh parçaları sayısı:
        return player.getArmor();
    }

    private void placeCrystalAsync() {
        executor.submit(() -> {
            try {
                if (mc.player == null || mc.world == null) return;

                Hand crystalHand;
                if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) crystalHand = Hand.OFF_HAND;
                else if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) crystalHand = Hand.MAIN_HAND;
                else return;

                for (PlayerEntity target : mc.world.getPlayers()) {
                    if (target == mc.player || target.isSpectator() || target.isDead()) continue;

                    BlockPos targetPos = target.getBlockPos();

                    for (int x = -3; x <= 3; x++) {
                        for (int y = -2; y <= 2; y++) {
                            for (int z = -3; z <= 3; z++) {
                                BlockPos basePos = targetPos.add(x, y, z);

                                if (mc.player.getPos().distanceTo(Vec3d.of(basePos)) > placeRange.getValue()) continue;
                                if (!isValidBaseBlock(basePos)) continue;
                                if (!mc.world.getBlockState(basePos.up()).isAir() || !mc.world.getBlockState(basePos.up(2)).isAir()) continue;

                                Vec3d crystalPos = Vec3d.of(basePos).add(0.5, 1.0, 0.5);

                                float targetDamage = calculateDamage(target, crystalPos);
                                float selfDamage = calculateDamage(mc.player, crystalPos);

                                if (targetDamage < minDamage.getValue() || selfDamage > maxSelfDamage.getValue()) continue;

                                mc.execute(() -> {
                                    if (mc.interactionManager != null) {
                                        if (rotate.getValue()) {
                                            if (silentRotate.getValue()) silentRotateToPos(crystalPos);
                                            else rotateToPos(crystalPos);
                                        }

                                        BlockHitResult hit = new BlockHitResult(crystalPos, Direction.UP, basePos, false);
                                        mc.interactionManager.interactBlock(mc.player, crystalHand, hit);
                                        mc.player.swingHand(crystalHand);
                                        lastPlacedCrystalPos = basePos;
                                        lastPlaceTime = System.currentTimeMillis();
                                    }
                                });
                                return; // Sadece bir tane yerleştir
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    private boolean isValidBaseBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == BEDROCK || mc.world.getBlockState(pos).getBlock() == OBSIDIAN;
    }

    private float calculateDamage(LivingEntity target, Vec3d crystalPos) {
        // Minecraft 1.21.1'de hasar hesaplaması detaylıdır ve genelde entegre yöntemler gerektirir.
        // Burada basit örnek olarak mesafeye göre ters orantılı hasar varsayalım.
        double distance = target.getPos().distanceTo(crystalPos);
        float baseDamage = 12.0f; // Ortalama patlama hasarı

        float damage = (float) (baseDamage - distance * 2.0);
        if (damage < 0) damage = 0;

        // Gerçek projede, patlama hasar hesaplaması ve zırh, direnç faktörleri burada hesaplanmalı.
        return damage;
    }

    // Rotation helper methods
    private void rotateToEntity(Entity entity) {
        // Oyuncuyu direkt entity'ye bakacak şekilde döndürür
        // Bu kısım Minecraft API'ye göre implement edilmeli
        Vec3d eyesPos = mc.player.getEyePos();
        Vec3d targetPos = entity.getPos().add(0, entity.getHeight() / 2.0, 0);
        float[] rotations = calculateLookAt(targetPos, eyesPos);
        mc.player.setYaw(rotations[0]);
        mc.player.setPitch(rotations[1]);
    }

    private void silentRotateToEntity(Entity entity) {
        // Paket bazında dönme, oyuncu görünmez ama sunucu dönme olarak algılar
        // Bu örnek için boş bırakıldı, kendi paketinle değiştir
    }

    private void rotateToPos(Vec3d pos) {
        Vec3d eyesPos = mc.player.getEyePos();
        float[] rotations = calculateLookAt(pos, eyesPos);
        mc.player.setYaw(rotations[0]);
        mc.player.setPitch(rotations[1]);
    }

    private void silentRotateToPos(Vec3d pos) {
        // Aynı şekilde silent rotate için boş bırakıldı
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
