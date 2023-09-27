package net.jomcraft.frustrator.mixins.client;

import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.FrustumBounds;
import net.jomcraft.frustrator.IMixinEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {

    @Inject(method = "renderTileEntityAt", at = @At(value = "HEAD"), cancellable = true, remap = true)
    public void renderTileEntityAt(TileEntity p_147549_1_, double p_147549_2_, double p_147549_4_, double p_147549_6_, float p_147549_8_, CallbackInfo info) {
        if (ClientEventHandler.showAllMainAreas)
            return;

        final FrustumBounds frustum = ((IMixinEntity) p_147549_1_).getFrustum();
        if (frustum != null) {

            if(ClientEventHandler.localFrustums.isEmpty()){
                info.cancel();
                return;
            }

            boolean success = false;
            for (int ii = 0; ii < ClientEventHandler.localFrustums.size(); ii++) {
                final FrustumBounds localFrustum = ClientEventHandler.localFrustums.get(ii);

                if (frustum.equalsArea(localFrustum)) {
                    success = true;
                    break;
                }
            }

            if(!success) {
                info.cancel();
                return;
            }
        }
    }
}
