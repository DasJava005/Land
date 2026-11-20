package org.github.DasJava.land.listeners;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;
import org.github.DasJava.land.LandMotd;
import org.github.DasJava.land.events.PlayerLandChangeEvent;
import org.github.DasJava.land.service.LandService;

import java.util.Optional;
import java.util.Set;

public class LandChangeListener implements Listener {

    private final LandService landService;

    public LandChangeListener(LandService landService) {
        this.landService = landService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent e) {
        var from = e.getFrom();
        var to = e.getTo();

        if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) return; // filter tiny movements

        final Player p = e.getPlayer();

        LandID idFrom = LandID.fromLocation(from);
        LandID idTo = LandID.fromLocation(to);

        if(idFrom.equals(idTo)) return; // land didnt change

        Set<LandID> neighbors = LandID.fromLocation(from).getNeighbors();
        neighbors.forEach(landService::preload);

        /*
            Call PlayerLandChangeEvent
         */
        Optional<Land> optFromLand = landService.getLandIfPresent(idFrom);
        Optional<Land> optToLand = landService.getLandIfPresent(idTo);

        if(optFromLand.isPresent() && optToLand.isPresent()) {
            Land fromLand = optFromLand.get();
            Land toLand = optToLand.get();

            final PlayerLandChangeEvent event = new PlayerLandChangeEvent(p, PlayerLandChangeEvent.LandChangeReason.BORDER_CROSS, fromLand, toLand);
            event.callEvent();

            if(event.isCancelled()) {
                e.setCancelled(true);
                return;
            }

            if(event.isShowMotd()) {
                LandMotd motd = toLand.motd();
                motd.showRaw(p);
            }

            if(event.isPlaySound()) {
                p.playSound(Sound.sound(Key.key("minecraft", "entity.player.levelup"),Sound.Source.PLAYER, 1.0f, 1.0f));
            }

        } // data not present
    }

}
