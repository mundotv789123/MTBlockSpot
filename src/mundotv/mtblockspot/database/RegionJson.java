package mundotv.mtblockspot.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mundotv.mtblockspot.config.Region;

public class RegionJson extends RegionDatabase {
    
    private final File jsonFile;
    private boolean saving;

    public RegionJson(File jsonFile) {
        this.jsonFile = jsonFile;
    }
    
    public void saveJson() throws IOException {
        if (this.saving)
            return;
        this.saving = true;
        Gson gson = new Gson();
        BufferedWriter bw = new BufferedWriter(new FileWriter(jsonFile));
        bw.write(gson.toJson(regions));
        bw.close();
        this.saving = false;
    }

    @Override
    public boolean addRegion(Region r) {
        this.regions.add(r);
        try {
            saveJson();
            return true;
        } catch (IOException e) {
            this.regions.remove(r);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeRegion(Region r) {
        this.regions.remove(r);
        try {
            saveJson();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public void loadRegions() throws IOException {
        if (jsonFile.exists()) {
            Gson gson = new Gson();
            this.regions = gson.fromJson(new FileReader(jsonFile), new TypeToken<List<Region>>() {}.getType());
        } else {
            this.regions = new ArrayList();
            saveJson();
        }
    }

    @Override
    public boolean updateRegion(Region r) {
        try {
            saveJson();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
