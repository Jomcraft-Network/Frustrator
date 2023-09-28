package net.jomcraft.frustrator.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.Frustrator;
import net.jomcraft.frustrator.FrustumBounds;
import net.jomcraft.frustrator.storage.FileManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class C2SAddTriggerPacket implements IMessage {

    private Vec3 triggerMin;
    private Vec3 triggerMax;
    private Vec3 mainMin;
    private Vec3 mainMax;

    public C2SAddTriggerPacket() {

    }

    public C2SAddTriggerPacket(Vec3 triggerMin, Vec3 triggerMax, Vec3 mainMin, Vec3 mainMax) {
        this.triggerMin = triggerMin;
        this.triggerMax = triggerMax;
        this.mainMin = mainMin;
        this.mainMax = mainMax;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.triggerMin = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        this.triggerMax = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());

        this.mainMin = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        this.mainMax = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt((int) this.triggerMin.xCoord);
        buf.writeInt((int) this.triggerMin.yCoord);
        buf.writeInt((int) this.triggerMin.zCoord);

        buf.writeInt((int) this.triggerMax.xCoord);
        buf.writeInt((int) this.triggerMax.yCoord);
        buf.writeInt((int) this.triggerMax.zCoord);

        buf.writeInt((int) this.mainMin.xCoord);
        buf.writeInt((int) this.mainMin.yCoord);
        buf.writeInt((int) this.mainMin.zCoord);

        buf.writeInt((int) this.mainMax.xCoord);
        buf.writeInt((int) this.mainMax.yCoord);
        buf.writeInt((int) this.mainMax.zCoord);
    }

    public static class Handler implements IMessageHandler<C2SAddTriggerPacket, IMessage> {
        @Override
        public IMessage onMessage(C2SAddTriggerPacket message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final HashMap<Integer, ArrayList<FrustumBounds>> dimMap = FileManager.getFrustumJSON().getFrustumMap();

            if (dimMap.containsKey(player.worldObj.provider.dimensionId)) {
                ArrayList<FrustumBounds> bounds = dimMap.get(player.worldObj.provider.dimensionId);
                final int triggerMinX = (int) message.triggerMin.xCoord;
                final int triggerMinY = (int) message.triggerMin.yCoord;
                final int triggerMinZ = (int) message.triggerMin.zCoord;

                final int triggerMaxX = (int) message.triggerMax.xCoord;
                final int triggerMaxY = (int) message.triggerMax.yCoord;
                final int triggerMaxZ = (int) message.triggerMax.zCoord;


                final int mainMinX = (int) message.mainMin.xCoord;
                final int mainMinY = (int) message.mainMin.yCoord;
                final int mainMinZ = (int) message.mainMin.zCoord;

                final int mainMaxX = (int) message.mainMax.xCoord;
                final int mainMaxY = (int) message.mainMax.yCoord;
                final int mainMaxZ = (int) message.mainMax.zCoord;

                Integer index = null;
                boolean trigger = false;
                FrustumBounds[] parents = null;

                for (int i = 0; i < bounds.size(); i++) {
                    final FrustumBounds frustum = bounds.get(i);

                    if (frustum.minX == triggerMinX && frustum.minY == triggerMinY && frustum.minZ == triggerMinZ && frustum.maxX == triggerMaxX && frustum.maxY == triggerMaxY && frustum.maxZ == triggerMaxZ) {
                        index = i;
                        trigger = frustum.trigger;
                        parents = frustum.parents;
                        break;
                    }
                }

                if (index != null) {
                    if (parents == null)
                        parents = new FrustumBounds[0];

                    ArrayList<FrustumBounds> parentList = new ArrayList<FrustumBounds>(Arrays.asList(parents));
                    boolean alreadyContained = false;
                    final FrustumBounds newFrustum = new FrustumBounds(mainMinX, mainMinY, mainMinZ, mainMaxX, mainMaxY, mainMaxZ, false, null);
                    for (FrustumBounds parent : parentList) {
                        if (parent.equalsArea(newFrustum)) {
                            alreadyContained = true;
                            break;
                        }
                    }
                    if (!alreadyContained) {
                        parentList.add(newFrustum);
                        bounds.set((int) index, new FrustumBounds(triggerMinX, triggerMinY, triggerMinZ, triggerMaxX, triggerMaxY, triggerMaxZ, trigger, parentList.toArray(new FrustumBounds[parentList.size()])));
                        player.addChatMessage(new ChatComponentText("Successfully linked the clicked main area to the selected trigger area. To unlink please delete the trigger area").setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.GREEN)));
                    } else {
                        player.addChatMessage(new ChatComponentText("This main area and trigger area are already linked!").setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.YELLOW)));
                    }
                } else {
                    player.addChatMessage(new ChatComponentText("No area has been modified").setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.RED)));
                }

                FileManager.getFrustumJSON().save();

                Frustrator.network.sendToAll(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), false, null, null));
            }
            return null;
        }
    }
}