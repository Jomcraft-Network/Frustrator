package net.jomcraft.frustrator.mixins.client;

import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.FrustumBounds;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectRenderer.class)
public class MixinEffectRenderer {

    @Inject(method = "addEffect", at = @At(value = "HEAD"), cancellable = true, remap = true)
    public void addEffect(EntityFX p_78873_1_, CallbackInfo info) {
        if (ClientEventHandler.showAllMainAreas)
            return;

        final int x = MathHelper.floor_double(p_78873_1_.posX);
        final int y = MathHelper.floor_double(p_78873_1_.posY);
        final int z = MathHelper.floor_double(p_78873_1_.posZ);

        for (int i = 0; i < ClientEventHandler.frustumBounds.length; i++) {
            final FrustumBounds frustum = ClientEventHandler.frustumBounds[i];
            if (ClientEventHandler.frustumCheck(x, y, z, frustum)) {
                if (!frustum.equalsArea(ClientEventHandler.localFrustum))
                    info.cancel();
            }
        }
    }

}
