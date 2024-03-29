package org.cubeville.portal.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.commons.utils.BlockUtils;

import org.cubeville.portal.Portal;
import org.cubeville.portal.PortalManager;

public class PortalCreate extends Command
{
    public PortalCreate() {
        super("create");
        addBaseParameter(new CommandParameterString());
        addFlag("fullheight");
	addFlag("tolerant");
	addFlag("reduced");
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {
        
        String name = (String) baseParameters.get(0);

        PortalManager portalManager = PortalManager.getInstance();
        if(portalManager.getPortal(name) != null) throw new CommandExecutionException("&cPortal already exists!");

        boolean fullHeight = flags.contains("fullheight");
        
        Vector min = null;
        Vector max = null;
        boolean noreg = false;
        try {
            min = BlockUtils.getWESelectionMin(player).toVector();
            max = BlockUtils.getWESelectionMax(player).toVector();
            max = max.add(new Vector(1.0, fullHeight ? 1.0 : 0.9, 1.0));
        }
        catch(IllegalArgumentException e) {
            noreg = true;
        }

        if(flags.contains("tolerant")) {
            min = min.add(new Vector(-0.2, -0.2, -0.2));
            max = max.add(new Vector(0.2, 0.3, 0.2));
        }
	
	if(flags.contains("reduced")) {
	    min = min.add(new Vector(0.35, 0, 0.35));
	    max = max.add(new Vector(-0.35, 0, -0.35));
	}
	
        Portal portal = new Portal(name, player.getLocation().getWorld(), min, max);
        portalManager.addPortal(portal);

        return new CommandResponse(noreg ? "&aPortal created without region." : "&aPortal created.");
    }
}
