package org.cubeville.cvportal.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvipc.CVIPC;
import org.cubeville.cvplayerdata.playerdata.PlayerDataManager;
import org.cubeville.cvportal.CVPortal;

public class TpCommand extends Command
{
    CVPortal plugin;
    CVIPC ipc;
    PlayerDataManager pdm;
    
    public TpCommand(CVPortal plugin, CVIPC ipc, PlayerDataManager pdm) {
        super("tp", "cvportal.tp");
        this.plugin = plugin;
        this.ipc = ipc;
        this.pdm = pdm;
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(args.length == 0) {
            sender.sendMessage("§c/tp <target>");
        }
        if(args.length == 1) {
            String target = args[0];
            if(target.startsWith("portal:")) {
                if(!sender.hasPermission("cvportal.tp.portal")) {
                    sender.sendMessage("§cNo permission");
                    return;
                }
                target = target.substring(7);
                String targetServer = target.substring(0, target.indexOf("|"));
                String targetPortal = target.substring(target.indexOf("|") + 1);
                sender.sendMessage("Teleport to portal " + targetPortal + " on server " + targetServer);
                ipc.sendMessage(targetServer, "xwportal|" + sender.getUniqueId() + "|portal:" + targetPortal + "|" + targetServer);
            }
            else {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pdm.getPlayerByVisibleName(target));
                if(player == null) {
                    sender.sendMessage("§cPlayer not found.");
                    return;
                }

                if(player.hasPermission("cvportal.sa") && !sender.hasPermission("cvportal.tp.sa.unlimited")) {
                    sendRequest(sender, player);
                } else if(player.hasPermission("cvportal.admin") && !sender.hasPermission("cvportal.tp.admin.unlimited")) {
                    sendRequest(sender, player);
                } else if(player.hasPermission("cvportal.smod") && !sender.hasPermission("cvportal.tp.smod.unlimited")) {
                    sendRequest(sender, player);
                } else if(player.hasPermission("cvportal.mod") && !sender.hasPermission("cvportal.tp.mod.unlimited")) {
                    sendRequest(sender, player);
                } else {
                    String targetServer = player.getServer().getInfo().getName();
                    String sourceServer = sender.getServer().getInfo().getName();
                    if(sourceServer.equals(targetServer)) {
                        ipc.sendMessage(targetServer, "tplocal|" + sender.getUniqueId() + "|player:" + player.getUniqueId());
                    }
                    else {
                        ipc.sendMessage(targetServer, "xwportal|" + sender.getUniqueId() + "|player:" + player.getUniqueId() + "|" + targetServer);
                    }
                }
            }
        }
    }

    public void sendRequest(ProxiedPlayer source, ProxiedPlayer target) {
        if(plugin.pendingTeleportExists(source.getUniqueId()) || plugin.pendingTeleportExists(target.getUniqueId())) {
            source.sendMessage(new TextComponent(ChatColor.RED + "You or the target player already has a pending teleport! Please wait for that to be resolved before attempting another."));
        } else {
            plugin.addPendingTeleport(source.getUniqueId(), target.getUniqueId());
            target.sendMessage(new TextComponent(ChatColor.YELLOW + source.getName() + ChatColor.LIGHT_PURPLE + " has requested to teleport to you."));
            TextComponent requestOptions = new TextComponent();
            TextComponent accept = new TextComponent(ChatColor.AQUA + "[" + ChatColor.GREEN + "ACCEPT" + ChatColor.AQUA + "]");
            accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Accept teleport")));
            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept tp " + source.getUniqueId()));
            requestOptions.addExtra(accept);
            requestOptions.addExtra(" - ");
            TextComponent deny =new TextComponent(ChatColor.AQUA + "[" + ChatColor.DARK_RED + "DENY" + ChatColor.AQUA + "]");
            deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Deny teleport")));
            deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/deny tp " + source.getUniqueId()));
            requestOptions.addExtra(deny);
            target.sendMessage(requestOptions);
            source.sendMessage(new TextComponent(ChatColor.LIGHT_PURPLE + "You've requested to teleport to " + ChatColor.YELLOW + target.getName()));
        }
    }
}
