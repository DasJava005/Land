package org.github.DasJava.land.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;
import org.github.DasJava.land.service.LandService;

import java.util.concurrent.CompletableFuture;

public class BlockBreakListener implements Listener {

    private final LandService landService;

    public BlockBreakListener(LandService landService) {
        this.landService = landService;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player p = event.getPlayer();
        final LandID id = LandID.fromLocation(event.getBlock().getLocation());

        CompletableFuture<Land> futureLand = landService.getLand(id);

        if (!futureLand.isDone()) {
            p.sendMessage(ChatColor.RED + "Please wait. Data is still being loaded.");
            event.setCancelled(true);
            return;
        }

        if (futureLand.isCompletedExceptionally() || futureLand.isCancelled()) {
            p.sendMessage(ChatColor.RED + "Could not load land data.");
            event.setCancelled(true);
            return;
        }

        Land land = futureLand.join();

        if (!land.owner().equals(p.getUniqueId())
                && !land.members().contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "You have no permission to do that.");
            event.setCancelled(true);
        }
    }

}
