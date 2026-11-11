package org.github.DasJava.land;

public class LandLoadException extends RuntimeException {
    public LandLoadException() {
        super("Land loading was cancelled...");
    }

}
