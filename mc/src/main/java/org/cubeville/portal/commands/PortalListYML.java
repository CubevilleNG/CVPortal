package org.cubeville.portal.commands;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.commons.commands.Command;

import org.cubeville.portal.Portal;
import org.cubeville.portal.PortalManager;

public class PortalListYML extends Command
{
    public PortalListYML() {
        super("listyml");
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        List<Portal> portals = PortalManager.getInstance().getPortals();

        List<Map<String, String>> portalList = new ArrayList<>();
        for(Portal portal: portals) {
            Map<String, String> portalinfo = new HashMap<>();
            portalinfo.put("name", portal.getName());
            portalinfo.put("worldname", Bukkit.getWorld(portal.getWorld()).getName());
            portalList.add(portalinfo);
        }
        
        FileConfiguration config = new YamlConfiguration();
        config.set("PortalList", portalList);

        return new CommandResponse(config.saveToString());
    }
}
