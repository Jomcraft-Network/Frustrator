package net.jomcraft.frustrator.mixins.client;

import net.jomcraft.frustrator.FrustumBounds;
import net.jomcraft.frustrator.IMixinEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public class MixinEntity implements IMixinEntity {

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

}
