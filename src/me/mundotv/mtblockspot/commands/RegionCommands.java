package me.mundotv.mtblockspot.commands;

import me.mundotv.mtblockspot.utils.Messages;
import java.util.List;
import me.mundotv.mtblockspot.storange.Region;
import me.mundotv.mtblockspot.storange.Regions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class RegionCommands implements CommandExecutor {

    private final Plugin plugin;
    private final Regions regions;

    public RegionCommands(Plugin plugin, Regions regions) {
        this.plugin = plugin;
        this.regions = regions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        if (sender == null) {
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCommand from console is disabled");
            return true;
        }
        Player p = (Player) sender;
        if (args.length <= 0 || args[0].equals("help")) {
            help(p);
            return true;
        }
        switch (args[0]) {
            case "info":
                if (args.length >= 2 && args[1].equals("all")) {
                    //info(p);
                } else {
                    Region r = regions.getRegionByRadiuns(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
                    if (r != null) {
                        info(p, r);
                    } else {
                        p.sendMessage(Messages.OUT_REGION.getMessage());
                    }
                }
                break;
            case "add":
                if (args.length < 2) {
                    p.sendMessage(Messages.WRONG_COMMAND.getMessage().replace("{usage}", "/bs add <nick>"));
                    break;
                }
                if (args.length >= 3 && args[2].equals("all")) {
                    //code
                } else {
                    Region r = regions.getRegionByRadiuns(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
                    if (r == null) {
                        p.sendMessage(Messages.OUT_REGION.getMessage());
                        break;
                    }
                    add(p, r, args[1]);
                }
                break;
            case "remove":
                if (args.length < 2) {
                    p.sendMessage(Messages.WRONG_COMMAND.getMessage().replace("{usage}", "/bs remove <nick>"));
                    break;
                }
                if (args.length >= 3 && args[2].equals("all")) {
                    //code
                } else {
                    Region r = regions.getRegionByRadiuns(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
                    if (r == null) {
                        p.sendMessage(Messages.OUT_REGION.getMessage());
                        break;
                    }
                    remove(p, r, args[1]);
                }
                break;
            case "farm":
                //code
                break;
            case "pvp":
                //code
                break;
            case "on":
                if (p.hasMetadata("mtspotblock-disabled")) {
                    p.removeMetadata("mtspotblock-disabled", plugin);
                    p.sendMessage(Messages.PROTECT_ON.getMessage());
                }
                break;
            case "off":
                if (!p.hasMetadata("mtspotblock-disabled")) {
                    p.setMetadata("mtspotblock-disabled", new FixedMetadataValue(plugin, 1));
                    p.sendMessage(Messages.PROTECT_OFF.getMessage());
                }
                break;
            default:
                help(p);
        }
        return true;
    }

    public void remove(Player p, Region r, String nick) {
        if (!r.getOwn().equals(p.getName())) {
            p.sendMessage("§cVocê não é o dono desse terreno");
            return;
        }
        if (Bukkit.getPlayer(nick) == null) {
            p.sendMessage("§cPlayer não encontrado ou offline");
            return;
        }
        if (r.getOwn().equals(nick)) {
            p.sendMessage("§cVocê é o dono do terreno");
            return;
        }
        if (!r.getPlayers().contains(nick)) {
            p.sendMessage("§cEsse player já foi removido");
            return;
        }
        r.getPlayers().remove(nick);
        if (!regions.updateRegion(r)) {
            r.getPlayers().add(nick);
            p.sendMessage(Messages.ERROR.getMessage());
            return;
        }
        p.sendMessage("§aPlayer removido com sucesso!");
    }

    public void remove(Player p, String nick) {

    }

    public void add(Player p, Region r, String nick) {
        if (!r.getOwn().equals(p.getName())) {
            p.sendMessage("§cVocê não é o dono desse terreno");
            return;
        }
        if (Bukkit.getPlayer(nick) == null) {
            p.sendMessage("§cPlayer não encontrado ou offline");
            return;
        }
        if (r.getOwn().equals(nick)) {
            p.sendMessage("§cVocê é o dono do terreno");
            return;
        }
        if (r.getPlayers().contains(nick)) {
            p.sendMessage("§cEsse player já foi adicionado");
            return;
        }
        r.getPlayers().add(nick);
        if (!regions.updateRegion(r)) {
            r.getPlayers().remove(nick);
            p.sendMessage(Messages.ERROR.getMessage());
            return;
        }
        p.sendMessage("§aPlayer adicionando com sucesso!");
    }

    public void info(Player p, Region r) {
        String players;
        if (r.getPlayers().isEmpty()) {
            players = "§cnenhum player";
        } else {
            players = "§e[ §a";
            players = r.getPlayers().stream().map(s -> s).reduce(players, String::concat);
            players += "§e ]";
        }
        p.sendMessage("§e-----------------\n"
                + "§eLocal: §ax: " + r.getPosX() + ", y: " + r.getPosY() + ", z: " + r.getPosZ() + "\n"
                + "§eRaio: §a" + r.getPosR() + "\n"
                + "§ePvp: " + (r.getOptions().isPvp() ? "§aon" : "§coff") + "\n"
                + "§eFarm: " + (r.getOptions().isFarm() ? "§aon" : "§coff") + "\n"
                + "§eDono: §a" + r.getOwn() + "\n"
                + "§ePlayers: " + players + "\n"
                + "§e-----------------");
    }

    public void info(Player p) {
        List<Region> rs = regions.getRegions(p.getName());
        if (rs.isEmpty()) {
            p.sendMessage("§cVocê não tem nenhum terreno");
        } else {
            rs.forEach((r) -> {
                info(p, r);
            });
        }
    }

    public void help(Player p) {
        p.sendMessage("§e====================[MTBlockSpot]=====================\n"
                + "§e/bs info §aInformações do seu terreno atual\n"
                /*+ "§e/bs info all §aInformações de todos os seus terrenos\n"*/
                + "§e/bs <add/remove> <nick> §aAdicionar ou remover um membro do seu terreno atual\n"
                + "§e/bs <add/remove> all <nick> §aAdicionar ou remover um membro de todos os seus terrenos\n"
                + "§e/bs farm <on/off> §aAtivar ou desativar coleita de plantação do seu terreno atual\n"
                + "§e/bs pvp <on/off> §aAtivar ou desativar pvp do seu terreno atual\n"
                + "§e/bs <off/on> §aAitvar ou desativar proteção ao colocar bloco\n"
                + "§e/bs help §aListar todos os comandos\n"
                + "§e====================================================");
    }
}
