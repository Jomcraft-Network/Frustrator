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
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class C2SNewAreaPacket implements IMessage {

    private Vec3 pos1;
    private Vec3 pos2;
    private int channelID;

    @Nullable
    private FrustumBounds parent;

    public C2SNewAreaPacket() {

    }

    public C2SNewAreaPacket(Vec3 pos1, Vec3 pos2, FrustumBounds parent, int channelID) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.parent = parent;
        this.channelID = channelID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos1 = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        this.pos2 = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        if (buf.readBoolean())
            this.parent = new FrustumBounds(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), false, null, -1);
        this.channelID = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt((int) this.pos1.xCoord);
        buf.writeInt((int) this.pos1.yCoord);
        buf.writeInt((int) this.pos1.zCoord);

        buf.writeInt((int) this.pos2.xCoord);
        buf.writeInt((int) this.pos2.yCoord);
        buf.writeInt((int) this.pos2.zCoord);

        buf.writeBoolean(this.parent == null ? false : true);

        if (this.parent != null) {
            buf.writeInt(this.parent.minX);
            buf.writeInt(this.parent.minY);
            buf.writeInt(this.parent.minZ);

            buf.writeInt(this.parent.maxX);
            buf.writeInt(this.parent.maxY);
            buf.writeInt(this.parent.maxZ);
        }
        buf.writeInt(this.channelID);
    }

    public static class Handler implements IMessageHandler<C2SNewAreaPacket, IMessage> {
        @Override
        public IMessage onMessage(C2SNewAreaPacket message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final HashMap<Integer, ArrayList<FrustumBounds>> dimMap = FileManager.getFrustumJSON().getFrustumMap();

            ArrayList<FrustumBounds> bounds;

            final int minX = Math.min((int) message.pos1.xCoord, (int) message.pos2.xCoord);
            final int minY = Math.min((int) message.pos1.yCoord, (int) message.pos2.yCoord);
            final int minZ = Math.min((int) message.pos1.zCoord, (int) message.pos2.zCoord);

            final int maxX = Math.max((int) message.pos1.xCoord, (int) message.pos2.xCoord);
            final int maxY = Math.max((int) message.pos1.yCoord, (int) message.pos2.yCoord);
            final int maxZ = Math.max((int) message.pos1.zCoord, (int) message.pos2.zCoord);

            if(!FileManager.getFrustumJSON().getChannelMap().get(player.dimension).containsKey(message.channelID)){
                player.addChatMessage(new ChatComponentTranslation("frustrator.create.fail.channel", new Object[]{(message.parent == null ? "main" : "trigger")}).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.YELLOW)));
                return null;
            }

            if (dimMap.containsKey(player.worldObj.provider.dimensionId)) {
                bounds = dimMap.get(player.worldObj.provider.dimensionId);
                Integer index = null;

                for (int i = 0; i < bounds.size(); i++) {
                    FrustumBounds frustum = bounds.get(i);
                    if (frustum.minX == minX && frustum.minY == minY && frustum.minZ == minZ && frustum.maxX == maxX && frustum.maxY == maxY && frustum.maxZ == maxZ) {
                        index = i;
                        break;
                    }
                }

                if (index == null) {
                    bounds.add(new FrustumBounds(minX, minY, minZ, maxX, maxY, maxZ, message.parent == null ? false : true, new FrustumBounds[]{message.parent}, message.channelID));
                    player.addChatMessage(new ChatComponentTranslation("frustrator.create.success", new Object[]{(message.parent == null ? "main" : "trigger")}).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.GREEN)));

                    FileManager.getFrustumJSON().save();
                    Frustrator.network.sendToDimension(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), true, Vec3.createVectorHelper(minX, minY, minZ), Vec3.createVectorHelper(maxX, maxY, maxZ)), player.dimension);
                } else {
                    player.addChatMessage(new ChatComponentTranslation("frustrator.create.fail", new Object[]{(message.parent == null ? "main" : "trigger")}).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.YELLOW)));
                }
            } else {
                bounds = new ArrayList<FrustumBounds>();
                bounds.add(new FrustumBounds(minX, minY, minZ, maxX, maxY, maxZ, message.parent == null ? false : true, new FrustumBounds[]{message.parent}, message.channelID));
                player.addChatMessage(new ChatComponentTranslation("frustrator.create.success", new Object[]{(message.parent == null ? "main" : "trigger")}).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.GREEN)));
                dimMap.put(player.worldObj.provider.dimensionId, bounds);
                FileManager.getFrustumJSON().save();
                Frustrator.network.sendToDimension(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), true, Vec3.createVectorHelper(minX, minY, minZ), Vec3.createVectorHelper(maxX, maxY, maxZ)), player.dimension);
            }
            return null;
        }
    }
}