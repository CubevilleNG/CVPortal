package org.cubeville.portal.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import org.cubeville.commons.commands.*;

import org.cubeville.portal.LoginTeleporter;

public class PortalLoginTarget extends BaseCommand
{
    LoginTeleporter loginTeleporter;
    
    public PortalLoginTarget(LoginTeleporter loginTeleporter) {
        super("logintarget");
        addBaseParameter(new CommandParameterUUID());
        addBaseParameter(new CommandParameterString());
        this.loginTeleporter = loginTeleporter;
    }

    public CommandResponse execute(CommandSender sender, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        UUID playerId = (UUID) baseParameters.get(0);
        String location = (String) baseParameters.get(1);

        loginTeleporter.setLoginTarget(playerId, "portal:" + location);
        return null;
    }
}
