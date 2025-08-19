package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoTotem extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private int tickTimer = 0;

    public AutoTotem() {
        super("AutoTotem", "Automatically equips totem in offhand", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        // Tick timer (gecikme için)
        if (tickTimer > 0) {
            tickTimer--;
            return;
        }

        // Can kontrolü
        float currentHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (currentHealth <= 16.0f) {

            // Offhand'de totem var mı kontrol et
            if (mc.player.getStackInHand(Hand.OFF_HAND).getItem() != Items.TOTEM_OF_UNDYING) {
                swapTotem();
            }
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

    private void swapTotem() {
        // Inventory'de totem ara
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {

                // Slot hesapla (hotbar için +36)
                int slot = i < 9 ? i + 36 : i;

                // Totem'i al
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        slot,
                        0,
                        SlotActionType.PICKUP,
                        mc.player
                );

                // Offhand'e koy
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        45, // Offhand slot
                        0,
                        SlotActionType.PICKUP,
                        mc.player
                );

                // Kalan item'i geri koy
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        slot,
                        0,
                        SlotActionType.PICKUP,
                        mc.player
                );

                // Gecikme ayarla
                tickTimer = 3;
                break;
            }
        }
    }
}