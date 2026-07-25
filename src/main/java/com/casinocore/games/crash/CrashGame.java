package com.casinocore.games.crash;

import com.casinocore.core.CasinoPlugin;
import com.casinocore.games.BaseCasinoGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CrashGame extends BaseCasinoGame {

    private final Map<UUID, CrashGUI> sessions = new ConcurrentHashMap<>();

    public CrashGame(CasinoPlugin plugin) {
        super(plugin, "crash", "Crash", "Watch the multiplier rise and cash out before it crashes.");
    }

    @Override
    protected boolean executeGame(Player player, double bet) {
        CrashGUI gui = new CrashGUI(plugin, this, player, bet);
        sessions.put(player.getUniqueId(), gui);
        Bukkit.getScheduler().runTask(plugin.getPlugin(), gui::open);
        return true;
    }

    public void cashOut(Player player, CrashGUI gui) {
        if (gui.isFinished()) {
            return;
        }
        gui.finish();
        double payout = gui.getCurrentPayout();
        if (payWinnings(player, payout)) {
            handleWin(player, gui.getBet(), payout);
            gui.showCashout(payout);
        }
        sessions.remove(player.getUniqueId());
    }

    public void resolveCrash(Player player, CrashGUI gui) {
        if (gui.isFinished()) {
            return;
        }
        gui.finish();
        handleLoss(player, gui.getBet());
        gui.showCrash();
        sessions.remove(player.getUniqueId());
    }

    public void startTicker(CrashGUI gui) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gui.isFinished() || !gui.getPlayer().isOnline()) {
                    cancel();
                    return;
                }
                gui.bumpMultiplier(nextTickMultiplier(gui.getMultiplier()));
                gui.render();
                if (gui.getMultiplier() >= getMaxMultiplier() || ThreadLocalRandom.current().nextDouble() < getCrashChance(gui.getMultiplier())) {
                    cancel();
                    resolveCrash(gui.getPlayer(), gui);
                }
            }
        }.runTaskTimer(plugin.getPlugin(), 20L, getTickIntervalTicks());
    }

    public double nextTickMultiplier(double current) {
        double step = 1.02 + (ThreadLocalRandom.current().nextDouble() * 0.03);
        return Math.round(current * step * 100.0) / 100.0;
    }

    public void handleClose(CrashGUI gui) {
        if (gui.isFinished()) {
            sessions.remove(gui.getPlayer().getUniqueId());
            return;
        }
        if (!plugin.getConfigManager().getConfig().getBoolean("anti-abuse.crash.block-inventory-close-during-round", true)) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin.getPlugin(), () -> {
            if (!gui.isFinished() && sessions.containsKey(gui.getPlayer().getUniqueId())) {
                gui.getPlayer().openInventory(gui.getInventory());
            }
        }, 1L);
    }

    public double getActiveMultiplier(UUID playerId) {
        CrashGUI gui = sessions.get(playerId);
        return gui == null || gui.isFinished() ? 0.0 : gui.getMultiplier();
    }

    public double getActivePayout(UUID playerId) {
        CrashGUI gui = sessions.get(playerId);
        return gui == null || gui.isFinished() ? 0.0 : gui.getCurrentPayout();
    }

    public double getActiveProfit(UUID playerId) {
        CrashGUI gui = sessions.get(playerId);
        return gui == null || gui.isFinished() ? 0.0 : gui.getCurrentProfit();
    }

    public String getActiveRisk(UUID playerId) {
        CrashGUI gui = sessions.get(playerId);
        return gui == null || gui.isFinished() ? "none" : gui.getRiskKey();
    }

    public double getMaxMultiplier() {
        return plugin.getConfigManager().getConfig().getDouble("games.crash.max-multiplier", 8.0);
    }

    private long getTickIntervalTicks() {
        return Math.max(2L, plugin.getConfigManager().getConfig().getLong("games.crash.tick-interval-ticks", 12L));
    }

    private double getCrashChance(double multiplier) {
        double base = plugin.getConfigManager().getConfig().getDouble("games.crash.crash-chance-base", 0.08);
        double factor = plugin.getConfigManager().getConfig().getDouble("games.crash.crash-chance-multiplier-factor", 0.03);
        return Math.max(0.0, Math.min(0.95, base + (multiplier * factor)));
    }

    @Override
    public boolean canPlay(Player player) {
        return !sessions.containsKey(player.getUniqueId()) && super.canPlay(player);
    }
}
