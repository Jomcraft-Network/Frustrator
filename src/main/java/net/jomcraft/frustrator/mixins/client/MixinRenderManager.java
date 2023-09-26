package net.jomcraft.frustrator.mixins.client;

import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.FrustumBounds;
import net.jomcraft.frustrator.IMixinEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public class MixinRenderManager {

    @Inject(method = "func_147939_a", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public boolean func_147939_a(Entity p_147939_1_, double p_147939_2_, double p_147939_4_, double p_147939_6_, float p_147939_8_, float p_147939_9_, boolean p_147939_10_, CallbackInfoReturnable<Boolean> info) {

        if (ClientEventHandler.showAllMainAreas)
            return false;

        final FrustumBounds entityFrustum = ((IMixinEntity) p_147939_1_).getFrustum();
        if (entityFrustum == null)
            return false;

        if (!entityFrustum.equalsArea(ClientEventHandler.localFrustum) && !(p_147939_1_ instanceof EntityPlayer)) {
            info.setReturnValue(false);
            info.cancel();
        }

        return false;
    }

}
