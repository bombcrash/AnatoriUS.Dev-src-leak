package me.alpha432.oyvey.util;

import me.alpha432.oyvey.util.traits.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BlockUtil implements Util {

    @Environment(EnvType.CLIENT)
    public static void placeBlockSmart(BlockPos pos, Block block) {
        int slot = findBlockInHotbar(block);
        if (slot == -1) return;

        int oldSlot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot);

        BlockHitResult hitResult = new BlockHitResult(
                mc.player.getPos(), Direction.UP, pos, false
        );
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

        mc.player.getInventory().setSelectedSlot(oldSlot);
    }

    @Environment(EnvType.CLIENT)
    public static int findBlockInHotbar(Block block) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem) {
                if (((BlockItem) stack.getItem()).getBlock() == block) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Environment(EnvType.CLIENT)
    public static void placeBlock(BlockPos pos) {
        if (mc.world == null || mc.player == null) return;

        BlockHitResult hitResult = new BlockHitResult(
                mc.player.getPos(), Direction.UP, pos, false
        );
        mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, hitResult);
    }

    public static boolean isHole(BlockPos pos) {
        return false;
    }
}
