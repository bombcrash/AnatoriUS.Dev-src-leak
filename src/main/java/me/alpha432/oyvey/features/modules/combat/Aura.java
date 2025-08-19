package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class Aura extends Module {

    private final Setting<Float> range = this.register(new Setting<>("Range", 6.0f, 1.0f, 10.0f, "Saldırı menzili"));
    private final Setting<Boolean> players = this.register(new Setting<>("Players", true, "Oyuncuları hedefle"));
    private final Setting<Boolean> mobs = this.register(new Setting<>("Mobs", true, "Düşman canavarları hedefle"));

    public Aura() {
        super("Aura", "Sadece elinde kılıç varken oyuncu ve düşmanlara saldırır", Category.COMBAT);
    }

    private boolean isHoldingSword() {
        Item mainItem = mc.player.getMainHandStack().getItem();
        // Kılıç mı? (Demir, altın, elmas, netherite, taş, tahta)
        return mainItem == Items.WOODEN_SWORD ||
                mainItem == Items.STONE_SWORD ||
                mainItem == Items.IRON_SWORD ||
                mainItem == Items.GOLDEN_SWORD ||
                mainItem == Items.DIAMOND_SWORD ||
                mainItem == Items.NETHERITE_SWORD;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (!isHoldingSword()) return; // Sadece kılıç varsa devam et

        LivingEntity target = null;
        double closestDistance = range.getValue();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity == mc.player || !entity.isAlive() || entity.isInvisible()) continue;

            // Hedef tipi filtrele
            if (entity instanceof PlayerEntity && !players.getValue()) continue;
            if (entity instanceof HostileEntity && !mobs.getValue()) continue;
            if (!(entity instanceof PlayerEntity) && !(entity instanceof HostileEntity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist <= closestDistance) {
                closestDistance = dist;
                target = (LivingEntity) entity;
            }
        }

        if (target != null && mc.player.getAttackCooldownProgress(0.5f) > 0.9f) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    // Geri kalan render ve packet metodları boş bırakılabilir veya ileride kullanılabilir
    @Override public void onRender(MatrixStack matrices, float tickDelta) {}
    @Override public void onRender() {}

    @Override
    public void onRenderWorldLast(MatrixStack matrices) {

    }

    @Override
    public void onRender(DrawContext context) {

    }

    @Override public void onRenderWorldLast(MatrixStack matrices, float tickDelta) {}
    @Override public void onRender3D(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, float tickDelta) {}

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

    @Override public void onPacketSend(PacketEvent.Send event) {}
    @Override public void onPacketReceive(PacketEvent.Receive event) {}
}
