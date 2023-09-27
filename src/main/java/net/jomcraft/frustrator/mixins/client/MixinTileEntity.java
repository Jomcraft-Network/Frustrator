package net.jomcraft.frustrator.mixins.client;

import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.FrustumBounds;
import net.jomcraft.frustrator.IMixinEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(TileEntity.class)
public class MixinTileEntity implements IMixinEntity {

    @Shadow
    protected World worldObj;

    @Unique
    @Nullable
    private FrustumBounds frustumBounds;

    @Override
    public FrustumBounds getFrustum() {
        return this.frustumBounds;
    }

    @Override
    public void setFrustum(final FrustumBounds frustum) {
        this.frustumBounds = frustum;
    }

    @Inject(method = "updateEntity", at = @At(value = "HEAD"), cancellable = true, remap = true)
    public void updateEntity(CallbackInfo info) {
        if (worldObj.isRemote) {

            if (ClientEventHandler.showAllMainAreas)
                return;

            if(this.frustumBounds != null) {

                if(ClientEventHandler.localFrustums.isEmpty()){
                    info.cancel();
                    return;
                }

                boolean success = false;
                for (int ii = 0; ii < ClientEventHandler.localFrustums.size(); ii++) {
                    final FrustumBounds localFrustum = ClientEventHandler.localFrustums.get(ii);

                    if (this.frustumBounds.equalsArea(localFrustum)) {
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
}
