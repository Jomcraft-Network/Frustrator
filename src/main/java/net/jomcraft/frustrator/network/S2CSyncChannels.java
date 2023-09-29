package net.jomcraft.frustrator.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.FrustumBounds;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;

public class S2CSyncChannels implements IMessage {

    private HashMap<Integer, String> channelMap;

    public S2CSyncChannels() {

    }

    public S2CSyncChannels(HashMap<Integer, String> channelMap) {
        this.channelMap = channelMap;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        final int channelMapSize = buf.readInt();
        final HashMap<Integer, String> channelMap = new HashMap<Integer, String>();
        for (int x = 0; x < channelMapSize; x++) {
            int index = buf.readInt();
            String tag = ByteBufUtils.readUTF8String(buf);
            channelMap.put(index, tag);
        }

        this.channelMap = channelMap;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.channelMap.size());
        for(Integer index : this.channelMap.keySet()){
            buf.writeInt(index);
            ByteBufUtils.writeUTF8String(buf, this.channelMap.get(index));
        }
    }

    public static class Handler implements IMessageHandler<S2CSyncChannels, IMessage> {
        @Override
        public IMessage onMessage(S2CSyncChannels message, MessageContext ctx) {
            System.out.println("A: " + message.channelMap.size());
            ClientEventHandler.channelMap = message.channelMap;

            return null;
        }
    }
}