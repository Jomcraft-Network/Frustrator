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
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;

public class C2SResizeAreaPacket implements IMessage {

    private Vec3 pos1;
    private Vec3 pos2;
    private Vec3 oldPos1;
    private Vec3 oldPos2;

    public C2SResizeAreaPacket() {

    }

    public C2SResizeAreaPacket(Vec3 pos1, Vec3 pos2, Vec3 oldPos1, Vec3 oldPos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.oldPos1 = oldPos1;
        this.oldPos2 = oldPos2;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos1 = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        this.pos2 = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());

        this.oldPos1 = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        this.oldPos2 = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt((int) this.pos1.xCoord);
        buf.writeInt((int) this.pos1.yCoord);
        buf.writeInt((int) this.pos1.zCoord);

        buf.writeInt((int) this.pos2.xCoord);
        buf.writeInt((int) this.pos2.yCoord);
        buf.writeInt((int) this.pos2.zCoord);

        buf.writeInt((int) this.oldPos1.xCoord);
        buf.writeInt((int) this.oldPos1.yCoord);
        buf.writeInt((int) this.oldPos1.zCoord);

        buf.writeInt((int) this.oldPos2.xCoord);
        buf.writeInt((int) this.oldPos2.yCoord);
        buf.writeInt((int) this.oldPos2.zCoord);
    }

    public static class Handler implements IMessageHandler<C2SResizeAreaPacket, IMessage> {
        @Override
        public IMessage onMessage(C2SResizeAreaPacket message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final HashMap<Integer, ArrayList<FrustumBounds>> dimMap = FileManager.getFrustumJSON().getFrustumMap();

            if (dimMap.containsKey(player.worldObj.provider.dimensionId)) {
                ArrayList<FrustumBounds> bounds = dimMap.get(player.worldObj.provider.dimensionId);
                final int minX = Math.min((int) message.pos1.xCoord, (int) message.pos2.xCoord);
                final int minY = Math.min((int) message.pos1.yCoord, (int) message.pos2.yCoord);
                final int minZ = Math.min((int) message.pos1.zCoord, (int) message.pos2.zCoord);

                final int maxX = Math.max((int) message.pos1.xCoord, (int) message.pos2.xCoord);
                final int maxY = Math.max((int) message.pos1.yCoord, (int) message.pos2.yCoord);
                final int maxZ = Math.max((int) message.pos1.zCoord, (int) message.pos2.zCoord);

                Integer index = null;
                boolean trigger = false;
                int channelID = -1;
                FrustumBounds[] parents = null;

                for (int i = 0; i < bounds.size(); i++) {
                    final FrustumBounds frustum = bounds.get(i);

                    if (frustum.minX == message.oldPos1.xCoord && frustum.minY == message.oldPos1.yCoord && frustum.minZ == message.oldPos1.zCoord && frustum.maxX == message.oldPos2.xCoord && frustum.maxY == message.oldPos2.yCoord && frustum.maxZ == message.oldPos2.zCoord) {
                        index = i;
                        trigger = frustum.trigger;
                        parents = frustum.parents;
                        channelID = frustum.channelID;
                        break;
                    }
                }

                if (index != null) {
                    bounds.set((int) index, new FrustumBounds(minX, minY, minZ, maxX, maxY, maxZ, trigger, parents, channelID));
                    player.addChatMessage(new ChatComponentTranslation("frustrator.resize.success", new Object[]{(trigger ? "trigger" : "main")}).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.GREEN)));
                } else {
                    player.addChatMessage(new ChatComponentTranslation("frustrator.resize.fail", new Object[0]).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.RED)));
                }

                if (!trigger) {
                    for (int i = 0; i < bounds.size(); i++) {
                        final FrustumBounds frustum = bounds.get(i);
                        for (int ii = 0; ii < frustum.parents.length; ii++) {
                            final FrustumBounds parent = frustum.parents[ii];
                            if (frustum.trigger && parent.minX == message.oldPos1.xCoord && parent.minY == message.oldPos1.yCoord && parent.minZ == message.oldPos1.zCoord && parent.maxX == message.oldPos2.xCoord && parent.maxY == message.oldPos2.yCoord && parent.maxZ == message.oldPos2.zCoord) {
                                frustum.parents[ii] = new FrustumBounds(minX, minY, minZ, maxX, maxY, maxZ, false, null, -1);
                            }
                        }
                    }
                }

                FileManager.getFrustumJSON().save();

                Frustrator.network.sendToDimension(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), true, Vec3.createVectorHelper(minX, minY, minZ), Vec3.createVectorHelper(maxX, maxY, maxZ)), player.dimension);
            }

            return null;
        }
    }
}