package org.github.DasJava.land.gson;

import com.google.gson.Gson;
import org.bukkit.Location;
import org.github.DasJava.land.gson.adapters.InstantTypeAdapter;

import java.time.Instant;

public final class GsonProvider {

    private GsonProvider() {}

    public static Gson instance = new Gson().newBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .setPrettyPrinting()
            .create();

}
