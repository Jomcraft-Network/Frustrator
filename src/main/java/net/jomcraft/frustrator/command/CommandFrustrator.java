package net.jomcraft.frustrator.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.Frustrator;
import net.jomcraft.frustrator.FrustumBounds;
import net.jomcraft.frustrator.items.ItemFrustrator;
import net.jomcraft.frustrator.network.S2CSyncAllAreas;
import net.jomcraft.frustrator.network.S2CSyncChannels;
import net.jomcraft.frustrator.storage.FileManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class CommandFrustrator implements ICommand {

    public CommandFrustrator() {

    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "frustrator";
    }

    public String getCommandUsage(ICommandSender var1) {
        return "/frustrator [args...]";
    }

    @Override
    public List getCommandAliases() {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] argString) {

        if(!(sender instanceof EntityPlayer))
            return;

        if (argString.length == 0) {
            sender.addChatMessage(new ChatComponentText((EnumChatFormatting.RED + getCommandUsage(sender))));
            return;
        }

        if (argString.length == 1) {
            if (argString[0].equals("list")) {
                EntityPlayer player = (EntityPlayer) sender;
                final HashMap<Integer, String> dimMap = getDimMap(player.dimension);
                player.addChatMessage(new ChatComponentTranslation("frustrator.command.list", new Object[0]).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.GREEN)));
                ArrayList<FrustumBounds> frustumList = FileManager.getFrustumJSON().getFrustumMap().get(player.dimension);
                for(Integer id : dimMap.keySet()) {
                    String name = dimMap.get(id);
                    int found = 0;
                    for(FrustumBounds frustum : frustumList){
                        if(frustum.channelID == id)
                            found++;
                    }
                    player.addChatMessage(new ChatComponentText(id.toString() + ": " + name + " [" + found + " area(s)]"));
                }
            } else {
                sender.addChatMessage(new ChatComponentText((EnumChatFormatting.RED + getCommandUsage(sender))));
                return;
            }
        } else if(argString.length == 2){
            EntityPlayer player = (EntityPlayer) sender;

            if(argString[0].equals("remove")) {
                final HashMap<Integer, String> dimMap = getDimMap(player.dimension);

                int id = Integer.parseInt(argString[1]);

                if(dimMap.containsKey(id) && id >= 0 && id != 0) {
                    getDimMap(player.dimension).remove(id);
                    ArrayList<FrustumBounds> bounds = null;
                    if(FileManager.getFrustumJSON().getFrustumMap().containsKey(player.dimension)){
                        bounds = FileManager.getFrustumJSON().getFrustumMap().get(player.dimension);
                        for(FrustumBounds frustum : bounds){
                            if(frustum.channelID == id){
                                frustum.channelID = 0;
                            }
                        }
                    }

                    FileManager.getFrustumJSON().save();
                    if(bounds != null)
                        Frustrator.network.sendToDimension(new S2CSyncAllAreas(bounds.toArray(new FrustumBounds[bounds.size()]), false, null, null), player.dimension);

                    player.addChatMessage(new ChatComponentTranslation("frustrator.command.remove.success", new Object[0]).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.GREEN)));
                    Frustrator.network.sendToDimension(new S2CSyncChannels(dimMap), player.dimension);
                } else {
                    player.addChatMessage(new ChatComponentTranslation("frustrator.command.remove.fail", new Object[0]).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.RED)));
                }

            } else if(argString[0].equals("claim")) {
                if(!player.worldObj.isRemote){
                    final HashMap<Integer, String> dimMap = getDimMap(player.dimension);
                    int id = Integer.parseInt(argString[1]);
                    if(dimMap.containsKey(id)) {
                        if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemFrustrator) {
                            if (player.getHeldItem().hasTagCompound()) {
                                player.getHeldItem().getTagCompound().setInteger("channelID", id);
                            } else {
                                NBTTagCompound tagCompound = new NBTTagCompound();
                                tagCompound.setInteger("channelID", Integer.parseInt(argString[1]));
                                player.getHeldItem().setTagCompound(tagCompound);
                            }
                        }
                    } else {
                        player.addChatMessage(new ChatComponentTranslation("frustrator.command.claim.fail", new Object[0]).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.RED)));
                    }
                }
            } else {
                sender.addChatMessage(new ChatComponentText((EnumChatFormatting.RED + getCommandUsage(sender))));
                return;
            }

            return;
        } else if(argString.length == 3){

            EntityPlayer player = (EntityPlayer) sender;

            if(argString[0].equals("set")) {

                final HashMap<Integer, String> dimMap = getDimMap(player.dimension);

                if(dimMap.containsKey(player.dimension)) {
                    getDimMap(player.dimension).put(Integer.parseInt(argString[1]), argString[2]);
                    FileManager.getFrustumJSON().save();
                    Frustrator.network.sendToDimension(new S2CSyncChannels(dimMap), player.dimension);
                }

            } else if(argString[0].equals("add")) {
                getDimMap(player.dimension).put(Integer.parseInt(argString[1]), argString[2]);
                FileManager.getFrustumJSON().save();
                Frustrator.network.sendToDimension(new S2CSyncChannels(getDimMap(player.dimension)), player.dimension);

            } else {
                sender.addChatMessage(new ChatComponentText((EnumChatFormatting.RED + getCommandUsage(sender))));
                return;
            }
        } else {
            sender.addChatMessage(new ChatComponentText((EnumChatFormatting.RED + getCommandUsage(sender))));
            return;
        }
    }

    public static HashMap<Integer, String> getDimMap(int dim){
        final HashMap<Integer, HashMap<Integer, String>> channels = FileManager.getFrustumJSON().getChannelMap();
        HashMap<Integer, String> dimMap = null;

        if(!channels.containsKey(dim)){
            dimMap = new HashMap<Integer, String>();
            channels.put(dim, dimMap);
        } else {
            dimMap = channels.get(dim);
        }

        return dimMap;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public List addTabCompletionOptions(ICommandSender var1, String[] p_71516_2_) {
        return p_71516_2_.length == 1 ? CommandBase.getListOfStringsMatchingLastWord(p_71516_2_, new String[]{"add", "set", "remove", "list", "claim"}) : null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }
}