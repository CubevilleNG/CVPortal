package org.cubeville.portal.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import org.cubeville.commons.commands.CommandParameterEnumeratedString;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.portal.Portal;
import org.cubeville.portal.PortalManager;
import org.cubeville.portal.actions.GameMode;

public class PortalSetGameMode extends Command
{
    public PortalSetGameMode() {
        super("set gamemode");
        setPermission("cvportal.setgamemode");
        addBaseParameter(new CommandParameterPortal());
        addBaseParameter(new CommandParameterEnumeratedString("survival", "creative", "spectator"));
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Portal portal = (Portal) baseParameters.get(0);

        org.bukkit.GameMode gamemode = org.bukkit.GameMode.valueOf(((String) baseParameters.get(1)).toUpperCase());
        portal.addAction(new GameMode(gamemode));
        PortalManager.getInstance().save();

        return new CommandResponse("&aGamemode set.");
    }
        
}
