package com.casinocore.core;

import com.casinocore.crash.ExceptionInterceptor;
import com.casinocore.core.commands.CasinoCommand;
import com.casinocore.economy.EconomyManager;
import com.casinocore.games.blackjack.BlackjackGame;
import com.casinocore.games.blackjack.BlackjackListener;
import com.casinocore.games.crash.CrashGame;
import com.casinocore.games.crash.CrashListener;
import com.casinocore.games.commands.PlayCommand;
import com.casinocore.games.diceroll.DiceRollGame;
import com.casinocore.games.diceroll.DiceRiskListener;
import com.casinocore.games.doubleup.DoubleUpGame;
import com.casinocore.games.doubleup.DoubleUpListener;
import com.casinocore.games.highlow.HighLowGame;
import com.casinocore.games.highlow.HighLowListener;
import com.casinocore.games.GameManager;
import com.casinocore.games.horserace.HorseRaceGame;
import com.casinocore.games.horserace.HorseRaceListener;
import com.casinocore.games.impl.CoinFlipListener;
import com.casinocore.games.impl.CoinFlipGame;
import com.casinocore.games.impl.CoinFlipGuiListener;
import com.casinocore.games.impl.LotteryDrawListener;
import com.casinocore.games.impl.LotteryGame;
import com.casinocore.games.impl.LotteryPromptListener;
import com.casinocore.integrations.citizens.CasinoGameTrait;
import com.casinocore.integrations.citizens.CitizensCasinoListener;
import com.casinocore.games.mines.MinesGame;
import com.casinocore.games.mines.MinesListener;
import com.casinocore.games.ridethebus.RideTheBusGame;
import com.casinocore.games.ridethebus.RideTheBusListener;
import com.casinocore.games.impl.WheelGame;
import com.casinocore.games.impl.WheelListener;
import com.casinocore.games.treasure.TreasureGame;
import com.casinocore.games.treasure.TreasureListener;
import com.casinocore.gui.AdminGamesListener;
import com.casinocore.gui.CasinoHubListener;
import com.casinocore.gui.CustomBetListener;
import com.casinocore.gui.GUIListener;
import com.casinocore.utils.CasinoNpcManager;
import com.casinocore.utils.CasinoNpcListener;
import com.casinocore.games.roulette.RouletteGame;
import com.casinocore.games.roulette.RouletteListener;
import com.casinocore.games.slots.SlotMachineGame;
import com.casinocore.games.slots.SlotMachineListener;
import com.casinocore.integrations.CasinoPlaceholderExpansion;
import com.casinocore.stats.PlayerStatsManager;
import com.casinocore.utils.AntiAbuseManager;
import com.casinocore.utils.BetLogManager;
import com.casinocore.utils.ConfigManager;
import com.casinocore.utils.CooldownManager;
import com.casinocore.utils.LocaleManager;
import com.casinocore.utils.MessageManager;
import com.casinocore.utils.ProtectionManager;
import com.casinocore.utils.RegionAccessManager;
import com.casinocore.utils.UxManager;
import com.casinocore.utils.VersionChecker;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CasinoCore extends JavaPlugin implements CasinoPlugin {

    private ConfigManager configManager;
    private LocaleManager localeManager;
    private MessageManager messageManager;
    private EconomyManager economyManager;
    private CooldownManager cooldownManager;
    private AntiAbuseManager antiAbuseManager;
    private ProtectionManager protectionManager;
    private PlayerStatsManager playerStatsManager;
    private UxManager uxManager;
    private BetLogManager betLogManager;
    private VersionChecker versionChecker;
    private RegionAccessManager regionAccessManager;
    private CasinoNpcManager casinoNpcManager;
    private GameManager gameManager;
    private CoinFlipGame coinFlipGame;
    private BlackjackGame blackjackGame;
    private RouletteGame rouletteGame;
    private HorseRaceGame horseRaceGame;
    private WheelGame wheelGame;
    private HighLowGame highLowGame;
    private DoubleUpGame doubleUpGame;
    private TreasureGame treasureGame;
    private RideTheBusGame rideTheBusGame;
    private CrashGame crashGame;
    private MinesGame minesGame;
    private ExceptionInterceptor exceptionInterceptor;

    @Override
    public void onEnable() {
        if (!initializeManagers()) {
            getLogger().severe("Failed to initialize CasinoCore. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        exceptionInterceptor = new ExceptionInterceptor(this);
        exceptionInterceptor.initialize();
        registerCommands();
        registerEvents();
        registerCitizensIntegration();
        versionChecker.checkAsync();
        getLogger().info("CasinoCore enabled. games=" + gameManager.getEnabledCasinoGames().keySet());
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.shutdown();
        }
        if (cooldownManager != null) {
            cooldownManager.shutdown();
        }
        if (antiAbuseManager != null) {
            antiAbuseManager.shutdown();
        }
        if (playerStatsManager != null) {
            playerStatsManager.shutdown();
        }
        if (economyManager != null) {
            economyManager.shutdown();
        }
        if (betLogManager != null) {
            betLogManager.shutdown();
        }

        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("CasinoCore disabled.");
    }

    private boolean initializeManagers() {
        try {
            configManager = new ConfigManager(this);
            configManager.loadConfig();
            localeManager = new LocaleManager(this);
            localeManager.load();

            messageManager = new MessageManager(this);
            protectionManager = new ProtectionManager(this);
            protectionManager.initialize();

            economyManager = new EconomyManager(this);
            if (!economyManager.setupEconomy()) {
                getLogger().warning("Economy system not available.");
            }

            cooldownManager = new CooldownManager(this);
            antiAbuseManager = new AntiAbuseManager(this);
            playerStatsManager = new PlayerStatsManager(this);
            uxManager = new UxManager(this);
            betLogManager = new BetLogManager(this);
            versionChecker = new VersionChecker(this);
            regionAccessManager = new RegionAccessManager(this);
            casinoNpcManager = new CasinoNpcManager(this);
            gameManager = new GameManager(this);
            registerCasinoGames();
            registerIntegrations();
            return true;
        } catch (Exception e) {
            getLogger().severe("Error initializing managers: " + e.getMessage());
            return false;
        }
    }

    private void registerCasinoGames() {
        coinFlipGame = new CoinFlipGame(this);
        gameManager.registerCasinoGame(coinFlipGame);
        gameManager.registerCasinoGame(new SlotMachineGame(this));
        gameManager.registerCasinoGame(new DiceRollGame(this));
        gameManager.registerCasinoGame(new LotteryGame(this));
        blackjackGame = new BlackjackGame(this);
        gameManager.registerCasinoGame(blackjackGame);
        rouletteGame = new RouletteGame(this);
        gameManager.registerCasinoGame(rouletteGame);
        horseRaceGame = new HorseRaceGame(this);
        gameManager.registerCasinoGame(horseRaceGame);
        wheelGame = new WheelGame(this);
        gameManager.registerCasinoGame(wheelGame);
        highLowGame = new HighLowGame(this);
        gameManager.registerCasinoGame(highLowGame);
        doubleUpGame = new DoubleUpGame(this);
        gameManager.registerCasinoGame(doubleUpGame);
        treasureGame = new TreasureGame(this);
        gameManager.registerCasinoGame(treasureGame);
        rideTheBusGame = new RideTheBusGame(this);
        gameManager.registerCasinoGame(rideTheBusGame);
        crashGame = new CrashGame(this);
        gameManager.registerCasinoGame(crashGame);
        minesGame = new MinesGame(this);
        gameManager.registerCasinoGame(minesGame);
    }

    private void registerCommands() {
        CasinoCommand casinoCommand = new CasinoCommand(this);
        PlayCommand playCommand = new PlayCommand(this);
        exceptionInterceptor.bindCommand("casino", casinoCommand, casinoCommand);
        exceptionInterceptor.bindCommand("play", playCommand, playCommand);
    }

    private void registerIntegrations() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CasinoPlaceholderExpansion(this).register();
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new CasinoHubListener(), this);
        getServer().getPluginManager().registerEvents(new AdminGamesListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new CustomBetListener(this), this);
        getServer().getPluginManager().registerEvents(new CasinoNpcListener(casinoNpcManager), this);
        getServer().getPluginManager().registerEvents(versionChecker, this);
        getServer().getPluginManager().registerEvents(new SlotMachineListener(), this);
        getServer().getPluginManager().registerEvents(new DiceRiskListener(), this);
        getServer().getPluginManager().registerEvents(new LotteryPromptListener(this), this);
        getServer().getPluginManager().registerEvents(new LotteryDrawListener(this), this);
        if (coinFlipGame != null) {
            getServer().getPluginManager().registerEvents(new CoinFlipListener(coinFlipGame), this);
            getServer().getPluginManager().registerEvents(new CoinFlipGuiListener(), this);
        }
        if (blackjackGame != null) {
            getServer().getPluginManager().registerEvents(new BlackjackListener(blackjackGame), this);
        }
        if (rouletteGame != null) {
            getServer().getPluginManager().registerEvents(new RouletteListener(rouletteGame), this);
        }
        if (horseRaceGame != null) {
            getServer().getPluginManager().registerEvents(new HorseRaceListener(horseRaceGame), this);
        }
        if (wheelGame != null) {
            getServer().getPluginManager().registerEvents(new WheelListener(wheelGame), this);
        }
        if (highLowGame != null) {
            getServer().getPluginManager().registerEvents(new HighLowListener(highLowGame), this);
        }
        if (doubleUpGame != null) {
            getServer().getPluginManager().registerEvents(new DoubleUpListener(doubleUpGame), this);
        }
        if (treasureGame != null) {
            getServer().getPluginManager().registerEvents(new TreasureListener(treasureGame), this);
        }
        if (rideTheBusGame != null) {
            getServer().getPluginManager().registerEvents(new RideTheBusListener(rideTheBusGame), this);
        }
        if (crashGame != null) {
            getServer().getPluginManager().registerEvents(new CrashListener(crashGame), this);
        }
        if (minesGame != null) {
            getServer().getPluginManager().registerEvents(new MinesListener(minesGame), this);
        }
    }

    private void registerCitizensIntegration() {
        if (!getServer().getPluginManager().isPluginEnabled("Citizens")) {
            return;
        }

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(CasinoGameTrait.class));
        getServer().getPluginManager().registerEvents(new CitizensCasinoListener(this), this);
        getLogger().info("Citizens integration enabled.");
    }

    public void reloadPlugin() {
        configManager.reloadConfig();
        messageManager.reload();
        gameManager.reloadGames();
    }

    @Override
    public JavaPlugin getPlugin() {
        return this;
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public MessageManager getMessageManager() {
        return messageManager;
    }

    @Override
    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    @Override
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    @Override
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    @Override
    public AntiAbuseManager getAntiAbuseManager() {
        return antiAbuseManager;
    }

    @Override
    public PlayerStatsManager getPlayerStatsManager() {
        return playerStatsManager;
    }

    @Override
    public UxManager getUxManager() {
        return uxManager;
    }

    @Override
    public ProtectionManager getProtectionManager() {
        return protectionManager;
    }

    @Override
    public GameManager getGameManager() {
        return gameManager;
    }

    @Override
    public BetLogManager getBetLogManager() {
        return betLogManager;
    }

    @Override
    public VersionChecker getVersionChecker() {
        return versionChecker;
    }

    @Override
    public RegionAccessManager getRegionAccessManager() {
        return regionAccessManager;
    }

    @Override
    public CasinoNpcManager getCasinoNpcManager() {
        return casinoNpcManager;
    }
}
