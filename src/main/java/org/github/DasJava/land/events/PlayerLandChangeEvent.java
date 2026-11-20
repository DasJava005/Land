package org.github.DasJava.land.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.github.DasJava.land.Land;
import org.jetbrains.annotations.NotNull;

public final class PlayerLandChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public static enum LandChangeReason {
        TELEPORT_NATURAL, // stuff like ender-pearl but not via command
        TELEPORT_COMMAND,
        WORLD_CHANGE,
        BORDER_CROSS
        ;
    }

    public final Player player;
    public final LandChangeReason reason;
    public final Land from;
    public final Land to;

    private boolean cancelled = false;

    private boolean showMotd = true;
    private boolean playSound = true;

    public PlayerLandChangeEvent(Player player, LandChangeReason reason, Land from, Land to) {
        this.player = player;
        this.reason = reason;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public boolean isShowMotd() {
        return showMotd;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public void setShowMotd(boolean showMotd) {
        this.showMotd = showMotd;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}
