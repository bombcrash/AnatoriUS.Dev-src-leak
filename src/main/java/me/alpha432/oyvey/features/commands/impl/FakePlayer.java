package me.alpha432.oyvey.features.commands.impl;

import com.mojang.authlib.GameProfile;
import me.alpha432.oyvey.features.commands.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;

import java.util.UUID;

public class FakePlayer extends Command {

    private OtherClientPlayerEntity fakePlayer = null;

    public FakePlayer() {
        super("fakeplayer", new String[]{"fp"});
    }

    @Override
    public void execute(String[] commands) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null || mc.world == null) {
            sendMessage(Formatting.RED + "World or player is null.");
            return;
        }

        if (fakePlayer == null) {
            ClientPlayerEntity realPlayer = mc.player;
            ClientWorld world = (ClientWorld) mc.world;

            // Yeni benzersiz GameProfile oluştur
            GameProfile fakeProfile = new GameProfile(UUID.randomUUID(), realPlayer.getName().getString() + "_Fake");

            fakePlayer = new OtherClientPlayerEntity(world, fakeProfile);
            BlockPos pos = realPlayer.getBlockPos();

            fakePlayer.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), realPlayer.getYaw(), realPlayer.getPitch());
            fakePlayer.setHealth(20.0F); // Full can
            fakePlayer.setCustomNameVisible(true);
            fakePlayer.setCustomName(realPlayer.getName().copy().append(" Fake"));

            // Dünyaya entity id ile eklenmeli
            world.addEntity(fakePlayer);

            sendMessage(Formatting.GREEN + "Fake player spawned at your location.");
        } else {
            mc.world.removeEntity(fakePlayer.getId(), Entity.RemovalReason.DISCARDED);
            fakePlayer = null;
            sendMessage(Formatting.RED + "Fake player removed.");
        }
    }
}
