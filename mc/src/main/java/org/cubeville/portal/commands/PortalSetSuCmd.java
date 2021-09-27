package org.cubeville.portal.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.portal.Portal;
import org.cubeville.portal.PortalManager;
import org.cubeville.portal.actions.SuCmd;

public class PortalSetSuCmd extends Command
{
    public PortalSetSuCmd() {
        super("set sucommand");
        setPermission("cvportal.setsucmd");
        addBaseParameter(new CommandParameterPortal());
        addBaseParameter(new CommandParameterString());
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Portal portal = (Portal) baseParameters.get(0);

        String cmd = (String) baseParameters.get(1);
        portal.addAction(new SuCmd(cmd));
        PortalManager.getInstance().save();

        return new CommandResponse("&aSu-Command set.");        
    }
}
