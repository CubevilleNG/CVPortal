package org.cubeville.portal.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.commons.utils.BlockUtils;

import org.cubeville.portal.Portal;
import org.cubeville.portal.PortalManager;

public class PortalSelect extends Command
{
    public PortalSelect() {
        super("select");
        addBaseParameter(new CommandParameterPortal());
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Portal portal = (Portal) baseParameters.get(0);
        if(!portal.getWorld().equals(player.getLocation().getWorld().getUID())) throw new CommandExecutionException("&cPortal is in different world!");

        Vector min = portal.getMinCorner();
        Vector max = portal.getMaxCorner().clone();
        if(min == null || max == null) throw new CommandExecutionException("&cPortal is regionless");
        max.subtract(new Vector(1, 1, 1));
        BlockUtils.setWESelection(player, player.getLocation().getWorld(), min, max.clone().add(new Vector(0, 0.1, 0)));
        
        return new CommandResponse("Portal selected.");
    }
}
