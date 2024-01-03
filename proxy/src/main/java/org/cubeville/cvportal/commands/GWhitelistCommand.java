package org.cubeville.cvportal.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;
import org.cubeville.cvplayerdata.PlayerDataManager;
import org.cubeville.cvportal.CVPortal;

import java.util.UUID;

public class GWhitelistCommand extends Command {

    CVPortal plugin;
    PlayerDataManager pdm;

    public GWhitelistCommand(CVPortal plugin, PlayerDataManager pdm) {
        super("gwhitelist", "cvportal.gwhitelist");
        this.plugin = plugin;
        this.pdm = pdm;
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(args.length != 2 && args.length != 3) {
            commandSender.sendMessage(ChatColor.DARK_RED + "Incorrect syntax! Use /gwhitelist <server> <status|on|off|list|add|remove> [player]");
            return;
        }
        ServerInfo server = ProxyServer.getInstance().getServerInfo(args[0].toLowerCase());
        if(server == null) {
            commandSender.sendMessage(ChatColor.DARK_RED + args[0] + " is not a valid server!");
            return;
        }

        if(args[1].equalsIgnoreCase("status")) {
            String status = plugin.serverWhitelistOn(server) ? "on" : "off";
            commandSender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + args[0] + "'s whitelist is set to " + status));
        } else if(args[1].equalsIgnoreCase("list")) {
            if(plugin.serverWhitelistOn(server)) {
                commandSender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + args[0] + "'s whitelisted players:"));
                for(UUID uuid : plugin.getServerWhitelists().get(server)) {
                    if(pdm.getPlayerName(uuid) != null) {
                        commandSender.sendMessage(new TextComponent(ChatColor.GOLD + " - " + pdm.getPlayerName(uuid)));
                    }
                }
            } else {
                commandSender.sendMessage(new TextComponent(ChatColor.RED + args[0] + "'s whitelist is off so there are no whitelisted players!"));
            }
        } else if(args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("off")) {
            if(args[1].equalsIgnoreCase("on")) {
                plugin.setServerWhitelistStatus(server, true);
                commandSender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + args[0] + "'s whitelist is now set to on"));
            } else {
                plugin.setServerWhitelistStatus(server, false);
                commandSender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + args[0] + "'s whitelist is now set to off"));
            }
        } else if(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
            if(plugin.serverWhitelistOn(server)) {
                if(pdm.getPlayerId(args[2]) != null) {
                    if(args[1].equalsIgnoreCase("add")) {
                        plugin.addPlayerToServerWhitelist(server, pdm.getPlayerId(args[2]));
                        commandSender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + args[2] + " has been added to " + args[0] + "'s whitelist"));
                    } else {
                        plugin.removePlayerToServerWhitelist(server, pdm.getPlayerId(args[2]));
                        commandSender.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + args[2] + " has been removed from " + args[0] + "'s whitelist"));
                    }
                } else {
                    commandSender.sendMessage(new TextComponent(ChatColor.RED + args[1] + " is an invalid player name"));
                }
            } else {
                commandSender.sendMessage(new TextComponent(ChatColor.RED + args[0] + "'s whitelist is off so you cannot add or remove players!"));
            }
        } else {
            commandSender.sendMessage(ChatColor.DARK_RED + "Incorrect syntax! Use /gwhitelist <server> <status|on|off|list|add|remove> [player]");
        }
    }
}
