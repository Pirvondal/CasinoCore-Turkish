package com.casinocore.integrations;

import com.casinocore.core.CasinoPlugin;
import com.casinocore.utils.BetLogManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class CasinoPlaceholderExpansion extends PlaceholderExpansion {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final CasinoPlugin plugin;

    public CasinoPlaceholderExpansion(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "casino";
    }

    @Override
    public String getAuthor() {
        return "CasinoCore Team";
    }

    @Override
    public String getVersion() {
        return plugin.getPlugin().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String key = params.toLowerCase();
        if (player == null
            && !key.equals("available_games")
            && !key.equals("total_games")
            && !key.equals("enabled_games")
            && !key.equals("enabled_games_list")
            && !key.equals("plugin_version")
            && !key.equals("locale")
            && !key.equals("min_bet")
            && !key.equals("max_bet")
            && !key.equals("daily_reward")
            && !key.equals("economy")
            && !key.startsWith("time_")) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        BetLogManager.PeriodStats weekStats = player == null ? new BetLogManager.PeriodStats(0, 0, 0)
            : plugin.getBetLogManager().getCurrentWeekStats(player.getUniqueId());
        BetLogManager.PeriodStats monthStats = player == null ? new BetLogManager.PeriodStats(0, 0, 0)
            : plugin.getBetLogManager().getCurrentMonthStats(player.getUniqueId());
        BetLogManager.PeriodStats lastMonthStats = player == null ? new BetLogManager.PeriodStats(0, 0, 0)
            : plugin.getBetLogManager().getPreviousMonthStats(player.getUniqueId());

        return switch (key) {
            case "balance" -> plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player));
            case "balance_raw" -> String.format("%.2f", plugin.getEconomyManager().getBalance(player));
            case "wins" -> String.valueOf(plugin.getPlayerStatsManager().getWins(player.getUniqueId()));
            case "wins_w", "wins_week" -> String.valueOf(weekStats.wins());
            case "wins_m", "wins_month" -> String.valueOf(monthStats.wins());
            case "wins_lm", "wins_last_month" -> String.valueOf(lastMonthStats.wins());
            case "losses" -> String.valueOf(plugin.getPlayerStatsManager().getLosses(player.getUniqueId()));
            case "losses_w", "losses_week" -> String.valueOf(weekStats.losses());
            case "losses_m", "losses_month" -> String.valueOf(monthStats.losses());
            case "losses_lm", "losses_last_month" -> String.valueOf(lastMonthStats.losses());
            case "games_played" -> String.valueOf(plugin.getPlayerStatsManager().getGamesPlayed(player.getUniqueId()));
            case "games_played_w", "games_played_week" -> String.valueOf(weekStats.gamesPlayed());
            case "games_played_m", "games_played_month" -> String.valueOf(monthStats.gamesPlayed());
            case "games_played_lm", "games_played_last_month" -> String.valueOf(lastMonthStats.gamesPlayed());
            case "streak" -> String.valueOf(plugin.getPlayerStatsManager().getWinStreak(player.getUniqueId()));
            case "best_streak" -> String.valueOf(plugin.getPlayerStatsManager().getBestWinStreak(player.getUniqueId()));
            case "daily_ready" -> String.valueOf(plugin.getPlayerStatsManager().canClaimDaily(player.getUniqueId()));
            case "next_daily" -> plugin.getUxManager().formatNextDailyClaim(player);
            case "available_games" -> String.valueOf(plugin.getGameManager().getEnabledCasinoGames().size());
            case "enabled_games" -> String.valueOf(plugin.getGameManager().getEnabledCasinoGames().size());
            case "total_games" -> String.valueOf(plugin.getGameManager().getAllCasinoGames().size());
            case "enabled_games_list" -> String.join(", ", plugin.getGameManager().getEnabledCasinoGames().keySet());
            case "plugin_version" -> plugin.getPlugin().getDescription().getVersion();
            case "locale" -> plugin.getLocaleManager().getActiveLocale();
            case "min_bet" -> plugin.getEconomyManager().format(plugin.getConfigManager().getMinBet());
            case "max_bet" -> plugin.getEconomyManager().format(plugin.getConfigManager().getMaxBet());
            case "daily_reward" -> plugin.getEconomyManager().format(plugin.getConfigManager().getConfig().getDouble("daily-rewards.free-spin-value", 0.0));
            case "economy" -> plugin.getEconomyManager().getEconomyName();
            case "time_year" -> String.valueOf(now.getYear());
            case "time_month" -> String.valueOf(now.getMonthValue());
            case "time_month_name" -> now.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
            case "time_week" -> String.valueOf(now.get(WeekFields.ISO.weekOfWeekBasedYear()));
            case "time_day" -> String.valueOf(now.getDayOfMonth());
            case "time_day_name" -> now.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
            case "time_date" -> now.format(DATE_FORMAT);
            case "time_clock" -> now.format(TIME_FORMAT);
            case "last_bet_game" -> getLastSnapshotValue(player, snapshot -> snapshot.game());
            case "last_bet_section" -> getLastSnapshotValue(player, snapshot -> snapshot.section());
            case "last_bet_amount" -> getLastSnapshotDetail(player, "amount", "bet");
            case "last_bet_status" -> getLastSnapshotDetail(player, "status", "result");
            case "last_bet_payout" -> getLastSnapshotDetail(player, "payout", "0");
            case "last_bet_profit" -> getLastSnapshotDetail(player, "profit", "0");
            default -> null;
        };
    }

    private String getLastSnapshotValue(OfflinePlayer player, java.util.function.Function<BetLogManager.BetLogSnapshot, String> mapper) {
        if (player == null) {
            return "";
        }
        BetLogManager.BetLogSnapshot snapshot = plugin.getBetLogManager().getLatestSnapshot(player.getUniqueId());
        return snapshot == null ? "" : mapper.apply(snapshot);
    }

    private String getLastSnapshotDetail(OfflinePlayer player, String key, String fallback) {
        if (player == null) {
            return fallback;
        }
        BetLogManager.BetLogSnapshot snapshot = plugin.getBetLogManager().getLatestSnapshot(player.getUniqueId());
        if (snapshot == null) {
            return fallback;
        }
        return snapshot.details().getOrDefault(key, fallback);
    }
}
