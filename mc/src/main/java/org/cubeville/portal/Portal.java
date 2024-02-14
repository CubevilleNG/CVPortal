package org.cubeville.portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Particle;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import org.cubeville.portal.actions.Action;
import org.cubeville.portal.actions.Teleport;

@SerializableAs("Portal")
public class Portal implements ConfigurationSerializable
{
    private String name;
    private UUID world;
    private Vector minCorner;
    private Vector maxCorner;
    private boolean permanent;
    private boolean deathTriggered;
    private boolean keepInventory;
    private boolean loginTriggered;
    private int cooldown;
    private int globalCooldown;
    private String permission;
    private int minYaw;
    private int maxYaw;
    private String condition;
    private boolean moveEventTriggered;
    private boolean jumpEventTriggered;
    private Particle particle;
    private int particleDelay;

    private boolean disabled = false;
    
    private int particleDelayCounter;
    
    private Map<UUID, Long> cooldownTimer = new HashMap<>();
    private Long globalCooldownTimer = 0L;
    
    private List<Action> actions;

    private Random random = new Random();

    public Portal(String name, World world, Vector minCorner, Vector maxCorner) {
        this.name = name;
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
        this.world = world.getUID();
        this.cooldown = 0;
        this.globalCooldown = 0;
        this.permanent = true;
        this.deathTriggered = false;
        this.keepInventory = false;
        this.permission = null;
        this.condition = null;
        this.minYaw = 0;
        this.maxYaw = 0;
        this.loginTriggered = false;
        this.moveEventTriggered = false;
	this.jumpEventTriggered = false;
        this.particle = null;
        this.particleDelay = 0;
        actions = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public Portal(Map<String, Object> config) {
        minCorner = (Vector) config.get("minCorner");
        maxCorner = (Vector) config.get("maxCorner");
        world = UUID.fromString((String) config.get("world"));
        name = (String) config.get("name");
        permanent = (boolean) config.get("permanent");
        actions = (List<Action>) config.get("actions");
        cooldown = (int) config.get("cooldown");
        if(config.get("globalCooldown") != null)
            globalCooldown = (int) config.get("globalCooldown");
        else
            globalCooldown = 0;
        permission = null;
        if(config.get("permission") != null) {
            permission = (String) config.get("permission");
            if(permission.equals("")) permission = null;
        }

        condition = null;
        if(config.get("condition") != null) {
            condition = (String) config.get("condition");
            if(condition.equals("")) condition = null;
        }

        if(config.get("minYaw") != null && config.get("maxYaw") != null) {
            minYaw = (int) config.get("minYaw");
            maxYaw = (int) config.get("maxYaw");
        }
        else {
            minYaw = 0;
            maxYaw = 0;
        }

        if(config.get("deathTriggered") == null) deathTriggered = false;
        else deathTriggered = (boolean) config.get("deathTriggered");

        if(config.get("keepInventory") == null) keepInventory = false;
        else keepInventory = (boolean) config.get("keepInventory");

        if(config.get("loginTriggered") == null) loginTriggered = false;
        else loginTriggered = (boolean) config.get("loginTriggered");

        if(config.get("moveEventTriggered") == null) moveEventTriggered = false;
        else moveEventTriggered = (boolean) config.get("moveEventTriggered");

        if(config.get("jumpEventTriggered") == null) jumpEventTriggered = false;
        else jumpEventTriggered = (boolean) config.get("jumpEventTriggered");

        if(config.get("particle") == null) particle = null;
        else particle = Particle.valueOf((String) config.get("particle"));

        if(config.get("particleDelay") == null) particleDelay = 0;
        else particleDelay = (int) config.get("particleDelay");
    }

    public Map<String, Object> serialize() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("minCorner", minCorner);
        ret.put("maxCorner", maxCorner);
        ret.put("world", world.toString());
        ret.put("name", name);
        ret.put("permanent", permanent);
        ret.put("actions", actions);
        ret.put("cooldown", cooldown);
        ret.put("globalCooldown", globalCooldown);
        ret.put("deathTriggered", deathTriggered);
        ret.put("keepInventory", keepInventory);
        ret.put("loginTriggered", loginTriggered);
        ret.put("permission", permission == null ? "" : permission);
        ret.put("condition", condition == null ? "" : condition);
        ret.put("minYaw", minYaw);
        ret.put("maxYaw", maxYaw);
        ret.put("moveEventTriggered", moveEventTriggered);
        ret.put("jumpEventTriggered", jumpEventTriggered);
        if(particle != null) {
            ret.put("particle", particle.toString());
            ret.put("particleDelay", particleDelay);
        }
        return ret;
    }

    public void cyclicTrigger(Collection<Player> players) {
        if(permanent == true && moveEventTriggered == false && jumpEventTriggered == false && disabled == false) trigger(players);
    }

    public void particleTrigger() {
        if(particle == null || minCorner == null) return;
        if(particleDelay == 0 || ++particleDelayCounter >= particleDelay) {
            particleDelayCounter = 0;
            double rndx = random.nextDouble() * 0.7 + 0.15;
            double rndy = random.nextDouble() * 0.7 + 0.15;
            double rndz = random.nextDouble() * 0.7 + 0.15;
            double x = minCorner.getX() + (maxCorner.getX() - minCorner.getX()) * rndx;
            double y = minCorner.getY() + (maxCorner.getY() - minCorner.getY()) * rndy;
            double z = minCorner.getZ() + (maxCorner.getZ() - minCorner.getZ()) * rndz;
            Bukkit.getServer().getWorld(world).spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0.0);
        }
    }
    
    public void moveEventTrigger(Player player) {
	if(permanent == false || moveEventTriggered == false || disabled == true) return;
        trigger(player);
    }

    public void jumpEventTrigger(Player player) {
	if(permanent == false || jumpEventTriggered == false || disabled == true) return;
	trigger(player);
    }
    
    public void trigger(Collection<Player> players) {
        for(Player player: players) trigger(player, false);
    }

    public boolean trigger(Player player) {
        return trigger(player, false);
    }

    private boolean playerHasPermission(Player player) {
        if(permission == null || permission.equals("")) return true;
	if(permission.startsWith("!")) {
	    return !player.hasPermission("cvportal.portal." + permission.substring(1));
	}
	else {
	    return player.hasPermission("cvportal.portal." + permission);
	}
    }

    public boolean conditionIsTrue(Player player) {
        if(condition == null || condition.equals("")) return true;
        if(condition.startsWith("!")) {
            return !CVPortal.getInstance().conditionIsTrue(player, condition.substring(1));
        }
        else {
            return CVPortal.getInstance().conditionIsTrue(player, condition);
        }
    }
    
    public void triggerRandom(Collection<Player> players, int count) {
        List<Player> playersInPortal = new ArrayList<>();
        for(Player player: players) {
            if(isPlayerInPortal(player) && playerHasPermission(player) && conditionIsTrue(player)) {
                playersInPortal.add(player);
            }
        }
        
        while(playersInPortal.size() > 0 && count > 0) {
            int i = random.nextInt(playersInPortal.size());
            executeActions(playersInPortal.remove(i));
            count--;
        }
    }
    
    public boolean trigger(Player player, boolean overrideCooldown) {
        if(isPlayerInPortal(player) && playerHasPermission(player) && conditionIsTrue(player)) {
            long now = System.currentTimeMillis();
            
            if(globalCooldown != 0 && overrideCooldown == false) {
                if(now - globalCooldownTimer < globalCooldown) return false;
            }
            
            if(cooldown == 0 || overrideCooldown) {
                globalCooldownTimer = now;
                executeActions(player);
                return true;
            }

            UUID uuid = player.getUniqueId();
            if(cooldownTimer.containsKey(uuid)) {
                if(now - cooldownTimer.get(uuid) < cooldown) return false;
            }
            cooldownTimer.put(uuid, now);
            globalCooldownTimer = now;
            executeActions(player);
            return true;
        }
        return false;
    }

    public void sendMessage(Collection<Player> players, String message) {
        for(Player player: players) {
            if(isPlayerInPortal(player) && playerHasPermission(player) && conditionIsTrue(player)) {
                player.sendMessage(message);
            }
        }
    }

    public void sendTitle(Collection<Player> players, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for(Player player: players) {
            if(isPlayerInPortal(player) && playerHasPermission(player) && conditionIsTrue(player)) {
                player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            }
        }
    }

    public void runSuCommand(Collection<Player> players, String command) {
        for(Player player: players) {
            if(isPlayerInPortal(player)) {
                String cmd = command;
                cmd = cmd.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
    }
    
    public void executeActions(Player player) {
        String actionList = "";
        for(Action action: actions) {
            if(actionList.length() > 0) actionList += ",";
            actionList += action.getShortInfo();
            action.execute(player);
        }
    }

    public void executeActionsWithConditionCheck(Player player) {
        if(conditionIsTrue(player)) {
            executeActions(player);
        }
    }
    
    public void executeActionsExceptTeleport(Player player) {
        for(Action action: actions) {
            if(!(action instanceof Teleport)) {
                action.execute(player);
            }
        }
    }
    
    public Location getTeleportLocation() {
        for(Action action: actions) {
            if(action instanceof Teleport) {
                return ((Teleport) action).getLocation();
            }
        }
        return null;
    }

    public boolean isPlayerNearTarget(Player player, double radius) {
        Location tloc = getTeleportLocation();
        if(tloc == null) return false;
        Location ploc = player.getLocation();
        if(!ploc.getWorld().getUID().equals(tloc.getWorld().getUID())) return false;
        return (ploc.distance(tloc) < radius);
    }
    
    public boolean isPlayerInPortal(Player player) {
        if(minCorner == null) return false;
        Location loc = player.getLocation();
        if(!loc.getWorld().getUID().equals(world)) return false;
        Vector vloc = loc.toVector();
        if(!vloc.isInAABB(minCorner, maxCorner)) return false;
        if(minYaw != maxYaw) {
            int pyaw = (int) loc.getYaw();
            while(pyaw < -180) pyaw += 360;
            while(pyaw > 180) pyaw -= 360;
            if(minYaw < maxYaw) {
                if(pyaw < minYaw || pyaw > maxYaw) return false;
            }
            else {
                if(pyaw < minYaw && pyaw > maxYaw) return false;
            }
        }
        return true;
    }

    public boolean isPlayerNearPortal(Player player, double radius) {
        if(minCorner == null) return false;
        Location loc = player.getLocation();
        if(!loc.getWorld().getUID().equals(world)) return false;
        Vector vloc = loc.toVector();
        Vector min = minCorner.clone().subtract(new Vector(radius, radius, radius));
        Vector max = maxCorner.clone().add(new Vector(radius, radius, radius));
        return vloc.isInAABB(min, max);
    }

    public String getInfo() {
        String ret = name;
        if(minCorner == null) {
            ret += " (regionless)";
        }
        else {
            Vector max = maxCorner.clone().subtract(new Vector(1, 1, 1));
            ret += " (" + minCorner.getX() + "," + minCorner.getY() + "," + minCorner.getZ() + " - " + max.getX() + "," + max.getY() + "," + max.getZ();
            if(minYaw != maxYaw) {
                ret += "; " + minYaw + "-" + maxYaw;
            }
            ret += ")";
        }

        String a = "";
        for(Action action: actions) {
            if(a.length() > 0) a += ",";
            a += action.getShortInfo();
        }
        if(a.length() > 0) a = " " + a;
        ret += a;
        return ret;
    }
    
    public List<String> getLongInfo() {
        List<String> ret = new ArrayList<String>();
        ret.add("&6" + name + "&r:");
        if(minCorner == null) {
            ret.add("&6Region: &cregionless");
        }
        else {
            Vector max = maxCorner.clone().subtract(new Vector(1, 1, 1));
            String sloc = "&6Region: &a" + minCorner.getX() + "," + minCorner.getY() + "," + minCorner.getZ() + " - " + max.getX() + "," + max.getY() + "," + max.getZ();
            if(minYaw != maxYaw) {
                sloc += "; " + minYaw + "-" + maxYaw;
            }
            sloc += " in " + Bukkit.getWorld(world).getName();
            ret.add(sloc);
        }
        String permissionString = "&cNone";
        if(permission != null && (!permission.equals(""))) permissionString = "&a" + permission;
        ret.add("&bPermission: " + permissionString);
        String conditionString = "&cNone";
        if(condition != null && (!condition.equals(""))) conditionString = "&a" + condition;
        ret.add("&bCondition: " + conditionString);
        String parameter = "";
        parameter += "&bPermanent: ";
        if(permanent) { parameter += "&aEnabled"; }
        else { parameter += "&cDisabled"; }
        ret.add(parameter);
        ret.add("&bDeath Triggered: " + (deathTriggered ? "&aEnabled" : "&cDisabled"));
        ret.add("&bLogin Triggered: " + (loginTriggered ? "&aEnabled" : "&cDisabled"));
        ret.add("&bKeep Inventory: " + (keepInventory ? "&aEnabled" : "&cDisabled"));
	if(particle != null) {
	    ret.add("&bParticles: " + particle + " with delay " + particleDelay);
	}
        parameter = "&bCooldown Time (seconds): ";
        if((cooldown / 1000) <= 0) { parameter += "&cNo cooldown"; }
        else { parameter += ("&a" + (cooldown / 1000)); }
        ret.add(parameter);
        parameter = "&bGlobal Cooldown Time (seconds): ";
        if((globalCooldown / 1000) <= 0) { parameter += "&cNo global cooldown"; } else { parameter += ("&a" + (globalCooldown / 1000)); }
        ret.add(parameter);
        ret.add("&6Actions:");
        for(Action action: actions) {
            ret.add(action.getLongInfo());
        }
        return ret;
    }

    public void addAction(Action action) {
        if(action.isSingular()) {
            String t = action.getClass().getName();
            for(int i = actions.size() - 1; i >= 0; i--) {
                if(actions.get(i).getClass().getName().equals(t)) {
                    actions.remove(i);
                }
            }
        }
        actions.add(action);
    }

    public List<Action> getActionsByType(Class<? extends Action> Type) {
        List<Action> ret = new ArrayList<>();
        for(Action action: actions) {
            if(action.getClass().getName().equals(Type.getName())) {
                ret.add(action);
            }
        }
        return ret;
    }

    public int countActionsByType(Class<? extends Action> Type) {
        int ret = 0;
        for(Action action: actions) {
            if(action.getClass().getName().equals(Type.getName())) {
                ret++;
            }
        }
        return ret;
    }

    public boolean removeActionByType(Class<? extends Action> Type, int Index) {
        int aIndex = 0;
        for(int i = 0; i < actions.size(); i++) {
            if(actions.get(i).getClass().getName().equals(Type.getName())) {
                if(aIndex == Index) {
                    actions.remove(i);
                    return true;
                }
                aIndex++;
            }
        }
        return false;
    }
    
    public void redefine(World world, Vector minCorner, Vector maxCorner) {
        this.world = world.getUID();
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
    }

    public void move(Vector offset) {
        this.minCorner.add(offset);
        this.maxCorner.add(offset);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) { // should not be changed for registered portals!
        this.name = name;
    }
    
    public Vector getMinCorner() {
        return minCorner;
    }

    public Vector getMaxCorner() {
        return maxCorner;
    }

    public UUID getWorld() {
        return world;
    }

    public void setWorld(UUID world) {
        this.world = world;
    }
    
    public void setActive(boolean active) {
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void setGlobalCooldown(int globalCooldown) {
        this.globalCooldown = globalCooldown;
    }
    
    public boolean isDeathTriggered() {
        return deathTriggered;
    }

    public void setDeathTriggered(boolean deathTriggered) {
        this.deathTriggered = deathTriggered;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }

    public boolean isLoginTriggered() {
        return loginTriggered;
    }

    public void setMoveEventTriggered(boolean moveEventTriggered) {
        this.moveEventTriggered = moveEventTriggered;
	if(moveEventTriggered) jumpEventTriggered = false;
    }

    public boolean isMoveEventTriggered() {
        return moveEventTriggered;
    }
    
    public void setJumpEventTriggered(boolean jumpEventTriggered) {
	this.jumpEventTriggered = jumpEventTriggered;
	if(jumpEventTriggered) moveEventTriggered = false;
    }

    public boolean isJumpEventTriggered() {
	return jumpEventTriggered;
    }
    
    public void setLoginTriggered(boolean loginTriggered) {
        this.loginTriggered = loginTriggered;
    }
    
    public void setPermission(String permission) {
        if(permission.equals("")) {
            this.permission = null;
        }
        else {
            this.permission = permission;
        }
    }

    public void setCondition(String condition) {
        if(condition.equals("")) {
            this.condition = null;
        }
        else {
            this.condition = condition;
        }
    }

    public void setYaw(int minYaw, int maxYaw) {
        this.minYaw = minYaw;
        this.maxYaw = maxYaw;
    }

    public void setParticle(Particle particle, int particleDelay) {
        this.particle = particle;
        this.particleDelay = particleDelay;
    }

    public boolean isDisabled() {
	return disabled;
    }

    public void setDisabled(boolean disabled) {
	this.disabled = disabled;
    }
}

