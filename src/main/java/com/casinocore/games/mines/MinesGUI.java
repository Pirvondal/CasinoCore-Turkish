package com.casinocore.games.mines;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MinesGUI implements InventoryHolder {

    private static final int SLOT_STATUS = 4;
    private static final int SLOT_CASHOUT = 49;
    private static final int SLOT_BACK = 45;
    private static final int[] TILE_SLOTS = {
        10, 11, 12, 13, 14,
        19, 20, 21, 22, 23,
        28, 29, 30, 31, 32,
        37, 38, 39, 40, 41, 42
    };

    private final CasinoPlugin plugin;
    private final MinesGame game;
    private final Player player;
    private final double bet;
    private final Inventory inventory;
    private final Set<Integer> mineSlots = new HashSet<>();
    private final Set<Integer> revealedSlots = new HashSet<>();
    private boolean finished;

    public MinesGUI(CasinoPlugin plugin, MinesGame game, Player player, double bet) {
        this.plugin = plugin;
        this.game = game;
        this.player = player;
        this.bet = bet;
        this.inventory = Bukkit.createInventory(this, 54, Component.text(t("mines.gui.title-plain")));
        prepareMines();
        render();
    }

    public void open() {
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.0f);
    }

    public void handleClick(int slot) {
        if (slot == SLOT_BACK && finished) {
            game.backToHub(player);
            return;
        }
        if (slot == SLOT_CASHOUT) {
            game.cashOut(player, this);
            return;
        }
        if (finished || revealedSlots.contains(slot) || !isTileSlot(slot)) {
            return;
        }
        if (mineSlots.contains(slot)) {
            revealedSlots.add(slot);
            revealAll();
            render();
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.0f);
            game.hitMine(player, this);
            return;
        }
        revealedSlots.add(slot);
        render();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.2f);
    }

    public Player getPlayer() {
        return player;
    }

    public double getBet() {
        return bet;
    }

    public boolean isFinished() {
        return finished;
    }

    public void finish() {
        this.finished = true;
    }

    public int getSafeReveals() {
        int safe = 0;
        for (int slot : revealedSlots) {
            if (!mineSlots.contains(slot)) {
                safe++;
            }
        }
        return safe;
    }

    public double getCurrentPayout() {
        return Math.round(bet * game.getMultiplier(getSafeReveals()) * 100.0) / 100.0;
    }

    public String getFormattedMultiplier() {
        return String.format("%.2fx", game.getMultiplier(getSafeReveals()));
    }

    public void showCashedOut() {
        revealAll();
        render();
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    private void render() {
        ItemStack filler = item(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
        inventory.setItem(SLOT_STATUS, item(
            finished ? Material.EMERALD_BLOCK : Material.MAP,
            t("mines.gui.title"),
            f("mines.gui.bet", "amount", plugin.getEconomyManager().format(bet)),
            f("mines.gui.safe", "count", String.valueOf(getSafeReveals())),
            f("mines.gui.multiplier", "value", getFormattedMultiplier()),
            f("mines.gui.payout", "amount", plugin.getEconomyManager().format(getCurrentPayout()))
        ));

        int tileCount = game.getTileCount();
        for (int i = 0; i < TILE_SLOTS.length; i++) {
            int slot = TILE_SLOTS[i];
            if (i >= tileCount) {
                inventory.setItem(slot, item(Material.GRAY_STAINED_GLASS_PANE, " "));
            } else if (revealedSlots.contains(slot) && mineSlots.contains(slot)) {
                inventory.setItem(slot, item(Material.TNT, t("mines.gui.mine"), t("mines.gui.mine-lore")));
            } else if (revealedSlots.contains(slot)) {
                inventory.setItem(slot, item(Material.EMERALD, t("mines.gui.safe-tile"), t("mines.gui.safe-lore")));
            } else {
                inventory.setItem(slot, item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, t("mines.gui.hidden"), t("mines.gui.hidden-lore")));
            }
        }

        Material cashoutMaterial = getSafeReveals() > 0 && !finished ? Material.LIME_CONCRETE : Material.GRAY_DYE;
        inventory.setItem(SLOT_CASHOUT, item(cashoutMaterial, t("mines.gui.cashout"), t("mines.gui.cashout-lore")));
        inventory.setItem(SLOT_BACK, item(Material.BARRIER, t("mines.gui.back"), finished ? t("mines.gui.back-lore") : t("mines.gui.finish-first")));
    }

    private void prepareMines() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < game.getTileCount() && i < TILE_SLOTS.length; i++) {
            slots.add(TILE_SLOTS[i]);
        }
        Collections.shuffle(slots);
        mineSlots.addAll(slots.subList(0, game.getMineCount()));
    }

    private boolean isTileSlot(int slot) {
        for (int i = 0; i < game.getTileCount() && i < TILE_SLOTS.length; i++) {
            if (TILE_SLOTS[i] == slot) {
                return true;
            }
        }
        return false;
    }

    private void revealAll() {
        for (int i = 0; i < game.getTileCount() && i < TILE_SLOTS.length; i++) {
            revealedSlots.add(TILE_SLOTS[i]);
        }
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
