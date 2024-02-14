package org.cubeville.portal.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
    
import org.cubeville.commons.commands.BaseCommand;
import org.cubeville.commons.commands.CommandParameterVector;
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.portal.Portal;
import org.cubeville.portal.PortalManager;

public class PortalLoad extends BaseCommand
{
    public PortalLoad() {
        super("load");
        addBaseParameter(new CommandParameterString()); // filename
        addBaseParameter(new CommandParameterString()); // world
        addBaseParameter(new CommandParameterVector()); // Offset
        addBaseParameter(new CommandParameterString()); // name
    }

    public CommandResponse execute(CommandSender sender, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        String filename = (String) baseParameters.get(0);
        String worldname = (String) baseParameters.get(1);
        Vector offset = (Vector) baseParameters.get(2);
        String portalname = (String) baseParameters.get(3);

        FileConfiguration config = new YamlConfiguration();
        File file = new File("/home/b5/sharedportals", filename);
        try {
            config.load(file);
        }
        catch(FileNotFoundException e) {
            throw new CommandExecutionException("&cFile not found");
        }
        catch(IOException e) {
            throw new CommandExecutionException("&cUnable to load file");
        }
        catch(InvalidConfigurationException e) {
            throw new CommandExecutionException("&cInvalid file");
        }

        ConfigurationSection portalConfig = config.getConfigurationSection("Portal");
        Map<String, Object> portalData = portalConfig.getValues(true);
        Portal portal = new Portal(portalData);
        portal.setWorld(Bukkit.getWorld(worldname).getUID());
        portal.setName(portalname);
        portal.move(offset);
        PortalManager.getInstance().addPortal(portal);

        return null;
    }
}
