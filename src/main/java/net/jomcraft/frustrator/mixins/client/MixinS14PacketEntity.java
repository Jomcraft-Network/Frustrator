package net.jomcraft.frustrator.mixins.client;

import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.Frustrator;
import net.jomcraft.frustrator.FrustumBounds;
import net.jomcraft.frustrator.IMixinEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(S14PacketEntity.class)
public abstract class MixinS14PacketEntity {

    @Shadow
    public abstract Entity func_149065_a(World p_149065_1_);

    @Inject(method = "processPacket", at = @At(value = "HEAD"), cancellable = true, remap = true)
    public void processPacket(INetHandlerPlayClient handler, CallbackInfo info) {
        final Entity e = this.func_149065_a(Frustrator.proxy.getClientWorld());
        if (e != null) {
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
}
