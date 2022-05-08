package moe.nyan10.utils.internal;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;

import moe.nyan10.utils.ItemListInventory;
import moe.nyan10.utils.ItemListInventory.Status;

public class EventListener implements Listener {

    private Map<Player, ItemListInventory> inventories = new HashMap<>();


    public void hook(Player player, ItemListInventory inv) {
        player.closeInventory();
        inventories.put(player, inv);
    }


    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent event) {
        ItemListInventory inv = inventories.get(event.getPlayer());

        if (inv != null && inv.getStatus() != Status.PAGE_CHANGING)
            inventories.remove(event.getPlayer());
    }


    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventoryClicked(InventoryClickEvent event) {
        if (!(event.getSlotType() == SlotType.CONTAINER  || event.getSlotType() == SlotType.QUICKBAR))
            return;

        Player player = (Player) event.getWhoClicked();
        ItemListInventory inv = inventories.get(player);

        if (inv != null) {
            if (event.getCurrentItem().getType() == Material.AIR)
                return;
            event.setCancelled(true);

            int slot = event.getRawSlot();
            if (slot >= 0 && slot < 45) {
                inv.select(slot, event.getClick());
            } else if (slot == 45) {
                inv.openPrevPage();
            } else if (slot > 45 && slot < 53) {
                inv.openAt((int)(((inv.getNPages()-1)/6.0)*(slot-46)));
            } else if (slot == 53) {
                inv.openNextPage();
            }
        }
    }

}
