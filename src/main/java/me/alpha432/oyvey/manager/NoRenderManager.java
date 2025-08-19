package me.alpha432.oyvey.manager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class NoRenderManager {
    // Modülün açık olup olmadığını takip eder
    public static boolean active = false;

    // End kristali patlamalarının koordinatlarını tutar
    private static final List<double[]> explosionPositions = new LinkedList<>();

    // Yeni bir patlama pozisyonu kaydet (MixinClientWorld bunu çağıracak)
    public static void registerExplosionPos(double x, double y, double z) {
        explosionPositions.add(new double[]{x, y, z});
    }

    // Verilen koordinatta end kristali patlaması olmuş mu diye kontrol eder
    public static boolean isExplosionAt(double x, double y, double z) {
        Iterator<double[]> it = explosionPositions.iterator();
        while (it.hasNext()) {
            double[] pos = it.next();
            // 0.5 blok yakınlıkta patlama varsa, bu partikülü engelle
            if (Math.abs(x - pos[0]) < 0.5 && Math.abs(y - pos[1]) < 0.5 && Math.abs(z - pos[2]) < 0.5) {
                it.remove(); // Bir kere yakalanınca listeden çıkar
                return true;
            }
        }
        return false;
    }
}
