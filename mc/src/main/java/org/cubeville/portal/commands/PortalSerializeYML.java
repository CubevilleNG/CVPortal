package org.cubeville.portal.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.Command;

import org.cubeville.portal.Portal;

public class PortalSerializeYML extends Command
{
    public PortalSerializeYML() {
        super("serializeyml");
        addBaseParameter(new CommandParameterPortal());
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Portal portal = (Portal) baseParameters.get(0);

        FileConfiguration config = new YamlConfiguration();
        config.set("PortalData", portal.serialize());

        return new CommandResponse(config.saveToString());
    }

}
