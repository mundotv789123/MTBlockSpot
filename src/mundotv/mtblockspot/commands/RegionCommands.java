package mundotv.mtblockspot.commands;

import mundotv.mtblockspot.MTMain;
import mundotv.mtblockspot.config.BlockSpot;
import mundotv.mtblockspot.config.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class RegionCommands implements CommandExecutor {

    private final MTMain main;

    public RegionCommands(MTMain main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        if (sender == null) {
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            switch (args[0]) {
                case "give":
                    if (!sender.hasPermission("mtblockspot.admin.give")) {
                        sender.sendMessage("§cSem permissão!");
                        break;
                    }
                    if (args.length != 3) {
                        sender.sendMessage("/bs give <player> <name>");
                        break;
                    }
                    Player p = Bukkit.getPlayer(args[1]);
                    if (p == null) {
                        sender.sendMessage("§cJogador " + args[1] + "não encontrado!");
                        break;
                    }
                    give(p, args[2]);
                    break;
                default:
                    sender.sendMessage("§cVocê não pode usar esse comando!");
            }
            return true;
        }

        Player p = (Player) sender;
        switch (args[0]) {
            case "off":
                updateStatus(p, false);
                break;
            case "on":
                updateStatus(p, false);
                break;
            case "give":
                if (!p.hasPermission("mtblockspot.admin.give")) {
                    p.sendMessage("§cSem permissão!");
                    break;
                }
                if (args.length != 2) {
                    p.sendMessage("/bs give <name>");
                    break;
                }
                give(p, args[1]);
                break;
            case "pvp":
                if (args.length != 2 || (!args[1].equals("on") && !args[1].equals("off"))) {
                    p.sendMessage("/bs pvp <on/off>");
                    break;
                }
                pvp(p, args[1].equals("on"));
                break;
            case "farm":
                if (args.length != 2 || (!args[1].equals("on") && !args[1].equals("off"))) {
                    p.sendMessage("/bs farm <on/off>");
                    break;
                }
                farm(p, args[1].equals("on"));
                break;
            case "add":
                if (args.length != 2) {
                    p.sendMessage("/bs add <player>");
                    break;
                }
                updatePlayers(p, args[1], true);
                break;
            case "remove":
                if (args.length != 2) {
                    p.sendMessage("/bs remove <player>");
                    break;
                }
                updatePlayers(p, args[1], false);
                break;
            case "trace":
                trace(p);
                break;
            default:
                sendHelp(p);
        }
        return true;
    }

    public void sendHelp(CommandSender s) {
        String msg = "\n\n"
                + "§e=====================================================\n\n"
                + "§eTodos os comandos que você tem acesso.\n"
                + "§eComandos para membro normal:\n"
                + "§f/bs pvp <on/off> §e- Ativa ou Desativa o PVP no seu terreno.\n"
                + "§f/bs farm <on/off> §e- Permitir que jogadores conseguir pegar plantação\n"
                + "§f/bs add <jogador> §e- Adicionar amigo no seu terreno\n"
                + "§f/bs remove <jogador> §e- Remove amigo no seu terreno\n"
                + "§f/bs trace §e- Visualizar área de proteção\n";
        if (s.hasPermission("mtblockspot.admin")) {
            msg += "\n§eComandos para admin:\n"
                    + "§f/bs admin §e- Conseguir quebrar terrenos dos jogadores\n"
                    + "§f/bs give <jogador> <bloco> §e- Da bloco personalizado para o jogador\n"
                    + "§f/bs reload §e- Recarregar configurações\n";
        }
        msg += "\n§e=====================================================";
        s.sendMessage(msg);
    }

    public void pvp(Player p, boolean on) {
        Location loc = p.getLocation();
        Region region = main.getDatabase().getRegionByRadius(loc.getBlockX(), loc.getBlockZ(), 0, loc.getWorld().getName());
        if (region == null) {
            p.sendMessage("§cVocê não está em nenhum terreno");
            return;
        }
        if (!region.getOwn().equals(p.getName())) {
            p.sendMessage("§cVocê não é dono desse terreno");
            return;
        }
        region.getOptions().setPvp(on);
        main.getDatabase().updateRegion(region);
        p.sendMessage("pvp " + (on ? "" : "des") + "ativado com sucesso");
    }

    public void farm(Player p, boolean on) {
        Location loc = p.getLocation();
        Region region = main.getDatabase().getRegionByRadius(loc.getBlockX(), loc.getBlockZ(), 0, loc.getWorld().getName());
        if (region == null) {
            p.sendMessage("§cVocê não está em nenhum terreno");
            return;
        }
        if (!region.getOwn().equals(p.getName())) {
            p.sendMessage("§cVocê não é dono desse terreno");
            return;
        }
        region.getOptions().setFarm(on);
        main.getDatabase().updateRegion(region);
        p.sendMessage("farm " + (on ? "" : "des") + "ativado com sucesso");
    }

    public void give(Player p, String name) {
        for (BlockSpot bs : main.getBlocks()) {
            if (bs.getName().equals(name)) {
                p.getInventory().addItem(bs.getItem());
                return;
            }
        }
        p.sendMessage("§cBloco não encontrado");
    }

    public void updatePlayers(Player p, String p2, boolean add) {
        Location loc = p.getLocation();
        Region region = main.getDatabase().getRegionByRadius(loc.getBlockX(), loc.getBlockZ(), 0, loc.getWorld().getName());
        if (region == null) {
            p.sendMessage("§cVocê não está em nenhum terreno");
            return;
        }
        if (!region.getOwn().equals(p.getName())) {
            p.sendMessage("§cVocê não é dono desse terreno");
            return;
        }
        if (add) {
            if (Bukkit.getPlayer(p2) == null) {
                p.sendMessage("§cEsse player não foi encontrado no servidor!");
                return;
            }
            if (region.getPlayers().contains(p2)) {
                p.sendMessage("§cEsse player já foi adicionado!");
                return;
            }
            if (Bukkit.getPlayer(p2) == null) {
                p.sendMessage("§cEsse player não está online!");
                return;
            }
            region.getPlayers().add(p2);
            main.getDatabase().updateRegion(region);
            p.sendMessage("§aPlayer adicionado com sucesso!");
        } else {
            if (!region.getPlayers().contains(p2)) {
                p.sendMessage("§cEsse player já foi removido!");
                return;
            }
            region.getPlayers().remove(p2);
            main.getDatabase().updateRegion(region);
            p.sendMessage("§aPlayer removido com sucesso!");
        }
    }

    public void updateStatus(Player p, boolean enable) {
        if (enable) {
            if (p.hasMetadata("mtspotblock-disabled")) {
                p.removeMetadata("mtspotblock-disabled", main);
                p.sendMessage("§aAtivado!");
                return;
            }
            p.sendMessage("§cJá ativado!");
        } else {
            if (!p.hasMetadata("mtspotblock-disabled")) {
                p.setMetadata("mtspotblock-disabled", new FixedMetadataValue(main, 1));
                p.sendMessage("§aDesativado!");
                return;
            }
            p.sendMessage("§cJá desativado!");
        }
    }

    public void trace(Player p) {
        Location loc = p.getLocation();
        Region region = main.getDatabase().getRegionByRadius(loc.getBlockX(), loc.getBlockZ(), 0, loc.getWorld().getName());
        if (region == null) {
            if (!Region.removeTraceRadiuns(p, main)) {
                p.sendMessage("§cVocê não está em nenhum terreno");
            }
            return;
        }
        region.traceRadiuns(p, main);
        p.sendMessage("§aÁrea de proteção visível");
    }
}
