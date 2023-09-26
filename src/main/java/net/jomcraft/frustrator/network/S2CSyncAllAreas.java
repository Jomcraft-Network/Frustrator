package net.jomcraft.frustrator.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.FrustumBounds;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;
import java.util.ArrayList;

public class S2CSyncAllAreas implements IMessage {

    private FrustumBounds[] frustums;
    private boolean shouldUpdate;
    private Vec3 min;
    private Vec3 max;

    public S2CSyncAllAreas() {

    }

    public S2CSyncAllAreas(FrustumBounds[] frustums, boolean shouldUpdate, Vec3 min, Vec3 max) {
        this.frustums = frustums;
        this.shouldUpdate = shouldUpdate;
        this.min = min;
        this.max = max;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        final int frustumsSize = buf.readInt();
        final FrustumBounds[] frustums = new FrustumBounds[frustumsSize];

        for (int x = 0; x < frustumsSize; x++) {
            final int minX = buf.readInt();
            final int minY = buf.readInt();
            final int minZ = buf.readInt();
            final int maxX = buf.readInt();
            final int maxY = buf.readInt();
            final int maxZ = buf.readInt();
            final boolean trigger = buf.readBoolean();
            FrustumBounds parent = null;
            if (trigger)
                parent = new FrustumBounds(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), false, null);

            frustums[x] = new FrustumBounds(minX, minY, minZ, maxX, maxY, maxZ, trigger, parent);
        }

        this.frustums = frustums;
        this.shouldUpdate = buf.readBoolean();
        if (this.shouldUpdate) {
            this.min = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
            this.max = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.frustums.length);
        for (FrustumBounds frustum : this.frustums) {
            buf.writeInt(frustum.minX);
            buf.writeInt(frustum.minY);
            buf.writeInt(frustum.minZ);
            buf.writeInt(frustum.maxX);
            buf.writeInt(frustum.maxY);
            buf.writeInt(frustum.maxZ);
            buf.writeBoolean(frustum.trigger);
            if (frustum.trigger) {
                buf.writeInt(frustum.parent.minX);
                buf.writeInt(frustum.parent.minY);
                buf.writeInt(frustum.parent.minZ);
                buf.writeInt(frustum.parent.maxX);
                buf.writeInt(frustum.parent.maxY);
                buf.writeInt(frustum.parent.maxZ);
            }
        }
        buf.writeBoolean(this.shouldUpdate);
        if (this.shouldUpdate) {
            buf.writeInt((int) min.xCoord);
            buf.writeInt((int) min.yCoord);
            buf.writeInt((int) min.zCoord);

            buf.writeInt((int) max.xCoord);
            buf.writeInt((int) max.yCoord);
            buf.writeInt((int) max.zCoord);
        }
    }

    public static class Handler implements IMessageHandler<S2CSyncAllAreas, IMessage> {
        @Override
        public IMessage onMessage(S2CSyncAllAreas message, MessageContext ctx) {
            final FrustumBounds[] frustums = message.frustums;
            final ArrayList<FrustumBounds> newFrustumList = new ArrayList<FrustumBounds>();
            final ArrayList<FrustumBounds> triggerList = new ArrayList<FrustumBounds>();
            for (int i = 0; i < frustums.length; i++) {
                final FrustumBounds frustum = frustums[i];
                if (frustum.trigger) {
                    triggerList.add(frustum);
                } else {
                    newFrustumList.add(frustum);
                }
            }

            ClientEventHandler.frustumBounds = newFrustumList.toArray(new FrustumBounds[newFrustumList.size()]);
            ClientEventHandler.triggerBounds = triggerList.toArray(new FrustumBounds[triggerList.size()]);

            if (message.shouldUpdate) {
                final int minX = (int) message.min.xCoord;
                final int minY = (int) message.min.yCoord;
                final int minZ = (int) message.min.zCoord;

                final int maxX = (int) message.max.xCoord;
                final int maxY = (int) message.max.yCoord;
                final int maxZ = (int) message.max.zCoord;
                Minecraft.getMinecraft().renderGlobal.markBlocksForUpdate(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1);
            }

            return null;
        }
    }
}