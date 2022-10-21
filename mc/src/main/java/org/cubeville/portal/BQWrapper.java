package org.cubeville.portal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

class BQWrapper
{
    private Object betonQuest;
    private boolean bqNewVersion;

    private Method bqConditionMethod;
    private Method bqGetIdMethod;
    private Class<?> bqConditionIdClass;
    private Constructor<?> bqConditionIdCtor;

    BQWrapper(PluginManager pm) {
        betonQuest = pm.getPlugin("BetonQuest");
        if(betonQuest == null) return;

        try {
            bqConditionIdClass = Class.forName("org.betonquest.betonquest.id.ConditionID");
        }
        catch (ClassNotFoundException e) {
            betonQuest = null;
            return;
        }

        bqNewVersion = false;
        try {
            bqConditionMethod = betonQuest.getClass().getMethod("condition", Class.forName("org.betonquest.betonquest.api.profiles.Profile"), Class.forName("org.betonquest.betonquest.id.ConditionID"));
            Class<?> playerConverter = Class.forName("org.betonquest.betonquest.utils.PlayerConverter");
            bqGetIdMethod = playerConverter.getMethod("getID", Player.class);
            bqConditionIdCtor = bqConditionIdClass.getConstructor(Class.forName("org.betonquest.betonquest.api.config.QuestPackage"), String.class);
            bqNewVersion = true;
        }
        catch(Exception e) {
            try {
                bqConditionMethod = betonQuest.getClass().getMethod("condition", String.class, Class.forName("org.betonquest.betonquest.id.ConditionID"));
                bqConditionIdCtor = bqConditionIdClass.getConstructor(Class.forName("org.betonquest.betonquest.config.ConfigPackage"), String.class);
            }
            catch(Exception ex) {
                betonQuest = null;
            }
        }
    }

    boolean conditionIsTrue(Player player, String condition) {
        if(betonQuest == null) return false;

        try {
            if(bqNewVersion) {
                Object conditionId = bqConditionIdCtor.newInstance(null, condition);
                Object playerId = bqGetIdMethod.invoke(null, player);
                return (boolean)bqConditionMethod.invoke(null, playerId, conditionId);
            }
            else {
                Object conditionId = bqConditionIdCtor.newInstance(null, condition);
                return (boolean)bqConditionMethod.invoke(null, player.getUniqueId().toString(), conditionId);
            }
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

}
