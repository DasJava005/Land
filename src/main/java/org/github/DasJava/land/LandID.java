package org.github.DasJava.land;

import org.bukkit.Location;

import java.util.Objects;

public record LandID(String worldName, int x, int z) {

    public static final int LAND_SIZE = 10;

    public static LandID fromLocation(Location location) {
        Objects.requireNonNull(location, "Location cannot be null");
        return new LandID(location.getWorld().getName(), Math.floorDiv(location.getBlockX(), LAND_SIZE), Math.floorDiv(location.getBlockZ(), LAND_SIZE));
    }

    @Override
    public String toString() {
        return worldName + ":" + x + ":" + z;
    }
}
