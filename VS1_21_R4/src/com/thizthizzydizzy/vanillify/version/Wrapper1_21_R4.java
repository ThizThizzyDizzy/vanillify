package com.thizthizzydizzy.vanillify.version;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
public class Wrapper1_21_R4 implements VersionWrapper{
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
        net.minecraft.world.entity.Entity e = ((CraftEntity)entity).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        e.f(nbt);
        if(value instanceof Boolean)nbt.a(tag, (boolean)value);
        else if(value instanceof Byte)nbt.a(tag, (byte)value);
        else if(value instanceof Double)nbt.a(tag, (double)value);
        else if(value instanceof Float)nbt.a(tag, (float)value);
        else if(value instanceof Integer)nbt.a(tag, (int)value);
        else if(value instanceof Long)nbt.a(tag, (long)value);
        else if(value instanceof Short)nbt.a(tag, (short)value);
        else if(value instanceof String)nbt.a(tag, (String)value);
        else throw new IllegalArgumentException("Invalid NBT data type: "+(value==null?value:value.getClass().getName())+"! (Vanillify only supports primitive types)");
        e.g(nbt);
    }
    @Override
    public int getEntityNBTInt(Entity entity, String tag){
        throw new UnsupportedOperationException("This feature is not currently supported by Vanillify for this version.");
    }
    @Override
    public float getEntityNBTFloat(Entity entity, String tag){
        throw new UnsupportedOperationException("This feature is not currently supported by Vanillify for this version.");
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
}