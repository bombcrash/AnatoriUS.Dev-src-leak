package me.alpha432.oyvey.mixin;

import me.alpha432.oyvey.event.Stage;
import me.alpha432.oyvey.event.impl.PacketEvent;
import me.alpha432.oyvey.util.traits.Util;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinVelocity {

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void onEntityVelocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        // Paket iptal ediliyor böylece velocity uygulanmıyor
        ci.cancel();

        // Event bus varsa event gönderilebilir
        Util.EVENT_BUS.post(new PacketEvent.Receive(packet, Stage.PRE));
    }

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    private void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        // Patlama velocity iptal ediliyor
        ci.cancel();

        Util.EVENT_BUS.post(new PacketEvent.Receive(packet, Stage.PRE));
    }
}
