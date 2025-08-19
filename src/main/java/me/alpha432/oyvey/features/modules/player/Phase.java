package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class Phase extends Module {
    private static Phase INSTANCE = new Phase();

    public enum Mode {
        CLIP,
        PEARL,
        STRICT_PEARL
    }

    private final Setting<Mode> mode = register(new Setting<>("Mode", Mode.CLIP));
    private final Setting<Double> pearlDistance = register(new Setting<>("Pearl Distance", 4.0, 2.0, 8.0));
    private final Setting<Boolean> autoDisable = register(new Setting<>("Auto Disable", true));
    private final Setting<Boolean> antiPush = register(new Setting<>("Anti Push", true));
    private final Setting<Integer> pearlDelay = register(new Setting<>("Pearl Delay", 5, 1, 20));

    private boolean pearlThrown = false;
    private int ticksAfterPearl = 0;
    private int pearlAttempts = 0;
    private final int maxPearlAttempts = 3;
    private Vec3d lastValidPosition = null;
    private boolean insideWall = false;

    public Phase() {
        super("Phase", "Phase through walls", Category.PLAYER);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Phase getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Phase();
        }
        return INSTANCE;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        // Anti-push sistemi - oyuncunun bloklar tarafından itilmesini engelle
        if (antiPush.getValue() && (mode.getValue() != Mode.CLIP)) {
            handleAntiPush();
        }

        if (mode.getValue() == Mode.CLIP) {
            handleClipMode();
        } else if (mode.getValue() == Mode.PEARL || mode.getValue() == Mode.STRICT_PEARL) {
            handlePearlMode();
        }

        // Pearl takip sistemi
        if (pearlThrown) {
            ticksAfterPearl++;

            // Pearl'ın etkisini kontrol et
            if (hasPlayerTeleported()) {
                sendMessage("Pearl teleport successful!");
                pearlThrown = false;
                ticksAfterPearl = 0;
                pearlAttempts = 0;
                if (autoDisable.getValue()) {
                    disable();
                }
            } else if (ticksAfterPearl > 60) { // 3 saniye bekle
                sendMessage("Pearl failed, retrying... (" + (pearlAttempts + 1) + "/" + maxPearlAttempts + ")");
                pearlThrown = false;
                ticksAfterPearl = 0;
                pearlAttempts++;

                if (pearlAttempts >= maxPearlAttempts) {
                    sendMessage("Max pearl attempts reached!");
                    if (autoDisable.getValue()) {
                        disable();
                    }
                }
            }
        }
    }

    private void handleAntiPush() {
        if (mc.player.isInsideWall() || isPlayerStuckInBlock()) {
            insideWall = true;
            // Oyuncunun pozisyonunu sabit tut
            mc.player.setVelocity(0, 0, 0);
            mc.player.velocityDirty = true;

            // NoClip'i geçici olarak etkinleştir
            mc.player.noClip = true;
        } else if (insideWall) {
            insideWall = false;
            mc.player.noClip = false;
        }
    }

    private boolean isPlayerStuckInBlock() {
        BlockPos playerPos = mc.player.getBlockPos();
        BlockState blockState = mc.world.getBlockState(playerPos);
        return blockState.isSolidBlock(mc.world, playerPos);
    }

    private void handleClipMode() {
        if (mc.player.isInsideWall() || isPlayerStuckInBlock()) {
            mc.player.noClip = true;
            mc.player.setVelocity(0, 0, 0);
            mc.player.velocityDirty = true;
        } else {
            mc.player.noClip = false;
        }
    }

    private void handlePearlMode() {
        if (pearlThrown) return;

        int pearlSlot = findPearlSlot();
        if (pearlSlot == -1) {
            sendMessage("No Ender Pearl found in hotbar!");
            disable();
            return;
        }

        // Pearl atmak için bekle (spam önleme)
        if (ticksAfterPearl < pearlDelay.getValue()) {
            ticksAfterPearl++;
            return;
        }

        Vec3d targetPosition = calculateOptimalPearlTarget();
        if (targetPosition == null) {
            sendMessage("No valid target found for pearl!");
            return;
        }

        // Son geçerli pozisyonu kaydet
        lastValidPosition = mc.player.getPos();

        throwPearlWithPrecision(pearlSlot, targetPosition);
    }

    private int findPearlSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }

    private Vec3d calculateOptimalPearlTarget() {
        Vec3d playerPos = mc.player.getEyePos();
        Vec3d lookVec = mc.player.getRotationVec(1.0f);

        // Çoklu raycast ile en iyi hedefi bul
        Vec3d bestTarget = null;
        double minDistance = Double.MAX_VALUE;

        // Farklı açılarla deneme yap
        for (double yOffset = -0.2; yOffset <= 0.2; yOffset += 0.1) {
            for (double xzOffset = -0.1; xzOffset <= 0.1; xzOffset += 0.05) {
                Vec3d adjustedLook = new Vec3d(
                        lookVec.x + xzOffset,
                        lookVec.y + yOffset,
                        lookVec.z + xzOffset
                ).normalize();

                Vec3d target = findWallTarget(playerPos, adjustedLook);
                if (target != null) {
                    double distance = playerPos.distanceTo(target);
                    if (distance < minDistance && isValidPearlTarget(target)) {
                        minDistance = distance;
                        bestTarget = target;
                    }
                }
            }
        }

        return bestTarget;
    }

    private Vec3d findWallTarget(Vec3d start, Vec3d direction) {
        // İlk duvarı bul
        Vec3d rayEnd = start.add(direction.multiply(pearlDistance.getValue()));

        BlockHitResult hitResult = mc.world.raycast(new RaycastContext(
                start,
                rayEnd,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        // Duvarın arkasında boş alan bul
        Vec3d hitPos = hitResult.getPos();
        Vec3d behindWall = hitPos.add(direction.multiply(2.0)); // 2 blok arkası

        // Hedefe giderken yere indir
        behindWall = findSafeGroundPosition(behindWall);

        return behindWall;
    }

    private Vec3d findSafeGroundPosition(Vec3d pos) {
        // Güvenli zemin pozisyonu bul
        for (int y = (int)pos.y; y >= (int)pos.y - 5; y--) {
            BlockPos checkPos = new BlockPos((int)pos.x, y, (int)pos.z);
            BlockPos abovePos = checkPos.up();
            BlockPos above2Pos = checkPos.up(2);

            // Zemin solid, üst 2 blok boş olmalı
            if (mc.world.getBlockState(checkPos).isSolidBlock(mc.world, checkPos) &&
                    !mc.world.getBlockState(abovePos).isSolidBlock(mc.world, abovePos) &&
                    !mc.world.getBlockState(above2Pos).isSolidBlock(mc.world, above2Pos)) {

                return new Vec3d(pos.x, y + 1.1, pos.z); // Zeminin biraz üstü
            }
        }

        return pos; // Güvenli zemin bulunamadı, orijinal pozisyonu döndür
    }

    private boolean isValidPearlTarget(Vec3d target) {
        BlockPos targetPos = new BlockPos((int)target.x, (int)target.y, (int)target.z);
        BlockPos aboveTarget = targetPos.up();

        // Hedef pozisyon ve üstü boş olmalı
        return !mc.world.getBlockState(targetPos).isSolidBlock(mc.world, targetPos) &&
                !mc.world.getBlockState(aboveTarget).isSolidBlock(mc.world, aboveTarget);
    }

    private void throwPearlWithPrecision(int pearlSlot, Vec3d targetPos) {
        int oldSlot = mc.player.getInventory().getSelectedSlot();

        // Pearl slot'una geç
        mc.player.getInventory().setSelectedSlot(pearlSlot);

        // Hassas açı hesaplama
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d direction = targetPos.subtract(eyePos);
        double distance = direction.length();

        // Yerçekimi kompensasyonu
        double gravity = 0.03; // Pearl'ın yerçekimi etkisi
        double velocity = 1.5;  // Pearl'ın başlangıç hızı
        double time = distance / velocity;
        double dropCompensation = 0.5 * gravity * time * time;

        Vec3d adjustedTarget = targetPos.add(0, dropCompensation, 0);
        Vec3d finalDirection = adjustedTarget.subtract(eyePos).normalize();

        // Açıları hesapla
        float yaw = (float) Math.toDegrees(Math.atan2(-finalDirection.x, finalDirection.z));
        float pitch = (float) Math.toDegrees(-Math.asin(finalDirection.y));

        // Yumuşak açı değişimi
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);

        // 1 tick bekle (açı değişimi için)
        if (mode.getValue() == Mode.STRICT_PEARL) {
            // Strict modda daha hassas atış
            try {
                Thread.sleep(50); // 50ms bekle
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Pearl at
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        // Eski slot'a geri dön
        mc.player.getInventory().setSelectedSlot(oldSlot);

        pearlThrown = true;
        ticksAfterPearl = 0;

        sendMessage("Precision pearl thrown to: " +
                String.format("%.1f, %.1f, %.1f", targetPos.x, targetPos.y, targetPos.z));
    }

    private boolean hasPlayerTeleported() {
        if (lastValidPosition == null) return false;

        Vec3d currentPos = mc.player.getPos();
        double teleportDistance = lastValidPosition.distanceTo(currentPos);

        return teleportDistance > 3.0; // 3 bloktan fazla hareket = teleport
    }

    @Override
    public void onEnable() {
        pearlThrown = false;
        ticksAfterPearl = 0;
        pearlAttempts = 0;
        lastValidPosition = null;
        insideWall = false;

        if (mc.player != null) {
            lastValidPosition = mc.player.getPos();
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.noClip = false;
        }
        pearlThrown = false;
        ticksAfterPearl = 0;
        pearlAttempts = 0;
        insideWall = false;
    }

    // Diğer metodlar aynı kalıyor...
    @Override
    public void onRender(MatrixStack matrices, float tickDelta) {}

    @Override
    public void onRenderWorldLast(MatrixStack matrices, float tickDelta) {}

    @Override
    public void onRender3D(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, float tickDelta) {}

    @Override
    public void onRender3D(float partialTicks) {}

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider) {}

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, float tickDelta) {}

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumers vertexConsumers, float tickDelta) {}

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumer vertexConsumer, float tickDelta) {}

    @Override
    public void onRender3D(MatrixStack matrixStack, float tickDelta) {}

    @Override
    public void onPacketSend(PacketEvent.Send event) {}

    @Override
    public void onPacketReceive(PacketEvent.Receive event) {}

    @Override
    public void onRender() {}

    @Override
    public void onRenderWorldLast(MatrixStack matrices) {}

    @Override
    public void onRender(DrawContext context) {}

    private void sendMessage(String message) {
        System.out.println("[Phase] " + message);
    }
}