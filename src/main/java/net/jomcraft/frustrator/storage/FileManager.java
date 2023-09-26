package net.jomcraft.frustrator.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.jomcraft.frustrator.Frustrator;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.Level;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class FileManager {

    public static File worldFolder;
    public static File frustratorFolder;
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static FrustumJSON frustumJson;

    public static void initialize() {

        worldFolder = getCurrentSaveRootDirectory();
        frustratorFolder = new File(worldFolder, "frustrator");

        if (!frustratorFolder.exists()) {
            frustratorFolder.mkdir();
            initJSON();

        } else {
            //READJson
            File frustratorFile = new File(frustratorFolder, "frustums.json");
            if (frustratorFile.exists()) {

                try (Reader reader = new FileReader(frustratorFile)) {
                    frustumJson = gson.fromJson(reader, FrustumJSON.class);

                } catch (Exception e) {
                    Frustrator.log.log(Level.ERROR, "Exception while reading frustum file: ", e);
                    if (e instanceof JsonSyntaxException) {
                        frustratorFile.renameTo(new File(frustratorFolder, "frustums_malformed.json"));
                        initJSON();
                    }
                }

            } else {
                initJSON();
            }
        }

    }

    public static void deinitialize() {
        frustumJson = null;
    }

    public static FrustumJSON getFrustumJSON() {

        if (frustumJson != null)
            return frustumJson;

        File frustratorFile = new File(frustratorFolder, "frustums.json");
        if (frustratorFile.exists()) {

            try (Reader reader = new FileReader(frustratorFile)) {
                frustumJson = gson.fromJson(reader, FrustumJSON.class);

            } catch (Exception e) {
                Frustrator.log.log(Level.ERROR, "Exception while reading frustum file: ", e);
                if (e instanceof JsonSyntaxException) {
                    frustratorFile.renameTo(new File(frustratorFolder, "frustums_malformed.json"));
                    initJSON();
                }
            }

        } else {
            initJSON();
        }

        return frustumJson;
    }

    public static void initJSON() {
        frustumJson = new FrustumJSON().setInitialVersion(Frustrator.VERSION);
        frustumJson.save();
    }

    public static String getMinecraftDirectory() {
        if (MinecraftServer.getServer() != null && MinecraftServer.getServer().isDedicatedServer()) {
            return MinecraftServer.getServer().getFolderName();
        }
        return Minecraft.getMinecraft().mcDataDir.toString();
    }

    public static File getCurrentSaveRootDirectory() {
        if (DimensionManager.getWorld(0) != null) {
            return ((SaveHandler) DimensionManager.getWorld(0).getSaveHandler()).getWorldDirectory();
        } else if (MinecraftServer.getServer() != null) {
            MinecraftServer srv = MinecraftServer.getServer();
            SaveHandler saveHandler = (SaveHandler) srv.getActiveAnvilConverter().getSaveLoader(srv.getFolderName(), false);
            return saveHandler.getWorldDirectory();
        } else {
            return null;
        }
    }

}