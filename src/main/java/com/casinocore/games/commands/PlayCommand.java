package com.casinocore.games.commands;

import com.casinocore.core.CasinoPlugin;
import com.casinocore.games.CasinoGame;
import com.casinocore.games.diceroll.DiceRollGame;
import com.casinocore.games.diceroll.RiskLevel;
import com.casinocore.games.impl.CoinFlipGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PlayCommand implements CommandExecutor, TabCompleter {

    private final CasinoPlugin plugin;

    public PlayCommand(CasinoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                plugin.getMessageManager().sendMessage(sender, "player-only");
                return true;
            }

            if (!plugin.getRegionAccessManager().canUseCasino(player)) {
                plugin.getRegionAccessManager().sendBlockedMessage(player);
                return true;
            }

            if (!plugin.getAntiAbuseManager().tryRecordCommand(player)) {
                player.sendMessage(plugin.getLocaleManager().getText("command.too-fast"));
                return true;
            }

            if (args.length == 0) {
                sendUsage(player);
                return true;
            }

            if (args[0].equalsIgnoreCase("list")) {
                listGames(player);
                return true;
            }

            String gameName = args[0].toLowerCase();
            CasinoGame game = plugin.getGameManager().getCasinoGameDirect(gameName);
            if (game == null) {
                player.sendMessage(plugin.getLocaleManager().formatText(
                    "command.game-not-found",
                    plugin.getLocaleManager().placeholders("game", gameName)
                ));
                return true;
            }

            if (game instanceof CoinFlipGame coinFlipGame) {
                return handleCoinFlipCommand(player, args, coinFlipGame);
            }

            if (args.length < 2) {
                sendUsage(player);
                return true;
            }

            if (game instanceof DiceRollGame diceGame && args[1].equalsIgnoreCase("info")) {
                diceGame.showRiskLevels(player);
                return true;
            }

            double bet;
            try {
                bet = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getLocaleManager().getText("command.invalid-bet"));
                return true;
            }

            if (game instanceof DiceRollGame diceGame) {
                return handleDiceCommand(player, args, bet, diceGame);
            }

            game.play(player, bet);
            return true;
        } catch (Exception e) {
            plugin.getPlugin().getLogger().log(Level.SEVERE, "Error handling /play", e);
            sender.sendMessage(plugin.getLocaleManager().getText("command.error"));
            return true;
        }
    }

    private boolean handleCoinFlipCommand(Player player, String[] args, CoinFlipGame game) {
        if (args.length < 2) {
            sendUsage(player);
            return true;
        }

        String subcommand = args[1].toLowerCase();
        switch (subcommand) {
            case "create" -> {
                if (args.length < 3) {
                    player.sendMessage("Usage: /play coinflip create <bet>");
                    return true;
                }

                double bet;
                try {
                    bet = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getLocaleManager().getText("command.invalid-bet"));
                    return true;
                }
                return game.createOffer(player, bet);
            }
            case "join" -> {
                if (args.length < 3) {
                    player.sendMessage("Usage: /play coinflip join <player>");
                    return true;
                }
                return game.joinOffer(player, args[2]);
            }
            case "cancel" -> {
                return game.cancelOffer(player);
            }
            case "list" -> {
                game.showOpenOffers(player);
                return true;
            }
            default -> {
                player.sendMessage("Coinflip usage: /play coinflip <create|join|cancel|list>");
                return true;
            }
        }
    }

    private boolean handleDiceCommand(Player player, String[] args, double bet, DiceRollGame diceGame) {
        RiskLevel riskLevel = RiskLevel.MEDIUM;
        if (args.length >= 3) {
            riskLevel = RiskLevel.fromString(args[2]);
            if (riskLevel == null) {
                player.sendMessage("Invalid dice risk. Use low, medium, or high.");
                return true;
            }
        }

        diceGame.playWithRisk(player, bet, riskLevel);
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(plugin.getLocaleManager().getText("command.usage-header"));
        player.sendMessage("/play list");
        player.sendMessage("/play <game> <bet>");
        player.sendMessage("/play dice <bet> [low|medium|high]");
        player.sendMessage("/play highlow <bet>");
        player.sendMessage("/play ridethebus <bet>");
        player.sendMessage("/play doubleup <bet>");
        player.sendMessage("/play treasure <bet>");
        player.sendMessage("/play mines <bet>");
        player.sendMessage("/play coinflip create <bet>");
        player.sendMessage("/play coinflip join <player>");
        player.sendMessage("/play coinflip cancel");
        player.sendMessage("/play coinflip list");
    }

    private void listGames(Player player) {
        try {
            Map<String, CasinoGame> games = plugin.getGameManager().getEnabledCasinoGames();
            if (games.isEmpty()) {
                player.sendMessage(plugin.getLocaleManager().getText("command.no-games"));
                return;
            }

            player.sendMessage(plugin.getLocaleManager().getText("command.games-header"));
            for (CasinoGame game : games.values()) {
                player.sendMessage("- " + game.getDisplayName() + ": " + game.getDescription());
                if (game instanceof CoinFlipGame) {
                    player.sendMessage("  /play coinflip create <bet>");
                    player.sendMessage("  /play coinflip join <player>");
                    player.sendMessage("  /play coinflip list");
                } else if (game instanceof DiceRollGame diceGame) {
                    player.sendMessage("  /play dice <bet> [low|medium|high]");
                    player.sendMessage("  " + diceGame.getRiskSummary());
                } else {
                    player.sendMessage("  /play " + game.getName() + " <bet>");
                }
            }
        } catch (Exception e) {
            plugin.getPlugin().getLogger().log(Level.SEVERE,
                "Error listing games for " + player.getName(), e);
            player.sendMessage(plugin.getLocaleManager().getText("command.error"));
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("list");
            completions.addAll(plugin.getGameManager().getCasinoGameNames());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("coinflip")) {
                completions.add("create");
                completions.add("join");
                completions.add("cancel");
                completions.add("list");
            } else {
                completions.add("10");
                completions.add("50");
                completions.add("100");
                completions.add("500");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("dice")) {
            completions.add("low");
            completions.add("medium");
            completions.add("high");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("coinflip") && args[1].equalsIgnoreCase("create")) {
            completions.add("10");
            completions.add("50");
            completions.add("100");
            completions.add("500");
        }

        return completions;
    }
}
