package org.github.DasJava.land.cmd;

import com.github.DasJava005.cmdApi.base.arguments.InputArgument;
import com.github.DasJava005.cmdApi.base.arguments.impl.bukkit.VectorArgument;
import com.github.DasJava005.cmdApi.base.parser.ParseException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.github.DasJava.land.LandID;

import java.util.List;

public final class LandIDArgument extends InputArgument<LandID> {

    public LandIDArgument(String key) {
        super(key);
    }

    @Override
    public TokenConsumerType getConsumerType() {
        return TokenConsumerType.exactly(3);
    }

    @Override
    public LandID parse(String[] tokens) {
        if(Bukkit.getWorld(tokens[0]) == null) {
            throw new ParseException(tokens[0] + " is not a valid world.");
        }

        int x = parseOrThrow(tokens[1]);
        int z = parseOrThrow(tokens[2]);

        return new LandID(tokens[0], x, z);
    }

    public int parseOrThrow(String token){
        try {
            return Integer.parseInt(token);
        }catch (ParseException e) {
            throw new ParseException(token, int.class);
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
