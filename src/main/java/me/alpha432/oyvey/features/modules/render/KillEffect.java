package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class KillEffect extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final Setting<Boolean> lightning = this.register(new Setting<>("Lightning", true, "Gerçek şimşek efekti gösterir."));
    private final Setting<Boolean> tnt = this.register(new Setting<>("TNT", true, "TNT patlama efekti gösterir."));
    private final Setting<ParticleMode> particleMode = this.register(new Setting<>("Particle", ParticleMode.FIREWORK, "Partikül efekti türü."));
    private final Setting<SoundEffect> soundEffect = this.register(new Setting<>("SoundEffect", SoundEffect.WITHER, "Çalacak ses efekti."));
    private final Setting<Boolean> explosion = this.register(new Setting<>("Explosion", true, "TNT patlama efekti gösterir."));

    private final Set<UUID> triggeredEntities = new HashSet<>();

    public KillEffect() {
        super("KillEffect", "Öldürdüğünde seçili efektleri tetikler", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        World world = mc.world;

        for (LivingEntity entity : world.getEntitiesByClass(
                LivingEntity.class,
                mc.player.getBoundingBox().expand(20),
                e -> e != mc.player
        )) {
            if (entity.isDead() && entity.deathTime == 1 && !triggeredEntities.contains(entity.getUuid())) {
                triggeredEntities.add(entity.getUuid());
                Vec3d pos = entity.getPos();

                if (explosion.getValue()) {
                    triggerExplosion(pos);
                }
                if (lightning.getValue()) {
                    triggerLightning(pos);
                }
                triggerParticle(pos);
                triggerSound(pos);
            }
        }
    }

    private void triggerExplosion(Vec3d pos) {
        if (mc.world == null || mc.player == null) return;


        mc.world.createExplosion(mc.player, pos.x, pos.y, pos.z, 2.0f, false, World.ExplosionSourceType.NONE);


        mc.world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_GENERIC_EXPLODE, mc.player.getSoundCategory(), 2.0f, 1.0f);



    }

    private void triggerLightning(Vec3d pos) {
        if (mc.world == null || mc.player == null) return;


        var lightningEntity = new net.minecraft.entity.LightningEntity(
                net.minecraft.entity.EntityType.LIGHTNING_BOLT,
                mc.world
        );
        lightningEntity.setPos(pos.x, pos.y, pos.z);
        lightningEntity.setCosmetic(false);
        mc.world.spawnEntity(lightningEntity);


        mc.player.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
    }

    private void triggerParticle(Vec3d pos) {
        if (mc.world == null) return;
        for (int i = 0; i < 25; i++) {
            double dx = (Math.random() - 0.5) * 1.5;
            double dy = Math.random() * 1.5;
            double dz = (Math.random() - 0.5) * 1.5;

            switch (particleMode.getValue()) {
                case FIREWORK -> mc.world.addParticleClient(ParticleTypes.FIREWORK, pos.x, pos.y + 1, pos.z, dx, dy, dz);
                case SMOKE    -> mc.world.addParticleClient(ParticleTypes.SMOKE,    pos.x, pos.y + 1, pos.z, dx, dy, dz);
                case SOUL     -> mc.world.addParticleClient(ParticleTypes.SOUL,     pos.x, pos.y + 1, pos.z, dx, dy, dz);
            }
        }
    }

    private void triggerSound(Vec3d pos) {
        if (mc.player == null) return;


        SoundEvent sound = switch (soundEffect.getValue()) {
            case WITHER   -> SoundEvents.ENTITY_WITHER_SPAWN;
            case DRAGON   -> SoundEvents.ENTITY_ENDER_DRAGON_GROWL;
            case LAVA     -> SoundEvents.BLOCK_LAVA_POP;
            case TNT      -> SoundEvents.ENTITY_TNT_PRIMED;
            case LIGHTNING-> SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER;
        };


        mc.player.playSound(sound, 2.0f, 1.0f);
    }

    public enum ParticleMode {
        FIREWORK, SMOKE, SOUL
    }

    public enum SoundEffect {
        WITHER, DRAGON, LAVA, TNT, LIGHTNING
    }

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
