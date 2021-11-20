package me.mundotv.mtblockspot.storange;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegionsJson extends Regions {

    private final File f;
    private boolean saving = false;
    private List<Region> regions;

    public RegionsJson(File f) {
        this.f = f;
    }

    public void saveRegions() throws IOException {
        if (saving) {
            return;
        }
        saving = true;
        Gson gson = new Gson();
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(gson.toJson(regions));
        bw.close();
        saving = false;
    }

    @Override
    public Region getRegionByRadiuns(int x, int z) {
        for (Region r : regions) {
            if (r.inRadiuns(x, z)) {
                return r;
            }
        }
        return null;
    }
    
    @Override
    public Region getRegionByRadiuns(int x, int z, int r) {
        for (Region rs : regions) {
            if (rs.inRadiuns(x, z, r)) {
                return rs;
            }
        }
        return null;
    }

    @Override
    public boolean addRegion(Region r) {
        regions.add(r);
        try {
            saveRegions();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeRegion(Region r) {
        regions.remove(r);
        try {
            saveRegions();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateRegion(Region r) {
        try {
            saveRegions();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean loadRegions() {
        try {
            if (f.exists()) {
                Gson gson = new Gson();
                regions = gson.fromJson(new FileReader(f), new TypeToken<List<Region>>() {
                }.getType());
            } else {
                regions = new ArrayList();
                f.createNewFile();
                saveRegions();
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Region> getRegions(String own) {
        List<Region> rs = new ArrayList();
        regions.stream().filter(r -> (r.getOwn().equals(own))).forEachOrdered(r -> {
            rs.add(r);
        });
        return rs;
    }
}
