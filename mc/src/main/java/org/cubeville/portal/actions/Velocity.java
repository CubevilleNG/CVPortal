package org.cubeville.portal.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;

@SerializableAs("Velocity")
public class Velocity implements Action
{
    boolean adjustToPlayerDirection;
    boolean adjustToPitch;
    Vector velocity;
    boolean ignoreY;
    
    public Velocity(Vector velocity) {
        this.velocity = velocity;
        adjustToPlayerDirection = false;
    }

    public Velocity(Vector velocity, boolean adjustToPlayerDirection, boolean adjustToPitch, boolean ignoreY) {
        this.velocity = velocity;
        this.adjustToPlayerDirection = adjustToPlayerDirection;
        this.adjustToPitch = adjustToPitch;
	this.ignoreY = ignoreY;
    }
    
    public Velocity(Map<String, Object> config) {
        velocity = (Vector) config.get("velocity");
        if(config.get("adjustToPlayerDirection") != null)
            adjustToPlayerDirection = (Boolean) config.get("adjustToPlayerDirection");
        else
            adjustToPlayerDirection = false;

        if(config.get("ignoreY") != null)
	    ignoreY = (Boolean) config.get("ignoreY");
	else
	    ignoreY = false;

        if(config.get("adjustToPitch") != null)
            adjustToPitch = (Boolean) config.get("adjustToPitch");
        else
            adjustToPitch = false;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("velocity", velocity);
        ret.put("adjustToPlayerDirection", adjustToPlayerDirection);
	ret.put("ignoreY", ignoreY);
        ret.put("adjustToPitch", adjustToPitch);
        return ret;
    }

    public void execute(Player player) {
        if(! adjustToPlayerDirection) {
	    Vector newVelocity = velocity;
	    if(ignoreY) newVelocity.setY(player.getVelocity().getY());
            player.setVelocity(newVelocity);
        }
        else {
            Vector pl = player.getLocation().getDirection();
            Vector vel;
            if(adjustToPitch) {
                vel = pl.multiply(velocity.getX());
            }
            else {
                vel = new Vector(pl.getX() * velocity.getX(), velocity.getY(), pl.getZ() * velocity.getX());
                if(ignoreY) vel.setY(player.getVelocity().getY());
            }
            player.setVelocity(vel);
        }
    }

    public String getLongInfo() {
        return " - &bVelocity: " + velocity;
    }
    
    public String getShortInfo() {
        return "VEL";
    }

    public boolean isSingular() {
        return true;
    }

    public Vector getVelocity() {
        return this.velocity;
    }

    public boolean isAdjustToPlayerDirection() {
        return adjustToPlayerDirection;
    }

    public void setAdjustToPlayerDirection(boolean adjustToPlayerDirection) {
        this.adjustToPlayerDirection = adjustToPlayerDirection;
    }
}
