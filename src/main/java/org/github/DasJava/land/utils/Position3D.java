package org.github.DasJava.land.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public record Position3D(double x, double y, double z, float yaw, float pitch) {

    public Position3D add(Position3D position) {
        return new Position3D(x + position.x, y + position.y, z + position.z, yaw + position.yaw, pitch +  position.pitch);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Position3D fromVector(Vector vector) {
        return new Position3D(vector.getX(), vector.getY(), vector.getZ(), 0, 0);
    }

    public static Position3D fromLocation(Location location) {
        return new Position3D(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

}
