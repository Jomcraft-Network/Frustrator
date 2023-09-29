package net.jomcraft.frustrator;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.jomcraft.frustrator.network.S2CClearSelection;
import net.jomcraft.frustrator.network.S2CSyncAllAreas;
import net.jomcraft.frustrator.storage.FileManager;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.HashMap;

public class CommonEventHandler {

    @SubscribeEvent
    public void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Frustrator.network.sendTo(new S2CClearSelection(), (EntityPlayerMP) event.player);
        final HashMap<Integer, ArrayList<FrustumBounds>> dimMap = FileManager.getFrustumJSON().getFrustumMap();
        final HashMap<Integer, HashMap<Integer, String>> channelMap = FileManager.getFrustumJSON().getChannelMap();
        if (dimMap.containsKey(event.player.worldObj.provider.dimensionId)) {
            ArrayList<FrustumBounds> bounds = dimMap.get(event.player.worldObj.provider.dimensionId);
            Frustrator.network.sendTo(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), false, null, null), (EntityPlayerMP) event.player);
        } else {
            Frustrator.network.sendTo(new S2CSyncAllAreas(new FrustumBounds[0], false, null, null), (EntityPlayerMP) event.player);
        }

        /*if (channelMap.containsKey(event.player.worldObj.provider.dimensionId)) {
            HashMap<Integer, String> channels = channelMap.get(event.player.worldObj.provider.dimensionId);
            Frustrator.network.sendTo(new S2CSyncChannels(channels));
        }*/
    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        final HashMap<Integer, ArrayList<FrustumBounds>> dimMap = FileManager.getFrustumJSON().getFrustumMap();
        final HashMap<Integer, HashMap<Integer, String>> channelMap = FileManager.getFrustumJSON().getChannelMap();
        Frustrator.network.sendTo(new S2CClearSelection(), (EntityPlayerMP) event.player);
        if (dimMap.containsKey(event.player.worldObj.provider.dimensionId)) {
            ArrayList<FrustumBounds> bounds = dimMap.get(event.player.worldObj.provider.dimensionId);
            Frustrator.network.sendTo(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), false, null, null), (EntityPlayerMP) event.player);
        }

        /*if (channelMap.containsKey(event.player.worldObj.provider.dimensionId)) {
            HashMap<Integer, String> channels = channelMap.get(event.player.worldObj.provider.dimensionId);
            Frustrator.network.sendTo(new S2CSyncChannels(channels));
        }*/
    }
}
