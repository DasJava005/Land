package org.github.DasJava.land.service;

import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;
import org.github.DasJava.land.LandMotd;
import org.github.DasJava.land.utils.Position3D;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LandClaimService {

    private final LandService landService;

    public LandClaimService(LandService landService) {
        this.landService = landService;
    }

    public CompletableFuture<ClaimResult> claim(final LandID landID, UUID owner, Position3D home, String name) {
        // TODO: call LandPreClaimEvent
        return landService.getLand(landID).thenCompose(land -> {
            if (land.isClaimed()) {
                return CompletableFuture.completedFuture(ClaimResult.ALREADY_CLAIMED);
            }

            return landService.getLands(owner).thenCompose(claimedLands -> {
                boolean nameExists = claimedLands.stream().anyMatch(l -> l.name().equalsIgnoreCase(name));
                if (nameExists) {
                    return CompletableFuture.completedFuture(ClaimResult.NAME_ALREADY_IN_USE);
                }

                return landService.update(landID, landOperator -> new Land(landID, Instant.now(), home, name, owner, List.of(), LandMotd.emptyMotd()))
                        .thenApply(result -> {
                            // TODO: call LandClaimEvent but sync with main thread
                            return ClaimResult.SUCCESS;
                        });
            });
        });
    }

    public static enum ClaimResult {
        SUCCESS,
        ALREADY_CLAIMED,
        NAME_ALREADY_IN_USE
    }

}
