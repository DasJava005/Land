package org.github.DasJava.land.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.github.DasJava.land.LandID;
import org.github.DasJava.land.service.LandService;

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

        //TODO preload and print motd
        landService.getLandIfPresent(idFrom).ifPresent(land -> {});
        p.sendMessage("Land has changed!");
        //TODO
    }

}
