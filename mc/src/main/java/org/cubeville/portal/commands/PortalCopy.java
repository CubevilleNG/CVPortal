package org.cubeville.portal.commands;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
    
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandParameterListInteger;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.portal.Portal;

public class PortalCopy extends Command
{
    public PortalCopy() {
        super("copy");
        addBaseParameter(new CommandParameterPortal());
        addBaseParameter(new CommandParameterString()); // server
        addBaseParameter(new CommandParameterString()); // world
        addOptionalBaseParameter(new CommandParameterListInteger(3)); // optional Offset
        addOptionalBaseParameter(new CommandParameterString()); // optional new name
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {
        
        Portal portal = (Portal) baseParameters.get(0);

        FileConfiguration config = new YamlConfiguration();
        config.set("Portal", portal.serialize());

        File file = new File("/home/b5/sharedportals", player.getUniqueId().toString());
        try {
            if(!file.exists()) file.createNewFile();
            config.save(file);
        }
        catch (IOException e) {
            throw new CommandExecutionException("Could not write file.");
        }
        
        return null;
    }
}
