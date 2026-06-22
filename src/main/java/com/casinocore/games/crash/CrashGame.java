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
        double payout = gui.getBet() * gui.getMultiplier();
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
                if (gui.getMultiplier() >= 8.0 || ThreadLocalRandom.current().nextDouble() < 0.08 + (gui.getMultiplier() * 0.03)) {
                    cancel();
                    resolveCrash(gui.getPlayer(), gui);
                }
            }
        }.runTaskTimer(plugin.getPlugin(), 20L, 12L);
    }

    public double nextTickMultiplier(double current) {
        double step = 1.02 + (ThreadLocalRandom.current().nextDouble() * 0.03);
        return Math.round(current * step * 100.0) / 100.0;
    }

    @Override
    public boolean canPlay(Player player) {
        return !sessions.containsKey(player.getUniqueId()) && super.canPlay(player);
    }
}
