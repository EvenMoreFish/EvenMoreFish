package com.oheers.fish.database.data.manager;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.model.user.EmptyUserReport;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.ObjIntConsumer;

public class UserManager implements Listener {
    private final Database database;
    private final Executor writeExecutor;
    private final ObjIntConsumer<UUID> joinPreloadHook;
    private final Map<UUID, Integer> userCache;

    public UserManager(Database database, Executor writeExecutor, @Nullable ObjIntConsumer<UUID> joinPreloadHook) {
        this.database = database;
        this.writeExecutor = writeExecutor;
        this.joinPreloadHook = joinPreloadHook;
        this.userCache = new ConcurrentHashMap<>();
    }

    @EventHandler
    public void onJoin(final @NotNull PlayerJoinEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        // Resolve (creating if missing) the user row off the server thread.
        // The write executor is single-threaded and FIFO, so the row is
        // guaranteed to exist before any subsequently queued writes that
        // reference this user.
        writeExecutor.execute(() -> {
            int id = loadOrCreateUser(uuid);
            if (id != 0 && joinPreloadHook != null) {
                joinPreloadHook.accept(uuid, id);
            }
        });
    }

    @EventHandler
    public void onLeave(final @NotNull PlayerQuitEvent event) {
        userCache.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Resolves the user's database id, using the cache populated on join.
     * On a cache miss (a fish caught before the join-time load completed,
     * or after a plugin reload) this falls back to a blocking lookup on the
     * calling thread.
     */
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

    private int loadOrCreateUser(final UUID uuid) {
        int id = database.getUserId(uuid);
        if (id == 0) {
            id = database.upsertUserReport(new EmptyUserReport(uuid));
        }

        if (id != 0) {
            userCache.putIfAbsent(uuid, id);
        }
        EvenMoreFish.getInstance().debug("User ID: %d UUID: %s".formatted(id, uuid));
        return id;
    }
}
