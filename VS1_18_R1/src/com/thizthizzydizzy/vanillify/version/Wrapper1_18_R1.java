package com.thizthizzydizzy.vanillify.version;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
public class Wrapper1_18_R1 implements VersionWrapper{
    @Override
    public void actionbar(Player player, String text){
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\""+text+"\"}"), ChatMessageType.c, new UUID(0L,0L));
        ((CraftPlayer)player).getHandle().b.a(packet);
    }
    @Override
    public void actionbarRaw(Player player, String text){
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(text), ChatMessageType.c, new UUID(0L,0L));
        ((CraftPlayer)player).getHandle().b.a(packet);
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
        net.minecraft.world.entity.Entity e = ((CraftEntity)entity).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        e.f(nbt);
        return nbt.h(tag);
    }
    @Override
    public float getEntityNBTFloat(Entity entity, String tag){
        net.minecraft.world.entity.Entity e = ((CraftEntity)entity).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        e.f(nbt);
        return nbt.j(tag);
    }
}