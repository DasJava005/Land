package org.github.DasJava.land.service.repo;

import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LandRepository {

    CompletableFuture<Land> findById(LandID id);

    CompletableFuture<List<Land>> findByOwner(UUID owner);

    CompletableFuture<List<LandID>> findLandIdsByOwner(UUID owner);

    CompletableFuture<Void> save(Land land);

    CompletableFuture<Void> delete(LandID id);

}
