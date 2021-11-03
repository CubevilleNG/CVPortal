package org.cubeville.cvportal.commands;

import java.util.*;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvportal.warps.WarpManager;


public class WarpCommand extends Command
{
    private WarpManager warpManager;

    private List<String> warpPrefixes = Arrays.asList("e_", "g_", "q_", "s_");
    private List<ChatColor> warpPrefixColours = Arrays.asList(ChatColor.YELLOW, ChatColor.DARK_AQUA, ChatColor.GOLD, ChatColor.GREEN, ChatColor.GRAY); // List must contain one extra item with the default colour

    public WarpCommand(WarpManager warpManager) {
        super("warp");
        this.warpManager = warpManager;
    }

    public void execute(CommandSender commandSender, String[] args) {

        if(args.length >= 1) {
            if(args[0].equals("delete")) {
                if(!commandSender.hasPermission("cvportal.warp.delete")) {
                    commandSender.sendMessage("§cNo permission.");
                    return;
                }
                if(args.length != 2) {
                    commandSender.sendMessage("§c/warp delete <warp>");
                    return;
                }
                String warp = args[1];
                if(warpManager.warpExists(warp)) {
                    warpManager.delete(warp);
                    commandSender.sendMessage("§aWarp deleted.");
                }
                else {
                    commandSender.sendMessage("§cWarp does not exist!");
                }
                return;
            }
            if(args[0].equals("rename")) {
                if(!commandSender.hasPermission("cvportal.warp.rename")) {
                    commandSender.sendMessage("§cNo permission.");
                    return;
                }
                if(args.length != 3) {
                    commandSender.sendMessage("§c/warp rename <old> <new>");
                    return;
                }
                String old = args[1];
                String neww = args[2];
                if(!warpManager.warpExists(old)) {
                    commandSender.sendMessage("§cWarp does not exist!");
                }
                else if(warpManager.warpExists(neww)) {
                    commandSender.sendMessage("§cWarp with that name already exists!");
                }
                else {
                    warpManager.rename(old, neww);
                    commandSender.sendMessage("§aWarp renamed.");
                }
                return;
            }
            if(args[0].equals("list")) {
                if(args.length >= 2 && (!(commandSender.hasPermission("cvportal.warp.listfiltered")))) {
                    commandSender.sendMessage("§cNo permission.");
                    return;
                }
                String server = null;
                String world = null;
                if(args.length == 2) {
                    server = args[1];
                }
                else if(args.length == 3) {
                    server = args[1];
                    world = args[2];
                }
                else if(args.length >= 4) {
                    commandSender.sendMessage("§c/warp list [server] [world]");
                    return;
                }

                List<List<String>> warplist = new ArrayList<>();
                for(int prefixNumber = 0; prefixNumber <= warpPrefixes.size(); prefixNumber++) {
                    warplist.add(new ArrayList<>());
                }

                int warpCount = 0;
                for(String warp: warpManager.getWarpNames(server, world)) {
                    if(commandSender.hasPermission("cvportal.warp." + warp)) {
                        warpCount++;
                        boolean found = false;
                        for(int prefixNumber = 0; prefixNumber < warpPrefixes.size(); prefixNumber++) {
                            if(warp.startsWith(warpPrefixes.get(prefixNumber))) {
                                warplist.get(prefixNumber).add(warp);
                                found = true;
                                break;
                            }
                        }
                        if(!found) warplist.get(warpPrefixes.size()).add(warp);
                    }
                }

                if(warpCount > 0) {
                    commandSender.sendMessage(ChatColor.DARK_GREEN + "--------------------" + ChatColor.GREEN + "Warps" + ChatColor.DARK_GREEN + "--------------------");

                    for(int prefixNumber = 0; prefixNumber <= warpPrefixes.size(); prefixNumber++) {
                        TextComponent out = new TextComponent(" ");
                        for(int warpNumber = 0; warpNumber < warplist.get(prefixNumber).size(); warpNumber++) {
                            if(warpNumber > 0)
                                out.addExtra(new TextComponent(ChatColor.DARK_GREEN + " - "));
                                Collections.sort(warplist.get(prefixNumber));
                            String warp = warplist.get(prefixNumber).get(warpNumber);
                            TextComponent w = new TextComponent(warp);
                            w.setColor(warpPrefixColours.get(prefixNumber));
                            w.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp));
                            w.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to warp to " + warpPrefixColours.get(prefixNumber) + warp).create()));
                            out.addExtra(w);
                        }
                        commandSender.sendMessage(out);
                    }
                }
                else {
                    commandSender.sendMessage("§cNo warps found.");
                }
                return;
            }
        }

        if(args.length == 2) {
            if(!commandSender.hasPermission("cvportal.warp.others")) {
                commandSender.sendMessage("§c/warp <target>");
                return;
            }
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
            String target = args[1].toLowerCase();
            warpManager.teleport(player, target);
            return;
        }

        if(args.length != 1) {
            commandSender.sendMessage("§c/warp <target>");
            return;
        }

        if(!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage("§cOnly players can use warps.");
            return;
        }

        ProxiedPlayer sender = (ProxiedPlayer) commandSender;
        String target = args[0].toLowerCase();
        if(sender.hasPermission("cvportal.warp." + target)) {
            warpManager.teleport(sender, target);
        }
        else {
            sender.sendMessage("§cNo permission.");
        }
    }
}
