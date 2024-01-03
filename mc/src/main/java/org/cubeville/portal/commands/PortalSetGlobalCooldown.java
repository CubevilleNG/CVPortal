package org.cubeville.portal.commands;

import java.util.Set;
import java.util.Map;
import java.util.List;

import org.bukkit.entity.Player;

import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandParameterInteger;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.portal.Portal;
import org.cubeville.portal.PortalManager;

public class PortalSetGlobalCooldown extends Command
{
    public PortalSetGlobalCooldown() {
        super("set globalcooldown");
        addBaseParameter(new CommandParameterPortal());
        addBaseParameter(new CommandParameterInteger());
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Portal portal = (Portal) baseParameters.get(0);

        int cooldown = (int) baseParameters.get(1);
        if(cooldown < 0) { throw new CommandExecutionException("&cPlease use a time of 0 or more seconds."); }
        portal.setGlobalCooldown(new Integer(cooldown * 1000).intValue());
        PortalManager.getInstance().save();
        
        return new CommandResponse("&aGlobal cooldown set.");   
    }
}
