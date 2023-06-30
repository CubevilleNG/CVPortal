package org.cubeville.cvportal.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.cubeville.cvipc.CVIPC;
import org.cubeville.cvportal.CVPortal;

import java.util.UUID;

public class AcceptCommand extends Command {

    public CVPortal plugin;
    public CVIPC ipc;

    public AcceptCommand(CVPortal plugin, CVIPC ipc) {
        super("accept");
        this.plugin = plugin;
        this.ipc = ipc;
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

        String targetServer = target.getServer().getInfo().getName();
        String sourceServer = source.getServer().getInfo().getName();
        if(sourceServer.equals(targetServer)) {
            ipc.sendMessage(targetServer, "tplocal|" + source.getUniqueId() + "|player:" + target.getUniqueId());
        }
        else {
            ipc.sendMessage(targetServer, "xwportal|" + source.getUniqueId() + "|player:" + target.getUniqueId() + "|" + targetServer);
        }
        plugin.removePendingTeleport(source.getUniqueId());
        source.sendMessage(new TextComponent(ChatColor.GREEN + "You're teleport request has been accepted"));
        target.sendMessage(new TextComponent(ChatColor.GREEN + "You've accepted the teleport request"));
    }
}
