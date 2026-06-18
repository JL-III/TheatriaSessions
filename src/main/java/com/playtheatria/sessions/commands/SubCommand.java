package com.playtheatria.sessions.commands;

import java.util.List;
import org.bukkit.command.CommandSender;

/**
 * A {@code /session} subcommand. The dispatcher checks {@link #permission()} before
 * calling {@link #execute}, and {@code args} excludes the subcommand name itself.
 */
public interface SubCommand {
    String name();

    String permission();

    boolean execute(CommandSender sender, String[] args);

    default List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
