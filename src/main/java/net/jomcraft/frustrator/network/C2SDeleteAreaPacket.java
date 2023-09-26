package net.jomcraft.frustrator.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.jomcraft.frustrator.Frustrator;
import net.jomcraft.frustrator.FrustumBounds;
import net.jomcraft.frustrator.storage.FileManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Vec3;
import java.util.ArrayList;
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
                }

                if (!trigger) {
                    for (int i = 0; i < bounds.size(); i++) {
                        final FrustumBounds frustum = bounds.get(i);

                        if (frustum.trigger && frustum.parent.minX == message.min.xCoord && frustum.parent.minY == message.min.yCoord && frustum.parent.minZ == message.min.zCoord && frustum.parent.maxX == message.max.xCoord && frustum.parent.maxY == message.max.yCoord && frustum.parent.maxZ == message.max.zCoord) {
                            toRemoveTriggers.add(frustum);
                        }
                    }

                    for (FrustumBounds frustum : toRemoveTriggers)
                        bounds.remove(frustum);
                }

                if (index != null || !trigger) {
                    FileManager.getFrustumJSON().save();
                    Frustrator.network.sendToAll(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), true, message.min, message.max));
                }

            }
            return null;
        }
    }
}