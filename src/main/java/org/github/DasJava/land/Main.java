package org.github.DasJava.land;

import com.github.DasJava005.cmdApi.base.registry.CommandRegistry;
import com.github.DasJava005.cmdApi.reflection.AnnotationCommandBuilder;
import com.github.DasJava005.cmdApi.reflection.InputArgumentRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.github.DasJava.land.cmd.LandCommand;
import org.github.DasJava.land.cmd.LandIDArgument;
import org.github.DasJava.land.listeners.BlockBreakListener;
import org.github.DasJava.land.service.LandClaimService;
import org.github.DasJava.land.service.LandMemberService;
import org.github.DasJava.land.service.LandTeleportService;
import org.github.DasJava.land.service.cache.DefaultCache;
import org.github.DasJava.land.service.LandService;
import org.github.DasJava.land.service.repo.sqlite.SqLiteDatabase;
import org.github.DasJava.land.service.repo.sqlite.SqLiteRepository;
import org.github.DasJava.land.utils.MessageProvider;

import java.util.Objects;
import java.util.concurrent.Executor;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        /*
            Executors
         */
        final Executor mainThreadExecutor = command -> Bukkit.getScheduler().runTask(this, command);

        /*
            Configuration
         */
        saveDefaultConfig();

        /*
            Messages
         */
        final Component pluginPrefix = MiniMessage.miniMessage().deserialize(Objects.requireNonNull(getConfig().getString("prefix")));
        final MessageProvider messageProvider = new MessageProvider(pluginPrefix);

        /*
            Database & Services
         */
        SqLiteDatabase database = new SqLiteDatabase("jdbc:sqlite:" + getDataFolder().toPath().resolve("lands.db"));
        SqLiteRepository repository = new SqLiteRepository(database);

        LandService service = new LandService(repository, new DefaultCache(repository));
        LandTeleportService teleportService = new LandTeleportService(service, mainThreadExecutor);
        LandClaimService landClaimService = new LandClaimService(service);
        LandMemberService memberService = new LandMemberService(service);

        /*
            Commands
         */
        final CommandRegistry registry = new CommandRegistry(this);
        final InputArgumentRegistry inputArgumentRegistry = new InputArgumentRegistry();
        inputArgumentRegistry.registerDefaults();
        inputArgumentRegistry.register(LandID.class, LandIDArgument::new);

        AnnotationCommandBuilder builder = new AnnotationCommandBuilder(inputArgumentRegistry);

        registry.registerCommands(builder.buildCommands(
                new LandCommand(
                        mainThreadExecutor,
                        messageProvider,
                        service,
                        teleportService,
                        landClaimService,
                        memberService)
        ));

        /*
            Listeners
         */
        register(new BlockBreakListener(service));
    }

    public void register(Listener listener){
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
