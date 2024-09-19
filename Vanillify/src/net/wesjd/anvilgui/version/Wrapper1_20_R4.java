package net.wesjd.anvilgui.version;

import org.bukkit.entity.Player;

public final class Wrapper1_20_R4 implements VersionWrapper {
    @Override
    public int getNextContainerId(Player player, AnvilContainerWrapper container){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void handleInventoryCloseEvent(Player player){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void sendPacketOpenWindow(Player player, int containerId, Object inventoryTitle){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void sendPacketCloseWindow(Player player, int containerId){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void setActiveContainerDefault(Player player){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void setActiveContainer(Player player, AnvilContainerWrapper container){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void setActiveContainerId(AnvilContainerWrapper container, int containerId){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void addActiveContainerSlotListener(AnvilContainerWrapper container, Player player){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public AnvilContainerWrapper newContainerAnvil(Player player, Object title){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public Object literalChatComponent(String content){
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public Object jsonChatComponent(String json){
        throw new UnsupportedOperationException("Not supported yet.");
    }
}