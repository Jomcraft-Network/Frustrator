package net.jomcraft.frustrator;

import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class FocusManager {

    public static void manageFocus(Minecraft mc, float p_78473_1_) {
        if(ClientEventHandler.bypassFrustrator)
            return;

        if (!ClientEventHandler.showAllMainAreas && !ClientEventHandler.showAllTriggerAreas) return;

        double d1 = (double) mc.playerController.getBlockReachDistance();

        Vec3 vec3 = mc.renderViewEntity.getPosition(p_78473_1_);
        Vec3 vec31 = mc.renderViewEntity.getLook(p_78473_1_);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * d1, vec31.yCoord * d1, vec31.zCoord * d1);
        Vec3 vec33 = null;

        boolean main = false;
        boolean trigger = false;

        if (ClientEventHandler.showAllMainAreas) {

            for (int i = 0; i < ClientEventHandler.frustumBounds.length; i++) {
                FrustumBounds frustum = ClientEventHandler.frustumBounds[i];

                if (frustum.channelID != ClientEventHandler.currentChannelID && !Minecraft.getMinecraft().thePlayer.isSneaking())
                    continue;

                final AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(frustum.minX, frustum.minY, frustum.minZ, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (0.0D < d1 || d1 == 0.0D) {
                        ClientEventHandler.focusedFrustum = null;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d1 || d1 == 0.0D) {
                        ClientEventHandler.focusedFrustum = frustum;
                        main = true;
                    }
                }
            }
        }

        if (ClientEventHandler.showAllTriggerAreas) {

            for (int i = 0; i < ClientEventHandler.triggerBounds.length; i++) {
                FrustumBounds frustum = ClientEventHandler.triggerBounds[i];

                if (frustum.channelID != ClientEventHandler.currentChannelID && !Minecraft.getMinecraft().thePlayer.isSneaking())
                    continue;

                final AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(frustum.minX, frustum.minY, frustum.minZ, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (0.0D < d1 || d1 == 0.0D) {
                        ClientEventHandler.focusedTrigger = null;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d1 || d1 == 0.0D) {
                        ClientEventHandler.focusedTrigger = frustum;
                        trigger = true;
                    }
                }
            }
        }
        if (!main) ClientEventHandler.focusedFrustum = null;

        if (!trigger) ClientEventHandler.focusedTrigger = null;
    }

}
