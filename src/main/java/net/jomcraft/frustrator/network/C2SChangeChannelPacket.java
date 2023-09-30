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
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;

public class C2SChangeChannelPacket implements IMessage {

    private int channelID;
    private Vec3 min;
    private Vec3 max;

    public C2SChangeChannelPacket() {

    }

    public C2SChangeChannelPacket(int channelID, Vec3 min, Vec3 max) {
        this.channelID = channelID;
        this.min = min;
        this.max = max;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.channelID = buf.readInt();
        this.min = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        this.max = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.channelID);
        buf.writeInt((int) this.min.xCoord);
        buf.writeInt((int) this.min.yCoord);
        buf.writeInt((int) this.min.zCoord);

        buf.writeInt((int) this.max.xCoord);
        buf.writeInt((int) this.max.yCoord);
        buf.writeInt((int) this.max.zCoord);
    }

    public static class Handler implements IMessageHandler<C2SChangeChannelPacket, IMessage> {
        @Override
        public IMessage onMessage(C2SChangeChannelPacket message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final HashMap<Integer, ArrayList<FrustumBounds>> dimMap = FileManager.getFrustumJSON().getFrustumMap();

            if (dimMap.containsKey(player.worldObj.provider.dimensionId)) {
                ArrayList<FrustumBounds> bounds = dimMap.get(player.worldObj.provider.dimensionId);
                final int minX = (int) message.min.xCoord;
                final int minY = (int) message.min.yCoord;
                final int minZ = (int) message.min.zCoord;

                final int maxX = (int) message.max.xCoord;
                final int maxY = (int) message.max.yCoord;
                final int maxZ = (int) message.max.zCoord;

                Integer index = null;
                boolean trigger = false;
                FrustumBounds[] parents = null;

                for (int i = 0; i < bounds.size(); i++) {
                    final FrustumBounds frustum = bounds.get(i);

                    if (frustum.minX == minX && frustum.minY == minY && frustum.minZ == minZ && frustum.maxX == maxX && frustum.maxY == maxY && frustum.maxZ == maxZ) {
                        index = i;
                        trigger = frustum.trigger;
                        parents = frustum.parents;
                        break;
                    }
                }

                if (index != null) {
                    HashMap<Integer, String> map = FileManager.getFrustumJSON().getChannelMap().get(player.worldObj.provider.dimensionId);
                    if (map != null && map.containsKey(message.channelID)) {
                        bounds.set((int) index, new FrustumBounds(minX, minY, minZ, maxX, maxY, maxZ, trigger, parents, message.channelID));
                        player.addChatMessage(new ChatComponentTranslation("frustrator.channel.success", new Object[]{(trigger ? "trigger" : "main"), (map.get(message.channelID) + " (" + message.channelID + ")")}).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.GREEN)));
                    } else {
                        player.addChatMessage(new ChatComponentTranslation("frustrator.channel.missing", new Object[]{(String.valueOf(message.channelID))}).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.YELLOW)));
                    }

                } else {
                    player.addChatMessage(new ChatComponentTranslation("frustrator.channel.fail", new Object[]{(trigger ? "trigger" : "main")}).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.RED)));
                }

                FileManager.getFrustumJSON().save();
                Frustrator.network.sendToDimension(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), true, Vec3.createVectorHelper(minX, minY, minZ), Vec3.createVectorHelper(maxX, maxY, maxZ)), player.dimension);
            }

            return null;
        }
    }
}