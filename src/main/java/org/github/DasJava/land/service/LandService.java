package org.github.DasJava.land.service;

import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;
import org.github.DasJava.land.service.cache.LandCache;
import org.github.DasJava.land.service.repo.LandRepository;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

public class LandService {

    private final LandRepository landRepository;
    private final LandCache landCache;

    public LandService(LandRepository landRepository, LandCache landCache) {
        this.landRepository = landRepository;
        this.landCache = landCache;
    }

    public CompletableFuture<Land> getLand(LandID id) {
        return landCache.getOrLoad(id);
    }

    public CompletableFuture<List<Land>> getLands(UUID user) {
        return landCache.getOrLoad(user);
    }

    public CompletableFuture<Void> update(LandID id, UnaryOperator<Land> update) {
        return getLand(id)
                .thenApply(update)
                .thenCompose(land -> landRepository.save(land).thenApply(result -> land))
                .thenAccept(land -> landCache.remove(land.id(), land.owner()));
    }

    public CompletableFuture<Void> delete(LandID id, @Nullable UUID owner) {
        Objects.requireNonNull(id, "id cannot be null");
        landCache.remove(id, owner);
        return landRepository.delete(id);
    }

}
