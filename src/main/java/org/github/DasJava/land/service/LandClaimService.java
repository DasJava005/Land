package org.github.DasJava.land.service;

import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;
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
        //TODO: call LandPreClaimEvent

        CompletableFuture<Land> futureLand = landService.getLand(landID);
        return futureLand.thenCompose(land -> {
            if(land.isClaimed()) {
                return CompletableFuture.completedFuture(ClaimResult.ALREADY_CLAIMED);
            }

            return landService.update(landID, landOperator ->
                    new Land(landID, Instant.now(), home, name, owner, List.of(), "")
            ).thenApply(result -> {
                // TODO: call LandClaimEvent
                return ClaimResult.SUCCESS;
            });
        });
    }

    public static enum ClaimResult {
        SUCCESS,
        ALREADY_CLAIMED,
    }

}
