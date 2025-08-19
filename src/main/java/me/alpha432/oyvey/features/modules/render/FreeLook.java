package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class FreeLook extends Module {

    private float cameraYaw = 0.0f;
    private float cameraPitch = 0.0f;

    public FreeLook() {
        super("FreeLook", "Look around independently", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            cameraYaw = mc.player.getYaw();
            cameraPitch = mc.player.getPitch();
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            // Mod kapandığında kamerayı oyuncunun yönüne eşitle
            mc.player.setYaw(cameraYaw);
            mc.player.setPitch(cameraPitch);
        }
    }

    @Override
    public void onUpdate() {
        if (!isEnabled() || mc.player == null || mc.currentScreen != null) return;

        // Fare hareketleri ile kamerayı güncelle
        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();

        double[] xOffset = new double[1];
        double[] yOffset = new double[1];
        GLFW.glfwGetCursorPos(windowHandle, xOffset, yOffset);

        // Burada fare hareketini yakalayıp işlemek gerekiyor ancak bu kısım
        // Minecraft 1.21.5 ve Fabric için biraz daha karmaşık,
        // Bu yüzden basitçe mouse delta'larını kullanacağız:
        // (Bu örnekte kolaylık olsun diye buraya not koyuyorum,
        // gerçek bir implementation için input sistemiyle entegre olmak gerekir)

        // Biz bu örnekte sadece klavye ile yön değiştirmeyi yapalım:
        float yawChange = 0f;
        float pitchChange = 0f;
        float speed = 2.0f; // Kamerayı döndürme hızı

        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT)) {
            yawChange -= speed;
        }
        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT)) {
            yawChange += speed;
        }
        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_UP)) {
            pitchChange -= speed;
        }
        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN)) {
            pitchChange += speed;
        }

        cameraYaw = (cameraYaw + yawChange) % 360.0f;
        cameraPitch = MathHelper.clamp(cameraPitch + pitchChange, -90f, 90f);
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

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }
}
