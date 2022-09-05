package moe.nyan10.utils.internal;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class EventListener implements Listener {

    private Map<Player, HookableInventory> inventories = new HashMap<>();


    public void hook(Player player, HookableInventory inv) {
        player.closeInventory();
        inventories.put(player, inv);
    }


    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent event) {
        HookableInventory inv = inventories.get(event.getPlayer());

        if (inv != null && !inv.isReplacing)
            inventories.remove(event.getPlayer());
    }


    @EventHandler
    public void onInventoryClicked(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        HookableInventory inv = inventories.get(player);

        if (inv != null) {
    		inv.onClickInventory(event);
        }
    }

}
