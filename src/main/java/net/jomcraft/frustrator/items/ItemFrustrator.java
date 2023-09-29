package net.jomcraft.frustrator.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.Frustrator;
import net.jomcraft.frustrator.network.*;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.command.CommandHelp;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public class ItemFrustrator extends Item {

    public static final String[] itemNames = new String[]{"mainArea", "triggerArea"};

    public static Vec3 pos1 = null;
    public static Vec3 pos2 = null;

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public ItemFrustrator() {
        this.maxStackSize = 1;
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(CreativeTabs.tabTools);
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        if (entityLiving.worldObj.isRemote) {
            if (ClientEventHandler.selectedFrustum != null && stack.getItemDamage() == 0) {
                if (entityLiving.isSneaking()) {
                    Frustrator.network.sendToServer(new C2SDeleteAreaPacket(Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.minX, ClientEventHandler.selectedFrustum.minY, ClientEventHandler.selectedFrustum.minZ), Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.maxX, ClientEventHandler.selectedFrustum.maxY, ClientEventHandler.selectedFrustum.maxZ)));
                    ClientEventHandler.focusedFrustum = null;
                }
                ClientEventHandler.selectedFrustum = null;
                ClientEventHandler.selectedTrigger = null;

            } else if (ClientEventHandler.selectedTrigger != null && stack.getItemDamage() == 1) {
                if (entityLiving.isSneaking()) {
                    Frustrator.network.sendToServer(new C2SDeleteAreaPacket(Vec3.createVectorHelper(ClientEventHandler.selectedTrigger.minX, ClientEventHandler.selectedTrigger.minY, ClientEventHandler.selectedTrigger.minZ), Vec3.createVectorHelper(ClientEventHandler.selectedTrigger.maxX, ClientEventHandler.selectedTrigger.maxY, ClientEventHandler.selectedTrigger.maxZ)));
                    ClientEventHandler.focusedTrigger = null;
                }
                ClientEventHandler.selectedTrigger = null;
            }

            if (ClientEventHandler.selectedFrustum != null) {
                pos1 = Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.minX, ClientEventHandler.selectedFrustum.minY, ClientEventHandler.selectedFrustum.minZ);
                pos2 = Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.maxX, ClientEventHandler.selectedFrustum.maxY, ClientEventHandler.selectedFrustum.maxZ);
            } else {
                pos1 = null;
                pos2 = null;
            }
        }
        return super.onEntitySwing(entityLiving, stack);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (worldIn.isRemote) {
            MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(worldIn, player, true);
            if (movingobjectposition == null) {
                if (itemStackIn.getItemDamage() == 0) {
                    if (ClientEventHandler.focusedFrustum == null) {

                        if (!player.isSneaking()) {

                            if (pos1 != null && pos2 != null) {

                                if (ClientEventHandler.selectedFrustum != null) {

                                    final Vec3 oldPos1 = Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.minX, ClientEventHandler.selectedFrustum.minY, ClientEventHandler.selectedFrustum.minZ);
                                    final Vec3 oldPos2 = Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.maxX, ClientEventHandler.selectedFrustum.maxY, ClientEventHandler.selectedFrustum.maxZ);
                                    ClientEventHandler.selectedFrustum = null;
                                    Frustrator.network.sendToServer(new C2SResizeAreaPacket(pos1, pos2, oldPos1, oldPos2));

                                } else {
                                    int channelID = 0;
                                    if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemFrustrator && player.getHeldItem().hasTagCompound() && player.getHeldItem().getTagCompound().hasKey("channelID")){
                                        channelID = player.getHeldItem().getTagCompound().getInteger("channelID");
                                    }
                                    Frustrator.network.sendToServer(new C2SNewAreaPacket(pos1, pos2, null, channelID));
                                }

                                pos1 = null;
                                pos2 = null;
                            }
                        }
                    } else {
                        if (ClientEventHandler.focusedFrustum != null && ClientEventHandler.selectedFrustum == null && ClientEventHandler.selectedTrigger != null) {
                            if(ClientEventHandler.focusedFrustum.channelID == ClientEventHandler.currentChannelID && ClientEventHandler.selectedTrigger.channelID == ClientEventHandler.currentChannelID)
                                Frustrator.network.sendToServer(new C2SAddTriggerPacket(Vec3.createVectorHelper(ClientEventHandler.selectedTrigger.minX, ClientEventHandler.selectedTrigger.minY, ClientEventHandler.selectedTrigger.minZ), Vec3.createVectorHelper(ClientEventHandler.selectedTrigger.maxX, ClientEventHandler.selectedTrigger.maxY, ClientEventHandler.selectedTrigger.maxZ), Vec3.createVectorHelper(ClientEventHandler.focusedFrustum.minX, ClientEventHandler.focusedFrustum.minY, ClientEventHandler.focusedFrustum.minZ), Vec3.createVectorHelper(ClientEventHandler.focusedFrustum.maxX, ClientEventHandler.focusedFrustum.maxY, ClientEventHandler.focusedFrustum.maxZ)));
                        }

                        if(!player.isSneaking()) {
                            ClientEventHandler.selectedFrustum = ClientEventHandler.focusedFrustum;
                            pos1 = Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.minX, ClientEventHandler.selectedFrustum.minY, ClientEventHandler.selectedFrustum.minZ);
                            pos2 = Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.maxX, ClientEventHandler.selectedFrustum.maxY, ClientEventHandler.selectedFrustum.maxZ);
                        } else {
                            Frustrator.network.sendToServer(new C2SChangeChannelPacket(ClientEventHandler.currentChannelID, Vec3.createVectorHelper(ClientEventHandler.focusedFrustum.minX, ClientEventHandler.focusedFrustum.minY, ClientEventHandler.focusedFrustum.minZ), Vec3.createVectorHelper(ClientEventHandler.focusedFrustum.maxX, ClientEventHandler.focusedFrustum.maxY, ClientEventHandler.focusedFrustum.maxZ)));
                        }
                        }

                    return itemStackIn;
                } else if (itemStackIn.getItemDamage() == 1) {
                    if (ClientEventHandler.focusedTrigger == null) {
                        if (!player.isSneaking()) {

                            if (pos1 != null && pos2 != null) {

                                if (ClientEventHandler.selectedTrigger != null) {
                                    final Vec3 oldPos1 = Vec3.createVectorHelper(ClientEventHandler.selectedTrigger.minX, ClientEventHandler.selectedTrigger.minY, ClientEventHandler.selectedTrigger.minZ);
                                    final Vec3 oldPos2 = Vec3.createVectorHelper(ClientEventHandler.selectedTrigger.maxX, ClientEventHandler.selectedTrigger.maxY, ClientEventHandler.selectedTrigger.maxZ);
                                    ClientEventHandler.selectedTrigger = null;
                                    Frustrator.network.sendToServer(new C2SResizeAreaPacket(pos1, pos2, oldPos1, oldPos2));

                                } else {
                                    if (ClientEventHandler.selectedFrustum == null) {
                                        player.addChatMessage(new ChatComponentTranslation("frustrator.trigger.noParent", new Object[0]).setChatStyle(ClientEventHandler.style.setColor(EnumChatFormatting.RED)));
                                    } else {
                                        int channelID = 0;
                                        if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemFrustrator && player.getHeldItem().hasTagCompound() && player.getHeldItem().getTagCompound().hasKey("channelID")){
                                            channelID = player.getHeldItem().getTagCompound().getInteger("channelID");
                                        }
                                        Frustrator.network.sendToServer(new C2SNewAreaPacket(pos1, pos2, ClientEventHandler.selectedFrustum, channelID));
                                    }
                                }

                                if (ClientEventHandler.selectedFrustum != null) {
                                    pos1 = Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.minX, ClientEventHandler.selectedFrustum.minY, ClientEventHandler.selectedFrustum.minZ);
                                    pos2 = Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.maxX, ClientEventHandler.selectedFrustum.maxY, ClientEventHandler.selectedFrustum.maxZ);
                                } else {
                                    pos1 = null;
                                    pos2 = null;
                                }
                            }

                        }
                    } else {
                        if(!player.isSneaking()) {
                            ClientEventHandler.selectedTrigger = ClientEventHandler.focusedTrigger;
                            pos1 = Vec3.createVectorHelper(ClientEventHandler.selectedTrigger.minX, ClientEventHandler.selectedTrigger.minY, ClientEventHandler.selectedTrigger.minZ);
                            pos2 = Vec3.createVectorHelper(ClientEventHandler.selectedTrigger.maxX, ClientEventHandler.selectedTrigger.maxY, ClientEventHandler.selectedTrigger.maxZ);
                        } else {
                            //LINK!!
                            Frustrator.network.sendToServer(new C2SChangeChannelPacket(ClientEventHandler.currentChannelID, Vec3.createVectorHelper(ClientEventHandler.focusedTrigger.minX, ClientEventHandler.focusedTrigger.minY, ClientEventHandler.focusedTrigger.minZ), Vec3.createVectorHelper(ClientEventHandler.focusedTrigger.maxX, ClientEventHandler.focusedTrigger.maxY, ClientEventHandler.focusedTrigger.maxZ)));
                        }
                        }
                }
            } else {
                if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {

                    int x = movingobjectposition.blockX;
                    int y = movingobjectposition.blockY;
                    int z = movingobjectposition.blockZ;

                    if (!player.isSneaking()) {
                        pos1 = Vec3.createVectorHelper(x, y, z);
                    } else {
                        pos2 = Vec3.createVectorHelper(x, y, z);
                    }

                }
            }
        }
        return itemStackIn;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        int j = MathHelper.clamp_int(damage, 0, 1);
        return this.icons[j];
    }

    public String getUnlocalizedName(ItemStack stack) {
        int i = MathHelper.clamp_int(stack.getItemDamage(), 0, 1);
        return super.getUnlocalizedName() + "." + itemNames[i];
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
        for (int i = 0; i < 2; ++i) {
            p_150895_3_.add(new ItemStack(p_150895_1_, 1, i));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.icons = new IIcon[itemNames.length];

        for (int i = 0; i < itemNames.length; ++i) {
            this.icons[i] = register.registerIcon("frustrator:frustrator" + "_" + itemNames[i]);
        }
    }

    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
        super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
        p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.rightClick.onBlock") + " " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("frustrator.rightClick.firstMarker"));
        p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.rightClick.shiftOnBlock") + " " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("frustrator.rightClick.secondMarker"));

        if (p_77624_1_.getItemDamage() == 0) {
            if (ClientEventHandler.focusedFrustum == null) {
                if (pos1 != null && pos2 != null) {
                    if (ClientEventHandler.selectedFrustum != null) {
                        p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.rightClick.onAir") + " " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("frustrator.confirm.resizeMainArea"));
                    } else {
                        p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.rightClick.onAir") + " " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("frustrator.confirm.creationMainArea"));
                    }
                }

            } else {
                if (ClientEventHandler.focusedFrustum != null && ClientEventHandler.selectedFrustum == null && ClientEventHandler.selectedTrigger != null) {
                    p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.rightClick.onMainArea") + " " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("frustrator.confirm.linking"));
                } else {
                    p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.rightClick.onMainArea") + " " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("frustrator.select.mainArea"));
                }
            }

        } else if (p_77624_1_.getItemDamage() == 1) {
            if (ClientEventHandler.focusedTrigger == null) {

                if (pos1 != null && pos2 != null) {

                    if (ClientEventHandler.selectedTrigger != null) {
                        p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.rightClick.onAir") + " " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("frustrator.confirm.resizeTriggerArea"));
                    } else {
                        if (ClientEventHandler.selectedFrustum != null) {
                            p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.rightClick.onAir") + " " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("frustrator.confirm.creationTriggerArea"));
                        }
                    }
                }

            } else {
                p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.rightClick.onTriggerArea") + " " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("frustrator.select.triggerArea"));
            }
        }

        if (ClientEventHandler.selectedFrustum != null && p_77624_1_.getItemDamage() == 0) {
            p_77624_3_.add("");
            p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.leftClick.onAir") + " " + EnumChatFormatting.AQUA + StatCollector.translateToLocal("frustrator.deselect.mainArea"));
            p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.leftClick.shiftOnAir") + " " + EnumChatFormatting.AQUA + StatCollector.translateToLocal("frustrator.delete.mainArea"));

        } else if (ClientEventHandler.selectedTrigger != null && p_77624_1_.getItemDamage() == 1) {
            p_77624_3_.add("");
            p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.leftClick.onAir") + " " + EnumChatFormatting.AQUA + StatCollector.translateToLocal("frustrator.deselect.triggerArea"));
            p_77624_3_.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("frustrator.leftClick.shiftOnAir") + " " + EnumChatFormatting.AQUA + StatCollector.translateToLocal("frustrator.delete.triggerArea"));
        }

        p_77624_3_.add("");
        int channelID = 0;
        if(p_77624_1_.hasTagCompound() && p_77624_1_.getTagCompound().hasKey("channelID")){
            channelID = p_77624_1_.getTagCompound().getInteger("channelID");
        }

        String channelTag = "UNKNOWN";

        if(ClientEventHandler.channelMap != null && ClientEventHandler.channelMap.containsKey(channelID))
            channelTag = ClientEventHandler.channelMap.get(channelID);

        p_77624_3_.add(EnumChatFormatting.RED + StatCollector.translateToLocal("frustrator.channelID") + " " + EnumChatFormatting.RESET + channelTag + " (" + channelID + ")");
    }
}