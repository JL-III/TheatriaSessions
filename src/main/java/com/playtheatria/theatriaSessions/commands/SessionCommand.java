package com.playtheatria.theatriaSessions.commands;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.data.Price;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.enums.HistoryType;
import com.playtheatria.theatriaSessions.events.PricesGraduationEvent;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.managers.PriceManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SessionCommand implements CommandExecutor, TabCompleter {
    private final SessionManager sessionManager;
    private final PriceManager priceManager;
    private final ConfigManager configManager;
    private final String ADMIN_PERMISSION = "theatria.sessions.admin";

    public SessionCommand(
            SessionManager sessionManager,
            PriceManager priceManager,
            ConfigManager configManager
    ) {
        this.sessionManager = sessionManager;
        this.priceManager = priceManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("theatria.sessions.allow")) return true;
        switch (args.length) {
            case 0 -> {
                if (sender instanceof Player player) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss");
                    String formattedDate = LocalDateTime.now(TimeUtils.timeZone).format(formatter);
                    switch (sessionManager.getSession(player.getUniqueId())) {
                        case Ok<Session, Exception> ok -> {
                            player.sendMessage(Util.formatMessage("Date", formattedDate + " EST"));
                            player.sendMessage(Util.formatPlayerMessage(ok.value()));
                        }
                        case Err<Session, Exception> err -> {
                            player.sendMessage(err.error().getMessage());
                        }
                    }
                    return true;
                }
            }
            case 1 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "show-all" -> {
                        sender.sendMessage(Util.formatMessage("Number of Sessions", sessionManager.getSessions().size()));
                        for (Session session : sessionManager.getSessions().values()) {
                            sender.sendMessage(Util.formatAdminMessage(session));
                        }
                        return true;
                    }
                    case "reload-config" -> {
                        Util.sendFormattedMessage("Reloading config", sender);
                        configManager.reloadConfig();
                        return true;
                    }
                    case "test" -> {
                        Bukkit.getPluginManager().callEvent(new PricesGraduationEvent(Util.getTestPriceList(), LocalDateTime.now(TimeUtils.timeZone).minusDays(2), LocalDateTime.now(TimeUtils.timeZone)));
                        return true;
                    }
                }
            }
            case 2 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "check" -> {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss");
                        String formattedDate = LocalDateTime.now(TimeUtils.timeZone).format(formatter);
                        for (Session session : sessionManager.getSessions().values()) {
                            if (!session.getPlayerName().equalsIgnoreCase(args[1])) continue;
                            sender.sendMessage(Util.formatMessage("Date", formattedDate + " EST"));
                            sender.sendMessage(Util.formatMessage("Progress", session.getSessionTime() + "/" + session.THRESHOLD));
                            sender.sendMessage(Util.formatMessage("AfkTime", session.getAfkTime()));
                            sender.sendMessage(Util.formatMessage("EarnedReward", session.isRewarded()));
                            return true;
                        }
                    }
                    case "force-reward" -> {
                        for (Session session : sessionManager.getSessions().values()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                Bukkit.getPluginManager().callEvent(new RewardPlayerEvent(session));
                                return true;
                            }
                        }
                    }
                    case "reset-progress" -> {
                        for (Session session : sessionManager.getSessions().values()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                session.setSessionTime(0);
                                return true;
                            }
                        }
                    }
                }
            }
            case 3 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "create" -> {
                        try {
                            Integer integer = Integer.parseInt(args[2]);
                            if (integer < 0) throw new NumberFormatException("Must be higher than 0");
                            for (Session session : sessionManager.getSessions().values()) {
                                if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                    session.setSessionTime(integer);
                                    return true;
                                }
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                            if (offlinePlayer.getName() != null) {
                                Session session = new Session(offlinePlayer.getUniqueId(), offlinePlayer.getName());
                                session.setSessionTime(integer);
                                sessionManager.addSession(session);
                                return true;
                            }
                            Util.sendFormattedMessage(String.format("Player returned null: %s", args[1]), sender);
                        } catch (NumberFormatException exception) {
                            sender.sendMessage("Not a valid number: " + exception.getMessage());
                        }
                    }
                    case "get-price" -> {
                        String materialString = args[1].toUpperCase();
                        String historyString = args[2].toUpperCase();
                        try {
                            Material material = Material.valueOf(materialString);
                            HistoryType historyType = HistoryType.valueOf(historyString);
                            sender.sendMessage("Diamond Prices:");
                            priceManager.getPriceListCache()
                                    .stream()
                                    .filter(x -> x.getMaterial() == material)
                                    .filter(x -> x.getHistoryType() == historyType)
                                    .sorted(Comparator.comparing(Price::getTimestamp))
                                    .forEach(x -> {
                                        sender.sendMessage(Util.formatPrice(x));
                                    });
                        } catch (IllegalArgumentException exception) {
                            sender.sendMessage(exception.getMessage());
                        }
                        return true;
                    }
                    case "set-progress" -> {
                        try {
                            Integer integer = Integer.parseInt(args[2]);
                            if (integer < 0) throw new NumberFormatException("Must be higher than 0");
                            for (Session session : sessionManager.getSessions().values()) {
                                if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                    session.setSessionTime(integer);
                                    return true;
                                }
                            }
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Not a valid number: " + e.getMessage());
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) return List.of();
        switch (args.length) {
            case 1 -> {
                return List.of(
                        "check",
                        "create",
                        "force-reward",
                        "get-price",
                        "reload-config",
                        "reset-progress",
                        "set-progress",
                        "show-all",
                        "test"
                );
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("show-all")) return List.of();
                if (args[0].equalsIgnoreCase("get-price")) return priceManager.getMaterials().stream().map(Enum::toString).collect(Collectors.toList());
                return sessionManager.getSessions().values().stream()
                        .map(Session::getPlayerName)
                        .collect(Collectors.toList());
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("get-price")) return Arrays.stream(HistoryType.values()).map(Enum::toString).toList();
                if (args[1].equalsIgnoreCase("set-progress") || args[1].equalsIgnoreCase("create")) return List.of("<amount>");
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }
}
