package org.github.DasJava.land.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class MessageProvider {

    public final Component prefix;
    private final DateTimeFormatter dateTimeFormatter;

    public MessageProvider(Component prefix, DateTimeFormatter dateTimeFormatter) {
        this.prefix = Objects.requireNonNull(prefix);
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public MessageProvider(Component prefix) {
        this(Objects.requireNonNull(prefix), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    public String formatInstant(Instant instant) {
        return dateTimeFormatter.format(instant.atZone(ZoneId.systemDefault()));
    }

    public void send(Player p, Component message) {
        p.sendMessage(prefix.append(Component.text(" ")).append(message));
    }

    public void send(Player p, String message) {
        send(p, Component.text(message));
    }

}
