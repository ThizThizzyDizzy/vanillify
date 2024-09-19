package com.thizthizzydizzy.vanillify.version;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
public class Wrapper1_17_R1 implements VersionWrapper{
    @Override
    public void actionbar(Player player, String text){
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\""+text+"\"}"), ChatMessageType.c, new UUID(0L,0L));
        ((CraftPlayer)player).getHandle().b.sendPacket(packet);
    }
    @Override
    public void actionbarRaw(Player player, String text){
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(text), ChatMessageType.c, new UUID(0L,0L));
        ((CraftPlayer)player).getHandle().b.sendPacket(packet);
    }
    @Override
    public void modifyEntityNBT(Entity entity, String tag, Object value){
        net.minecraft.world.entity.Entity e = ((CraftEntity)entity).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        e.save(nbt);
        if(value instanceof Boolean)nbt.setBoolean(tag, (boolean)value);
        else if(value instanceof Byte)nbt.setByte(tag, (byte)value);
        else if(value instanceof Double)nbt.setDouble(tag, (double)value);
        else if(value instanceof Float)nbt.setFloat(tag, (float)value);
        else if(value instanceof Integer)nbt.setInt(tag, (int)value);
        else if(value instanceof Long)nbt.setLong(tag, (long)value);
        else if(value instanceof Short)nbt.setShort(tag, (short)value);
        else if(value instanceof String)nbt.setString(tag, (String)value);
        else throw new IllegalArgumentException("Invalid NBT data type: "+(value==null?value:value.getClass().getName())+"! (Vanillify only supports primitive types)");
        e.load(nbt);
    }
    @Override
    public int getEntityNBTInt(Entity entity, String tag){
        net.minecraft.world.entity.Entity e = ((CraftEntity)entity).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        e.save(nbt);
        return nbt.getInt(tag);
    }
    @Override
    public float getEntityNBTFloat(Entity entity, String tag){
        net.minecraft.world.entity.Entity e = ((CraftEntity)entity).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        e.save(nbt);
        return nbt.getFloat(tag);
    }
}