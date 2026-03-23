package com.oheers.fish.update;

import com.oheers.fish.EvenMoreFish;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {
    
    private final EvenMoreFish plugin;

    public UpdateChecker(final EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("UnstableApiUsage")
    public String getVersion() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.modrinth.com/v2/project/vlh7rLCf/version"))
                    .header("User-Agent", "EvenMoreFish/" + plugin.getPluginMeta().getVersion())
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode());
            }

            JSONArray versions = (JSONArray) new JSONParser().parse(response.body());
            if (versions.isEmpty()) {
                return plugin.getPluginMeta().getVersion();
            }

            JSONObject latestVersion = (JSONObject) versions.getFirst();
            String version = latestVersion.get("version_number").toString();
            int mcVersionSeparatorIndex = version.lastIndexOf("-");
            return version.substring(0, mcVersionSeparatorIndex);
        } catch (Exception e) {
            plugin.getLogger().warning("Update check failed: " + e.getMessage());
            plugin.getLogger().info("Manual update check: https://modrinth.com/plugin/evenmorefish/versions");
            return plugin.getPluginMeta().getVersion(); // Fallback
        }
    }

    // Checks for updates, surprisingly
    @Contract(" -> new")
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull CompletableFuture<Boolean> checkUpdate() {
        return CompletableFuture.supplyAsync(() -> {
            ComparableVersion modrinthVersion = new ComparableVersion(new UpdateChecker(plugin).getVersion());
            ComparableVersion serverVersion = new ComparableVersion(plugin.getPluginMeta().getVersion());
            return modrinthVersion.compareTo(serverVersion) > 0;
        });
    }

}
