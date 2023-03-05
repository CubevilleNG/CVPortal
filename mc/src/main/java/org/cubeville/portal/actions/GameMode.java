package org.cubeville.portal.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

@SerializableAs("GameMode")
public class GameMode implements Action
{
    org.bukkit.GameMode gamemode;

    public GameMode(org.bukkit.GameMode gamemode) {
        this.gamemode = gamemode;
    }

    public GameMode(Map<String, Object> config) {
        this.gamemode = org.bukkit.GameMode.valueOf((String) config.get("gamemode"));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("gamemode", gamemode.toString());
        return ret;
    }

    public void execute(Player player) {
        player.setGameMode(gamemode);
    }

    public String getLongInfo() {
        return " - &bGameMode: " + gamemode.toString();
    }

    public String getShortInfo() {
        return "GM";
    }

    public boolean isSingular() {
        return true;
    }
}
