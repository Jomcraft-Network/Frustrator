package net.jomcraft.frustrator;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.jomcraft.frustrator.items.ItemFrustrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.lwjgl.opengl.GL11;
import javax.annotation.Nullable;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class ClientEventHandler {

    public static FrustumBounds[] frustumBounds = new FrustumBounds[0];

    public static FrustumBounds[] triggerBounds = new FrustumBounds[0];

    public static ArrayList<FrustumBounds> localFrustums = new ArrayList<FrustumBounds>();

    public int ticker = 0;

    @Nullable
    public static FrustumBounds focusedFrustum = null;

    @Nullable
    public static FrustumBounds focusedTrigger = null;
    @Nullable
    public static FrustumBounds selectedTrigger = null;

    @Nullable
    public static FrustumBounds selectedFrustum = null;
    public static boolean showAllMainAreas = false;
    public static boolean showAllTriggerAreas = false;

    public static boolean frustumCheck(final int x, final int y, final int z, final FrustumBounds frustum) {
        if ((x >= frustum.minX && x <= (frustum.maxX)) && (y >= frustum.minY && y <= (frustum.maxY)) && (z >= frustum.minZ && z <= (frustum.maxZ))) {
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        final int x = MathHelper.floor_double(event.entity.posX);
        final int y = MathHelper.floor_double(event.entity.posY);
        final int z = MathHelper.floor_double(event.entity.posZ);

        boolean inFrustum = false;
        for (int a = 0; a < frustumBounds.length; a++) {
            final FrustumBounds frustum = frustumBounds[a];

            if (frustumCheck(x, y, z, frustum)) {
                ((IMixinEntity) event.entity).setFrustum(frustum);
                inFrustum = true;
                break;
            }
        }

        if (!inFrustum) {
            ((IMixinEntity) event.entity).setFrustum(null);
        }
    }

    @SubscribeEvent
    public void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entity.worldObj.isRemote) {
            if (showAllMainAreas)
                return;

            final FrustumBounds frustum = ((IMixinEntity) event.entity).getFrustum();
            if(frustum != null) {
                boolean success = false;
                for (int ii = 0; ii < ClientEventHandler.localFrustums.size(); ii++) {
                    final FrustumBounds localFrustum = ClientEventHandler.localFrustums.get(ii);
                    if (frustum.equalsArea(localFrustum)) {
                        success = true;
                        break;
                    }
                }

                if(!success) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().theWorld == null)
            return;

        if (event.phase == TickEvent.Phase.START) {
            final List tileEntityList = Minecraft.getMinecraft().theWorld.loadedTileEntityList;
            for (int i = 0; i < tileEntityList.size(); i++) {
                final TileEntity e = (TileEntity) tileEntityList.get(i);
                if (this.ticker % 100 == 0) {

                    boolean inFrustum = false;
                    for (int a = 0; a < frustumBounds.length; a++) {
                        final FrustumBounds frustum = frustumBounds[a];

                        if (frustumCheck(e.xCoord, e.yCoord, e.zCoord, frustum)) {
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
            ticker++;
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.player.worldObj.isRemote) {
            if (Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem() != null && Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItem() instanceof ItemFrustrator) {

                if (Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItemDamage() == 0) {
                    if (selectedFrustum == null) {
                        if (showAllMainAreas == false) {
                            for (int i = 0; i < frustumBounds.length; i++) {
                                FrustumBounds frustum = frustumBounds[i];
                                Minecraft.getMinecraft().renderGlobal.markBlocksForUpdate(frustum.minX - 1, frustum.minY - 1, frustum.minZ - 1, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1);
                            }
                        }
                        showAllMainAreas = true;
                    }
                    showAllTriggerAreas = false;
                } else if (Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem().getItemDamage() == 1) {
                    if (selectedFrustum == null) {
                        if (showAllMainAreas == true) {
                            for (int i = 0; i < frustumBounds.length; i++) {
                                FrustumBounds frustum = frustumBounds[i];
                                Minecraft.getMinecraft().renderGlobal.markBlocksForUpdate(frustum.minX - 1, frustum.minY - 1, frustum.minZ - 1, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1);
                            }
                        }
                        showAllMainAreas = false;
                    }
                    showAllTriggerAreas = true;
                }

            } else {
                if (selectedFrustum == null) {
                    if (showAllMainAreas == true) {
                        for (int i = 0; i < frustumBounds.length; i++) {
                            FrustumBounds frustum = frustumBounds[i];
                            Minecraft.getMinecraft().renderGlobal.markBlocksForUpdate(frustum.minX - 1, frustum.minY - 1, frustum.minZ - 1, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1);
                        }
                    }
                    showAllMainAreas = false;
                }
                showAllTriggerAreas = false;
            }

            if (event.player.ticksExisted % 2 == 0) {
                if (showAllMainAreas)
                    return;

                final int x = MathHelper.floor_double(event.player.posX);
                final int y = MathHelper.floor_double(event.player.posY);
                final int z = MathHelper.floor_double(event.player.posZ);

                boolean inFrustum = false;

                final ArrayList<FrustumBounds> dummyFrustums = new ArrayList<FrustumBounds>();

                for (int i = 0; i < frustumBounds.length; i++) {
                    final FrustumBounds frustum = frustumBounds[i];
                    if (frustumCheck(x, y, z, frustum)) {
                        if (!localFrustums.contains(frustum)) {
                            Minecraft.getMinecraft().renderGlobal.markBlocksForUpdate(frustum.minX - 1, frustum.minY - 1, frustum.minZ - 1, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1);
                        }
                            dummyFrustums.add(frustum);

                        inFrustum = true;
                        //TODO: Change this!
                        break;
                    }
                }

                if (!inFrustum) {
                    for (int i = 0; i < triggerBounds.length; i++) {
                        final FrustumBounds trigger = triggerBounds[i];
                        if (frustumCheck(x, y, z, trigger)) {
                            for(int ii = 0; ii < trigger.parents.length; ii++){
                                final FrustumBounds parent = trigger.parents[ii];
                                if(!localFrustums.contains(parent)) {
                                    Minecraft.getMinecraft().renderGlobal.markBlocksForUpdate(parent.minX - 1, parent.minY - 1, parent.minZ - 1, parent.maxX + 1, parent.maxY + 1, parent.maxZ + 1);
                                }
                                    dummyFrustums.add(parent);

                            }

                            inFrustum = true;
                            //break;
                        }
                    }
                }

                if (!inFrustum) {

                    for(int i = 0; i < localFrustums.size(); i++){
                        final FrustumBounds frustum = localFrustums.get(i);
                        Minecraft.getMinecraft().renderGlobal.markBlocksForUpdate(frustum.minX - 1, frustum.minY - 1, frustum.minZ - 1, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1);
                    }

                    localFrustums.clear();
                } else {
                    for(int i = 0; i < localFrustums.size(); i++){
                        final FrustumBounds frustum = localFrustums.get(i);
                        if(!dummyFrustums.contains(frustum))
                            Minecraft.getMinecraft().renderGlobal.markBlocksForUpdate(frustum.minX - 1, frustum.minY - 1, frustum.minZ - 1, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1);
                    }

                    localFrustums = dummyFrustums;
                }
            }
        }
    }

    @SubscribeEvent
    public void renderWorldLast(RenderWorldLastEvent e) {
        final EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.8F);
        GL11.glLineWidth(3.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        final double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) e.partialTicks;
        final double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) e.partialTicks;
        final double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) e.partialTicks;

        AxisAlignedBB ab = null;

        if (selectedFrustum == null) {
            if (showAllMainAreas) {
                for (int i = 0; i < frustumBounds.length; i++) {
                    final FrustumBounds frustum = frustumBounds[i];
                    if (frustum == focusedFrustum || frustum == selectedFrustum)
                        GL11.glLineWidth(6.0F);
                    ab = AxisAlignedBB.getBoundingBox(frustum.minX, frustum.minY, frustum.minZ, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1).getOffsetBoundingBox(-d0, -d1, -d2);
                    Minecraft.getMinecraft().renderGlobal.drawOutlinedBoundingBox(ab, -1);
                    GL11.glLineWidth(3.0F);
                }
            }
            if (showAllTriggerAreas || selectedTrigger != null) {
                GL11.glColor4f(1.0F, 1.0F, 0.0F, 0.8F);
                for (int i = 0; i < triggerBounds.length; i++) {
                    final FrustumBounds frustum = triggerBounds[i];
                    if (frustum == focusedTrigger || frustum == selectedTrigger)
                        GL11.glLineWidth(6.0F);
                    ab = AxisAlignedBB.getBoundingBox(frustum.minX, frustum.minY, frustum.minZ, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1).getOffsetBoundingBox(-d0, -d1, -d2);
                    Minecraft.getMinecraft().renderGlobal.drawOutlinedBoundingBox(ab, -1);
                    GL11.glLineWidth(3.0F);
                }
                GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.8F);
            }
        } else {
            GL11.glLineWidth(6.0F);
            ab = AxisAlignedBB.getBoundingBox(selectedFrustum.minX, selectedFrustum.minY, selectedFrustum.minZ, selectedFrustum.maxX + 1, selectedFrustum.maxY + 1, selectedFrustum.maxZ + 1).getOffsetBoundingBox(-d0, -d1, -d2);
            Minecraft.getMinecraft().renderGlobal.drawOutlinedBoundingBox(ab, -1);
            GL11.glLineWidth(3.0F);

            if (showAllTriggerAreas) {
                GL11.glColor4f(1.0F, 1.0F, 0.0F, 0.8F);
                for (int i = 0; i < triggerBounds.length; i++) {

                    final FrustumBounds frustum = triggerBounds[i];

                    boolean isTrigger = false;

                    for(int ii = 0; ii < frustum.parents.length; ii++){
                        final FrustumBounds parent = frustum.parents[ii];
                        if (parent.equalsArea(selectedFrustum)) {
                            isTrigger = true;
                            break;
                        }
                    }

                    if (!isTrigger)
                        continue;

                    if (frustum == focusedTrigger || frustum == selectedTrigger)
                        GL11.glLineWidth(6.0F);
                    ab = AxisAlignedBB.getBoundingBox(frustum.minX, frustum.minY, frustum.minZ, frustum.maxX + 1, frustum.maxY + 1, frustum.maxZ + 1).getOffsetBoundingBox(-d0, -d1, -d2);
                    Minecraft.getMinecraft().renderGlobal.drawOutlinedBoundingBox(ab, -1);
                    GL11.glLineWidth(3.0F);
                }
                GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.8F);
            }

        }

        if (showAllMainAreas || showAllTriggerAreas) {
            if (ItemFrustrator.pos1 != null) {
                GL11.glColor4f(0.0F, 1.0F, 0.0F, 0.8F);
                final Vec3 pos1 = ItemFrustrator.pos1;
                ab = AxisAlignedBB.getBoundingBox(pos1.xCoord, pos1.yCoord, pos1.zCoord, pos1.xCoord + 1, pos1.yCoord + 1, pos1.zCoord + 1).getOffsetBoundingBox(-d0, -d1, -d2);
                Minecraft.getMinecraft().renderGlobal.drawOutlinedBoundingBox(ab, -1);
            }

            if (ItemFrustrator.pos2 != null) {
                GL11.glColor4f(0.0F, 0.0F, 1.0F, 0.8F);
                final Vec3 pos2 = ItemFrustrator.pos2;
                ab = AxisAlignedBB.getBoundingBox(pos2.xCoord, pos2.yCoord, pos2.zCoord, pos2.xCoord + 1, pos2.yCoord + 1, pos2.zCoord + 1).getOffsetBoundingBox(-d0, -d1, -d2);
                Minecraft.getMinecraft().renderGlobal.drawOutlinedBoundingBox(ab, -1);
            }
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}