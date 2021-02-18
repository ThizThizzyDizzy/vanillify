package com.thizthizzydizzy.vanillify;
import com.thizthizzydizzy.vanillify.version.VersionMatcher;
import com.thizthizzydizzy.vanillify.version.VersionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
public class Vanillify{
    private static VersionWrapper WRAPPER = new VersionMatcher().match();
    public static void actionbar(Player player, String text){
        WRAPPER.actionbar(player, text);
    }
    /**
     * Teleport an entity using relative coordinates. This results in a seamless teleport for clients.
     * @param entity the entity to teleport
     * @param to the location to teleport the entity to
     */
    public static void smoothTeleport(Entity entity, Location to){
        World world = entity.getWorld();
        if(world!=to.getWorld())entity.teleport(to);
        else{
            boolean val = world.getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK);
            world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
            Location from = entity.getLocation();
            double xDiff = to.getX()-from.getX();
            double yDiff = to.getY()-from.getY();
            double zDiff = to.getZ()-from.getZ();
            double xRotDiff = to.getYaw()-from.getYaw();
            double yRotDiff = to.getPitch()-from.getPitch();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute as "+entity.getUniqueId().toString()+" at @s run tp @s ~"+xDiff+" ~"+yDiff+" ~"+zDiff+" ~"+xRotDiff+" ~"+yRotDiff);
            world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, val);
        }
    }
}