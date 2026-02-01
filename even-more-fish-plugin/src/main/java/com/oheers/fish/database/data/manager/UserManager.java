package com.oheers.fish.database.data.manager;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.model.user.EmptyUserReport;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager implements Listener {
    private final Database database;
    private final Map<UUID, Integer> userCache;

    public UserManager(Database database) {
        this.database = database;
        this.userCache = new ConcurrentHashMap<>();
    }

    @EventHandler
    public void onJoin(final @NotNull PlayerJoinEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        int id = database.getUserId(uuid);
        if (id == 0) {
            id = EvenMoreFish.getInstance().getPluginDataManager().getDatabase().upsertUserReport(new EmptyUserReport(event.getPlayer().getUniqueId()));
        }

        userCache.putIfAbsent(uuid, id);
        EvenMoreFish.getInstance().debug("User ID: %d UUID: %s".formatted(id, uuid));
    }

    @EventHandler
    public void onLeave(final @NotNull PlayerQuitEvent event) {
        userCache.remove(event.getPlayer().getUniqueId());
    }

    public int getUserId(final UUID uuid) {
        Integer id = userCache.get(uuid);
        if (id != null) {
            return id;
        }

        id = database.getUserId(uuid);
        if (id != 0) {
            userCache.put(uuid, id);
        }
        return id;
    }
}
