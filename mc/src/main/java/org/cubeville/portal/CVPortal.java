package org.cubeville.portal;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.exceptions.ObjectNotFoundException;
import org.betonquest.betonquest.id.ConditionID;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.cubeville.commons.commands.CommandParser;

import org.cubeville.cvipc.CVIPC;

import org.cubeville.portal.actions.*;
import org.cubeville.portal.commands.*;

public class CVPortal extends JavaPlugin {

    private PortalManager portalManager;
    private CommandParser commandParser;
    private CommandParser ptpCommandParser;
    private CommandParser tpposCommandParser;
    private CommandParser setwarpCommandParser;
    private LoginTeleporter loginTeleporter;

    private CVIPC cvipc;
    //private BQWrapper betonQuestWrapper;
    
    private static CVPortal instance;
    public static CVPortal getInstance() {
        return instance;
    }

    public CVIPC getCVIPC() {
        return cvipc;
    }

    public void onEnable() {
        instance = this;

        ConfigurationSerialization.registerClass(ClearInventory.class);
        ConfigurationSerialization.registerClass(Cmd.class);
        ConfigurationSerialization.registerClass(CrossServerTeleport.class);
        ConfigurationSerialization.registerClass(Extinguish.class);
        ConfigurationSerialization.registerClass(GameMode.class);
        ConfigurationSerialization.registerClass(Heal.class);
        ConfigurationSerialization.registerClass(Message.class);
        ConfigurationSerialization.registerClass(Playsound.class);
        ConfigurationSerialization.registerClass(Portal.class);
        ConfigurationSerialization.registerClass(ApplyPotionEffect.class);
        ConfigurationSerialization.registerClass(Random.class);
        ConfigurationSerialization.registerClass(RemoveEffects.class);
        ConfigurationSerialization.registerClass(SpreadTeleport.class);
        ConfigurationSerialization.registerClass(SuCmd.class);
        ConfigurationSerialization.registerClass(Teleport.class);
        ConfigurationSerialization.registerClass(Title.class);
        ConfigurationSerialization.registerClass(Velocity.class);
        
        portalManager = new PortalManager(this);
        portalManager.start();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(portalManager, this);

        loginTeleporter = new LoginTeleporter(portalManager);
        pm.registerEvents(loginTeleporter, this);
        
        cvipc = (CVIPC) pm.getPlugin("CVIPC");
        if(cvipc != null) {
            cvipc.registerInterface("xwportal", loginTeleporter);
            cvipc.registerInterface("tplocal", loginTeleporter);
        }
        
        commandParser = new CommandParser();
        commandParser.addCommand(new PortalAddRandom());
        commandParser.addCommand(new PortalAddPotionEffect());
        commandParser.addCommand(new PortalCreate());
        commandParser.addCommand(new PortalDelete());
	commandParser.addCommand(new PortalDisable());
        commandParser.addCommand(new PortalFind());
        commandParser.addCommand(new PortalInfo());
        commandParser.addCommand(new PortalList());
        commandParser.addCommand(new PortalLoginTarget(loginTeleporter));
        commandParser.addCommand(new PortalRedefine());
        commandParser.addCommand(new PortalRemoveRandom());
        commandParser.addCommand(new PortalRunSuCommand());
        commandParser.addCommand(new PortalSelect());
        commandParser.addCommand(new PortalSendMessage());
        commandParser.addCommand(new PortalSendTitle());
        commandParser.addCommand(new PortalSet());
        commandParser.addCommand(new PortalSetBringHorses());
        commandParser.addCommand(new PortalSetCmd());
        commandParser.addCommand(new PortalSetCondition());
        commandParser.addCommand(new PortalSetCooldown());
        commandParser.addCommand(new PortalSetCrossServerTeleport());
        commandParser.addCommand(new PortalSetDeathTriggered());
        commandParser.addCommand(new PortalSetGameMode());
        commandParser.addCommand(new PortalSetLoginTriggered());
        commandParser.addCommand(new PortalSetKeepInventory());
        commandParser.addCommand(new PortalSetMessage());
        commandParser.addCommand(new PortalSetParticle());
        commandParser.addCommand(new PortalSetPermanent());
        commandParser.addCommand(new PortalSetPermission());
        commandParser.addCommand(new PortalSetSound());
        commandParser.addCommand(new PortalSetSpreadTeleport());
        commandParser.addCommand(new PortalSetSuCmd());
        commandParser.addCommand(new PortalSetTeleport());
        commandParser.addCommand(new PortalSetTitle());
        commandParser.addCommand(new PortalSetTrigger());
        commandParser.addCommand(new PortalSetVelocity());
        commandParser.addCommand(new PortalSetYaw());
        commandParser.addCommand(new PortalTrigger());
        commandParser.addCommand(new PortalRemoveAction("remove clearinventory", "ClearInventory"));
        commandParser.addCommand(new PortalRemoveAction("remove command", "Cmd"));
        commandParser.addCommand(new PortalRemoveAction("remove crossserver teleport", "CrossServerTeleport"));
        commandParser.addCommand(new PortalRemoveAction("remove extinguish", "Extinguish"));
        commandParser.addCommand(new PortalRemoveAction("remove heal", "Heal"));
        commandParser.addCommand(new PortalRemoveAction("remove message", "Message"));
        commandParser.addCommand(new PortalRemoveAction("remove potioneffect", "ApplyPotionEffect"));
        commandParser.addCommand(new PortalRemoveAction("remove removeeffects", "RemoveEffects"));
        commandParser.addCommand(new PortalRemoveAction("remove sound", "Playsound"));
        commandParser.addCommand(new PortalRemoveAction("remove sucommand", "SuCmd"));
        commandParser.addCommand(new PortalRemoveAction("remove teleport", "Teleport"));
        commandParser.addCommand(new PortalRemoveAction("remove title", "Title"));
        commandParser.addCommand(new PortalUnsetParticle());
        
        tpposCommandParser = new CommandParser();
        tpposCommandParser.addCommand(new Tppos());

        ptpCommandParser = new CommandParser();
        ptpCommandParser.addCommand(new Ptp());

        setwarpCommandParser = new CommandParser();
        setwarpCommandParser.addCommand(new Setwarp());

        //betonQuestWrapper = new BQWrapper(pm);
    }

    public void onDisable() {
        portalManager.stop();
        if(cvipc != null) cvipc.deregisterInterface("xwportal");
        instance = null;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("cvportal")) {
            return commandParser.execute(sender, args);
        }
        else if(command.getName().equals("tppos")) {
            return tpposCommandParser.execute(sender, args);
        }
        else if(command.getName().equals("ptp")) {
            return ptpCommandParser.execute(sender, args);
        }
        else if(command.getName().equals("setwarp")) {
            return setwarpCommandParser.execute(sender, args);
        }
        return false;
    }

    public boolean conditionIsTrue(Player player, String condition) {
        //return betonQuestWrapper.conditionIsTrue(player, condition);
        try {
            return BetonQuest.condition(PlayerConverter.getID(player), new ConditionID(null, condition));
        } catch(ObjectNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
