package me.mundotv.mtblockspot;

import me.mundotv.mtblockspot.commands.RegionCommands;
import me.mundotv.mtblockspot.utils.ProtectBlock;
import me.mundotv.mtblockspot.utils.Messages;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.mundotv.mtblockspot.events.RegionListener;
import me.mundotv.mtblockspot.storange.Regions;
import me.mundotv.mtblockspot.storange.RegionsJson;
import me.mundotv.mtblockspot.storange.RegionsSQL;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    public Main(Main instance) {
        Main.instance = instance;
    }
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Messages.config = getConfig();
        Regions regions = loadRegions();
        if (!regions.loadRegions()) {
            Bukkit.shutdown();
            return;
        }
        List<ProtectBlock> blocks = loadBlocks();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new RegionListener(regions, blocks), this);
        getCommand("blockspot").setExecutor(new RegionCommands(this, regions));
    }

    private Regions loadRegions() {
        if (getConfig().getBoolean("mysql.enabled")) {
            return new RegionsSQL(getConfig().getString("mysql.username"), getConfig().getString("mysql.host"), getConfig().getString("mysql.password"), getConfig().getString("mysql.database"));
        } else {
            return new RegionsJson(new File(getDataFolder(), "regions.json"));
        }
    }

    private List<ProtectBlock> loadBlocks() {
        List<ProtectBlock> blocks = new ArrayList();
        ConfigurationSection cs = this.getConfig().getConfigurationSection("blocks");
        if (cs != null) {
            cs.getKeys(false).forEach(b -> {
                String block_id = this.getConfig().getString("blocks." + b + ".block_id");
                Material blockId = block_id == null ? null : Material.getMaterial(block_id.toUpperCase());
                if (blockId != null) {
                    String message = this.getConfig().getString("blocks." + b + ".message_place");
                    String permission = this.getConfig().getString("blocks." + b + ".permission");
                    String name = this.getConfig().getString("blocks." + b + ".name");
                    List<String> lore = (List<String>)this.getConfig().getList("blocks." + b + ".lore");
                    int radiuns = this.getConfig().getInt("blocks." + b + ".radiuns");
                    blocks.add(new ProtectBlock(blockId, message, permission, name, lore, radiuns));
                }
            });
        }
        return blocks;
    }
    
    public static Main getInstance() {
        return Main.instance;
    }
}
