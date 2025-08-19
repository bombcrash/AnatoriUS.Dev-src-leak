package me.alpha432.oyvey.mixin;

import me.alpha432.oyvey.features.modules.misc.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinClientTick {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onClientTick(CallbackInfo ci) {
        if (Timer.INSTANCE != null && Timer.INSTANCE.isEnabled()) {
            float speed = Timer.INSTANCE.getTimerSpeed();

            // Eğer hız 1'den büyükse, world'e ek tick at
            if (speed > 1.0f) {
                ClientWorld world = MinecraftClient.getInstance().world;
                if (world != null) {
                    int extraTicks = (int) Math.floor(speed - 1.0f);
                    for (int i = 0; i < extraTicks; i++) {
                        world.tickEntities(); // Dünyanın entitilerini tekrar tikle
                    }
                }
            }
        }
    }
}
