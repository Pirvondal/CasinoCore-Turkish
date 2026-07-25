package com.casinocore.games.mines;

import com.casinocore.core.CasinoPlugin;
import com.casinocore.games.BaseCasinoGame;
import com.casinocore.gui.GuiNavigation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MinesGame extends BaseCasinoGame {

    private final Map<UUID, MinesGUI> sessions = new ConcurrentHashMap<>();

    public MinesGame(CasinoPlugin plugin) {
        super(plugin, "mines", "Mines", "Reveal safe tiles and cash out before hitting a mine.");
    }

    @Override
    protected boolean executeGame(Player player, double bet) {
        MinesGUI gui = new MinesGUI(plugin, this, player, bet);
        sessions.put(player.getUniqueId(), gui);
        Bukkit.getScheduler().runTask(plugin.getPlugin(), gui::open);
        return true;
    }

    public void handleClick(Player player, MinesGUI gui, int slot) {
        gui.handleClick(slot);
    }

    public void cashOut(Player player, MinesGUI gui) {
        if (gui.isFinished() || gui.getSafeReveals() <= 0) {
            return;
        }
        gui.finish();
        double payout = gui.getCurrentPayout();
        if (payWinnings(player, payout)) {
            handleWin(player, gui.getBet(), payout);
            sendMessage(player, plugin.getLocaleManager().formatText("mines.result.win", Map.of(
                "bet", plugin.getEconomyManager().format(gui.getBet()),
                "safe", String.valueOf(gui.getSafeReveals()),
                "multiplier", gui.getFormattedMultiplier(),
                "payout", plugin.getEconomyManager().format(payout)
            )));
            gui.showCashedOut();
        } else {
            sendMessage(player, plugin.getLocaleManager().getText("mines.payout-failed"));
        }
        sessions.remove(player.getUniqueId());
    }

    public void hitMine(Player player, MinesGUI gui) {
        if (gui.isFinished()) {
            return;
        }
        gui.finish();
        handleLoss(player, gui.getBet());
        sendMessage(player, plugin.getLocaleManager().formatText("mines.result.loss", Map.of(
            "bet", plugin.getEconomyManager().format(gui.getBet()),
            "safe", String.valueOf(gui.getSafeReveals())
        )));
        sessions.remove(player.getUniqueId());
    }

    public void backToHub(Player player) {
        sessions.remove(player.getUniqueId());
        player.closeInventory();
        GuiNavigation.openHub(plugin, player);
    }

    public void handleClose(MinesGUI gui) {
        if (gui.isFinished()) {
            sessions.remove(gui.getPlayer().getUniqueId());
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin.getPlugin(), () -> {
            if (sessions.containsKey(gui.getPlayer().getUniqueId())) {
                gui.getPlayer().openInventory(gui.getInventory());
            }
        }, 1L);
    }

    public int getTileCount() {
        return Math.max(6, Math.min(21, plugin.getConfigManager().getConfig().getInt("games.mines.tiles", 15)));
    }

    public int getMineCount() {
        return Math.max(1, Math.min(getTileCount() - 1, plugin.getConfigManager().getConfig().getInt("games.mines.mines", 3)));
    }

    public double getMultiplier(int safeReveals) {
        double base = plugin.getConfigManager().getConfig().getDouble("games.mines.base-multiplier", 1.0);
        double step = plugin.getConfigManager().getConfig().getDouble("games.mines.safe-step-multiplier", 0.35);
        return Math.round((base + (safeReveals * step)) * 100.0) / 100.0;
    }

    @Override
    public boolean canPlay(Player player) {
        return !sessions.containsKey(player.getUniqueId()) && super.canPlay(player);
    }
}
