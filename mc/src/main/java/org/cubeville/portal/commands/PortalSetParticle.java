package org.cubeville.portal.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.Particle;

import org.cubeville.commons.commands.BaseCommand;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandParameterEnum;
import org.cubeville.commons.commands.CommandParameterInteger;
import org.cubeville.commons.commands.CommandParameterFloat;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.portal.Portal;
import org.cubeville.portal.PortalManager;

public class PortalSetParticle extends BaseCommand
{
    public PortalSetParticle() {
        super("set particle");
        addBaseParameter(new CommandParameterPortal());
        addBaseParameter(new CommandParameterEnum(Particle.class));
        addBaseParameter(new CommandParameterInteger());
        addParameter("red", true, new CommandParameterInteger());
        addParameter("green", true, new CommandParameterInteger());
        addParameter("blue", true, new CommandParameterInteger());
        addParameter("size", true, new CommandParameterFloat());
        addFlag("silent");
    }

    public CommandResponse execute(CommandSender sender, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Portal portal = (Portal) baseParameters.get(0);
        Particle particle = (Particle) baseParameters.get(1);

        float size = 1.0f;
        if(parameters.containsKey("size"))
            size = (float) parameters.get("size");

        int red = 255;
        int green = 0;
        int blue = 0;
        if(parameters.containsKey("red")) red = (int) parameters.get("red");
        if(parameters.containsKey("green")) green = (int) parameters.get("green");
        if(parameters.containsKey("blue")) blue = (int) parameters.get("blue");
        
        portal.setParticle(particle, (Integer) baseParameters.get(2), size, red, green, blue);
        PortalManager.getInstance().save();

        if(flags.contains("silent"))
            return new CommandResponse("");
        else
            return new CommandResponse("&aParticle set.");
    }
}
