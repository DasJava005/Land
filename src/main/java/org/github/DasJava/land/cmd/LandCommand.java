package org.github.DasJava.land.cmd;

import com.github.DasJava005.cmdApi.reflection.annotations.Arg;
import com.github.DasJava005.cmdApi.reflection.annotations.Cmd;
import com.github.DasJava005.cmdApi.reflection.annotations.CommandGroup;
import com.github.DasJava005.cmdApi.reflection.annotations.Sender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.github.DasJava.land.Land;
import org.github.DasJava.land.LandID;
import org.github.DasJava.land.service.LandClaimService;
import org.github.DasJava.land.service.LandMemberService;
import org.github.DasJava.land.service.LandService;
import org.github.DasJava.land.service.LandTeleportService;
import org.github.DasJava.land.utils.MessageProvider;
import org.github.DasJava.land.utils.Position3D;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
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
    private final LandMemberService memberService;

    public LandCommand(Executor mainThreadExecutor,
                       MessageProvider messageProvider,
                       LandService landService,
                       LandTeleportService teleportService,
                       LandClaimService claimService,
                       LandMemberService memberService) {

        this.mainThreadExecutor = mainThreadExecutor;
        this.messageProvider = messageProvider;
        this.landService = landService;
        this.teleportService = teleportService;
        this.claimService = claimService;
        this.memberService = memberService;
    }

    @Cmd("info")
    public void info(@Sender Player player) {
        CompletableFuture<Land> futureLand = landService.getLand(LandID.fromLocation(player.getLocation()));
        futureLand.thenAcceptAsync(land -> {
            if(!land.isClaimed()){
                messageProvider.send(player, "This Land is unclaimed!");
                return;
            }

            messageProvider.send(player,  Component.text("--------------").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH));
            messageProvider.send(player,  Component.text("ID: ", NamedTextColor.GREEN).append(Component.text(land.id().toString(), NamedTextColor.YELLOW)));
            messageProvider.send(player, ChatColor.GREEN + "Name: " + land.name());
            messageProvider.send(player, ChatColor.GREEN + "Owner: " + ChatColor.GOLD + Bukkit.getOfflinePlayer(land.owner()).getName());
            messageProvider.send(player, ChatColor.GREEN + "Claimed at: " + messageProvider.formatInstant(land.createdAt()));
            messageProvider.send(player,  Component.text("--------------").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH));

        }, mainThreadExecutor);
    }

    @Cmd("list")
    public void list(@Sender Player player) {
        CompletableFuture<List<Land>> future = landService.getLands(player.getUniqueId());
        future.thenAcceptAsync(list -> {
            list.forEach(land -> {
                if(!player.isOnline()) return;

                Component text = Component.empty()
                        .append(Component.text("- ", NamedTextColor.GRAY))
                        .append(Component.text(land.name(), NamedTextColor.GOLD))
                        .append(Component.text(" <tp>", NamedTextColor.YELLOW, TextDecoration.ITALIC)
                                .clickEvent(ClickEvent.runCommand("/land go " + land.id().worldName() + " " +  land.id().x() + " " + land.id().z()))
                                .hoverEvent(HoverEvent.showText(Component.text("Click to teleport").color(NamedTextColor.YELLOW))));

                messageProvider.send(player, text);
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
                case NAME_ALREADY_IN_USE -> messageProvider.send(player, ChatColor.YELLOW + "'"+ name +"'"+ ChatColor.RED +" is already taken!");
            }
        },  mainThreadExecutor);
    }

    @Cmd("members")
    public void members(@Sender Player player) {
        membersList(player);
    }

    @Cmd("members list")
    public void membersList(@Sender Player player) {
        landService.getLand(LandID.fromLocation(player.getLocation())).thenAcceptAsync(land -> {
            if(!land.isClaimed()) {
                messageProvider.send(player,  Component.text("This Land is unclaimed!", NamedTextColor.RED));
                return;
            }

            if(land.members().isEmpty()) {
                messageProvider.send(player, Component.text("This Land does not have any members yet :(", NamedTextColor.YELLOW));
                return;
            }

            land.members().stream()
                    .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                    .filter(Objects::nonNull)
                    .forEach(playerName -> messageProvider.send(player, Component.text("- ", NamedTextColor.GRAY).append(Component.text(playerName, NamedTextColor.YELLOW))));

        }, mainThreadExecutor);
    }

    @Cmd("members add #player")
    public void addMember(@Sender Player player, @Arg("player") OfflinePlayer target) {
        LandID id = LandID.fromLocation(player.getLocation());

        memberService.addMember(id, player.getUniqueId(), target.getUniqueId()).thenAcceptAsync(result -> {
           switch (result) {
               case SUCCESS -> messageProvider.send(player, Component.text("Successfully added: ", NamedTextColor.GREEN)
                       .append(Component.text(Objects.requireNonNull(target.getName()), NamedTextColor.YELLOW)));
               case CANNOT_ADD_SELF ->   messageProvider.send(player, Component.text("Cannot add self!").color(NamedTextColor.RED));
               case PLAYER_NOT_OWNER -> messageProvider.send(player, Component.text("You do not own this land!", NamedTextColor.RED));
               case TARGET_ALREADY_MEMBER -> messageProvider.send(player, Component.text("Target player is already a member!", NamedTextColor.RED));
           }
        }, mainThreadExecutor);
    }

    @Cmd("members remove #player")
    public void removeMember(@Sender Player player, @Arg("player") @NonNull OfflinePlayer target) {
        LandID id = LandID.fromLocation(player.getLocation());

        memberService.removeMember(id, player.getUniqueId(), target.getUniqueId()).thenAcceptAsync(result -> {
            switch (result) {
                case SUCCESS -> messageProvider.send(player, Component.text("Successfully removed: ", NamedTextColor.GREEN)
                        .append(Component.text(Objects.requireNonNull(target.getName()), NamedTextColor.YELLOW)));
                case CANNOT_REMOVE_SELF ->   messageProvider.send(player, Component.text("Cannot remove self!").color(NamedTextColor.RED));
                case PLAYER_NOT_OWNER -> messageProvider.send(player, Component.text("You do not own this land!", NamedTextColor.RED));
                case TARGET_NO_MEMBER -> messageProvider.send(player, Component.text("Target player is not a member!", NamedTextColor.RED));
            }
        },  mainThreadExecutor);
    }

}

