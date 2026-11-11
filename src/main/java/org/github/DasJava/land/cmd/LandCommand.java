package org.github.DasJava.land.cmd;

import com.github.DasJava005.cmdApi.reflection.annotations.Arg;
import com.github.DasJava005.cmdApi.reflection.annotations.Cmd;
import com.github.DasJava005.cmdApi.reflection.annotations.CommandGroup;
import com.github.DasJava005.cmdApi.reflection.annotations.Sender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.conversations.PluginNameConversationPrefix;
import org.bukkit.entity.Player;
import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;
import org.github.DasJava.land.service.LandClaimService;
import org.github.DasJava.land.service.LandService;
import org.github.DasJava.land.service.LandTeleportService;
import org.github.DasJava.land.utils.MessageProvider;
import org.github.DasJava.land.utils.Position3D;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@CommandGroup(value = "land", aliases = {"l"})
public class LandCommand {

    private final Executor mainThreadExecutor;
    private final MessageProvider messageProvider;

    private final LandService landService;
    private final LandTeleportService teleportService;
    private final LandClaimService claimService;

    public LandCommand(Executor mainThreadExecutor,
                       MessageProvider messageProvider,
                       LandService landService,
                       LandTeleportService teleportService,
                       LandClaimService claimService) {

        this.mainThreadExecutor = mainThreadExecutor;
        this.messageProvider = messageProvider;
        this.landService = landService;
        this.teleportService = teleportService;
        this.claimService = claimService;
    }

    @Cmd("info")
    public void info(@Sender Player player) {
        CompletableFuture<Land> futureLand = landService.getLand(LandID.fromLocation(player.getLocation()));
        futureLand.thenAcceptAsync(land -> {
            if(!land.isClaimed()){
                messageProvider.send(player, "This Land is unclaimed!");
                return;
            }

            messageProvider.send(player,  ChatColor.STRIKETHROUGH + "--------------");
            messageProvider.send(player, ChatColor.GREEN + "ID: " + ChatColor.YELLOW +  land.id().toString());
            messageProvider.send(player, ChatColor.GREEN + "Name: " + land.name());
            messageProvider.send(player, ChatColor.GREEN + "Owner: " + ChatColor.GOLD + Bukkit.getOfflinePlayer(land.owner()).getName());
            messageProvider.send(player, ChatColor.GREEN + "Claimed at: " + messageProvider.formatInstant(land.createdAt()));
            messageProvider.send(player,  ChatColor.STRIKETHROUGH + "--------------");

        }, mainThreadExecutor).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    @Cmd("list")
    public void list(@Sender Player player) {
        UUID uuid = player.getUniqueId();
        CompletableFuture<List<Land>> future = landService.getLands(player.getUniqueId());
        future.thenAcceptAsync(list -> {
            list.forEach(land -> {
                Player p  = Bukkit.getPlayer(uuid);
                if(p == null) return;
                p.sendMessage(ChatColor.GREEN + land.name() + ChatColor.YELLOW + " (" + land.id().toString() + ")");
            });
        }, mainThreadExecutor);
    }

    @Cmd("go #id")
    public void go(@Sender Player player, @Arg("id") LandID id) {
        teleportService.teleport(player, id).thenAcceptAsync(result -> {
            if(!player.isOnline()) return;
            switch (result) {
                case SUCCESS -> player.sendMessage(ChatColor.GREEN + "You have been teleported.");
                case WORLD_NOT_FOUND ->   player.sendMessage(ChatColor.RED + "Could not find world");
                default -> {}
            }
        }, mainThreadExecutor);
    }

    @Cmd("claim #name")
    public void claim(@Sender Player player, @Arg("name") String name) {
        LandID id = LandID.fromLocation(player.getLocation());
        UUID owner = player.getUniqueId();
        Position3D home = Position3D.fromLocation(player.getLocation());

        claimService.claim(id, owner, home, name).thenAcceptAsync(result -> {
            if(!player.isOnline()) return;

            switch (result) {
                case SUCCESS -> player.sendMessage(ChatColor.GREEN + "You have claimed a new Land.");
                case ALREADY_CLAIMED -> player.sendMessage("Land is already claimed.!");
            }
        },  mainThreadExecutor);

    }

}

