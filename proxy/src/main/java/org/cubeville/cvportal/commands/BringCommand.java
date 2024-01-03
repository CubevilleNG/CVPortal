package org.cubeville.cvportal.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import org.cubeville.cvipc.CVIPC;
import org.cubeville.cvplayerdata.PlayerDataManager;

public class BringCommand extends Command
{
    CVIPC ipc;
    PlayerDataManager pdm;
    
    public BringCommand(CVIPC ipc, PlayerDataManager pdm) {
        super("bring", "cvportal.bring");
        this.ipc = ipc;
        this.pdm = pdm;
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof ProxiedPlayer)) return;
        ProxiedPlayer sender = (ProxiedPlayer) commandSender;

        if(args.length != 1) {
            sender.sendMessage("§c/bring <player>");
            return;
        }

        String playerName = args[0];
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pdm.getPlayerByVisibleName(playerName));
        
        if(player == null) {
            sender.sendMessage("§cPlayer not found.");
            return;
        }

        String targetServer = sender.getServer().getInfo().getName();
        String playerServer = player.getServer().getInfo().getName();

        if(targetServer.equals(playerServer)) {
            ipc.sendMessage(targetServer, "tplocal|" + player.getUniqueId() + "|player:" + sender.getUniqueId());
        }
        else {
            ipc.sendMessage(targetServer, "xwportal|" + player.getUniqueId() + "|player:" + sender.getUniqueId() + "|" + targetServer);
        }
    }
}
