package org.cubeville.cvportal;

import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import net.md_5.bungee.event.EventHandler;
import org.cubeville.cvipc.CVIPC;

import org.cubeville.cvplayerdata.PlayerDataManager;
import org.cubeville.cvportal.commands.*;

import org.cubeville.cvportal.warps.WarpManager;

public class CVPortal extends Plugin implements Listener
{
    public Map<UUID, UUID> pendingTeleports;
    public Map<UUID, Integer> scheduledTasks;
    public Map<UUID, Set<UUID>> tpExceptions;
    public Map<ServerInfo, Set<UUID>> serverWhitelists;

    public TaskScheduler taskScheduler;

    public WarpManager warpManager;

    public void onEnable() {
        this.pendingTeleports = new HashMap<>();
        this.scheduledTasks = new HashMap<>();
        this.tpExceptions = new HashMap<>();
        this.serverWhitelists = new HashMap<>();
        this.taskScheduler = getProxy().getScheduler();
        PluginManager pm = getProxy().getPluginManager();
        CVIPC ipc = (CVIPC) pm.getPlugin("CVIPC");
        PlayerDataManager pdm = PlayerDataManager.getInstance();
        pm.registerCommand(this, new TpCommand(this, ipc, pdm));
        pm.registerCommand(this, new BringCommand(ipc, pdm));
        pm.registerCommand(this, new AcceptCommand(this, ipc));
        pm.registerCommand(this, new DenyCommand(this));
        pm.registerCommand(this, new ReloadExceptionsCommand(this));
        pm.registerCommand(this, new ExceptionsCommand(this, pdm));
        pm.registerCommand(this, new GWhitelistCommand(this, pdm));
        pm.registerListener(this, this);

        File configFile = new File(getDataFolder(), "config.yml");
        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            warpManager = new WarpManager((Configuration) config.get("warps"), configFile, ipc, this);
            pm.registerCommand(this, new WarpCommand(warpManager));
            Map<String, String> warpCommands = warpManager.getWarpCommands();
            for(String command: warpCommands.keySet()) {
                pm.registerCommand(this, new WarpAliasCommand(warpManager, command, warpCommands.get(command)));
            }

            if(config.get("tp-exceptions") != null) {
                Configuration tpExceptions = (Configuration) config.get("tp-exceptions");
                for(String tpException : tpExceptions.getKeys()) {
                    Set<UUID> list = new HashSet<>();
                    for(String uuid : tpExceptions.getStringList(tpException)) {
                        try { list.add(UUID.fromString(uuid)); } catch(IllegalArgumentException ignored) {}
                    }
                    try {
                        UUID uuid = UUID.fromString(tpException);
                        this.tpExceptions.put(uuid, list);
                    } catch(IllegalArgumentException ignored) {}
                }
            }
            if(config.get("server-whitelists") != null) {
                Configuration serverWhitelists = (Configuration) config.get("server-whitelists");
                for(String server : serverWhitelists.getKeys()) {
                    Set<UUID> list = new HashSet<>();
                    for(String uuid : serverWhitelists.getStringList(server)) {
                        try {list.add(UUID.fromString(uuid)); } catch(IllegalArgumentException ignored) {}
                    }
                    if(ProxyServer.getInstance().getServerInfo(server) != null) {
                        this.serverWhitelists.put(ProxyServer.getInstance().getServerInfo(server), list);
                    }
                }
            }
        }
        catch(IOException e) {
            System.out.println("ERROR: Could not load cvportal warp configuration");
        }
    }

    public void startTeleportTimer(UUID source, UUID target) {
        int task = this.taskScheduler.schedule(this, () -> {
            if(ProxyServer.getInstance().getPlayer(source) != null) {
                ProxyServer.getInstance().getPlayer(source).sendMessage(new TextComponent(ChatColor.RED + "Target player ran out of time to accept the teleport request!"));
            }
            if(ProxyServer.getInstance().getPlayer(target) != null) {
                ProxyServer.getInstance().getPlayer(target).sendMessage(new TextComponent(ChatColor.RED + "You ran out of time to accept the teleport request!"));
            }
            this.pendingTeleports.remove(source);
            this.scheduledTasks.remove(source);
            this.scheduledTasks.remove(target);
        }, 10, TimeUnit.SECONDS).getId();
        this.scheduledTasks.put(source, task);
        this.scheduledTasks.put(target, task);
    }

    public void stopTeleportTimer(UUID source, UUID target) {
        if(this.scheduledTasks.get(source) != null) {
            try { this.taskScheduler.cancel(this.scheduledTasks.get(source)); } catch(IllegalArgumentException ignored) {}
            this.scheduledTasks.remove(source);
        }
        if(this.scheduledTasks.get(target) != null) {
            try { this.taskScheduler.cancel(this.scheduledTasks.get(target)); } catch(IllegalArgumentException ignored) {}
            this.scheduledTasks.remove(target);
        }
    }

    public void addPendingTeleport(UUID source, UUID target) {
        this.pendingTeleports.put(source, target);
        startTeleportTimer(source, target);
    }

    public void removePendingTeleport(UUID source) {
        if(this.pendingTeleports.get(source) != null) {
            stopTeleportTimer(source, this.pendingTeleports.remove(source));
        }
    }

    public UUID getPendingTarget(UUID source) {
        return this.pendingTeleports.get(source);
    }

    public UUID getPendingSource(UUID target) {
        for(UUID source : this.pendingTeleports.keySet()) {
            if(this.pendingTeleports.get(source).equals(target)) return source;
        }
        return null;
    }

    public boolean pendingTeleportExists(UUID player) {
        if(this.pendingTeleports.containsKey(player)) {
            return true;
        } else {
            return this.pendingTeleports.containsValue(player);
        }
    }

    public Map<UUID, Set<UUID>> getTpExceptions() {
        return this.tpExceptions;
    }

    public void setTpExceptions(Map<UUID, Set<UUID>> tpExceptions) {
        this.tpExceptions = tpExceptions;
    }

    public Configuration getTpExceptionsConfig() {
        Configuration tpException = new Configuration();
        for(UUID uuid : this.tpExceptions.keySet()) {
            List<String> uuids = new ArrayList<>();
            for(UUID u : this.tpExceptions.get(uuid)) {
                uuids.add(u.toString());
            }
            tpException.set(uuid.toString(), uuids);
        }
        return tpException;
    }

    public void saveTpExceptions() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            Configuration allConfig = new Configuration();
            Configuration tpException = new Configuration();
            for(UUID uuid : this.tpExceptions.keySet()) {
                List<String> uuids = new ArrayList<>();
                for(UUID u : this.tpExceptions.get(uuid)) {
                    uuids.add(u.toString());
                }
                tpException.set(uuid.toString(), uuids);
            }
            allConfig.set("tp-exceptions", tpException);
            allConfig.set("warps", warpManager.getConfig());
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(allConfig, configFile);
        } catch(IOException e) {
            System.out.println("ERROR: Could not save cvportal exceptions configuration");
        }
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if(serverWhitelistOn(player.getServer().getInfo())) {
            if(!isPlayerWhitelisted(player.getServer().getInfo(), player)) {
                player.sendMessage(new TextComponent(ChatColor.RED + "You are not whitelisted on this server!"));
                List<ListenerInfo> listeners = new ArrayList<>(ProxyServer.getInstance().getConfig().getListeners());
                player.connect(event.getFrom() != null ? event.getFrom() : ProxyServer.getInstance().getServerInfo(listeners.get(0).getServerPriority().get(0)));
            }
        }
    }

    public boolean serverWhitelistOn(ServerInfo server) {
        return this.serverWhitelists.containsKey(server);
    }

    public boolean isPlayerWhitelisted(ServerInfo server, ProxiedPlayer player) {
        return serverWhitelistOn(server) && (this.serverWhitelists.get(server).contains(player.getUniqueId())
                || player.hasPermission("cvportal.serverwhitelist.all"));
    }

    public Map<ServerInfo, Set<UUID>> getServerWhitelists() {
        return this.serverWhitelists;
    }

    public void setServerWhitelistStatus(ServerInfo server, boolean status) {
        if(status) {
            if(!this.serverWhitelists.containsKey(server)) {
                this.serverWhitelists.put(server, new HashSet<>());
                saveServerWhitelists();
            }
        } else {
            this.serverWhitelists.remove(server);
            saveServerWhitelists();
        }
    }

    public void addPlayerToServerWhitelist(ServerInfo server, UUID uuid) {
        if(this.serverWhitelists.containsKey(server)) {
            Set<UUID> uuids = this.serverWhitelists.get(server);
            uuids.add(uuid);
            this.serverWhitelists.put(server, uuids);
            saveServerWhitelists();
        }
    }

    public void removePlayerToServerWhitelist(ServerInfo server, UUID uuid) {
        if(this.serverWhitelists.containsKey(server)) {
            Set<UUID> uuids = this.serverWhitelists.get(server);
            uuids.remove(uuid);
            this.serverWhitelists.put(server, uuids);
            saveServerWhitelists();
        }
    }

    public Configuration getServerWhitelistsConfig() {
        Configuration serverWhitelists = new Configuration();
        for(ServerInfo server : this.serverWhitelists.keySet()) {
            List<String> uuids = new ArrayList<>();
            for(UUID u : this.serverWhitelists.get(server)) {
                uuids.add(u.toString());
            }
            serverWhitelists.set(server.getName().toLowerCase(), uuids);
        }
        return serverWhitelists;
    }

    public void saveServerWhitelists() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            Configuration allConfig = new Configuration();
            Configuration serverWhitelists = new Configuration();
            for(ServerInfo server : this.serverWhitelists.keySet()) {
                List<String> uuids = new ArrayList<>();
                for(UUID u : this.serverWhitelists.get(server)) {
                    uuids.add(u.toString());
                }
                serverWhitelists.set(server.getName().toLowerCase(), uuids);
            }
            allConfig.set("server-whitelists", serverWhitelists);
            allConfig.set("warps", warpManager.getConfig());
            allConfig.set("tp-exceptions", getTpExceptionsConfig());
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(allConfig, configFile);
        } catch(IOException e) {
            System.out.println("ERROR: Could not save cvportal exceptions configuration");
        }
    }
}
