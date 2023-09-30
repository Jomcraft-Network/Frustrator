package net.jomcraft.frustrator;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class BypassKeybind
{
    public static KeyBinding bypass;

    public static void register()
    {
        bypass = new KeyBinding("key.frustrator.bypass", Keyboard.KEY_COMMA, "key.categories.frustrator");
        ClientRegistry.registerKeyBinding(bypass);
    }
}