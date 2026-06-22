package com.casinocore.games.crash;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CrashListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CrashGUI gui)) {
            return;
        }

        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player player && event.getRawSlot() == 22) {
            gui.cashOut();
        }
    }
}
