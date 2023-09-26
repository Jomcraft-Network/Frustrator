package net.jomcraft.frustrator.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.items.ItemFrustrator;

public class S2CClearSelection implements IMessage {

    public S2CClearSelection() {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class Handler implements IMessageHandler<S2CClearSelection, IMessage> {
        @Override
        public IMessage onMessage(S2CClearSelection message, MessageContext ctx) {
            ClientEventHandler.selectedFrustum = null;
            ClientEventHandler.selectedTrigger = null;
            ItemFrustrator.pos1 = null;
            ItemFrustrator.pos2 = null;

            return null;
        }
    }
}