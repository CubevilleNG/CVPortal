package org.cubeville.portal.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

@SerializableAs("SuCmd")
public class SuCmd implements Action
{
    String sucmd;

    public SuCmd(String sucmd) {
        this.sucmd = sucmd;
    }

    public SuCmd(Map<String, Object> config) {
        this.sucmd = (String) config.get("sucmd");
    }

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("sucmd", sucmd);
        return ret;
    }

    public void execute(Player player) {
        String cmd = sucmd;
        cmd = cmd.replace("%player%", player.getName());
        cmd = cmd.replace("%player_x%", String.valueOf(player.getLocation().getX()));
        cmd = cmd.replace("%player_y%", String.valueOf(player.getLocation().getY()));
        cmd = cmd.replace("%player_z%", String.valueOf(player.getLocation().getZ()));
        System.out.println("Running command " + cmd);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    public String getLongInfo() {
        return " - &bSu Command: " + sucmd;
    }
    
    public String getShortInfo() {
        return "SC";
    }

    public boolean isSingular() {
        return false;
    }
}
