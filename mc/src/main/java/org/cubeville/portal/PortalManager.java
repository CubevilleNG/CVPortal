package org.cubeville.portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.cubeville.commons.commands.DynamicallyEnumeratedObjectSource;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

public class PortalManager implements Listener, DynamicallyEnumeratedObjectSource
{
    private Plugin plugin;
    private Integer taskId;

    private List<Portal> portals;
    private List<Portal> moveEventPortals;
    private List<Portal> jumpEventPortals;
    
    private static PortalManager instance;
    private Map<UUID, Portal> respawnPortals;
    private Set<UUID> ignoredPlayers;
    
    @SuppressWarnings("unchecked")
    public PortalManager(Plugin plugin) {
        this.plugin = plugin;
        instance = this;
        taskId = null;
        portals = (List<Portal>) plugin.getConfig().get("Portals");
        if(portals == null) {
            portals = new ArrayList<>();
        }
        updateTriggerEventPortalList();
        respawnPortals = new HashMap<>();
        ignoredPlayers = new HashSet<>();
    }

    public void ignorePlayer(UUID Player) {
        ignoredPlayers.add(Player);
    }

    public void unignorePlayer(UUID Player) {
        ignoredPlayers.remove(Player);
    }
    
    public void start() {
        if(taskId != null) plugin.getServer().getScheduler().cancelTask(taskId);

        Runnable runnable = new Runnable() {
                @SuppressWarnings("unchecked")
                public void run() {
                    Collection<Player> onlinePlayers = (Collection<Player>) plugin.getServer().getOnlinePlayers();
                    Collection<Player> players;
                    if(ignoredPlayers.size() == 0) {
                        players = onlinePlayers;
                    }
                    else {
                        List<Player> playerList = new ArrayList<>();
                        for(Player player: onlinePlayers) {
                            if(!ignoredPlayers.contains(player.getUniqueId())) {
                                playerList.add(player);
                            }
                        }
                        players = playerList;
                    }
                    for(Portal portal: portals) {
                        portal.cyclicTrigger(players);
                    }
                }
            };
        
        taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, 15, 15).getTaskId(); // TODO: Was 30, but reduced cause lag

        Runnable alsoARunnable = new Runnable() {
                public void run() {
                    for(Portal portal: portals) {
                        portal.particleTrigger();
                    }
                }
            };
        plugin.getServer().getScheduler().runTaskTimer(plugin, alsoARunnable, 1, 1);
    }

    public void stop() {
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }

    public void save() {
        plugin.getConfig().set("Portals", portals);
        plugin.saveConfig();
        updateTriggerEventPortalList();
    }

    private void updateTriggerEventPortalList() {
        moveEventPortals = new ArrayList<>();
        for(Portal portal: portals) {
            if(portal.isMoveEventTriggered()) moveEventPortals.add(portal);
        }

	jumpEventPortals = new ArrayList<>();
	for(Portal portal: portals) {
	    if(portal.isJumpEventTriggered()) jumpEventPortals.add(portal);
	}
    }
    
    public void addPortal(Portal portal) {
        portals.add(portal);
        save();
    }

    public void deletePortal(String name) {
        portals.remove(getPortal(name));
        save();
    }

    public Portal getPortal(String name) {
        for(Portal portal: portals) {
            if(portal.getName().equals(name)) {
                return portal;
            }
        }
        return null;
    }

    public List<Portal> getMatchingPortals(String search) {
        List<Portal> match = new ArrayList<Portal>();
        for(Portal portal: portals) {
            if(portal.getName().contains(search)) {
                match.add(portal);
            }
        }
        return match;
    }

    public static PortalManager getInstance() {
       return instance;
    }

    public List<Portal> getPortals() {
        return portals;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for(Portal portal: portals) {
            if(portal.isLoginTriggered() && portal.isPlayerInPortal(player)) {
                System.out.println("Player " + player.getName() + " joined in login triggered portal.");
                plugin.getServer().getScheduler().runTaskLater(CVPortal.getInstance(), new Runnable() {
                        public void run() {
                            portal.executeActions(player);
                        }
                    }, 2);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        for(Portal portal: portals) {
            if (!portal.isPlayerInPortal(player)) continue;
	    if (!portal.conditionIsTrue(player)) continue;
	    
            if(portal.isDeathTriggered()) {
                if(respawnPortals.containsKey(player.getUniqueId()) == false) {
                    respawnPortals.put(player.getUniqueId(), portal);
                }
            }

            if(portal.isKeepInventory()) {
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        if(respawnPortals.containsKey(player.getUniqueId())) {
            final Portal portal = respawnPortals.get(player.getUniqueId());
            Location loc = portal.getTeleportLocation();
            if(loc != null) event.setRespawnLocation(loc);
            respawnPortals.remove(player.getUniqueId());
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                    public void run() {
                        portal.executeActionsExceptTeleport(player);
                    }
                }, 10);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        for(Portal portal: moveEventPortals) {
            portal.moveEventTrigger(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
	Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		@Override
		public void run() {
		    for(Portal portal: jumpEventPortals) {
			portal.jumpEventTrigger(event.getPlayer());
		    }		    
		}
	    }, 1L);
    }
    
    public boolean containsObject(String value) {
        for(Portal portal: portals) {
            if(portal.getName().equals(value)) return true;
        }
        return false;
    }

    public Object getObject(String value) {
        for(Portal portal: portals) {
            if(portal.getName().equals(value)) return portal;
        }
        return null;
    }
}
