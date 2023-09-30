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
import java.util.Arrays;
import java.util.HashMap;

public class C2SDeleteAreaPacket implements IMessage {

    private Vec3 min;
    private Vec3 max;

    public C2SDeleteAreaPacket() {

    }

    public C2SDeleteAreaPacket(Vec3 min, Vec3 max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.min = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        this.max = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt((int) min.xCoord);
        buf.writeInt((int) min.yCoord);
        buf.writeInt((int) min.zCoord);

        buf.writeInt((int) max.xCoord);
        buf.writeInt((int) max.yCoord);
        buf.writeInt((int) max.zCoord);
    }

    public static class Handler implements IMessageHandler<C2SDeleteAreaPacket, IMessage> {
        @Override
        public IMessage onMessage(C2SDeleteAreaPacket message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            final HashMap<Integer, ArrayList<FrustumBounds>> dimMap = FileManager.getFrustumJSON().getFrustumMap();

            if (dimMap.containsKey(player.worldObj.provider.dimensionId)) {
                ArrayList<FrustumBounds> bounds = dimMap.get(player.worldObj.provider.dimensionId);

                Integer index = null;
                boolean trigger = false;
                final ArrayList<FrustumBounds> toRemoveTriggers = new ArrayList<FrustumBounds>();

                for (int i = 0; i < bounds.size(); i++) {
                    FrustumBounds frustum = bounds.get(i);

                    if (frustum.minX == message.min.xCoord && frustum.minY == message.min.yCoord && frustum.minZ == message.min.zCoord && frustum.maxX == message.max.xCoord && frustum.maxY == message.max.yCoord && frustum.maxZ == message.max.zCoord) {
                        index = i;
                        trigger = frustum.trigger;
                        break;
                    }
                }

                if (index != null) {
                    bounds.remove((int) index);
                    player.addChatMessage(new ChatComponentTranslation("frustrator.delete.success", new Object[]{(trigger ? "trigger" : "main")}).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.GREEN)));
                } else {
                    player.addChatMessage(new ChatComponentTranslation("frustrator.delete.fail", new Object[0]).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.RED)));
                }

                if (!trigger) {
                    for (int i = 0; i < bounds.size(); i++) {
                        final FrustumBounds frustum = bounds.get(i);
                        if (frustum.parents.length < 2) {
                            for (int ii = 0; ii < frustum.parents.length; ii++) {
                                final FrustumBounds parent = frustum.parents[ii];
                                if (frustum.trigger && parent.minX == message.min.xCoord && parent.minY == message.min.yCoord && parent.minZ == message.min.zCoord && parent.maxX == message.max.xCoord && parent.maxY == message.max.yCoord && parent.maxZ == message.max.zCoord) {
                                    toRemoveTriggers.add(frustum);
                                }
                            }
                        } else {
                            ArrayList<FrustumBounds> removeParents = getFrustumBounds(message, frustum);
                            ArrayList<FrustumBounds> originalParents = new ArrayList<FrustumBounds>(Arrays.asList(frustum.parents));
                            for (FrustumBounds toRemove : removeParents) {
                                originalParents.remove(toRemove);
                            }
                            frustum.parents = originalParents.toArray(new FrustumBounds[originalParents.size()]);

                        }
                    }

                    for (FrustumBounds frustum : toRemoveTriggers)
                        bounds.remove(frustum);
                }

                if (index != null || !trigger) {
                    FileManager.getFrustumJSON().save();
                    Frustrator.network.sendToDimension(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), true, message.min, message.max), player.dimension);
                }

            }
            return null;
        }

        private static ArrayList<FrustumBounds> getFrustumBounds(C2SDeleteAreaPacket message, FrustumBounds frustum) {
            final ArrayList<FrustumBounds> removeParents = new ArrayList<FrustumBounds>();
            for (int ii = 0; ii < frustum.parents.length; ii++) {
                final FrustumBounds parent = frustum.parents[ii];
                if (frustum.trigger && parent.minX == message.min.xCoord && parent.minY == message.min.yCoord && parent.minZ == message.min.zCoord && parent.maxX == message.max.xCoord && parent.maxY == message.max.yCoord && parent.maxZ == message.max.zCoord) {
                    removeParents.add(parent);
                }
            }
            return removeParents;
        }
    }
}