package org.github.DasJava.land.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LandTeleportService {

    private final Executor mainThreadExecutor;
    private final LandService landService;

    public LandTeleportService(LandService landService, Executor mainThreadExecutor) {
        this.landService = landService;
        this.mainThreadExecutor = mainThreadExecutor;
    }

    public CompletableFuture<TeleportResult> teleport(Player p, LandID id) {
        CompletableFuture<Land> futureLand = landService.getLand(id);

        return futureLand.thenApplyAsync(land -> {
            if(!p.isOnline()) return TeleportResult.PLAYER_OFFLINE;

            World w = Bukkit.getWorld(id.worldName());
            if(w == null) return  TeleportResult.WORLD_NOT_FOUND;

            Location spawn = land.spawnPoint() == null ? calculateDefaultSpawnPoint(id, w) : land.spawnPoint().toLocation(w);

            p.teleport(spawn);
            return TeleportResult.SUCCESS;
        }, mainThreadExecutor);
    }

    public static enum TeleportResult {
        SUCCESS,
        WORLD_NOT_FOUND,
        PLAYER_OFFLINE,
    }

    private Location calculateDefaultSpawnPoint(LandID id, World world) {
        int x =  id.x() * LandID.LAND_SIZE;
        int z = id.z() * LandID.LAND_SIZE;
        return new Location(world, x, world.getHighestBlockYAt(x, z) + 1, z);
    }

}
