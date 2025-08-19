
package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.event.impl.Render3DEvent;
import me.alpha432.oyvey.features.commands.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import me.alpha432.oyvey.manager.RotationManager;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.minecraft.block.Blocks.BEDROCK;
import static net.minecraft.block.Blocks.OBSIDIAN;

public class AutoCrystal extends Module {


    private final Setting<Float> attackRange = this.register(new Setting<>("AttackRange", 6.0f, 0.0f, 6.0f));
    private final Setting<Float> minDamage = this.register(new Setting<>("MinDamage", 6.0f, 0.0f, 20.0f));
    private final Setting<Float> maxSelfDamage = this.register(new Setting<>("MaxSelfDamage", 10.0f, 0.0f, 36.0f));
    private final Setting<Boolean> renderDebug = this.register(new Setting<>("RenderDebug", true));
    private final Setting<Integer> red = this.register(new Setting<>("DebugRed", 200, 0, 255, v -> renderDebug.getValue()));
    private final Setting<Integer> green = this.register(new Setting<>("DebugGreen", 10, 0, 255, v -> renderDebug.getValue()));
    private final Setting<Integer> blue = this.register(new Setting<>("DebugBlue", 230, 0, 255, v -> renderDebug.getValue()));
    private final Setting<Integer> breakSpeed = this.register(new Setting<>("BreakSpeed", 50, 0, 1000)); // ms cinsinden
    private final Setting<Integer> placeSpeed = this.register(new Setting<>("PlaceSpeed", 50, 0, 1000)); // ms cinsinden
    private final Setting<Boolean> rotate = this.register(new Setting<>("Rotate", false));
    private final Setting<Boolean> silentRotate = this.register(new Setting<>("SilentRotate", true));
    private final Setting<Boolean> prioritizeHealth = this.register(new Setting<>("PrioritizeHealth", true));
    private final Setting<Boolean> prioritizeArmor = this.register(new Setting<>("PrioritizeArmor", false));
    private final Setting<Boolean> prioritizeDistance = this.register(new Setting<>("PrioritizeDistance", true));
    private final Setting<Float> placeRange = register(new Setting<>("PlaceRange", 5.0f, 1.0f, 6.0f, "Kristal yerleştirme mesafesi"));
    private final Setting<Float> breakRange = register(new Setting<>("BreakRange", 5.0f, 1.0f, 6.0f, "Kristal kırma mesafesi"));
    private final Setting<Float> wallsRange = register(new Setting<>("WallsRange", 3.5f, 1.0f, 6.0f, "Duvar arkasındaki kristaller için mesafe"));
    private final Setting<Float> facePlace = register(new Setting<>("FacePlace", 8.0f, 0.1f, 36.0f, "Düşman canı bu değerin altındaysa face placement yapılır"));
    private final Setting<Boolean> predictPos = register(new Setting<>("PredictPosition", true, "Düşman pozisyonunu tahmin eder"));
    private final Setting<Float> predictTicks = register(new Setting<>("PredictTicks", 2.0f, 0.1f, 5.0f, "Kaç tick ileriyi tahmin edeceği"));


    private final Setting<Boolean> strictPlacement = this.register(new Setting<>("StrictPlacement", false));


    private BlockPos lastPlacedCrystalPos = null;
    private long lastBreakTime = 0;
    private long lastPlaceTime = 0;
    private BlockPos bestPosition = null;
    private double bestDamage = 0.0;
    private Entity targetPlayer = null;
    private PlayerEntity currentTarget = null;
    private static int cps = 0;
    private int clicksThisSecond = 0;
    private long lastClickTime = 0;
    private long cpsLastCheck = System.currentTimeMillis();
    private float serverYaw = 0f;
    private float serverPitch = 0f;
    private boolean isRotating = false;
    private long rotationStartTime = 0;
    private final long ROTATION_DURATION = 100;


    private final ExecutorService executor = Executors.newSingleThreadExecutor();



    public AutoCrystal() {
        super("AutoCrystal", "Improved crystal aura with damage calc, rotation, target prioritization and async.", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        Command.sendMessage(this.getName() + " was toggled " + Formatting.GREEN + "on" + Formatting.GRAY + ".");
    }

    @Override
    public void onDisable() {
        Command.sendMessage(this.getName() + " was toggled " + Formatting.RED + "off" + Formatting.GRAY + ".");
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        long currentTime = System.currentTimeMillis();

        long lastTotemPopTime = 0;


        if (currentTime - lastBreakTime >= breakSpeed.getValue()) {
            breakCrystal(currentTime);
        }


        if (currentTime - lastPlaceTime >= placeSpeed.getValue()) {
            placeCrystalAsync(currentTime);
        }
    }

    private void performSilentRotate(Vec3d targetPos, Runnable callback) {
        if (mc.player == null) return;

        // Hedef rotasyonu hesapla
        float[] targetRotation = calculateLookAngles(mc.player.getCameraPosVec(1.0f), targetPos);

        // Mevcut server rotasyonunu sakla
        float oldServerYaw = serverYaw;
        float oldServerPitch = serverPitch;

        // Yeni server rotasyonunu ayarla
        serverYaw = targetRotation[0];
        serverPitch = targetRotation[1];
        isRotating = true;
        rotationStartTime = System.currentTimeMillis();

        // Server'a yeni rotasyonu gönder (client görsel olarak değişmez)
        sendSilentRotationPacket(serverYaw, serverPitch);

        // Callback'i çalıştır (kristal yerleştirme/kırma işlemi)
        if (callback != null) {
            callback.run();
        }

        // Kısa bir süre sonra eski rotasyona geri dön
        new Thread(() -> {
            try {
                Thread.sleep(50); // 50ms bekle
                if (mc.player != null) {
                    // Eski rotasyona geri dön
                    sendSilentRotationPacket(oldServerYaw, oldServerPitch);
                    isRotating = false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Server'a rotasyon paketi gönder (client'ın görsel rotasyonunu etkilemez)
     */
    private void sendSilentRotationPacket(float yaw, float pitch) {
        if (mc.player == null || mc.player.networkHandler == null) return;

        // Sadece server'a rotasyon bilgisi gönder, client'ın görsel rotasyonunu değiştirme
        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.LookAndOnGround(
                yaw,
                pitch,
                mc.player.isOnGround(),
                false // Sequence number gerekmiyor
        );

        mc.player.networkHandler.sendPacket(packet);
    }

    /**
     * Gelişmiş açı hesaplama - MEVCUT calculateLookAngles METODUNU BU İLE DEĞİŞTİRİN
     */
    private float[] calculateLookAngles(Vec3d from, Vec3d to) {
        double deltaX = to.x - from.x;
        double deltaY = to.y - from.y;
        double deltaZ = to.z - from.z;

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, horizontalDistance));

        // Açıları normalize et
        yaw = normalizeAngle(yaw);
        pitch = Math.max(-90.0f, Math.min(90.0f, pitch));

        return new float[]{yaw, pitch};
    }

    /**
     * Açıyı -180 ile 180 arasında normalize et
     */
    private float normalizeAngle(float angle) {
        while (angle > 180.0f) {
            angle -= 360.0f;
        }
        while (angle < -180.0f) {
            angle += 360.0f;
        }
        return angle;
    }

    /**
     * Kristal yerleştirme için silent rotate
     */
    private void silentRotateForPlacement(Vec3d crystalPos, Runnable placementAction) {
        if (!silentRotate.getValue()) {
            // Normal rotation
            if (rotate.getValue()) {
                rotateToPosition(crystalPos);
            }
            placementAction.run();
            return;
        }

        // Silent rotation ile yerleştirme
        performSilentRotate(crystalPos, placementAction);
    }

    /**
     * Kristal kırma için silent rotate
     */
    private void silentRotateForBreaking(Entity crystal, Runnable breakAction) {
        if (!silentRotate.getValue()) {
            // Normal rotation
            if (rotate.getValue()) {
                rotateToEntity(crystal);
            }
            breakAction.run();
            return;
        }

        // Kristal merkezine bak
        Vec3d crystalCenter = crystal.getPos().add(0, crystal.getHeight() / 2.0, 0);
        performSilentRotate(crystalCenter, breakAction);
    }

    private void breakCrystal(long ignoredCurrentTime) {
        assert mc.world != null;
        List<EndCrystalEntity> crystals = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e instanceof EndCrystalEntity)
                .map(e -> (EndCrystalEntity) e)
                .filter(e -> e.squaredDistanceTo(mc.player) <= attackRange.getValue() * attackRange.getValue())
                .collect(Collectors.toList());


        if (crystals.isEmpty()) return;


        EndCrystalEntity targetCrystal = null;
        double bestScore = Double.MAX_VALUE;

        for (EndCrystalEntity crystal : crystals) {

            PlayerEntity closestPlayer = getPrioritizedTarget(crystal.getPos());

            if (closestPlayer == null) continue;


            float damage = calculateDamage(closestPlayer, crystal.getPos());

            if (damage < minDamage.getValue()) continue;


            double score = 0;

            if (prioritizeDistance.getValue()) {
                assert mc.player != null;
                score += mc.player.squaredDistanceTo(crystal);
            }
            if (prioritizeHealth.getValue()) {
                score += closestPlayer.getHealth();
            }
            if (prioritizeArmor.getValue()) {
                score += getArmorValue(closestPlayer) * 2;
            }

            if (score < bestScore) {
                bestScore = score;
                targetCrystal = crystal;
            }
        }

        if (targetCrystal != null) {
            if (rotate.getValue()) {
                if (silentRotate.getValue() && mc.world != null) {
                    silentRotateToEntity(targetCrystal);
                } else {
                    rotateToEntity(targetCrystal);
                }
            }
            if (mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, targetCrystal);
                assert mc.player != null;
                mc.player.swingHand(Hand.MAIN_HAND);
                lastBreakTime = System.currentTimeMillis();
            }
        }
    }


    private PlayerEntity getPrioritizedTarget(Vec3d fromPos) {
        assert mc.world != null;
        List<PlayerEntity> players = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && !p.isDead() && !p.isSpectator() && !p.isInvisible())
                .collect(Collectors.toList());

        PlayerEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (PlayerEntity player : players) {
            if (player.getHealth() <= 0) continue;

            double score = 0;

            if (prioritizeDistance.getValue()) {
                score += player.getPos().squaredDistanceTo(fromPos);
            }
            if (prioritizeHealth.getValue()) {
                score += player.getHealth();
            }
            if (prioritizeArmor.getValue()) {
                score += getArmorValue(player) * 2;
            }

            if (score < bestScore) {
                bestScore = score;
                best = player;
            }
        }

        return best;
    }

    private int getArmorValue(PlayerEntity player) {
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

                // En yakın/öncelikli hedefi bul
                PlayerEntity bestTarget = null;
                double bestTargetScore = Double.MAX_VALUE;

                for (PlayerEntity target : mc.world.getPlayers()) {
                    if (target == mc.player || target.isSpectator() || target.isDead()) continue;

                    double distance = mc.player.squaredDistanceTo(target);
                    if (distance > placeRange.getValue() * placeRange.getValue() * 2) continue; // Mesafe ön kontrolü

                    double score = 0;
                    if (prioritizeDistance.getValue()) score += distance;
                    if (prioritizeHealth.getValue()) score += target.getHealth();
                    if (prioritizeArmor.getValue()) score += getArmorValue(target) * 2;

                    if (score < bestTargetScore) {
                        bestTargetScore = score;
                        bestTarget = target;
                    }
                }

                if (bestTarget == null) return;

                // Hedefin ayak pozisyonunu al
                BlockPos targetPos = bestTarget.getBlockPos();

                // Kristal yerleştirme önceliği için pozisyonları oluştur
                List<BlockPos> possiblePositions = new ArrayList<>();

                // 1. AYAK SEVIYESI ÖNCELIKLE (En çok hasar verenler) - Y seviyesi ayağa eşit
                // Çapraz pozisyonlar (daha fazla hasar verdiği için öncelikli)
                possiblePositions.add(targetPos.add(1, 0, 1));
                possiblePositions.add(targetPos.add(-1, 0, 1));
                possiblePositions.add(targetPos.add(1, 0, -1));
                possiblePositions.add(targetPos.add(-1, 0, -1));

                // Yan pozisyonlar
                possiblePositions.add(targetPos.add(1, 0, 0));
                possiblePositions.add(targetPos.add(-1, 0, 0));
                possiblePositions.add(targetPos.add(0, 0, 1));
                possiblePositions.add(targetPos.add(0, 0, -1));

                // 2. AYAK ALTI SEVIYESI (Daha az öncelikli)
                possiblePositions.add(targetPos.add(1, -1, 1));
                possiblePositions.add(targetPos.add(-1, -1, 1));
                possiblePositions.add(targetPos.add(1, -1, -1));
                possiblePositions.add(targetPos.add(-1, -1, -1));
                possiblePositions.add(targetPos.add(1, -1, 0));
                possiblePositions.add(targetPos.add(-1, -1, 0));
                possiblePositions.add(targetPos.add(0, -1, 1));
                possiblePositions.add(targetPos.add(0, -1, -1));

                // 3. AYAK ÜSTÜ SEVIYESI (En az öncelikli)
                possiblePositions.add(targetPos.add(1, 1, 1));
                possiblePositions.add(targetPos.add(-1, 1, 1));
                possiblePositions.add(targetPos.add(1, 1, -1));
                possiblePositions.add(targetPos.add(-1, 1, -1));
                possiblePositions.add(targetPos.add(1, 1, 0));
                possiblePositions.add(targetPos.add(-1, 1, 0));
                possiblePositions.add(targetPos.add(0, 1, 1));
                possiblePositions.add(targetPos.add(0, 1, -1));

                // Uzak pozisyonlar (geniş arama alanı)
                for (int x = -3; x <= 3; x++) {
                    for (int y = -2; y <= 2; y++) {
                        for (int z = -3; z <= 3; z++) {
                            // Eğer bir öncelikli pozisyon değilse ekle
                            BlockPos pos = targetPos.add(x, y, z);
                            if (!possiblePositions.contains(pos)) {
                                possiblePositions.add(pos);
                            }
                        }
                    }
                }

                // En iyi kristal pozisyonunu bul
                BlockPos bestPosition = null;
                float bestDamage = 0;

                for (BlockPos basePos : possiblePositions) {
                    // Menzil kontrolü
                    if (mc.player.getPos().distanceTo(Vec3d.of(basePos)) > placeRange.getValue()) continue;

                    // Geçerli blok kontrolü
                    if (!isValidBaseBlock(basePos)) continue;

                    // Üst bloklar boş mu kontrolü
                    if (!mc.world.getBlockState(basePos.up()).isAir() || !mc.world.getBlockState(basePos.up(2)).isAir()) continue;

                    // Kristal pozisyonu (0.5 ile merkezleme)
                    Vec3d crystalPos = Vec3d.of(basePos).add(0.5, 1.0, 0.5);

                    // Hasar hesaplama
                    float targetDamage = calculateDamage(bestTarget, crystalPos);
                    float selfDamage = calculateDamage(mc.player, crystalPos);

                    // Hasar kontrolleri
                    if (targetDamage < minDamage.getValue() || selfDamage > maxSelfDamage.getValue()) continue;

                    // Daha iyi hasar veriyorsa kaydet
                    if (targetDamage > bestDamage) {
                        bestDamage = targetDamage;
                        bestPosition = basePos;
                    }
                }

                // En iyi pozisyona kristal yerleştir
                if (bestPosition != null) {
                    final BlockPos finalBestPosition = bestPosition;
                    Vec3d crystalPos = Vec3d.of(bestPosition).add(0.5, 1.0, 0.5);

                    mc.execute(() -> {
                        if (mc.interactionManager != null) {
                            if (rotate.getValue()) {
                                if (silentRotate.getValue()) silentRotateToPosition(crystalPos);
                                else rotateToPosition(crystalPos);
                            }

                            BlockHitResult hit = new BlockHitResult(crystalPos, Direction.UP, finalBestPosition, false);
                            mc.interactionManager.interactBlock(mc.player, crystalHand, hit);
                            mc.player.swingHand(crystalHand);
                            lastPlacedCrystalPos = finalBestPosition;
                            lastPlaceTime = System.currentTimeMillis();
                        }
                    });
                }
            } catch (Exception ignored) {
                // Hata durumunda sessizce devam et
            }
        });
    }

    private void Cps() {
        // Hedef oyuncuyu seç (örneğin en yakın oyuncu)
        currentTarget = mc.world.getPlayers().stream()
                .filter(player -> player != mc.player && mc.player.distanceTo(player) < 10)
                .min(Comparator.comparingDouble(player -> mc.player.distanceTo(player)))
                .orElse(null);

        // CPS güncellemesi
        long now = System.currentTimeMillis();
        if (now - lastClickTime > 1000) {
            clicksThisSecond = 0;
            lastClickTime = now;
        }
        clicksThisSecond++;
        cps = clicksThisSecond;

        // Kristal koyma işlemi burada yapılır
        // Örnek: send place crystal packet, veya oyun içi kristal yerleştirme kodu
    }


    private void placeCrystalAsync(long ignoredCurrentTime) {
        executor.submit(() -> {
            try {
                if (mc.player == null || mc.world == null) return;

                // Kristal kontrolü
                Hand crystalHand;
                if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
                    crystalHand = Hand.MAIN_HAND;
                } else if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
                    crystalHand = Hand.OFF_HAND;
                } else {
                    return;
                }


                // Hedef oyuncuları bul
                for (PlayerEntity target : mc.world.getPlayers()) {
                    if (target == mc.player || target.isSpectator() || target.isDead()) continue;

                    BlockPos targetPos = target.getBlockPos();

                    // Kristal yerleştirme pozisyonlarını kontrol et
                    for (int x = -3; x <= 3; x++) {
                        for (int y = -2; y <= 2; y++) {
                            for (int z = -3; z <= 3; z++) {
                                final BlockPos basePos = targetPos.add(x, y, z);

                                // Mesafe kontrolü
                                if (mc.player.getPos().distanceTo(Vec3d.of(basePos)) > placeRange.getValue())
                                    continue;

                                // Alt blok kontrolü
                                if (!isValidBaseBlock(basePos))
                                    continue;

                                // Üst blokların boş olup olmadığını kontrol et
                                if (!mc.world.getBlockState(basePos).isOf(Blocks.OBSIDIAN) &&
                                        !mc.world.getBlockState(basePos).isOf(Blocks.BEDROCK)) continue;

                                if (!mc.world.isAir(basePos.up()) || !mc.world.isAir(basePos.up(1))) continue;

                                if (!mc.world.getBlockState(basePos.up()).isReplaceable()) continue;


                                final Vec3d crystalPos = Vec3d.of(basePos).add(0.5, 1.0, 0.5);

                                // Hasar hesaplamaları
                                float targetDamage = calculateDamage(target, crystalPos);
                                float selfDamage = calculateDamage(mc.player, crystalPos);

                                if (targetDamage < minDamage.getValue() || selfDamage > maxSelfDamage.getValue())
                                    continue;

                                // Kristal yerleştirme
                                mc.execute(() -> {
                                    if (mc.interactionManager != null) {
                                        // Rotasyon
                                        if (rotate.getValue()) {
                                            if (silentRotate.getValue()) {
                                                silentRotateToPosition(crystalPos);
                                            } else {
                                                this.rotateToPosition(crystalPos);
                                            }
                                        }

                                        // Yerleştirme işlemi
                                        BlockHitResult hit = new BlockHitResult(
                                                crystalPos,
                                                Direction.UP,
                                                basePos,
                                                false
                                        );

                                        mc.interactionManager.interactBlock(mc.player, crystalHand, hit);
                                        mc.player.swingHand(crystalHand);
                                        lastPlacedCrystalPos = basePos;
                                        lastPlaceTime = System.currentTimeMillis();
                                    }
                                });
                                return; // İlk uygun pozisyonda çık
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    private boolean isInHole(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();

        return isResistantBlock(pos.north()) &&
                isResistantBlock(pos.south()) &&
                isResistantBlock(pos.east()) &&
                isResistantBlock(pos.west()) &&
                mc.world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN;
    }

    private boolean isResistantBlock(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK;
    }


    // Yeni yardımcı method
    private boolean isValidBaseBlock(BlockPos pos) {
        if (mc.world == null) return false;
        return mc.world.getBlockState(pos).isOf(OBSIDIAN) ||
                mc.world.getBlockState(pos).isOf(BEDROCK);
    }


    private float calculateDamage(LivingEntity entity, Vec3d explosionPos) {
        if (entity == null || explosionPos == null) return 0f;

        // Vec3d'yi BlockPos'a çevir (kristal yerleştirme için)
        BlockPos crystalPos = new BlockPos((int) Math.floor(explosionPos.x),
                (int) Math.floor(explosionPos.y - 1), // 1 çıkar çünkü crystal başlangıcı
                (int) Math.floor(explosionPos.z));

        // EntityUtil'deki daha iyi hasar hesaplama metodunu kullan
        return EntityUtil.calculateDamage(crystalPos, entity);
    }


    private double calculateBlockDensity(Vec3d explosionPos, LivingEntity entity) {
        Vec3d entityPos = entity.getPos().add(0, entity.getStandingEyeHeight() / 2.0, 0);
        return 1.0; // Ray trace kontrolü olmadan direkt 1.0 döndür
    }

    private boolean rayTraceBlocks(Vec3d start, Vec3d end) {
        if (start == null || end == null || mc.world == null || mc.player == null) return true;

        // Basit mesafe kontrolü
        double distance = start.distanceTo(end);
        if (distance > placeRange.getValue()) return true;

        try {
            RaycastContext context = new RaycastContext(
                    start,
                    end,
                    RaycastContext.ShapeType.VISUAL, // OUTLINE yerine VISUAL kullan
                    RaycastContext.FluidHandling.NONE, // Sıvıları görmezden gel
                    mc.player
            );

            BlockHitResult result = mc.world.raycast(context);

            // Eğer çarpışma yoksa veya mesafe uygunsa izin ver
            return result.getType() == HitResult.Type.BLOCK &&
                    result.getPos().squaredDistanceTo(start) < end.squaredDistanceTo(start);
        } catch (Exception e) {
            return false; // Hata durumunda yerleştirmeye izin ver
        }
    }


    private void rotateToEntity2(Entity entity) {
        assert mc.player != null;
        Vec3d eyesPos = mc.player.getCameraPosVec(1.0f);
        Vec3d targetPos = entity.getPos().add(0, entity.getHeight() / 2.0, 0);
        float[] rotations = getLookAngles(eyesPos, targetPos);
        mc.player.setYaw(rotations[0]);
        mc.player.setPitch(rotations[1]);
    }

    private void silentRotateToEntity(Entity entity) {
        assert mc.player != null;
        Vec3d eyesPos = mc.player.getCameraPosVec(1.0f);
        Vec3d targetPos = entity.getPos().add(0, entity.getHeight() / 2.0, 0);
        float[] rotations = getLookAngles(eyesPos, targetPos);
        mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround(rotations[0], rotations[1], mc.player.isOnGround(), true));
    }

    private void rotateToPosition(Vec3d pos) {
        assert mc.player != null;
        Vec3d eyesPos = mc.player.getCameraPosVec(1.0f);
        float[] rotations = getLookAngles(eyesPos, pos);
        mc.player.setYaw(rotations[0]);
        mc.player.setPitch(rotations[1]);
    }

    private void silentRotateToPosition(Vec3d pos) {
        assert mc.player != null;
        Vec3d eyesPos = mc.player.getCameraPosVec(1.0f);
        float[] rotations = getLookAngles(eyesPos, pos);
        mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround(rotations[0], rotations[1], mc.player.isOnGround(), true));
    }



    private float[] getLookAngles(Vec3d from, Vec3d to) {
        double diffX = to.x - from.x;
        double diffY = to.y - from.y;
        double diffZ = to.z - from.z;
        double distXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, distXZ));

        return new float[]{yaw, pitch};
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!renderDebug.getValue() || lastPlacedCrystalPos == null) return;

        MatrixStack matrices = event.getMatrix();
        VertexConsumerProvider.Immediate buffer = mc.getBufferBuilders().getEntityVertexConsumers();

        RenderUtil.drawBox(matrices, buffer, lastPlacedCrystalPos, new Color(red.getValue(), green.getValue(), blue.getValue(), 150), 1.5f, 1.5f);

        buffer.draw();
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

    // Sınıfın diğer alanlarına eklenmeli:
    private static LivingEntity target;

    // Getter'lar
    public static LivingEntity getTarget() {
        return target;
    }



    // Crystal yerleştirme fonksiyonu (bozulmadan)
    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        // En yakın hedefi seç
        target = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && !p.isDead())
                .min(Comparator.comparingDouble(p -> p.squaredDistanceTo(mc.player)))
                .orElse(null);
    }

    public PlayerEntity getTargetW() {
        return target.getAttackingPlayer();
    }

    // Sınıfın en üstünde, diğer field’ların yanında ekle:     // Güncel CPS değeri
    private final int maxCPS = 8;       // Ayarlanabilir maksimum CPS

    // placeCrystal metodu:
    private void placeCrystal() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // 1. En yakın hedef oyuncuyu bul
        target = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && !p.isDead())
                .min(Comparator.comparingDouble(p -> p.squaredDistanceTo(mc.player)))
                .orElse(null);
        if (target == null) return;

        // 2. Hedefin çevresinde uygun obsidian/bedrock bloğu bul
        BlockPos targetPos = target.getBlockPos();
        BlockPos bestPos = null;

        outerLoop:
        for (int x = -4; x <= 4; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = targetPos.add(x, y, z);
                    String key = mc.world.getBlockState(pos).getBlock().getTranslationKey().toLowerCase();
                    if (!(key.contains("obsidian") || key.contains("bedrock"))) continue;
                    if (!mc.world.isAir(pos.up()) || !mc.world.isAir(pos.up().up())) continue;
                    bestPos = pos;
                    break outerLoop;
                }
            }
        }
        if (bestPos == null) return;

        // 3. CPS delay kontrolü
        long now = System.currentTimeMillis();
        long delay = 1000L / maxCPS;
        if (now - lastPlaceTime < delay) return;

        // 4. Elde kristal var mı kontrol et
        Hand hand;
        if (mc.player.getOffHandStack().getItem() instanceof EndCrystalItem) {
            hand = Hand.OFF_HAND;
        } else if (mc.player.getMainHandStack().getItem() instanceof EndCrystalItem) {
            hand = Hand.MAIN_HAND;
        } else {
            return;
        }

        // 5. Yerleştirme için pozisyon ve yön ayarla
        Vec3d hitVec = Vec3d.ofCenter(bestPos);
        Direction direction = Direction.UP;

        // 6. Rotasyon gerekiyorsa yap
        if (rotate.getValue()) {
            float yaw = RotationManager.getYaw(hitVec);
            float pitch = RotationManager.getPitch(hitVec);
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround(), false)
            );
        }

        // 7. Kristali yerleştir
        mc.interactionManager.interactBlock(
                mc.player,
                hand,
                new BlockHitResult(hitVec, direction, bestPos, false)
        );
        mc.player.swingHand(hand);
        lastPlaceTime = now;

        // 8. CPS sayacını güncelle
        clicksThisSecond++;
        if (now - cpsLastCheck >= 1000) {
            cps = clicksThisSecond;
            clicksThisSecond = 0;
            cpsLastCheck = now;
        }
    }

    // HUD veya başka yerden okuyabilmek için getter’lar:

    public int getCPS() {
        return cps;
    }




    private void breakCrystals() {
        // Kırılacak en iyi kristali bul
        EndCrystalEntity bestCrystal = null;
        float bestDamage = 0.0f;

        for (Entity entity : mc.world.getEntities()) {
            // Sadece End kristallerini kontrol et
            if (!(entity instanceof EndCrystalEntity)) continue;
            EndCrystalEntity crystal = (EndCrystalEntity) entity;

            // Mesafe kontrolü
            double distance = mc.player.squaredDistanceTo(crystal);
            if (distance > breakRange.getValue() * breakRange.getValue()) continue;

            // Duvar kontrolü
            boolean canSee = mc.player.canSee(crystal);
            if (!canSee && distance > wallsRange.getValue() * wallsRange.getValue()) continue;

            // Kristalden gelen hasarı hesapla
            float targetDamage = calculateDamage(new Vec3d(crystal.getX(), crystal.getY(), crystal.getZ()), targetPlayer);
            float selfDamage = calculateDamage(new Vec3d(crystal.getX(), crystal.getY(), crystal.getZ()), mc.player);

            // Kendimize çok fazla hasar vermiyorsak ve düşmana yeterince hasar veriyorsak
            if (selfDamage <= maxSelfDamage.getValue() && targetDamage > minDamage.getValue()) {
                if (targetDamage > bestDamage) {
                    bestDamage = targetDamage;
                    bestCrystal = crystal;
                }
            }
        }

        // En iyi kristali kır
        if (bestCrystal != null) {
            if (rotate.getValue()) {
                rotateToEntity(bestCrystal);
            }

            mc.interactionManager.attackEntity(mc.player, bestCrystal);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        if (!(entity instanceof LivingEntity living)) return 0.0f;

        Vec3d explosionPos = new Vec3d(posX, posY, posZ);
        Vec3d entityCenter = living.getBoundingBox().getCenter();
        double distance = explosionPos.distanceTo(entityCenter) / 12.0;

        if (distance > 1.0) return 0.0f;

        double blockDensity = getBlockDensity(explosionPos, living);
        double exposure = (1.0 - distance) * blockDensity;

        float impact = (float) ((exposure * exposure + exposure) * 0.5 * 7.0 * 12.0 + 1.0f);

        return applyBlastReduction(living, impact);
    }

    private float calculateDamage(Vec3d pos, Entity entity) {
        return calculateDamage(pos.x, pos.y, pos.z, entity);
    }

    private double getBlockDensity(Vec3d explosionPos, Entity entity) {
        Box box = entity.getBoundingBox().expand(0.1);
        int total = 0;
        int passed = 0;

        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = 0; z <= 1; z++) {
                    double px = box.minX + (box.maxX - box.minX) * x;
                    double py = box.minY + (box.maxY - box.minY) * y;
                    double pz = box.minZ + (box.maxZ - box.minZ) * z;
                    Vec3d target = new Vec3d(px, py, pz);

                    BlockHitResult result = mc.world.raycast(new RaycastContext(
                            explosionPos,
                            target,
                            RaycastContext.ShapeType.COLLIDER,
                            RaycastContext.FluidHandling.NONE,
                            mc.player
                    ));

                    total++;
                    if (result.getType() == HitResult.Type.MISS) {
                        passed++;
                    }
                }
            }
        }

        return total == 0 ? 0.0 : (double) passed / total;
    }

    private float applyBlastReduction(LivingEntity entity, float damage) {
        if (!(entity instanceof PlayerEntity player)) return damage;

        float armor = player.getArmor(); // 0 - 20 arası
        float toughness = 0.0f; // Client'ta toughness alınamaz, varsayılan 0

        // Zırh indirimi (yaklaşık Minecraft formülü)
        float armorRatio = armor / 20.0f;
        float toughnessFactor = (float) (1.0 - Math.min(20.0f, Math.max(armor / 5.0f, armor - damage / 2.0f)) / 25.0f);
        float armorReduction = 1.0f - (armorRatio * toughnessFactor);

        damage *= armorReduction;

        // Resistance efekti kontrolü
        if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int level = Objects.requireNonNull(player.getStatusEffect(StatusEffects.RESISTANCE)).getAmplifier();
            damage *= (1.0f - ((level + 1) * 0.2f)); // %20 azaltım her seviye
        }

        return Math.max(damage, 0.0f);
    }



    private void rotateToEntity(Entity entity) {
        // Oyuncuyu entity'ye bakacak şekilde döndürür
        Vec3d eyesPos = new Vec3d(mc.player.getX(),
                mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
                mc.player.getZ());

        Vec3d targetPos = new Vec3d(entity.getX(),
                entity.getY() + entity.getHeight() / 2.0,
                entity.getZ());

        double diffX = targetPos.x - eyesPos.x;
        double diffY = targetPos.y - eyesPos.y;
        double diffZ = targetPos.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }}



