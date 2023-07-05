package org.cubeville.cvportal.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.cubeville.cvportal.CVPortal;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReloadExceptionsCommand extends Command {

    CVPortal plugin;

    public ReloadExceptionsCommand(CVPortal plugin) {
        super("reloadexceptions", "cvportal.reloadexceptions");
        this.plugin = plugin;
    }

    public void execute(CommandSender commandSender, String[] args) {
        if(args.length != 0) return;
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            Configuration tpExceptions = (Configuration) config.get("tp-exceptions");
            HashMap<UUID, Set<UUID>> out = new HashMap<>();
            for(String tpException : tpExceptions.getKeys()) {
                Set<UUID> list = new HashSet<>();
                for(String uuid : tpExceptions.getStringList(tpException)) {
                    try { list.add(UUID.fromString(uuid)); } catch(IllegalArgumentException ignored) {}
                }
                try {
                    UUID uuid = UUID.fromString(tpException);
                    out.put(uuid, list);
                } catch(IllegalArgumentException ignored) {}
            }
            plugin.setTpExceptions(out);
        }
        catch(IOException e) {
            System.out.println("ERROR: Could not load cvportal exceptions configuration");
        }
        commandSender.sendMessage("Tp Exceptions reloaded");
    }
}
