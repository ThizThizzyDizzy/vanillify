package com.thizthizzydizzy.vanillify.version;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
public class Wrapper1_19_R3 implements VersionWrapper{
    @Override
    public void actionbar(Player player, String text){
    }
    @Override
    public void actionbarRaw(Player player, String text){
    }
    @Override
    public void modifyEntityNBT(Entity entity, String tag, Object value){
    }
    @Override
    public float getEntityNBTFloat(Entity entity, String tag){
        return 0;
    }
    @Override
    public int getEntityNBTInt(Entity entity, String tag){
        return 0;
    }
}