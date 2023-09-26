package net.jomcraft.frustrator.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.jomcraft.frustrator.ClientEventHandler;
import net.jomcraft.frustrator.Frustrator;
import net.jomcraft.frustrator.network.C2SDeleteAreaPacket;
import net.jomcraft.frustrator.network.C2SNewAreaPacket;
import net.jomcraft.frustrator.network.C2SResizeAreaPacket;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
                                    Frustrator.network.sendToServer(new C2SNewAreaPacket(pos1, pos2, null));
                                }

                                pos1 = null;
                                pos2 = null;
                            }
                        }
                    } else {
                        ClientEventHandler.selectedFrustum = ClientEventHandler.focusedFrustum;
                        pos1 = Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.minX, ClientEventHandler.selectedFrustum.minY, ClientEventHandler.selectedFrustum.minZ);
                        pos2 = Vec3.createVectorHelper(ClientEventHandler.selectedFrustum.maxX, ClientEventHandler.selectedFrustum.maxY, ClientEventHandler.selectedFrustum.maxZ);
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
                                    if (ClientEventHandler.selectedFrustum == null)
                                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "No parent main area is selected!"));
                                    else
                                        Frustrator.network.sendToServer(new C2SNewAreaPacket(pos1, pos2, ClientEventHandler.selectedFrustum));
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
                        ClientEventHandler.selectedTrigger = ClientEventHandler.focusedTrigger;
                        pos1 = Vec3.createVectorHelper(ClientEventHandler.selectedTrigger.minX, ClientEventHandler.selectedTrigger.minY, ClientEventHandler.selectedTrigger.minZ);
                        pos2 = Vec3.createVectorHelper(ClientEventHandler.selectedTrigger.maxX, ClientEventHandler.selectedTrigger.maxY, ClientEventHandler.selectedTrigger.maxZ);
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
        p_77624_3_.add(new ChatComponentText(EnumChatFormatting.GOLD + "Right Click on block: " + EnumChatFormatting.AQUA + "First marker").getUnformattedTextForChat());
        p_77624_3_.add(new ChatComponentText(EnumChatFormatting.GOLD + "SHIFT + Right Click on block: " + EnumChatFormatting.AQUA + "Second marker").getUnformattedTextForChat());
        p_77624_3_.add(new ChatComponentText("").getUnformattedTextForChat());
        p_77624_3_.add(new ChatComponentText(EnumChatFormatting.GOLD + "SHIFT + Right Click on air: " + EnumChatFormatting.AQUA + "Confirm area modification/creation").getUnformattedTextForChat());
    }
}