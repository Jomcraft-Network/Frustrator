package net.jomcraft.frustrator;

import javax.annotation.Nullable;

public interface IMixinEntity {

    @Nullable
    FrustumBounds getFrustum();

    void setFrustum(@Nullable final FrustumBounds frustum);

}