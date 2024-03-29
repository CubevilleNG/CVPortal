package org.cubeville.portal.commands;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.util.Vector;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
    
import org.cubeville.commons.commands.CommandParameterVector;
import org.cubeville.commons.commands.CommandParameterString;
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
        addOptionalBaseParameter(new CommandParameterVector()); // optional Offset
        addOptionalBaseParameter(new CommandParameterString()); // optional new name
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {
        
        Portal portal = (Portal) baseParameters.get(0);

        String server = (String) baseParameters.get(1);
        String world = (String) baseParameters.get(2);
        Vector offset;
        if(baseParameters.size() >= 4)
            offset = (Vector) baseParameters.get(3);
        else
            offset = new Vector(0, 0, 0);
        String newname;
        if(baseParameters.size() >= 5)
            newname = (String) baseParameters.get(4);
        else
            newname = portal.getName();
        
        FileConfiguration config = new YamlConfiguration();
        config.set("Portal", portal.serialize());

        String filename = player.getUniqueId().toString();
        File file = new File("/home/b5/sharedportals", filename);
        try {
            if(!file.exists()) file.createNewFile();
            config.save(file);
        }
        catch (IOException e) {
            throw new CommandExecutionException("Could not write file.");
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pcmd scmd " + server + " console cvportal load " + filename + " " + world + " " + offset.getX() + "," + offset.getY() + "," + offset.getZ() + " " + newname);
        
        return new CommandResponse("&aCommand done. Can't validate if it worked though because it's happening on another server, or somewhere else, who knows.");
    }
}
