package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class EntitySpeed extends Module {

    private final Setting<Float> speed = this.num("Speed", 2.0f, 0.1f, 10.0f);
    private final Setting<Boolean> flight = this.bool("Flight", false);
    private final Setting<Float> flightSpeed = this.num("FlightSpeed", 0.5f, 0.1f, 5.0f);
    private final Setting<Boolean> waterOnWalk = this.bool("WaterOnWalk", false);
    private final Setting<Boolean> rage = this.bool("PvPOnAir", false);
    private final Setting<Boolean> antiKickGlide = this.bool("AntiKickGlide", true); // Yeni ayar

    public EntitySpeed() {
        super("EntitySpeed", "Bindiğin entity'lerin hızını ve uçuşunu kontrol eder.", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        // Rage mode hedefleme
        if (rage.getValue()) {
            Entity closestTarget = null;
            double minDistance = 6.0;

            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof PlayerEntity && entity != mc.player) {
                    double dist = entity.squaredDistanceTo(mc.player);
                    if (dist < minDistance * minDistance) {
                        minDistance = dist;
                        closestTarget = entity;
                    }
                }
            }

            if (closestTarget != null) {
                Vec3d targetPos = closestTarget.getPos();
                double dx = targetPos.x - mc.player.getX();
                double dz = targetPos.z - mc.player.getZ();
                float angle = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90F;
                mc.player.setYaw(angle);
                vehicle.setYaw(angle);
            }
        }

        float yaw = mc.player.getYaw();
        double radians = Math.toRadians(yaw);
        double motionX = 0.0;
        double motionZ = 0.0;
        double motionY = vehicle.getVelocity().y;

        if (mc.options.forwardKey.isPressed()) {
            motionX -= Math.sin(radians) * speed.getValue();
            motionZ += Math.cos(radians) * speed.getValue();
        }

        if (mc.options.backKey.isPressed()) {
            motionX += Math.sin(radians) * speed.getValue();
            motionZ -= Math.cos(radians) * speed.getValue();
        }

        if (mc.options.leftKey.isPressed()) {
            motionX += Math.cos(radians) * speed.getValue();
            motionZ -= Math.sin(radians) * speed.getValue();
        }

        if (mc.options.rightKey.isPressed()) {
            motionX -= Math.cos(radians) * speed.getValue();
            motionZ += Math.sin(radians) * speed.getValue();
        }

        if (flight.getValue()) {
            boolean jump = mc.options.jumpKey.isPressed();
            boolean ctrl = InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL);

            if (jump) {
                motionY = flightSpeed.getValue();
            } else if (ctrl) {
                motionY = -flightSpeed.getValue();
            } else if (antiKickGlide.getValue()) {
                motionY = -0.035; // Hafif iniş → sunucu uçuş kick'inden korur
            } else {
                motionY = 0;
            }
        }

        if (waterOnWalk.getValue()) {
            BlockPos posBelow = vehicle.getBlockPos().down();
            if (mc.world.getBlockState(posBelow).getBlock() == Blocks.WATER) {
                motionY = 0.1;
            }
        }

        vehicle.setVelocity(new Vec3d(motionX, motionY, motionZ));
    }

    // Boş metodlar
    @Override public void onRender(MatrixStack matrices, float tickDelta) {}
    @Override public void onRenderWorldLast(MatrixStack matrices, float tickDelta) {}
    @Override public void onRender3D(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, float tickDelta) {}
    @Override public void onRender3D(float partialTicks) {}
    @Override public void onRender3D(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider) {}
    @Override public void onRender3D(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, float tickDelta) {}

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumers vertexConsumers, float tickDelta) {

    }

    @Override
    public void onRender3D(MatrixStack matrixStack, VertexConsumer vertexConsumer, float tickDelta) {

    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float tickDelta) {

    }

    @Override public void onPacketSend(PacketEvent.Send event) {}
    @Override public void onPacketReceive(PacketEvent.Receive event) {}
    @Override public void onRender() {}

    @Override
    public void onRenderWorldLast(MatrixStack matrices) {

    }

    @Override
    public void onRender(DrawContext context) {

    }
}
