package org.github.DasJava.land.service.cache;

import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LandCache {

    public abstract CompletableFuture<Land> getOrLoad(LandID id);

    public abstract CompletableFuture<List<Land>> getOrLoad(UUID uuid);

    public abstract void remove(LandID id, @Nullable UUID owner);

    public abstract void clear();

    public abstract boolean isCached(LandID landID);
}