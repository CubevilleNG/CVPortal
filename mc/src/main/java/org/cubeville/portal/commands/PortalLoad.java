package org.cubeville.portal.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
    
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandParameterListInteger;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.portal.Portal;

public class PortalLoad extends Command
{
    public PortalLoad() {
        super("load");
        addBaseParameter(new CommandParameterString()); // filename
        addBaseParameter(new CommandParameterString()); // world
        addBaseParameter(new CommandParameterListInteger(3)); // optional Offset
        addBaseParameter(new CommandParameterString()); // name
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {


        String filename = (String) baseParameters.get(0);
        String world = (String) baseParameters.get(1);
        List<Integer> offset = (List<Integer>) baseParameters.get(2);
        String name = (String) baseParameters.get(3);
        
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

        Portal portal = (Portal) config.get("Portal");
        System.out.println("Loaded portal " + portal.getName());
        
        return null;
    }
}
