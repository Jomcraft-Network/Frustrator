package net.jomcraft.frustrator;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.jomcraft.frustrator.command.CommandFrustrator;
import net.jomcraft.frustrator.network.*;
import net.jomcraft.frustrator.proxy.ServerProxy;
import net.jomcraft.frustrator.storage.FileManager;
import net.minecraft.command.ICommand;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;

@Mod(modid = Frustrator.MODID, name = Frustrator.NAME, version = Frustrator.VERSION/*,  acceptableRemoteVersions = "*" */ /*dependencies = "required-after:grimoire@[3.2.10,);after:jcbc@[1.0.0,);required-after:journeymap@[5.1.4,);after:tconstruct@[1.8.8,)"*/)
public class Frustrator {

    public static final String MODID = "frustrator";
    public static final String NAME = "Frustrator";
    public static final String VERSION = "F-VERSION";

    @SidedProxy(clientSide = "net.jomcraft.frustrator.proxy.ClientProxy", serverSide = "net.jomcraft.frustrator.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static SimpleNetworkWrapper network = null;
    public static final Logger log = LogManager.getLogger(Frustrator.MODID);

    @Instance
    public static Frustrator instance;

    public Frustrator() {
        instance = this;
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand((ICommand) new CommandFrustrator());
        FileManager.initialize();
    }

    @EventHandler
    public void serverStarting(FMLServerStoppingEvent event) {
        FileManager.deinitialize();
    }

    @EventHandler
    public static void construction(FMLPreInitializationEvent event) {
        CommonEventHandler commonHandler = new CommonEventHandler();
        MinecraftForge.EVENT_BUS.register(commonHandler);
        FMLCommonHandler.instance().bus().register(commonHandler);

        if (event.getSide().isClient()) {
            BypassKeybind.register();
            ClientEventHandler clientHandler = new ClientEventHandler();
            MinecraftForge.EVENT_BUS.register(clientHandler);
            FMLCommonHandler.instance().bus().register(clientHandler);
        }

        ItemManager.preInit();

        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

        network.registerMessage(C2SNewAreaPacket.Handler.class, C2SNewAreaPacket.class, 0, Side.SERVER);
        network.registerMessage(C2SDeleteAreaPacket.Handler.class, C2SDeleteAreaPacket.class, 1, Side.SERVER);
        network.registerMessage(C2SResizeAreaPacket.Handler.class, C2SResizeAreaPacket.class, 2, Side.SERVER);
        network.registerMessage(C2SAddTriggerPacket.Handler.class, C2SAddTriggerPacket.class, 3, Side.SERVER);
        network.registerMessage(C2SChangeChannelPacket.Handler.class, C2SChangeChannelPacket.class, 4, Side.SERVER);
        network.registerMessage(S2CSyncAllAreas.Handler.class, S2CSyncAllAreas.class, 5, Side.CLIENT);
        network.registerMessage(S2CClearSelection.Handler.class, S2CClearSelection.class, 6, Side.CLIENT);
        network.registerMessage(S2CSyncChannels.Handler.class, S2CSyncChannels.class, 7, Side.CLIENT);
    }

    @EventHandler
    public static void init(FMLInitializationEvent event) {
        ItemManager.init();
    }

    public static Frustrator getInstance() {
        return instance;
    }
}