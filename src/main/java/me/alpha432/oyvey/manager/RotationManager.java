package me.alpha432.oyvey.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static float preYaw;
    public static float prePitch;
    private static boolean isRotating = false;
    public static float targetYaw;
    public static float targetPitch;
    private static boolean clientSide = false;

    // Güncel rotasyonları kaydet
    public static void updateRotations() {
        if (mc.player == null) return;

        preYaw = mc.player.getYaw();
        prePitch = mc.player.getPitch();

        targetYaw = getYaw(mc.player);
        targetPitch = getPitch(mc.player);
        clientSide = clientSide;

        if (clientSide) {
            mc.player.setYaw(targetYaw);
            mc.player.setPitch(targetPitch);
        }

        isRotating = true;
    }

    // Paket gönderimi
    public static void sendRotationPacket() {
        if (mc.player == null || !isRotating) return;

        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(targetYaw, targetPitch, mc.player.getAttackingPlayer().isOnGround(), mc.player.getAttackingPlayer().isOnGround())
        );
    }

    // Rotasyonu eski haline getir
    public static void restoreRotations() {
        if (mc.player == null || !isRotating) return;

        mc.player.setYaw(preYaw);
        mc.player.setPitch(prePitch);

        isRotating = false;
    }

    // Yaw (Vec3d)
    public static float getYaw(Vec3d target) {
        double diffX = target.x - mc.player.getX();
        double diffZ = target.z - mc.player.getZ();
        double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
        return MathHelper.wrapDegrees((float) yaw);
    }

    // Pitch (Vec3d)
    public static float getPitch(Vec3d target) {
        double diffX = target.x - mc.player.getX();
        double diffY = target.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = target.z - mc.player.getZ();
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        double pitch = -Math.toDegrees(Math.atan2(diffY, dist));
        return MathHelper.wrapDegrees((float) pitch);
    }

    // Yaw (BlockPos)
    public static float getYaw(BlockPos pos) {
        return getYaw(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    // Pitch (BlockPos)
    public static float getPitch(BlockPos pos) {
        return getPitch(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    // Yaw (Entity)
    public static float getYaw(Entity entity) {
        return getYaw(entity.getPos());
    }

    // Pitch (Entity)
    public static float getPitch(Entity entity) {
        return getPitch(entity.getPos().add(0, entity.getHeight() * 0.5, 0));
    }

    // Yaw (ClientPlayerEntity)
    public static float getYaw(ClientPlayerEntity entity) {
        return getYaw(entity.getPos());
    }

    // Pitch (ClientPlayerEntity)
    public static float getPitch(ClientPlayerEntity entity) {
        return getPitch(entity.getPos());
    }
}
