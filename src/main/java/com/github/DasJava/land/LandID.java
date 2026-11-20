package org.github.DasJava.land;

import org.bukkit.Location;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record LandID(String worldName, int x, int z) {

    public static final int LAND_SIZE = 10;

    public static LandID fromLocation(Location location) {
        Objects.requireNonNull(location, "Location cannot be null");
        return new LandID(location.getWorld().getName(), Math.floorDiv(location.getBlockX(), LAND_SIZE), Math.floorDiv(location.getBlockZ(), LAND_SIZE));
    }

    public LandID withX(int x) {
        return new LandID(worldName, x, z);
    }

    public LandID withZ(int z) {
        return new LandID(worldName, x, z);
    }

    public LandID withXZ(int x, int z) {
        return new LandID(worldName, x, z);
    }

    public LandID addXZ(int offsetX, int offsetZ) {
        return new LandID(worldName, x + offsetX, z + offsetZ);
    }

    public Set<LandID> getNeighbors() {
        return neighbors(this);
    }

    public static Set<LandID> neighbors(LandID land) {
        Objects.requireNonNull(land, "Land cannot be null");
        return Set.of(
                land.addXZ(-1, 1), land.addXZ(0, 1), land.addXZ(1, 1),
                land.addXZ(-1, 0), land.addXZ(1, 0),
                land.addXZ(-1, -1), land.addXZ(0, -1), land.addXZ(1, -1));
    }

    @Override
    public String toString() {
        return worldName + ":" + x + ":" + z;
    }

}
