package net.jomcraft.frustrator.mixins.client;

import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.FrustumBounds;
import net.jomcraft.frustrator.IMixinEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public abstract class MixinChunk {

    @Inject(method = "func_150812_a", at = @At(value = "RETURN"), cancellable = true, remap = true)
    public void func_150812_a(int p_150812_1_, int p_150812_2_, int p_150812_3_, TileEntity p_150812_4_, CallbackInfo info) {
        if (p_150812_4_.hasWorldObj() && p_150812_4_.getWorldObj().isRemote) {
            this.checkFrustum(p_150812_4_);
        }
    }

    public void checkFrustum(TileEntity e) {
        boolean inFrustum = false;

        for (int a = 0; a < ClientEventHandler.frustumBounds.length; a++) {
            final FrustumBounds frustum = ClientEventHandler.frustumBounds[a];

            if (ClientEventHandler.frustumCheck(e.xCoord, e.yCoord, e.zCoord, frustum)) {
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
