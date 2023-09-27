package net.jomcraft.frustrator;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.Arrays;

public class FrustumBounds {

    public final int minX;
    public final int minY;
    public final int minZ;

    public final int maxX;
    public final int maxY;
    public final int maxZ;

    public final boolean trigger;

    @Nullable
    @Deprecated
    public FrustumBounds parent;

    public FrustumBounds [] parents;

    public FrustumBounds(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ, boolean trigger, FrustumBounds [] parents) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.trigger = trigger;
        this.parents = parents;
    }

    public boolean equalsArea(FrustumBounds frustum) {
        if (frustum == null)
            return false;
        if (frustum.minX == this.minX && frustum.minY == this.minY && frustum.minZ == this.minZ && frustum.maxX == this.maxX && frustum.maxY == this.maxY && frustum.maxZ == this.maxZ)
            return true;

        return false;
    }

    @Override
    public String toString() {
        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]; isTrigger: " + this.trigger + " Parent: " + (this.parents == null ? "no parents" : Arrays.stream(this.parents).toString());
    }
}