package org.cubeville.portal.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.cubeville.commons.commands.BaseCommand;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandParameterInteger;
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.portal.Portal;
import org.cubeville.portal.PortalManager;

public class PortalTrigger extends BaseCommand
{
    public PortalTrigger() {
        super("trigger");
        addBaseParameter(new CommandParameterString());

        addFlag("random");
        addParameter("count", true, new CommandParameterInteger());

        addParameter("player", true, new CommandParameterString());
        addFlag("force");

        addFlag("search");
        addFlag("limit");
        addParameter("default", true, new CommandParameterString());

        addFlag("silent");
    }

    @SuppressWarnings("unchecked")
    public CommandResponse execute(CommandSender sender, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        PortalManager portalManager = PortalManager.getInstance();
        
        List<Portal> portals;
        {
            String name = (String) baseParameters.get(0);
            if(flags.contains("search")) {
                portals = portalManager.getMatchingPortals(name);
            }
            else {
                Portal portal = portalManager.getPortal(name);
                if(portal == null) throw new CommandExecutionException("&cPortal does not exist!");
                portals = new ArrayList<>();
                portals.add(portal);
            }
        }
        
        if(flags.contains("random") && parameters.get("player") != null) throw new CommandExecutionException("&cplayer and random parameters are exclusive!");
        Collection<Player> players = (Collection<Player>) Bukkit.getServer().getOnlinePlayers();

        if(flags.contains("force") && parameters.get("player") == null) throw new CommandExecutionException("&cforce can only be used with the player paramtere!");
        if(parameters.containsKey("count") && flags.contains("random") == false) throw new CommandExecutionException("&ccount can only be used with random!");

        if(flags.contains("limit") && (!parameters.containsKey("player"))) throw new CommandExecutionException("&climit can only be used with the player-parameter");
        if(flags.contains("limit") && (!flags.contains("search"))) throw new CommandExecutionException("&climit can only be used with the search flag");
        if(flags.contains("limit") && flags.contains("force")) throw new CommandExecutionException("&climit can't be used with the force flag");
        if(parameters.containsKey("default")) {
            if(!parameters.containsKey("player")) throw new CommandExecutionException("&cdefault parameter can only be used with player parameter");
            if(!flags.contains("search")) throw new CommandExecutionException("&cdefault parameter can only be used with search flag");
            if(flags.contains("force")) throw new CommandExecutionException("&cdefault parameter can not be used with force flag");
            if(portalManager.getPortal((String) parameters.get("default")) == null) throw new CommandExecutionException("&cdefault portal does not exist");
        }

        if(flags.contains("random")) {
            int count = 1;
            if(parameters.containsKey("count")) count = (Integer) parameters.get("count");
            for(Portal portal: portals) {
                portal.triggerRandom(players, count);
            }
        }
        else if(parameters.containsKey("player")) {
            String playerName = (String) parameters.get("player");
            Player player = Bukkit.getServer().getPlayer(playerName);
            if(player == null || (!player.getName().equals(playerName))) throw new CommandExecutionException("&cPlayer not found.");
            boolean portalTriggered = false;
            for(Portal portal: portals) {
                if(flags.contains("force")) {
                    portal.executeActions(player);
                }
                else {
                    boolean limit = flags.contains("limit");
                    if(portal.trigger(player)) {
                        portalTriggered = true;
                        if(limit) break;
                    }
                }
            }
            if(parameters.containsKey("default") && portalTriggered == false) {
                Portal defaultPortal = portalManager.getPortal((String) parameters.get("default"));
                defaultPortal.executeActions(player);
            }
        }
        else {
            for(Portal portal: portals) {
                portal.trigger(players);
            }
        }

        if(flags.contains("silent"))
            return new CommandResponse("");
        else
            return new CommandResponse("&aPortal triggered.");
    }
}
