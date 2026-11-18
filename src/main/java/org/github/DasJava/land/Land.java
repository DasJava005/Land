package org.github.DasJava.land;

import org.github.DasJava.land.utils.Position3D;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Land(LandID id,
                   Instant createdAt,
                   Position3D spawnPoint,
                   String name,
                   UUID owner,
                   List<UUID> members,
                   String motd) {

    public Land {
        Objects.requireNonNull(id, "id cannot be null");
        members = List.copyOf(members);
    }

    public boolean isClaimed() {
        return owner != null;
    }

    public boolean isOwner(UUID uuid) {
        if (!isClaimed()) return false;
        return this.owner.equals(uuid);
    }

    public Land withSpawnPoint(Position3D spawnPoint) {
        return new Land(id, createdAt, spawnPoint, name, owner, members, motd);
    }

    public Land withName(String name) {
        return new Land(id, createdAt, spawnPoint, name, owner, members, motd);
    }

    public Land withOwner(UUID owner) {
        return new Land(id, createdAt, spawnPoint, name, owner, members, motd);
    }

    public Land addMember(UUID uuid) {
        ArrayList<UUID> members = new ArrayList<>(this.members);
        members.add(uuid);
        return new Land(id, createdAt, spawnPoint, name, owner, members, motd);
    }

    public Land removeMember(UUID uuid) {
        ArrayList<UUID> members = new ArrayList<>(this.members);
        members.remove(uuid);
        return new Land(id, createdAt, spawnPoint, name, owner, members, motd);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public static Land unclaimed(LandID id) {
        return new Land(
                id,
                Instant.EPOCH,
                null,
                "__UNCLAIMED",
                null,
                List.of(),
                "");
    }

}
