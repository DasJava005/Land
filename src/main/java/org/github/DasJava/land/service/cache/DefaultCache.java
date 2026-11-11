package org.github.DasJava.land.service.cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;
import org.github.DasJava.land.service.repo.LandRepository;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DefaultCache implements LandCache {

    protected final AsyncLoadingCache<LandID, Land> lands;
    protected final AsyncLoadingCache<UUID, List<Land>> players;

    public DefaultCache(LandRepository repository, int ttlAfterAccess, int maxSize) {
        this.lands = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(ttlAfterAccess))
                .maximumSize(maxSize)
                .buildAsync((LandID id, Executor _) -> repository.findById(id));

        this.players = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(ttlAfterAccess))
                .maximumSize(maxSize)
                .buildAsync((UUID uuid, Executor _) ->
                        repository.findLandIdsByOwner(uuid)
                                .thenCompose(ids -> {
                                    List<CompletableFuture<Land>> futures = ids.stream()
                                            .map(lands::get)
                                            .toList();

                                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])
                                    ).thenApply(_ ->
                                            futures.stream()
                                                    .map(CompletableFuture::join)
                                                    .toList()
                                    );
                                })
                );
    }

    public DefaultCache(LandRepository repository) {
        this(repository, 10, 10_000);
    }

    @Override
    public CompletableFuture<Land> getOrLoad(LandID id) {
        return lands.get(id);
    }

    @Override
    public CompletableFuture<List<Land>> getOrLoad(UUID uuid) {
        return players.get(uuid);
    }

    @Override
    public void remove(LandID id, UUID owner) {
        lands.synchronous().invalidate(id);

        if (owner != null) {
            players.synchronous().invalidate(owner);
        }
    }

    @Override
    public void clear() {
        lands.synchronous().invalidateAll();
        players.synchronous().invalidateAll();
    }

    @Override
    public boolean isCached(LandID id) {
        return lands.synchronous().asMap().containsKey(id);
    }

}
