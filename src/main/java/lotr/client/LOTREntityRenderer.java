package lotr.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManager;

public class LOTREntityRenderer extends EntityRenderer {

    private Minecraft theMC;

    public LOTREntityRenderer(Minecraft p_i45076_1_, IResourceManager p_i45076_2_) {
        super(p_i45076_1_, p_i45076_2_);
    }

    @Override
    public void getMouseOver(float partialTick) {

    }
}
