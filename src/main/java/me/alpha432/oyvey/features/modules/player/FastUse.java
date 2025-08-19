package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class FastUse extends Module {
    private final Setting<Integer> delay = this.register(new Setting<>("Delay", 0, 0, 5));
    private final Setting<Boolean> blocks = this.register(new Setting<>("Blocks", false));
    private final Setting<Boolean> crystals = this.register(new Setting<>("Crystals", false));
    private final Setting<Boolean> xp = this.register(new Setting<>("XP", false));
    private final Setting<Boolean> all = this.register(new Setting<>("All", true));

    public FastUse() {
        super("FastUse", "Her şeyi hızlı kullan", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;

        Item mainItem = mc.player.getMainHandStack().getItem();

        if (check(mainItem) && mc.player.isUsingItem()) {
            if (mc.itemUseCooldown > delay.getValue()) {
                mc.itemUseCooldown = delay.getValue();
            }

            // Item'ı tekrar kullanmak için sağ tıklama tetikle
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
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

    private boolean check(Item item) {
        return (item instanceof BlockItem && blocks.getValue())
                || (item == Items.END_CRYSTAL && crystals.getValue())
                || (item == Items.EXPERIENCE_BOTTLE && xp.getValue())
                || all.getValue();
    }
}
