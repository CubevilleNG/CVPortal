package org.cubeville.cvportal.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.cubeville.cvplayerdata.playerdata.PlayerDataManager;
import org.cubeville.cvportal.CVPortal;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ExceptionsCommand extends Command {

    CVPortal plugin;
    PlayerDataManager pdm;

    public ExceptionsCommand(CVPortal plugin, PlayerDataManager pdm) {
        super("exceptions", "cvportal.exceptions");
        this.plugin = plugin;
        this.pdm = pdm;
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("list")) {
                player.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + player.getName() + "'s TP Exceptions:"));
                if(plugin.getTpExceptions().containsKey(player.getUniqueId())) {
                    for(UUID uuid : plugin.getTpExceptions().get(player.getUniqueId())) {
                        if(pdm.getPlayerName(uuid) != null) {
                            TextComponent out = new TextComponent(ChatColor.GOLD + " - " + pdm.getPlayerName(uuid));
                            TextComponent click = new TextComponent(" §c(-)");
                            click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/exceptions remove  " + pdm.getPlayerName(uuid)));
                            click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Remove " + pdm.getPlayerName(uuid))));
                            out.addExtra(click);
                            player.sendMessage(out);
                        }
                    }
                }
            } else {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /exceptions <list|add|remove> [player name]"));
            }
        } else if(args.length == 2) {
            if(args[0].equalsIgnoreCase("add")) {
                if(pdm.getPlayerId(args[1]) != null) {
                    Map<UUID, Set<UUID>> tpExceptions = plugin.getTpExceptions();
                    Set<UUID> currentExceptions = new HashSet<>();
                    if(tpExceptions.get(player.getUniqueId()) != null) {
                        currentExceptions = tpExceptions.get(player.getUniqueId());
                    }
                    currentExceptions.add(pdm.getPlayerId(args[1]));
                    tpExceptions.put(player.getUniqueId(), currentExceptions);
                    plugin.setTpExceptions(tpExceptions);
                    plugin.saveTpExceptions();
                    player.sendMessage(new TextComponent(ChatColor.GREEN + args[1] + " added to your tp exceptions list"));
                } else {
                    player.sendMessage(new TextComponent(ChatColor.RED + args[1] + " is an invalid player name"));
                }
            } else if(args[0].equalsIgnoreCase("remove")) {
                if(pdm.getPlayerId(args[1]) != null) {
                    Map<UUID, Set<UUID>> tpExceptions = plugin.getTpExceptions();
                    Set<UUID> currentExceptions = new HashSet<>();
                    if(tpExceptions.get(player.getUniqueId()) != null) {
                        currentExceptions = tpExceptions.get(player.getUniqueId());
                    }
                    currentExceptions.remove(pdm.getPlayerId(args[1]));
                    tpExceptions.put(player.getUniqueId(), currentExceptions);
                    plugin.setTpExceptions(tpExceptions);
                    plugin.saveTpExceptions();
                    player.sendMessage(new TextComponent(ChatColor.GREEN + args[1] + " removed from your tp exceptions list"));
                } else {
                    player.sendMessage(new TextComponent(ChatColor.RED + args[1] + " is an invalid player name"));
                }
            } else {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /exceptions <list|add|remove> [player name]"));
            }
        } else {
            player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /exceptions <list|add|remove> [player name]"));
        }
    }
}
