package me.alpha432.oyvey.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import me.alpha432.oyvey.util.traits.Util;

public class BoxUtil implements Util {
    public static Box getBlockBoundingBox(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getPos();
        Box offset = blockEntity.getCachedState().getOutlineShape(mc.world, pos).getBoundingBox();
        Box boundingBox = new Box(
                pos.getX() + offset.minX,
                pos.getY() + offset.minY,
                pos.getZ() + offset.minZ,
                pos.getX() + offset.maxX,
                pos.getY() + offset.maxY,
                pos.getZ() + offset.maxZ
        );

        return boundingBox;
    }

    public static Box getBlockBoundingBox(BlockPos pos) {
        VoxelShape voxelShape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
        if (voxelShape.isEmpty())
            return get1x1Box(pos.getX(), pos.getY(), pos.getZ());

        Box offset = voxelShape.getBoundingBox();
        Box boundingBox = new Box(
                pos.getX() + offset.minX,
                pos.getY() + offset.minY,
                pos.getZ() + offset.minZ,
                pos.getX() + offset.maxX,
                pos.getY() + offset.maxY,
                pos.getZ() + offset.maxZ
        );

        return boundingBox;
    }

    public static Box getBlockBoundingBoxf(BlockEntity blockEntity) {
        return getBlockBoundingBox(blockEntity);
    }

    public static Box getBlockBoundingBoxf(BlockPos pos) {
        return getBlockBoundingBox(pos);
    }

    public static Box get1x1Box(int x, int y, int z) {
        return new Box(x, y, z, x + 1, y + 1, z + 1);
    }

    public static Box get1x1Box(Vec3d pos) {
        return new Box(pos.x, pos.y, pos.z, pos.x + 1, pos.y + 1, pos.z + 1);
    }
}