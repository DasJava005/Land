package org.github.DasJava.land;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public sealed interface LandResult permits LandResult.Loaded, LandResult.Loading, LandResult.Failed {

    public record Loaded(Land land) implements LandResult {

        public Loaded {
            Objects.requireNonNull(land);
        }

    }

    public record Loading() implements LandResult {}

    public record Failed(Throwable exception) implements LandResult {}

    public static Loaded loaded(Land land){
        return new Loaded(land);
    }

    public static Loading loading(CompletableFuture<Land> future){
        return new Loading();
    }

    public static Failed failed(Throwable exception){
        return new Failed(exception);
    }

}
