package org.github.DasJava.land.cmd;

import com.github.DasJava005.cmdApi.base.arguments.TypeArgument;
import com.github.DasJava005.cmdApi.base.parser.ParseResult;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.github.DasJava.land.LandID;

import java.util.List;

public final class LandIDArgument extends TypeArgument<LandID> {

    public LandIDArgument(String key) {
        super(key);
    }

    @Override
    public int numTokens() {
        return 3;
    }

    @Override
    public ParseResult<LandID> parse(String[] tokens) {
        if(Bukkit.getWorld(tokens[0]) == null) {
            return ParseResult.mismatch(tokens[0], LandID.class);
        }

        try {
            int x = Integer.parseInt(tokens[1]);
            int z = Integer.parseInt(tokens[2]);

            return ParseResult.success(new LandID(tokens[0], x, z));
        } catch (NumberFormatException e) {
            return  ParseResult.mismatch(tokens[0], LandID.class);
        }
    }

    @Override
    public List<String> suggestions(CommandSender sender, String[] tokens) {
        if(sender instanceof Player p) {
            LandID id = LandID.fromLocation(p.getLocation());
            return switch (tokens.length){
                case 1 -> List.of(id.worldName(), id.worldName() + " " + id.x() + " " + id.z());
                case 2 -> List.of(""+id.x(), id.x() + " " + id.z());
                default -> List.of(""+id.z());
            };
        }
        return List.of();
    }

}
