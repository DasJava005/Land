package org.github.DasJava.land.utils;

import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class MessageProvider {

    public final String prefix;
    public final DateTimeFormatter dateTimeFormatter;

    public MessageProvider(String prefix, DateTimeFormatter dateTimeFormatter) {
        this.prefix = Objects.requireNonNull(prefix);
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public MessageProvider(String prefix) {
        this(Objects.requireNonNull(prefix), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    public String formatInstant(Instant instant) {
        return dateTimeFormatter.format(instant.atZone(ZoneId.systemDefault()));
    }

    public void send(Player p, String message) {
        p.sendMessage(this.prefix + " " + message);
    }

}
