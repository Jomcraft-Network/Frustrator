package net.jomcraft.frustrator.mixins.client;

import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.FrustumBounds;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldClient.class)
public class MixinWorldClient {

    @Inject(method = "playSound", at = @At(value = "HEAD"), cancellable = true, remap = true)
    public void playSound(double x, double y, double z, String soundName, float volume, float pitch, boolean distanceDelay, CallbackInfo info) {
        final int xCoord = MathHelper.floor_double(x);
        final int yCoord = MathHelper.floor_double(y);
        final int zCoord = MathHelper.floor_double(z);

        if (ClientEventHandler.showAllMainAreas)
            return;

        for (int i = 0; i < ClientEventHandler.frustumBounds.length; i++) {
            final FrustumBounds frustum = ClientEventHandler.frustumBounds[i];
            if (ClientEventHandler.frustumCheck(xCoord, yCoord, zCoord, frustum)) {
                if (!frustum.equalsArea(ClientEventHandler.localFrustum))
                    info.cancel();
            }
        }
    }

}
