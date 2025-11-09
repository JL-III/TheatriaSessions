package com.playtheatria.sessions.records;

import org.bukkit.command.CommandExecutor;

public record CommandRecord(String name, CommandExecutor executor) {}
