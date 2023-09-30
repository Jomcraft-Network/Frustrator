package net.jomcraft.frustrator.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import net.jomcraft.frustrator.Frustrator;
import net.jomcraft.frustrator.FrustumBounds;
import org.apache.logging.log4j.Level;

public class FrustumJSON {

    private String initialVersion;

    private HashMap<Integer, ArrayList<FrustumBounds>> frustumMap = new HashMap<Integer, ArrayList<FrustumBounds>>();

    private HashMap<Integer, HashMap<Integer, String>> channelMap = new HashMap<Integer, HashMap<Integer, String>>();

    public FrustumJSON setInitialVersion(String version) {
        this.initialVersion = version;
        return this;
    }

    public HashMap<Integer, ArrayList<FrustumBounds>> getFrustumMap() {
        return this.frustumMap;
    }

    public HashMap<Integer, HashMap<Integer, String>> getChannelMap() {
        return this.channelMap;
    }

    public String getVersion() {
        return this.initialVersion;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(new File(FileManager.frustratorFolder, "frustums.json"))) {
            FileManager.gson.toJson(this, writer);
        } catch (IOException e) {
            Frustrator.log.log(Level.ERROR, "Exception while saving frustum file: ", e);
        }
    }
}