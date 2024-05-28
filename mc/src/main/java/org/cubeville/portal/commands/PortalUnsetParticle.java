package org.cubeville.portal.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.Particle;

import org.cubeville.commons.commands.BaseCommand;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.portal.Portal;
import org.cubeville.portal.PortalManager;

public class PortalUnsetParticle extends BaseCommand
{
    public PortalUnsetParticle() {
        super("unset particle");
        addBaseParameter(new CommandParameterPortal());
        addFlag("silent");
    }

    public CommandResponse execute(CommandSender sender, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Portal portal = (Portal) baseParameters.get(0);
        portal.setParticle(null, 0, 0.0f, 0, 0, 0);
        PortalManager.getInstance().save();

        if(flags.contains("silent"))
            return new CommandResponse("");
        else
            return new CommandResponse("&aParticle unset.");
    }
}
