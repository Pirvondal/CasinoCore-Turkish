package com.casinocore.games.mines;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MinesListener implements Listener {

    private final MinesGame game;

    public MinesListener(MinesGame game) {
        this.game = game;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MinesGUI gui)) {
            return;
        }
        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player player) {
            game.handleClick(player, gui, event.getRawSlot());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof MinesGUI gui) {
            game.handleClose(gui);
        }
    }
}
