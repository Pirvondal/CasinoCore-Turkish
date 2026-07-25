package com.casinocore.gui;

import com.casinocore.core.CasinoPlugin;
import com.casinocore.games.CasinoGame;
import com.casinocore.games.crash.CrashGame;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CasinoHubGUI implements InventoryHolder {

    private static final int SLOT_WALLET = 4;
    private static final int SLOT_STATUS = 13;
    private static final int SLOT_HELP = 40;
    private static final int SLOT_FOOTER = 41;
    private static final int SLOT_CUSTOM_BET = 45;
    private static final int SLOT_BET_MINUS_100 = 46;
    private static final int SLOT_BET_MINUS_10 = 47;
    private static final int SLOT_BET_MIN = 48;
    private static final int SLOT_BET_INFO = 49;
    private static final int SLOT_BET_MAX = 50;
    private static final int SLOT_BET_PLUS_10 = 51;
    private static final int SLOT_BET_PLUS_100 = 52;
    private static final int SLOT_CLOSE = 53;
    private static final int[] GAME_SLOTS = {10, 11, 12, 13, 14, 15, 16, 28, 29, 30, 31, 32, 33, 34};
    private static final Map<String, Material> GAME_MATERIALS = Map.ofEntries(
        Map.entry("coinflip", Material.SUNFLOWER),
        Map.entry("dice", Material.TARGET),
        Map.entry("blackjack", Material.PAPER),
        Map.entry("highlow", Material.REDSTONE_TORCH),
        Map.entry("ridethebus", Material.MINECART),
        Map.entry("doubleup", Material.BLAZE_POWDER),
        Map.entry("treasure", Material.CHEST),
        Map.entry("roulette", Material.CLOCK),
        Map.entry("slots", Material.DIAMOND),
        Map.entry("lottery", Material.EMERALD),
        Map.entry("horserace", Material.SADDLE),
        Map.entry("wheel", Material.NAUTILUS_SHELL)
        ,
        Map.entry("crash", Material.REDSTONE_BLOCK),
        Map.entry("mines", Material.TNT)
    );

    private static final Map<UUID, Double> SELECTED_BETS = new ConcurrentHashMap<>();

    private final CasinoPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public CasinoHubGUI(CasinoPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, Component.text(t("hub.title-plain")));
        SELECTED_BETS.putIfAbsent(player.getUniqueId(), plugin.getConfigManager().getMinBet());
        render();
    }

    public static void setSelectedBet(UUID playerId, double bet) {
        SELECTED_BETS.put(playerId, bet);
    }

    public void open() {
        if (!plugin.getRegionAccessManager().canUseCasino(player)) {
            plugin.getRegionAccessManager().sendBlockedMessage(player);
            return;
        }
        render();
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.0f);
    }

    public void handleClick(int slot) {
        if (!plugin.getAntiAbuseManager().tryRecordClick(player, "casino-hub")) {
            return;
        }

        if (slot >= 0 && slot < inventory.getSize()) {
            switch (slot) {
                case SLOT_CUSTOM_BET -> CustomBetManager.prompt(plugin, player);
                case SLOT_BET_MINUS_100 -> adjustBet(-100.0);
                case SLOT_BET_MINUS_10 -> adjustBet(-10.0);
                case SLOT_BET_MIN -> setBet(plugin.getConfigManager().getMinBet());
                case SLOT_BET_MAX -> setBet(plugin.getConfigManager().getMaxBet());
                case SLOT_BET_PLUS_10 -> adjustBet(10.0);
                case SLOT_BET_PLUS_100 -> adjustBet(100.0);
                case SLOT_CLOSE -> player.closeInventory();
                default -> {
                    String gameName = getGameForSlot(slot);
                    if (gameName != null) {
                        launchGame(gameName);
                    }
                }
            }
        }
    }

    public void render() {
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }

        fillAccentRows();
        renderSummary();
        renderGameGrid();
        renderBetControls();
    }

    private void fillAccentRows() {
        ItemStack accent = item(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int slot = 9; slot <= 17; slot++) {
            inventory.setItem(slot, accent);
        }
        for (int slot = 36; slot <= 44; slot++) {
            inventory.setItem(slot, accent);
        }
    }

    private void renderSummary() {
        double balance = plugin.getEconomyManager().getBalance(player);
        inventory.setItem(SLOT_WALLET, item(
            Material.NETHER_STAR,
            t("hub.wallet-title"),
            fmt("hub.wallet-balance", "amount", plugin.getEconomyManager().format(balance)),
            fmt("hub.wallet-wins", "value", String.valueOf(plugin.getPlayerStatsManager().getWins(player.getUniqueId()))),
            fmt("hub.wallet-losses", "value", String.valueOf(plugin.getPlayerStatsManager().getLosses(player.getUniqueId()))),
            fmt("hub.wallet-daily", "value", plugin.getPlayerStatsManager().canClaimDaily(player.getUniqueId()) ? t("hub.daily-ready-short") : t("hub.daily-claimed-short"))
        ));
        inventory.setItem(SLOT_STATUS, item(
            Material.GOLD_BLOCK,
            t("hub.selected-bet-title"),
            "<yellow>" + plugin.getEconomyManager().format(getSelectedBet()) + "</yellow>",
            fmt("hub.games-played", "value", String.valueOf(plugin.getPlayerStatsManager().getGamesPlayed(player.getUniqueId()))),
            fmt("hub.streak", "value", String.valueOf(plugin.getPlayerStatsManager().getWinStreak(player.getUniqueId()))),
            fmt("hub.best-streak", "value", String.valueOf(plugin.getPlayerStatsManager().getBestWinStreak(player.getUniqueId())))
        ));
    }

    private void renderGameGrid() {
        List<CasinoGame> games = new ArrayList<>(plugin.getGameManager().getEnabledCasinoGames().values());
        games.sort(Comparator.comparing(CasinoGame::getName));
        for (int i = 0; i < GAME_SLOTS.length; i++) {
            if (i >= games.size()) {
                inventory.setItem(GAME_SLOTS[i], item(Material.BARRIER, t("hub.coming-soon-title"), t("hub.coming-soon-lore")));
                continue;
            }

            CasinoGame game = games.get(i);
            Material material = GAME_MATERIALS.getOrDefault(game.getName(), Material.GOLD_INGOT);
            setGameItem(GAME_SLOTS[i], game, material, t("hub.click." + game.getName()));
        }

        inventory.setItem(SLOT_HELP, item(Material.BOOK, t("hub.help-title"), t("hub.help-1"), t("hub.help-2"), t("hub.help-3")));
        inventory.setItem(SLOT_FOOTER, player.hasPermission("casinocore.admin")
            ? item(Material.COMPARATOR, t("hub.admin-title"), t("hub.admin-lore-1"), t("hub.admin-lore-2"))
            : item(Material.AMETHYST_SHARD, t("hub.tips-title"), t("hub.tips-1"), t("hub.tips-2")));
    }

    private void renderBetControls() {
        inventory.setItem(SLOT_CUSTOM_BET, item(Material.WRITABLE_BOOK, t("hub.custom-bet-title"), t("hub.custom-bet-lore")));
        inventory.setItem(SLOT_BET_MINUS_100, item(Material.REDSTONE, "<red>-100</red>", t("hub.lower-bet")));
        inventory.setItem(SLOT_BET_MINUS_10, item(Material.RED_DYE, "<red>-10</red>", t("hub.fine-tune-bet")));
        inventory.setItem(SLOT_BET_MIN, item(Material.BARRIER, t("hub.set-min"), "<white>" + plugin.getEconomyManager().format(plugin.getConfigManager().getMinBet()) + "</white>"));
        inventory.setItem(SLOT_BET_INFO, item(Material.GOLD_INGOT, t("hub.current-bet-title"), "<yellow>" + plugin.getEconomyManager().format(getSelectedBet()) + "</yellow>", t("hub.current-bet-lore")));
        inventory.setItem(SLOT_BET_MAX, item(Material.EMERALD_BLOCK, t("hub.set-max"), "<white>" + plugin.getEconomyManager().format(plugin.getConfigManager().getMaxBet()) + "</white>"));
        inventory.setItem(SLOT_BET_PLUS_10, item(Material.LIME_DYE, "<green>+10</green>", t("hub.fine-tune-bet")));
        inventory.setItem(SLOT_BET_PLUS_100, item(Material.EMERALD, "<green>+100</green>", t("hub.raise-bet")));
        inventory.setItem(SLOT_CLOSE, item(Material.BARRIER, t("hub.close-title"), t("hub.close-lore")));
    }

    private void setGameItem(int slot, CasinoGame game, Material material, String clickLine) {
        List<String> lore = new ArrayList<>();
        lore.add(t("hub.theme." + game.getName()));
        lore.add("<gray>" + game.getDescription() + "</gray>");
        lore.add(t("hub.how." + game.getName()));
        lore.add(fmt("hub.min-bet", "amount", plugin.getEconomyManager().format(game.getMinBet())));
        lore.add(fmt("hub.max-bet", "amount", plugin.getEconomyManager().format(game.getMaxBet())));
        lore.add("<yellow>" + clickLine + "</yellow>");

        if (game instanceof DiceRollGame) {
            lore.add(t("hub.dice-default"));
        } else if (game instanceof CoinFlipGame) {
            lore.add(t("hub.coinflip-extra"));
        } else if (game instanceof CrashGame) {
            lore.add(t("hub.crash-extra"));
        }

        String title = t("hub.game-title." + game.getName());
        if (title.equals("hub.game-title." + game.getName())) {
            title = "<gold><bold>" + game.getDisplayName() + "</bold></gold>";
        }
        inventory.setItem(slot, item(material, title, lore.toArray(new String[0])));
    }

    private void launchGame(String gameName) {
        CasinoGame game = plugin.getGameManager().getCasinoGameDirect(gameName);
        if (game == null) {
            player.sendMessage(t("hub.game-unavailable"));
            return;
        }

        double bet = getSelectedBet();
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);

        if (!plugin.getRegionAccessManager().canUseCasino(player)) {
            plugin.getRegionAccessManager().sendBlockedMessage(player);
            return;
        }

        if (game instanceof CoinFlipGame coinFlipGame) {
            coinFlipGame.play(player, bet);
        } else if (game instanceof DiceRollGame diceRollGame) {
            diceRollGame.openRiskSelection(player, bet);
        } else {
            game.play(player, bet);
        }
    }

    private void adjustBet(double delta) {
        setBet(getSelectedBet() + delta);
    }

    private void setBet(double value) {
        double min = plugin.getConfigManager().getMinBet();
        double max = plugin.getConfigManager().getMaxBet();
        double clamped = Math.max(min, Math.min(max, value));
        SELECTED_BETS.put(player.getUniqueId(), Math.round(clamped * 100.0) / 100.0);
        render();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
    }

    private double getSelectedBet() {
        return SELECTED_BETS.getOrDefault(player.getUniqueId(), plugin.getConfigManager().getMinBet());
    }

    private String getGameForSlot(int slot) {
        List<CasinoGame> games = new ArrayList<>(plugin.getGameManager().getEnabledCasinoGames().values());
        games.sort(Comparator.comparing(CasinoGame::getName));
        for (int i = 0; i < GAME_SLOTS.length && i < games.size(); i++) {
            if (GAME_SLOTS[i] == slot) {
                return games.get(i).getName();
            }
        }
        return null;
    }

    private String t(String key) {
        return plugin.getLocaleManager().getText(key);
    }

    private String fmt(String key, String placeholder, String value) {
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
