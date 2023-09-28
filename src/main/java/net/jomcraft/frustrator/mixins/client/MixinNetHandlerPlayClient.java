package net.jomcraft.frustrator.mixins.client;

import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.FrustumBounds;
import net.jomcraft.frustrator.IMixinEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "handleEntityMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPositionAndRotation2(DDDFFI)V", shift = At.Shift.BEFORE), cancellable = true, remap = true)
    public void handleEntityMovement(S14PacketEntity packetIn, CallbackInfo info) {
        final Entity e = packetIn.func_149065_a(Minecraft.getMinecraft().theWorld);
        if (e.ticksExisted % 3 != 0) return;

        final int x = MathHelper.floor_double((double) e.serverPosX / 32.0D);
        final int y = MathHelper.floor_double((double) e.serverPosY / 32.0D);
        final int z = MathHelper.floor_double((double) e.serverPosZ / 32.0D);

        boolean inFrustum = false;
        for (int a = 0; a < ClientEventHandler.frustumBounds.length; a++) {
            final FrustumBounds frustum = ClientEventHandler.frustumBounds[a];

            if (ClientEventHandler.frustumCheck(x, y, z, frustum)) {
                ((IMixinEntity) e).setFrustum(frustum);
                inFrustum = true;
                break;
            }
        }

        if (!inFrustum) {
            ((IMixinEntity) e).setFrustum(null);
        }
    }
}
