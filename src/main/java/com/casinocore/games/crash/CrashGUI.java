package com.casinocore.games.crash;

import com.casinocore.core.CasinoPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrashGUI implements InventoryHolder {

    private static final int SLOT_HEADER = 4;
    private static final int SLOT_MULTIPLIER = 13;
    private static final int SLOT_PROFIT = 20;
    private static final int SLOT_RISK = 24;
    private static final int SLOT_CASHOUT = 31;
    private static final int[] PROGRESS_SLOTS = {10, 11, 12, 14, 15, 16};

    private final CasinoPlugin plugin;
    private final CrashGame game;
    private final Player player;
    private final double bet;
    private final Inventory inventory;

    private double multiplier = 1.0;
    private boolean finished;

    public CrashGUI(CasinoPlugin plugin, CrashGame game, Player player, double bet) {
        this.plugin = plugin;
        this.game = game;
        this.player = player;
        this.bet = bet;
        this.inventory = Bukkit.createInventory(this, 45, Component.text(t("crash.gui.title-plain")));
        render();
    }

    public void open() {
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.8f, 1.0f);
        game.startTicker(this);
    }

    public void render() {
        ItemStack filler = item(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
        inventory.setItem(SLOT_HEADER, item(
            Material.GOLD_INGOT,
            t("crash.gui.header"),
            f("crash.gui.bet", "amount", plugin.getEconomyManager().format(bet)),
            f("crash.gui.max", "value", formatMultiplier(game.getMaxMultiplier()))
        ));
        inventory.setItem(SLOT_MULTIPLIER, item(
            Material.COMPASS,
            f("crash.gui.multiplier", "value", formatMultiplier(multiplier)),
            f("crash.gui.live-payout", "amount", plugin.getEconomyManager().format(getCurrentPayout())),
            t("crash.gui.cashout-lore")
        ));
        inventory.setItem(SLOT_PROFIT, item(
            Material.EMERALD,
            t("crash.gui.profit-title"),
            f("crash.gui.profit", "amount", plugin.getEconomyManager().format(getCurrentProfit()))
        ));
        inventory.setItem(SLOT_RISK, item(
            getRiskMaterial(),
            f("crash.gui.risk", "value", t("crash.gui.risk-level." + getRiskKey())),
            t("crash.gui.risk-lore")
        ));
        renderProgress();
        inventory.setItem(SLOT_CASHOUT, item(Material.LIME_CONCRETE, t("crash.gui.cashout"), t("crash.gui.cashout-lore")));
    }

    public void bumpMultiplier(double value) {
        this.multiplier = value;
    }

    public void cashOut() {
        game.cashOut(player, this);
    }

    public void showCashout(double payout) {
        inventory.setItem(SLOT_MULTIPLIER, item(
            Material.EMERALD_BLOCK,
            t("crash.gui.cashed-out"),
            f("crash.gui.payout", "amount", plugin.getEconomyManager().format(payout)),
            f("crash.gui.final-multiplier", "value", formatMultiplier(multiplier))
        ));
        inventory.setItem(SLOT_CASHOUT, item(Material.GRAY_DYE, t("crash.gui.locked"), t("crash.gui.round-finished")));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public void showCrash() {
        inventory.setItem(SLOT_MULTIPLIER, item(
            Material.REDSTONE_BLOCK,
            t("crash.gui.crashed"),
            t("crash.gui.crashed-lore"),
            f("crash.gui.final-multiplier", "value", formatMultiplier(multiplier))
        ));
        inventory.setItem(SLOT_CASHOUT, item(Material.GRAY_DYE, t("crash.gui.locked"), t("crash.gui.round-finished")));
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.0f);
    }

    public Player getPlayer() {
        return player;
    }

    public double getBet() {
        return bet;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public double getCurrentPayout() {
        return Math.round(bet * multiplier * 100.0) / 100.0;
    }

    public double getCurrentProfit() {
        return Math.round((getCurrentPayout() - bet) * 100.0) / 100.0;
    }

    public String getRiskKey() {
        if (multiplier >= 5.0) {
            return "extreme";
        }
        if (multiplier >= 3.0) {
            return "high";
        }
        if (multiplier >= 2.0) {
            return "medium";
        }
        return "low";
    }

    public boolean isFinished() {
        return finished;
    }

    public void finish() {
        this.finished = true;
    }

    private String t(String key) {
        return plugin.getLocaleManager().getText(key);
    }

    private String f(String key, String placeholder, String value) {
        return plugin.getLocaleManager().formatText(key, Map.of(placeholder, value));
    }

    private void renderProgress() {
        double ratio = Math.min(1.0, multiplier / game.getMaxMultiplier());
        int litSlots = Math.max(1, (int) Math.ceil(ratio * PROGRESS_SLOTS.length));
        for (int i = 0; i < PROGRESS_SLOTS.length; i++) {
            Material material = i < litSlots ? getProgressMaterial() : Material.GRAY_STAINED_GLASS_PANE;
            inventory.setItem(PROGRESS_SLOTS[i], item(material, " "));
        }
    }

    private Material getRiskMaterial() {
        return switch (getRiskKey()) {
            case "extreme" -> Material.REDSTONE_BLOCK;
            case "high" -> Material.ORANGE_CONCRETE;
            case "medium" -> Material.YELLOW_CONCRETE;
            default -> Material.LIME_CONCRETE;
        };
    }

    private Material getProgressMaterial() {
        return switch (getRiskKey()) {
            case "extreme" -> Material.RED_STAINED_GLASS_PANE;
            case "high" -> Material.ORANGE_STAINED_GLASS_PANE;
            case "medium" -> Material.YELLOW_STAINED_GLASS_PANE;
            default -> Material.LIME_STAINED_GLASS_PANE;
        };
    }

    private String formatMultiplier(double value) {
        return String.format("%.2f", value) + "x";
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageManager().parse(name));
            List<Component> lines = new ArrayList<>();
            for (String line : lore) {
                lines.add(plugin.getMessageManager().parse(line));
            }
            meta.lore(lines);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
