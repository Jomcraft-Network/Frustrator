package net.jomcraft.frustrator;

import cpw.mods.fml.common.registry.GameRegistry;
import net.jomcraft.frustrator.items.ItemFrustrator;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ItemManager {

    public static ItemFrustrator frustrator;

    public static void preInit() {
        frustrator = (ItemFrustrator) new ItemFrustrator().setUnlocalizedName("frustrator");
    }

    public static void init() {
        GameRegistry.registerItem(frustrator, "frustrator");

        GameRegistry.addShapedRecipe(new ItemStack(frustrator, 1, 0), "GTB", "RWR", " C ", 'T', Blocks.redstone_torch, 'G', new ItemStack(Items.dye, 1, 10), 'B', new ItemStack(Items.dye, 1, 4), 'R', new ItemStack(Items.redstone), 'W', Blocks.glass_pane, 'C', new ItemStack(Items.comparator));
        GameRegistry.addShapedRecipe(new ItemStack(frustrator, 1, 1), "PTY", "RWR", " C ", 'T', Blocks.redstone_torch, 'P', new ItemStack(Items.dye, 1, 5), 'Y', new ItemStack(Items.dye, 1, 11), 'R', new ItemStack(Items.redstone), 'W', Blocks.glass_pane, 'C', new ItemStack(Items.comparator));
    }

}