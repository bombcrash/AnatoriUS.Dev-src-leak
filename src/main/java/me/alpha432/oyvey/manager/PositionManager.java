package me.alpha432.oyvey.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;



public class PositionManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static double preX, preY, preZ;
    private static boolean isMoving = false;
    private static double targetX;
    private static double targetY;
    private static double targetZ;
    private static boolean clientSide = false;

    // Pozisyonu güncelle
    public static void updatePosition() {
        if (mc.player == null) return;

        preX = mc.player.getX();
        preY = mc.player.getY();
        preZ = mc.player.getZ();

        targetX = mc.player.getX();
        targetY = mc.player.getY();
        targetZ = mc.player.getZ();
        clientSide = clientSide;

        // Eğer clientSide ise pozisyonu direkt değiştir
        if (clientSide) {
            mc.player.setPosition(targetX, targetY, targetZ);
        }

        isMoving = true;
    }

    // Hareket paketini gönder (UpdateWalkingPlayer PRE event'inde çağır)
    public static void sendPositionPacket() {
        if (mc.player == null) return;
        if (!isMoving) return;

        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(targetX, targetY, targetZ, mc.player.getAttackingPlayer().isOnGround(), mc.player.getAttackingPlayer().isOnGround())

        );
    }

    // Pozisyonu eski haline döndür (UpdateWalkingPlayer POST event'inde çağır)
    public static void restorePosition() {
        if (mc.player == null || !isMoving) return;

        mc.player.setPosition(preX, preY, preZ);
        isMoving = false;
    }
}
