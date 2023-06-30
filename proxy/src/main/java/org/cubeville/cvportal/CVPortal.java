package org.cubeville.cvportal;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

    public TaskScheduler taskScheduler;

    public void onEnable() {
        this.pendingTeleports = new HashMap<>();
        this.scheduledTasks = new HashMap<>();
        this.taskScheduler = getProxy().getScheduler();
        PluginManager pm = getProxy().getPluginManager();
        CVIPC ipc = (CVIPC) pm.getPlugin("CVIPC");
        PlayerDataManager pdm = PlayerDataManager.getInstance();
        pm.registerCommand(this, new TpCommand(this, ipc, pdm));
        pm.registerCommand(this, new BringCommand(ipc, pdm));
        pm.registerCommand(this, new AcceptCommand(this, ipc));
        pm.registerCommand(this, new DenyCommand(this));

        File configFile = new File(getDataFolder(), "config.yml");
        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            WarpManager warpManager = new WarpManager((Configuration) config.get("warps"), configFile, ipc);
            pm.registerCommand(this, new WarpCommand(warpManager));
            Map<String, String> warpCommands = warpManager.getWarpCommands();
            for(String command: warpCommands.keySet()) {
                pm.registerCommand(this, new WarpAliasCommand(warpManager, command, warpCommands.get(command)));
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
}
