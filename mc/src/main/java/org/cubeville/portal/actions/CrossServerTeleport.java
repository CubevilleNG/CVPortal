package org.cubeville.portal.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.configuration.serialization.SerializableAs;

import org.cubeville.portal.CVPortal;

@SerializableAs("CrossServerTeleport")
public class CrossServerTeleport implements Action
{
    String server;
    String portal;

    public CrossServerTeleport(String server, String portal) {
        this.server = server;
        this.portal = portal;
    }

    public CrossServerTeleport(Map<String, Object> config) {
        this.server = (String) config.get("server");
        this.portal = (String) config.get("portal");
    }

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("server", server);
        ret.put("portal", portal);
        return ret;
    }

    public void execute(Player player) {
        System.out.println("Player " + player.getName() + " entered x-server portal " + portal + " to server " + server);
        if(CVPortal.getInstance().getCVIPC() != null) {
            CVPortal.getInstance().getCVIPC().sendMessage("fwd|" + server + "|xwportal|" + player.getUniqueId() + "|portal:" + portal + "|" + server);
        }
    }

    public boolean isSingular() {
        return true;
    }
    
    public String getLongInfo() {
        return " - &bXS: Server: " + server + " / Portal: " + portal;
    }

    public String getShortInfo() {
        return "XS";
    }
}
