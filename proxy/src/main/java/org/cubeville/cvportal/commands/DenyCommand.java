package org.cubeville.cvportal.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.cubeville.cvportal.CVPortal;

import java.util.UUID;

public class DenyCommand extends Command {

    public CVPortal plugin;

    public DenyCommand(CVPortal plugin) {
        super("deny");
        this.plugin = plugin;
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        if(args.length != 2) return;
        if(!args[0].equalsIgnoreCase("tp")) return;
        ProxiedPlayer target = ((ProxiedPlayer) commandSender);
        ProxiedPlayer source;
        try {
            UUID sourceUUID = UUID.fromString(args[1]);
            if(ProxyServer.getInstance().getPlayer(sourceUUID) == null) return;
            source = ProxyServer.getInstance().getPlayer(sourceUUID);
        } catch(IllegalArgumentException e) {
            return;
        }
        if(!plugin.pendingTeleportExists(target.getUniqueId()) || !plugin.pendingTeleportExists(source.getUniqueId())) return;

        plugin.removePendingTeleport(source.getUniqueId());
        source.sendMessage(new TextComponent(ChatColor.DARK_RED + "You're teleport request has been denied"));
        target.sendMessage(new TextComponent(ChatColor.DARK_RED + "You've denied the teleport request"));
    }
}
