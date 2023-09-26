package net.jomcraft.frustrator;

import cpw.mods.fml.common.registry.GameRegistry;
import net.jomcraft.frustrator.items.ItemFrustrator;

public class ItemManager {

    public static ItemFrustrator frustrator;

    public static void preInit() {
        frustrator = (ItemFrustrator) new ItemFrustrator().setUnlocalizedName("frustrator");
    }

    public static void init() {
        GameRegistry.registerItem(frustrator, "frustrator");
    }

}