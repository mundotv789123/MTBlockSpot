package mundotv.mtblockspot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mundotv.mtblockspot.commands.RegionCommands;
import mundotv.mtblockspot.config.BlockSpot;
import mundotv.mtblockspot.database.RegionDatabase;
import mundotv.mtblockspot.database.RegionJson;
import mundotv.mtblockspot.database.RegionMySQL;
import mundotv.mtblockspot.listener.PlayerListener;
import mundotv.mtblockspot.listener.RegionListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MTMain extends JavaPlugin {

    private RegionDatabase database;
    private List<BlockSpot> blocks;

    public RegionDatabase getDatabase() {
        return database;
    }

    public List<BlockSpot> getBlocks() {
        return blocks;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!this.loadDatabase()) {
            getServer().shutdown();
            return;
        }
        loadBlocks();
        loadCommands();
        loadEvents();
    }

    private void loadEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(database), this);
        pm.registerEvents(new RegionListener(this), this);
        /*List<String> worlds = (getConfig().isList("limits.worlds") ? (List<String>) getConfig().getList("limits.worlds") : new ArrayList());
        int maxw = getConfig().getInt("limits.max_width");
        int minw = getConfig().getInt("limits.min_width");
        if (maxw < minw) {
            maxw = 256;
            minw = 0;
        }
        List<String> farms_blocks = getConfig().getStringList("blocks_exclude.farm");
        List<String> interact_blocks = getConfig().getStringList("blocks_exclude.interact");
        
        pm.registerEvents(new SpotListener(this, worlds, maxw, minw, farms_blocks, interact_blocks), this);*/
    }

    private void loadCommands() {
        PluginCommand bs = getCommand("blockspot");
        if (bs != null) {
            bs.setExecutor(new RegionCommands(this));
        }
    }

    private boolean loadDatabase() {
        ConfigurationSection cs = getConfig().getConfigurationSection("database");
        if (cs == null) {
            return false;
        }
        String type = cs.getString("type");
        if (type == null) {
            return false;
        }
        switch (type.toLowerCase()) {
            case "json":
                this.database = new RegionJson(new File(this.getDataFolder(), cs.getString("json_file")));
                break;
            case "mysql":
                this.database = new RegionMySQL(cs.getString("mysql_database"), cs.getString("mysql_host"), cs.getString("mysql_username"), cs.getString("mysql_password"));
                break;
            default:
                return false;
        }
        try {
            this.database.loadRegions();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private void loadBlocks() {
        this.blocks = new ArrayList();
        ConfigurationSection cs = getConfig().getConfigurationSection("blocks");
        if (cs == null) {
            return;
        }
        cs.getKeys(false).forEach(str -> {
            String mname = cs.getString(str + ".material");
            Material material = mname != null ? Material.valueOf(mname.toUpperCase()) : null;
            if (material != null) {
                BlockSpot bs = new BlockSpot(material, str, cs.getInt(str + ".radius"));
                bs.setDisplayName(cs.isString(str + ".display_name") ? cs.getString(str + ".display_name") : null);
                bs.setLore(cs.isList(str + ".lore") ? (List<String>) cs.getList(str + ".lore") : null);
                this.blocks.add(bs);
            }
        });
    }
}
