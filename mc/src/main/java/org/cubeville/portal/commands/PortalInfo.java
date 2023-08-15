package org.cubeville.portal.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.portal.Portal;

public class PortalInfo extends Command
{
    public PortalInfo() {
        super("info");
        addBaseParameter(new CommandParameterPortal());
    }
    
    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Portal portal = (Portal) baseParameters.get(0);
        CommandResponse ret = new CommandResponse();
        List<String> portalInfo = portal.getLongInfo();
        for(String item: portalInfo) {
            ret.addMessage(item);
        }
        
        return ret;
    }
}
