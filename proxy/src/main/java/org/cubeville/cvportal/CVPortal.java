package org.cubeville.cvportal;

import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import org.cubeville.cvipc.CVIPC;

import org.cubeville.cvplayerdata.playerdata.PlayerDataManager;
import org.cubeville.cvportal.commands.*;

import org.cubeville.cvportal.warps.WarpManager;

public class CVPortal extends Plugin
{
    public Map<UUID, UUID> pendingTeleports;
    public Map<UUID, Integer> scheduledTasks;
    public Map<UUID, Set<UUID>> tpExceptions;

    public TaskScheduler taskScheduler;

    public WarpManager warpManager;

    public void onEnable() {
        this.pendingTeleports = new HashMap<>();
        this.scheduledTasks = new HashMap<>();
        this.tpExceptions = new HashMap<>();
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
}
