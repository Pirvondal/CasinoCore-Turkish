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
        this.inventory = Bukkit.createInventory(this, 27, Component.text(t("crash.gui.title-plain")));
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
        inventory.setItem(4, item(Material.GOLD_INGOT, t("crash.gui.header"), f("crash.gui.bet", "amount", plugin.getEconomyManager().format(bet))));
        inventory.setItem(13, item(Material.COMPASS, f("crash.gui.multiplier", "value", String.format("%.2f", multiplier) + "x"), t("crash.gui.cashout-lore")));
        inventory.setItem(22, item(Material.EMERALD, t("crash.gui.cashout"), t("crash.gui.cashout-lore")));
    }

    public void bumpMultiplier(double value) {
        this.multiplier = value;
    }

    public void cashOut() {
        game.cashOut(player, this);
    }

    public void showCashout(double payout) {
        inventory.setItem(13, item(Material.EMERALD_BLOCK, t("crash.gui.cashed-out"), f("crash.gui.payout", "amount", plugin.getEconomyManager().format(payout))));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public void showCrash() {
        inventory.setItem(13, item(Material.REDSTONE_BLOCK, t("crash.gui.crashed"), t("crash.gui.crashed-lore")));
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
