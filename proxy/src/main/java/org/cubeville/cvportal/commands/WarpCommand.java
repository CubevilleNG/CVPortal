package org.cubeville.cvportal.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import net.md_5.bungee.protocol.packet.Chat;
import org.cubeville.cvportal.warps.WarpManager;

import javax.xml.soap.Text;

public class WarpCommand extends Command
{
    private WarpManager warpManager;
    
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

                List<String> warplist = new ArrayList<>();
                for(String warp: warpManager.getWarpNames(server, world)) {
                    if(commandSender.hasPermission("cvportal.warp." + warp)) {
                        warplist.add(warp);
                    }
                }
                Collections.sort(warplist);
                if(warplist.size() > 0) {
                    commandSender.sendMessage(ChatColor.DARK_GREEN + "--------------------" + ChatColor.GREEN + "Warps" + ChatColor.DARK_GREEN + "--------------------");

                    List<TextComponent> eList = new ArrayList<>();
                    List<TextComponent> gList = new ArrayList<>();
                    List<TextComponent> qList = new ArrayList<>();
                    List<TextComponent> sList = new ArrayList<>();
                    List<TextComponent> mList = new ArrayList<>();
                    for(String warp: warplist) {
                        if (warp.startsWith("e_")) {
                            TextComponent eClickWarp = new TextComponent(warp);
                            eClickWarp.setColor(ChatColor.YELLOW);
                            eClickWarp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp));
                            eClickWarp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to warp to " + ChatColor.YELLOW + warp).create()));
                            eList.add(eClickWarp);
                        } else if (warp.startsWith("g_")) {
                            TextComponent gClickWarp = new TextComponent(warp);
                            gClickWarp.setColor(ChatColor.DARK_AQUA);
                            gClickWarp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp));
                            gClickWarp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to warp to " + ChatColor.DARK_AQUA + warp).create()));
                            gList.add(gClickWarp);
                        } else if (warp.startsWith("q_")) {
                            TextComponent qClickWarp = new TextComponent(warp);
                            qClickWarp.setColor(ChatColor.GOLD);
                            qClickWarp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp));
                            qClickWarp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to warp to " + ChatColor.GOLD + warp).create()));
                            qList.add(qClickWarp);
                        } else if (warp.startsWith("s_")) {
                            TextComponent sClickWarp = new TextComponent(warp);
                            sClickWarp.setColor(ChatColor.GREEN);
                            sClickWarp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp));
                            sClickWarp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to warp to " + ChatColor.GREEN + warp).create()));
                            sList.add(sClickWarp);
                        } else {
                            TextComponent mClickWarp = new TextComponent(warp);
                            mClickWarp.setColor(ChatColor.GRAY);
                            mClickWarp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + warp));
                            mClickWarp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to warp to " + ChatColor.GRAY + warp).create()));
                            mList.add(mClickWarp);
                        }
                    }
                    int e = 0;
                    TextComponent eWarps = new TextComponent(" ");
                    for(TextComponent warp: eList) {
                        if(e > 0) {
                            eWarps.addExtra(new TextComponent(ChatColor.DARK_GREEN + " - "));
                            eWarps.addExtra(warp);
                        } else {
                            eWarps.addExtra(warp);
                            e++;
                        }
                    }
                    commandSender.sendMessage(eWarps);
                    int g = 0;
                    TextComponent gWarps = new TextComponent(" ");
                    for(TextComponent warp: gList) {
                        if(g > 0) {
                            gWarps.addExtra(new TextComponent(ChatColor.DARK_GREEN + " - "));
                            gWarps.addExtra(warp);
                        } else {
                            gWarps.addExtra(warp);
                            g++;
                        }
                    }
                    commandSender.sendMessage(gWarps);
                    int q = 0;
                    TextComponent qWarps = new TextComponent(" ");
                    for(TextComponent warp: qList) {
                        if(q > 0) {
                            qWarps.addExtra(new TextComponent(ChatColor.DARK_GREEN + " - "));
                            qWarps.addExtra(warp);
                        } else {
                            qWarps.addExtra(warp);
                            q++;
                        }
                    }
                    commandSender.sendMessage(qWarps);
                    int s = 0;
                    TextComponent sWarps = new TextComponent(" ");
                    for(TextComponent warp: sList) {
                        if(s > 0) {
                            sWarps.addExtra(new TextComponent(ChatColor.DARK_GREEN + " - "));
                            sWarps.addExtra(warp);
                        } else {
                            sWarps.addExtra(warp);
                            s++;
                        }
                    }
                    commandSender.sendMessage(sWarps);
                    int m = 0;
                    TextComponent mWarps = new TextComponent(" ");
                    for(TextComponent warp: mList) {
                        if(m > 0) {
                            mWarps.addExtra(new TextComponent(ChatColor.DARK_GREEN + " - "));
                            mWarps.addExtra(warp);
                        } else {
                            mWarps.addExtra(warp);
                            m++;
                        }
                    }
                    commandSender.sendMessage(mWarps);
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
