package org.cubeville.portal.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandResponse;
import org.cubeville.commons.commands.Command;


public class PortalListEnumsYML extends Command
{
    public PortalListEnumsYML() {
        super("listenumsyml");
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        List<String> potionEffectTypes = new ArrayList<>();
        for(PotionEffectType pet: PotionEffectType.values()) {
            potionEffectTypes.add(pet.getName());
        }
        Collections.sort(potionEffectTypes);
        
        List<String> sounds = new ArrayList<>();
        for(Sound sound: Sound.values()) {
            sounds.add(sound.toString());
        }
        Collections.sort(sounds);
        
        List<String> particles = new ArrayList<>();
        for(Particle particle: Particle.values()) {
            particles.add(particle.toString());
        }
        Collections.sort(particles);
        
        FileConfiguration config = new YamlConfiguration();
        config.set("PotionEffectType", potionEffectTypes);
        config.set("Sound", sounds);
        config.set("Particle", particles);
        
        return new CommandResponse(config.saveToString());
    }
}
