package me.alpha432.oyvey.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class EntityUtil {

    private static final double CRYSTAL_DAMAGE_MULTIPLIER = 12.0;

    // Oyuncu pozisyonu al
    public static BlockPos getPlayerPos(PlayerEntity player) {
        return new BlockPos((int)Math.floor(player.getX()), (int)Math.floor(player.getY()), (int)Math.floor(player.getZ()));
    }

    // Entity ölü mü kontrol et
    public static boolean isDead(Entity entity) {
        if (entity instanceof LivingEntity) {
            return ((LivingEntity) entity).isDead() || ((LivingEntity) entity).getHealth() <= 0.0f;
        }
        return entity.isRemoved();
    }

    // Ender kristal hasarını basit hesapla
    public static float calculateDamage(BlockPos crystalPos, Entity target) {
        if (target == null || crystalPos == null) return 0f;

        // Pozisyonun ortası (kristalin patlama noktası)
        double x = crystalPos.getX() + 0.5;
        double y = crystalPos.getY() + 1.0;
        double z = crystalPos.getZ() + 0.5;

        // Mesafe hesapla (küp değil, gerçek mesafe)
        double dist = target.squaredDistanceTo(x, y, z);
        dist = Math.sqrt(dist);

        // Patlama yarıçapı (vanilla yaklaşık 12 blok)
        double maxRadius = 12.0;

        if (dist > maxRadius) return 0f;

        // Mesafeye bağlı hasar azaltma oranı
        double distFactor = (maxRadius - dist) / maxRadius;

        // Basit hasar formülü (vanilla fizik yaklaşımları)
        double baseDamage = (distFactor * distFactor + distFactor) * CRYSTAL_DAMAGE_MULTIPLIER;

        // Eğer hedef LivingEntity ise zırh, direnç gibi faktörler uygulanabilir (örnek basit)
        if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) target;

            float armor = living.getArmor();
            float armorToughness = 0; // İstersen buraya ekle

            // Basit zırh azaltma
            double damageAfterArmor = baseDamage * (1 - armor * 0.04);

            // Potion resistance etkisi eklenebilir (şimdilik yok)
            return (float) Math.max(damageAfterArmor, 0);
        }

        return (float) Math.max(baseDamage, 0);
    }
}
