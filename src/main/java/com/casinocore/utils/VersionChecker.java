package com.casinocore.utils;

import com.casinocore.core.CasinoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionChecker implements Listener {

    private static final String DEFAULT_VERSION_URL = "https://api.github.com/repos/plugglab/CasinoCore/releases/latest";
    private static final String DEFAULT_DOWNLOAD_URL = "https://modrinth.com/plugin/casinocore";
    private static final String ADMIN_PERMISSION = "casinocore.admin";
    private static final String ENABLED_PATH = "version-checker.enabled";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final Pattern STRING_FIELD_PATTERN =
            Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");

    private final CasinoPlugin plugin;
    private final HttpClient httpClient;

    private volatile VersionStatus status = VersionStatus.unknown();

    public VersionChecker(CasinoPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public void checkAsync() {
        if (!isEnabled()) {
            status = VersionStatus.disabledStatus();
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin.getPlugin(), () -> {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(DEFAULT_VERSION_URL))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    status = VersionStatus.error("Version feed returned HTTP " + response.statusCode());
                    plugin.getPlugin().getLogger().warning("Version check failed: HTTP " + response.statusCode());
                    return;
                }

                String body = response.body();
                String remoteVersion = extractField(body);

                if (remoteVersion == null || remoteVersion.isBlank()) {
                    status = VersionStatus.error("Version feed did not contain a supported version field");
                    plugin.getPlugin().getLogger().warning("Version check failed: no supported version field in " + DEFAULT_VERSION_URL);
                    return;
                }

                String currentVersion = plugin.getPlugin().getDescription().getVersion();
                String trimmedRemoteVersion = remoteVersion.trim();
                boolean updateAvailable = compareVersions(currentVersion, trimmedRemoteVersion) < 0;
                status = new VersionStatus(currentVersion, trimmedRemoteVersion, updateAvailable, DEFAULT_DOWNLOAD_URL, null, false);

                if (updateAvailable) {
                    plugin.getPlugin().getLogger().warning(
                        "Update available: current=" + currentVersion + ", latest=" + trimmedRemoteVersion
                    );
                }
            } catch (Exception exception) {
                status = VersionStatus.error(exception.getMessage());
                plugin.getPlugin().getLogger().warning("Version check failed: " + exception.getMessage());
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(ADMIN_PERMISSION)) {
            return;
        }

        VersionStatus currentStatus = status;
        if (currentStatus.disabled()) {
            return;
        }

        if (currentStatus.errorMessage() != null) {
            plugin.getMessageManager().sendPlayer(
                player,
                "<yellow>CasinoCore version check failed:</yellow> <white>" + escapeMiniMessage(currentStatus.errorMessage()) + "</white>"
            );
            return;
        }

        if (!currentStatus.updateAvailable()) {
            return;
        }

        StringBuilder message = new StringBuilder()
            .append("<yellow>CasinoCore update available.</yellow> ")
            .append("<gray>Current:</gray> <white>").append(escapeMiniMessage(currentStatus.currentVersion())).append("</white> ")
            .append("<gray>Latest:</gray> <green>").append(escapeMiniMessage(currentStatus.latestVersion())).append("</green>");

        if (currentStatus.downloadUrl() != null && !currentStatus.downloadUrl().isBlank()) {
            message.append(" <gray>URL:</gray> <aqua>").append(escapeMiniMessage(currentStatus.downloadUrl())).append("</aqua>");
        }

        plugin.getMessageManager().sendPlayer(player, message.toString());
    }

    private String extractField(String body) {
        Matcher matcher = VersionChecker.STRING_FIELD_PATTERN.matcher(body);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private boolean isEnabled() {
        return getConfig().getBoolean(ENABLED_PATH, true);
    }

    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getConfig();
    }

    private int compareVersions(String currentVersion, String latestVersion) {
        int[] currentParts = numericVersionParts(currentVersion);
        int[] latestParts = numericVersionParts(latestVersion);
        int maxLength = Math.max(currentParts.length, latestParts.length);

        for (int index = 0; index < maxLength; index++) {
            int currentPart = index < currentParts.length ? currentParts[index] : 0;
            int latestPart = index < latestParts.length ? latestParts[index] : 0;
            if (currentPart != latestPart) {
                return Integer.compare(currentPart, latestPart);
            }
        }

        return 0;
    }

    private int[] numericVersionParts(String version) {
        if (version == null || version.isBlank()) {
            return new int[0];
        }

        String normalized = version.trim();
        if (normalized.startsWith("v") || normalized.startsWith("V")) {
            normalized = normalized.substring(1);
        }

        String numericSection = normalized.split("[-+\\s]", 2)[0];
        String[] rawParts = numericSection.split("\\.");
        int[] parts = new int[rawParts.length];

        for (int index = 0; index < rawParts.length; index++) {
            String digits = rawParts[index].replaceAll("[^0-9]", "");
            if (digits.isEmpty()) {
                parts[index] = 0;
                continue;
            }

            try {
                parts[index] = Integer.parseInt(digits);
            } catch (NumberFormatException exception) {
                parts[index] = Integer.MAX_VALUE;
            }
        }

        return parts;
    }

    private String escapeMiniMessage(String value) {
        return value.replace("<", "\\<").replace(">", "\\>");
    }

    private record VersionStatus(
        String currentVersion,
        String latestVersion,
        boolean updateAvailable,
        String downloadUrl,
        String errorMessage,
        boolean disabled
    ) {
        private static VersionStatus unknown() {
            return new VersionStatus(null, null, false, null, null, false);
        }

        private static VersionStatus disabledStatus() {
            return new VersionStatus(null, null, false, null, null, true);
        }

        private static VersionStatus error(String errorMessage) {
            return new VersionStatus(null, null, false, null, errorMessage == null ? "unknown error" : errorMessage, false);
        }
    }
}
