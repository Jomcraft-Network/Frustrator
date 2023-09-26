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
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class C2SNewAreaPacket implements IMessage {

    private Vec3 pos1;
    private Vec3 pos2;

    @Nullable
    private FrustumBounds parent;

    public C2SNewAreaPacket() {

    }

    public C2SNewAreaPacket(Vec3 pos1, Vec3 pos2, FrustumBounds parent) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.parent = parent;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos1 = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        this.pos2 = Vec3.createVectorHelper(buf.readInt(), buf.readInt(), buf.readInt());
        if (buf.readBoolean())
            this.parent = new FrustumBounds(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), false, null);
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
                    bounds.add(new FrustumBounds(minX, minY, minZ, maxX, maxY, maxZ, message.parent == null ? false : true, message.parent));
                    FileManager.getFrustumJSON().save();
                    Frustrator.network.sendToAll(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), true, Vec3.createVectorHelper(minX, minY, minZ), Vec3.createVectorHelper(maxX, maxY, maxZ)));
                }
            } else {
                bounds = new ArrayList<FrustumBounds>();
                bounds.add(new FrustumBounds(minX, minY, minZ, maxX, maxY, maxZ, message.parent == null ? false : true, message.parent));

                dimMap.put(player.worldObj.provider.dimensionId, bounds);
                FileManager.getFrustumJSON().save();
                Frustrator.network.sendToAll(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), true, Vec3.createVectorHelper(minX, minY, minZ), Vec3.createVectorHelper(maxX, maxY, maxZ)));
            }
            return null;
        }
    }
}