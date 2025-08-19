package me.alpha432.oyvey.util;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class InventoryUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // Hotbar içinde belirtilen bloğu ara, slot numarasını döndür. Yoksa -1.
    public static int findHotbarBlock(Block block) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.BlockItem && ((net.minecraft.item.BlockItem)stack.getItem()).getBlock() == block) {
                return i;
            }
        }
        return -1;
    }

    public InventoryUtil() {
    }

    // Belirtilen hotbar slotuna geçiş yapar, silent ise oyuncu hareketi bozulmaz (bazı modlarda fark eder)
    public static void switchToSlot(int slot, boolean silent) {
        if (slot < 0 || slot > 8) return;
        if (mc.player.getInventory().getSelectedSlot() == slot) return;

        if (silent) {
            // Silent switch (ör: pakette görünmeyebilir, sadece client içi)
            mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                    mc.player.getBlockPos(), mc.player.getHorizontalFacing()));
            // Not: Bu örnek basit, silent switch için farklı yöntemler olabilir.
        } else {
            mc.player.getInventory().setSelectedSlot(slot);
            mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(slot));
        }
    }
}
