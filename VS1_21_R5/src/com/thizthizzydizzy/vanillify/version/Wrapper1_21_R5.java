package com.thizthizzydizzy.vanillify.version;
import net.minecraft.world.level.CommandBlockListenerAbstract;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftMinecartCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
public class Wrapper1_21_R5 implements VersionWrapper{
    @Override
    public void actionbar(Player player, String text){
        runCommand(player.getWorld(), "title "+player.getName()+" actionbar {\"text\":\""+text+"\"}");
    }
    @Override
    public void actionbarRaw(Player player, String text){
        runCommand(player.getWorld(), "title "+player.getName()+" actionbar "+text);
    }
    @Override
    public void modifyEntityNBT(Entity entity, String tag, Object value){
        String val = value.toString();
        String suffix = "";
        if(value instanceof Boolean){
            val = (((Boolean)value)?1:0)+"";
            suffix = "b";
        }else if(value instanceof Byte)suffix = "b";
        else if(value instanceof Double)suffix = "d";
        else if(value instanceof Float)suffix = "f";
        else if(value instanceof Integer)suffix = "i";
        else if(value instanceof Long)suffix = "l";
        else if(value instanceof Short)suffix = "s";
        else
            throw new IllegalArgumentException("Unsupported NBT data type: "+(value==null?value:value.getClass().getName())+"! (Vanillify only supports primitive types, and not strings)");
        runCommand(entity.getWorld(), "data modify entity "+entity.getUniqueId().toString()+" "+tag+" set value "+val+suffix);
    }
    @Override
    public int getEntityNBTInt(Entity entity, String tag){
        String output = runCommandAndGetOutput(entity.getWorld(), "data get entity " +entity.getUniqueId().toString()+" "+tag);
        String[] bits = output.split("has the following entity data: ");
        output = bits[bits.length-1];
        if(Character.isDigit(output.charAt(output.length()-1)))output = output.substring(0, output.length()-1);
        return Integer.parseInt(output);
    }
    @Override
    public float getEntityNBTFloat(Entity entity, String tag){
        String output = runCommandAndGetOutput(entity.getWorld(), "data get entity " +entity.getUniqueId().toString()+" "+tag);
        String[] bits = output.split("has the following entity data: ");
        output = bits[bits.length-1];
        if(Character.isDigit(output.charAt(output.length()-1)))output = output.substring(0, output.length()-1);
        return Float.parseFloat(output);
    }
    private void runCommand(World world, String command){
        boolean val = false;
        val = world.getGameRuleValue(GameRule.COMMAND_BLOCK_OUTPUT);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        CommandMinecart cart = (CommandMinecart)world.spawnEntity(new Location(world, 0, 0, 0), EntityType.COMMAND_BLOCK_MINECART);
        cart.setCustomName("Vanillify");
        Bukkit.dispatchCommand(cart, command);
        cart.remove();
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, val);
    }
    private String runCommandAndGetOutput(World world, String command){
        boolean val = false;
        val = world.getGameRuleValue(GameRule.COMMAND_BLOCK_OUTPUT);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        CommandMinecart cart = (CommandMinecart)world.spawnEntity(new Location(world, 0, 0, 0), EntityType.COMMAND_BLOCK_MINECART);
        cart.setCustomName("Vanillify");
        String output = null;
        try{
            CommandBlockListenerAbstract listener = ((CraftMinecartCommand)cart).getHandle().e();
            listener.c(null); // Clear "last output", probably not even neccesary given that this is the first command lol
            Bukkit.dispatchCommand(cart, command);
            output = listener.l().getString();
        }finally{
            cart.remove();
            world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, val);
        }
        return output;
    }
}