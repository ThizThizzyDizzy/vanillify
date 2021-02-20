package com.thizthizzydizzy.vanillify;
import com.thizthizzydizzy.vanillify.version.VersionMatcher;
import com.thizthizzydizzy.vanillify.version.VersionWrapper;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.util.Vector;
public class Vanillify{
    private static final VersionWrapper WRAPPER = new VersionMatcher().match();
    public static boolean hideOutput = false;
    public static void actionbar(Player player, String text){
        WRAPPER.actionbar(player, text);
    }
    public static void smoothTeleport(Entity entity, Location to){
        World world = entity.getWorld();
        if(world!=to.getWorld())entity.teleport(to);
        else{
            Location from = entity.getLocation();
            double xDiff = to.getX()-from.getX();
            double yDiff = to.getY()-from.getY();
            double zDiff = to.getZ()-from.getZ();
            double xRotDiff = to.getYaw()-from.getYaw();
            double yRotDiff = to.getPitch()-from.getPitch();
            runCommand(world, "execute at "+entity.getUniqueId().toString()+" run tp "+entity.getUniqueId().toString()+" ~"+xDiff+" ~"+yDiff+" ~"+zDiff+" ~"+xRotDiff+" ~"+yRotDiff);
        }
    }
    public static void setVelocity(Entity entity, Vector velocity){
        runCommand(entity.getWorld(), "data merge entity "+entity.getUniqueId().toString()+" {Motion:["+velocity.getX()+"d,"+velocity.getY()+"d,"+velocity.getZ()+"d]}");
    }
    public static void setPower(Fireball fireball, Vector power){
        runCommand(fireball.getWorld(), "data merge entity "+fireball.getUniqueId().toString()+" {power:["+power.getX()+"d,"+power.getY()+"d,"+power.getZ()+"d]}");
    }
    public static <T extends Entity> T summon(EntityType entityType, Location location){
        World world = location.getWorld();
        UUID uid = UUID.randomUUID();
        int a = (int)(uid.getMostSignificantBits() >> 32);
        int b = (int)uid.getMostSignificantBits();
        int c = (int)(uid.getLeastSignificantBits() >> 32);
        int d = (int)uid.getLeastSignificantBits();
        runCommand(world, "summon "+entityType.getKey().toString()+" "+location.getX()+" "+location.getY()+" "+location.getZ()+" {UUID:[I;"+a+","+b+","+c+","+d+"]}");
        for(Entity e : world.getEntitiesByClass(entityType.getEntityClass())){
            if(e.getUniqueId().equals(uid))return (T)e;
        }
        throw new IllegalArgumentException("Translated the UUID wrong! uh oh!");
    }
    public static void runCommand(World world, String command){
        boolean val = false;
        if(hideOutput){
            val = world.getGameRuleValue(GameRule.COMMAND_BLOCK_OUTPUT);
            world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        }
        CommandMinecart cart = (CommandMinecart)world.spawnEntity(new Location(world, 0, 0, 0), EntityType.MINECART_COMMAND);
        cart.setCustomName("Vanillify");
        Bukkit.dispatchCommand(cart, command);
        cart.remove();
        if(hideOutput)world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, val);
    }
}