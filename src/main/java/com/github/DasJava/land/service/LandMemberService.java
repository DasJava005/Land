package org.github.DasJava.land.service;

import org.github.DasJava.land.LandID;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LandMemberService {

    private final LandService landService;

    public LandMemberService(LandService landService) {
        this.landService = landService;
    }

    public CompletableFuture<AddMemberResult> addMember(LandID id, UUID executor, UUID target) {
        if(executor.equals(target)) return CompletableFuture.completedFuture(AddMemberResult.CANNOT_ADD_SELF);

        return landService.getLand(id).thenCompose(land -> {
            if(!land.isOwner(executor)) return CompletableFuture.completedFuture(AddMemberResult.PLAYER_NOT_OWNER);
            if(land.isMember(target)) return CompletableFuture.completedFuture(AddMemberResult.TARGET_ALREADY_MEMBER);

            return landService.update(id, landOperator -> landOperator.addMember(target))
                    .thenApply(_ -> AddMemberResult.SUCCESS);
        });
    }

    public static enum AddMemberResult {
        SUCCESS,
        PLAYER_NOT_OWNER,
        CANNOT_ADD_SELF,
        TARGET_ALREADY_MEMBER
    }

    public CompletableFuture<RemoveMemberResult> removeMember(LandID id, UUID executor, UUID target) {
        if(executor.equals(target)) return CompletableFuture.completedFuture(RemoveMemberResult.CANNOT_REMOVE_SELF);

        return landService.getLand(id).thenCompose(land -> {
           if(!land.isOwner(executor)) return CompletableFuture.completedFuture(RemoveMemberResult.PLAYER_NOT_OWNER);
           if(!land.isMember(target)) return CompletableFuture.completedFuture(RemoveMemberResult.TARGET_NO_MEMBER);

           return landService.update(id, landOperator -> landOperator.removeMember(target))
                   .thenApply(_ -> RemoveMemberResult.SUCCESS);
        });
    }

    public static enum RemoveMemberResult {
        SUCCESS,
        PLAYER_NOT_OWNER,
        CANNOT_REMOVE_SELF,
        TARGET_NO_MEMBER
    }

    //TODO
    private CompletableFuture<Void> transferOwnership() {
        return null;
    }

}
