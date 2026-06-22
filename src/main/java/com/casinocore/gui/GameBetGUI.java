package com.casinocore.gui;

import com.casinocore.core.CasinoPlugin;
import com.casinocore.games.CasinoGame;
import com.casinocore.games.diceroll.DiceRollGame;
import com.casinocore.games.impl.CoinFlipGame;
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
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameBetGUI implements GUI {

    private static final int SLOT_GAME = 4;
    private static final int SLOT_BET = 13;
    private static final int SLOT_MINUS_10 = 21;
    private static final int SLOT_CUSTOM = 22;
    private static final int SLOT_PLUS_10 = 23;
    private static final int SLOT_PLAY = 31;
    private static final int SLOT_BACK = 35;

    private final CasinoPlugin plugin;
    private final Player player;
    private final CasinoGame game;
    private final Inventory inventory;
    private double bet;

    public GameBetGUI(CasinoPlugin plugin, Player player, CasinoGame game, double bet) {
        this.plugin = plugin;
        this.player = player;
        this.game = game;
        this.bet = bet;
        this.inventory = Bukkit.createInventory(this, 45, Component.text(game.getDisplayName()));
        render();
    }

    public void open() {
        open(player);
    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.0f);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clicker) || clicker.getUniqueId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            handleClick(event.getRawSlot());
        }
    }

    public void handleClick(int slot) {
        if (slot == SLOT_MINUS_10) {
            setBet(bet - 10.0);
        } else if (slot == SLOT_CUSTOM) {
            CustomBetManager.prompt(plugin, player, this::setBet, this::open);
        } else if (slot == SLOT_PLUS_10) {
            setBet(bet + 10.0);
        } else if (slot == SLOT_PLAY) {
            launch();
        } else if (slot == SLOT_BACK) {
            player.closeInventory();
            new CasinoHubGUI(plugin, player).open();
        }
    }

    @Override
    public void close(Player player) {
        player.closeInventory();
    }

    public void setBet(double bet) {
        this.bet = Math.max(game.getMinBet(), Math.min(game.getMaxBet(), Math.round(bet * 100.0) / 100.0));
        render();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.15f);
    }

    private void render() {
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(SLOT_GAME, item(Material.GOLD_INGOT, "<gold><bold>" + game.getDisplayName() + "</bold></gold>", game.getDescription()));
        inventory.setItem(SLOT_BET, item(Material.PAPER, t("hub.current-bet-title"),
            f("hub.min-bet", "amount", plugin.getEconomyManager().format(game.getMinBet())),
            "<yellow>" + plugin.getEconomyManager().format(bet) + "</yellow>",
            f("hub.max-bet", "amount", plugin.getEconomyManager().format(game.getMaxBet()))));
        inventory.setItem(SLOT_MINUS_10, item(Material.RED_DYE, "<red>-10</red>", t("hub.lower-bet")));
        inventory.setItem(SLOT_CUSTOM, item(Material.WRITABLE_BOOK, t("hub.custom-bet-title"), t("hub.custom-bet-lore")));
        inventory.setItem(SLOT_PLUS_10, item(Material.LIME_DYE, "<green>+10</green>", t("hub.raise-bet")));
        inventory.setItem(SLOT_PLAY, item(Material.LIME_CONCRETE, "<green>Play</green>", t("hub.click." + game.getName())));
        inventory.setItem(SLOT_BACK, item(Material.BARRIER, t("hub.close-title"), t("hub.close-lore")));
    }

    private void launch() {
        player.closeInventory();
        if (game instanceof CoinFlipGame coinFlipGame) {
            coinFlipGame.play(player, bet);
        } else if (game instanceof DiceRollGame diceRollGame) {
            diceRollGame.openRiskSelection(player, bet);
        } else {
            game.play(player, bet);
        }
    }

    @Override
    public String getTitle() {
        return game.getDisplayName();
    }

    @Override
    public int getSize() {
        return inventory.getSize();
    }

    @Override
    public void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    @Override
    public void setItem(int slot, ItemStack item, ClickHandler handler) {
        inventory.setItem(slot, item);
    }

    @Override
    public void refresh() {
        render();
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
