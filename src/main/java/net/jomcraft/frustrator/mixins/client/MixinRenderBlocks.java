package net.jomcraft.frustrator.mixins.client;

import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.FrustumBounds;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderBlocks.class)
public class MixinRenderBlocks {

    @Inject(method = "renderBlockByRenderType", at = @At(value = "HEAD"), cancellable = true, remap = true)
    public boolean renderWorldBlock(Block p_147805_1_, int p_147805_2_, int p_147805_3_, int p_147805_4_, CallbackInfoReturnable<Boolean> info) {
        if(ClientEventHandler.bypassFrustrator)
            return false;

        for (int a = 0; a < ClientEventHandler.frustumBounds.length; a++) {
            final FrustumBounds frustum = ClientEventHandler.frustumBounds[a];
            if (ClientEventHandler.showAllMainAreas && frustum.channelID == ClientEventHandler.currentChannelID)
                continue;

            if (ClientEventHandler.frustumCheck(p_147805_2_, p_147805_3_, p_147805_4_, frustum)) {

                if (ClientEventHandler.localFrustums.isEmpty()) {
                    info.setReturnValue(false);
                    info.cancel();
                    return false;
                }

                boolean success = false;
                for (int ii = 0; ii < ClientEventHandler.localFrustums.size(); ii++) {
                    final FrustumBounds localFrustum = ClientEventHandler.localFrustums.get(ii);
                    if (frustum.equalsArea(localFrustum)) {
                        success = true;
                        break;
                    }
                }

                if (!success) {
                    info.setReturnValue(false);
                    info.cancel();
                    return false;
                }
            }
        }
        return false;
    }
}