package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.commands.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

import java.util.Comparator;
import java.util.stream.StreamSupport;

public class AimAsist extends Module {

    // Ayarları kaydet (GUI'de görünür olacak)
    public Setting<Float> range = register(new Setting<>("Range", 6.0f, 1.0f, 12.0f));
    public Setting<Float> smooth = register(new Setting<>("Smooth", 5.0f, 1.0f, 20.0f));
    public Setting<Boolean> playersOnly = register(new Setting<>("PlayersOnly", true));

    public AimAsist() {
        super("AimAssist", "Disables all knockback", Category.COMBAT);
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
        if (mc.player == null || mc.world == null || mc.currentScreen != null)
            return;

        LivingEntity target = getTarget();
        if (target == null) return;

        rotateTowards(target);
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

    private LivingEntity getTarget() {
        return StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> e != mc.player && !e.isDead() && mc.player.squaredDistanceTo(e) <= range.getValue() * range.getValue())
                .filter(e -> !playersOnly.getValue() || e instanceof PlayerEntity)
                .min(Comparator.comparingDouble(e -> getAngleDifference(e)))
                .orElse(null);
    }

    private double getAngleDifference(LivingEntity entity) {
        double deltaX = entity.getX() - mc.player.getX();
        double deltaZ = entity.getZ() - mc.player.getZ();
        double yawToTarget = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0;

        float deltaYaw = wrapDegrees(mc.player.getYaw() - (float) yawToTarget);
        return Math.abs(deltaYaw);
    }

    private void rotateTowards(LivingEntity target) {
        double deltaX = target.getX() - mc.player.getX();
        double deltaY = (target.getY() + target.getEyeHeight(target.getPose())) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double deltaZ = target.getZ() - mc.player.getZ();

        double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yawToTarget = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
        float pitchToTarget = (float) -Math.toDegrees(Math.atan2(deltaY, dist));

        mc.player.setYaw(smoothAngle(mc.player.getYaw(), yawToTarget, smooth.getValue()));
        mc.player.setPitch(smoothAngle(mc.player.getPitch(), pitchToTarget, smooth.getValue()));
    }

    private float smoothAngle(float current, float target, float smoothFactor) {
        float delta = wrapDegrees(target - current);
        return current + delta / smoothFactor;
    }

    private float wrapDegrees(float value) {
        float result = value % 360.0f;
        if (result >= 180.0f) result -= 360.0f;
        if (result < -180.0f) result += 360.0f;
        return result;
    }

    // GUI tarafı genellikle otomatik olarak register edilen ayarları gösterir.
    // Eğer senin GUI sisteminde ayarları manuel eklemen gerekiyorsa,
    // orada playersOnly gibi boolean ayarları toggle olarak gösterdiğinden emin olmalısın.

    // OnRender gibi metodlar boş bırakılmış, sadece işlevsel kısım için.

}
