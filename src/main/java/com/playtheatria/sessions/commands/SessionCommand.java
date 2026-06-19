package com.playtheatria.sessions.commands;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.enums.RewardTier;
import com.playtheatria.sessions.events.IncrementRewardCountEvent;
import com.playtheatria.sessions.events.RewardPlayerEvent;
import com.playtheatria.sessions.menus.Menu;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.service.StreakService;
import com.playtheatria.sessions.utils.Util;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Single entry point for the plugin. With no args it shows the player a compact,
 * tabbed screen -- Personal, Streaks, Community, and (for staff) the Joined Today
 * roster -- with clickable buttons that swap tabs via {@code /session view <tab>}.
 * The first arg otherwise selects a {@link SubCommand} (streaks) or an admin verb.
 */
public class SessionCommand implements CommandExecutor, TabCompleter {
    private static final List<String> ADMIN_VERBS =
            List.of(
                    "add-player-earned",
                    "create",
                    "force-reward",
                    "reload-config",
                    "reset-progress",
                    "set-progress");
    private static final List<String> TABS =
            List.of("personal", "streaks", "community", "joined");
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    // Book palette -- darker than the chat colors so text reads on the light book page.
    private static final String BK_GREEN = "#1f8a4d";
    private static final String BK_AMBER = "#9a6b00";
    private static final String BK_BLUE = "#1c5fa8";
    private static final String BK_PURPLE = "#7a2e9e";
    private static final String BK_LABEL = "#4a4a4a";
    private static final String BK_VALUE = "#8a5e00";
    private static final int ROSTER_PER_PAGE = 10;

    private final DailyStatsService dailyStatsService;
    private final SessionService sessionService;
    private final StreakService streakService;
    private final ConfigManager configManager;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public SessionCommand(
            DailyStatsService dailyStatsService,
            SessionService sessionService,
            StreakService streakService,
            ConfigManager configManager) {
        this.dailyStatsService = dailyStatsService;
        this.sessionService = sessionService;
        this.streakService = streakService;
        this.configManager = configManager;

        register(new StreaksSubCommand(streakService));
    }

    private void register(SubCommand sub) {
        subCommands.put(sub.name(), sub);
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission(Util.PERMISSION_ALLOW)) return true;
            if (sender instanceof Player player) openBook(player);
            return true;
        }

        String first = args[0].toLowerCase(Locale.ROOT);
        if (first.equals("view")) {
            if (!sender.hasPermission(Util.PERMISSION_ALLOW)) return true;
            if (sender instanceof Player player) {
                showView(player, label, args.length >= 2 ? args[1] : "personal");
            }
            return true;
        }

        SubCommand sub = subCommands.get(first);
        if (sub != null) {
            if (!sender.hasPermission(sub.permission())) return true;
            return sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return handleAdmin(sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            if (sender.hasPermission(Util.PERMISSION_ALLOW)) out.add("view");
            for (SubCommand sub : subCommands.values()) {
                if (sender.hasPermission(sub.permission())) out.add(sub.name());
            }
            if (sender.hasPermission(Util.PERMISSION_ADMIN)) out.addAll(ADMIN_VERBS);
            return out;
        }

        String first = args[0].toLowerCase(Locale.ROOT);
        if (first.equals("view")) {
            if (args.length != 2) return List.of();
            List<String> tabs = new ArrayList<>(List.of("personal", "streaks", "community"));
            if (canSeeRoster(sender)) tabs.add("joined");
            return tabs;
        }

        SubCommand sub = subCommands.get(first);
        if (sub != null) {
            if (!sender.hasPermission(sub.permission())) return List.of();
            return sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return adminTabComplete(sender, args);
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Util.PERMISSION_ADMIN)) return true;
        switch (args.length) {
            case 1 -> {
                switch (args[0].toLowerCase()) {
                    case "reload-config" -> {
                        sender.sendMessage(Util.formatMessage("sessions", "Reloading config"));
                        configManager.reloadConfig();
                        return true;
                    }
                    case "add-player-earned" -> {
                        return simulateEarned(sender, 1);
                    }
                }
            }
            case 2 -> {
                switch (args[0].toLowerCase()) {
                    case "add-player-earned" -> {
                        try {
                            return simulateEarned(sender, Integer.parseInt(args[1]));
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Not a valid number: " + args[1]);
                            return true;
                        }
                    }
                    case "force-reward" -> {
                        for (Session session : sessionService.getSessions()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                Bukkit.getPluginManager()
                                        .callEvent(new RewardPlayerEvent(session.getPlayerUUID()));
                                return true;
                            }
                        }
                    }
                    case "reset-progress" -> {
                        for (Session session : sessionService.getSessions()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                session.setSessionTime(0);
                                return true;
                            }
                        }
                    }
                }
            }
            case 3 -> {
                switch (args[0].toLowerCase()) {
                    case "create" -> {
                        try {
                            Integer integer = Integer.valueOf(args[2]);
                            if (integer < 0)
                                throw new NumberFormatException("Must be higher than 0");
                            for (Session session : sessionService.getSessions()) {
                                if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                    session.setSessionTime(integer);
                                    return true;
                                }
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                            if (offlinePlayer.getName() != null) {
                                Session session =
                                        new Session(
                                                offlinePlayer.getUniqueId(),
                                                offlinePlayer.getName());
                                session.setSessionTime(integer);
                                sessionService.addSession(session);
                                return true;
                            }
                            Util.msg(String.format("Player returned null: %s", args[1]), sender);
                        } catch (NumberFormatException exception) {
                            sender.sendMessage("Not a valid number: " + exception.getMessage());
                        }
                    }
                    case "set-progress" -> {
                        try {
                            Integer integer = Integer.valueOf(args[2]);
                            if (integer < 0)
                                throw new NumberFormatException("Must be higher than 0");
                            for (Session session : sessionService.getSessions()) {
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

    /**
     * Debug helper: fires {@link IncrementRewardCountEvent} {@code count} times, driving the
     * real reward pipeline one earn at a time so each tier threshold is checked and the
     * community LuckPerms bonus is actually granted as tiers are crossed.
     */
    private boolean simulateEarned(CommandSender sender, int count) {
        if (count < 1 || count > 10_000) {
            sender.sendMessage("Count must be between 1 and 10000.");
            return true;
        }
        for (int i = 0; i < count; i++) {
            Bukkit.getPluginManager().callEvent(new IncrementRewardCountEvent());
        }
        sender.sendMessage(
                Util.formatMessage(
                        "Debug",
                        "Simulated "
                                + count
                                + " player(s) earned; total now "
                                + dailyStatsService.getRewardsEarned()));
        return true;
    }

    private List<String> adminTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Util.PERMISSION_ADMIN)) return List.of();
        switch (args.length) {
            case 2 -> {
                if (args[0].equalsIgnoreCase("add-player-earned")) return List.of("<count>");
                if (args[0].equalsIgnoreCase("reload-config")) return List.of();
                return sessionService.getSessions().stream()
                        .map(Session::getPlayerName)
                        .collect(Collectors.toList());
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("set-progress") || args[0].equalsIgnoreCase("create"))
                    return List.of("<amount>");
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }

    // --- Tabbed player view -------------------------------------------------

    // --- Book view (experiment): the same data rendered as a written book the player
    // opens with /daily-reward and pages through. The chat tabs remain on /daily-reward
    // view <tab> for comparison. ---

    private void openBook(Player player) {
        switch (sessionService.getSession(player.getUniqueId())) {
            case Ok<Session, Exception> ok -> {
                switch (streakService.getStreak(player.getUniqueId())) {
                    case Ok<Streak, Exception> okStreak -> player.openBook(
                            buildBook(player, ok.value(), okStreak.value()));
                    case Err<Streak, Exception> errStreak -> player.sendMessage(
                            errStreak.error().getMessage());
                }
            }
            case Err<Session, Exception> err -> player.sendMessage(err.error().getMessage());
        }
    }

    private static String bookHeader(String accentHex, String text) {
        return "<bold><color:" + accentHex + ">" + text + "</color></bold>";
    }

    private static String bookLabel(String text) {
        return "<color:" + BK_LABEL + ">" + text + "</color>";
    }

    /** A book value in the highlight color, with an explanatory tooltip on hover. */
    private static String bookValue(String text, String tooltip) {
        return "<hover:show_text:\""
                + tooltip
                + "\"><color:"
                + BK_VALUE
                + ">"
                + text
                + "</color></hover>";
    }

    /** A "back to contents" link for the bottom of each page. */
    private static String bookHome() {
        return "<click:change_page:'1'><hover:show_text:\"Back to contents\"><color:"
                + BK_LABEL
                + ">« Contents</color></hover></click>";
    }

    private Book buildBook(Player player, Session session, Streak streak) {
        boolean staff = canSeeRoster(player);
        int rewardCount = dailyStatsService.getRewardsEarned();

        List<Component> pages = new ArrayList<>();

        // Page 1: contents, with click-to-jump links (book page numbers are 1-based).
        StringBuilder contents = new StringBuilder();
        contents.append("<bold><gradient:#1f8a4d:#0e5c30>Daily-Reward</gradient></bold>\n");
        contents.append(bookLabel(String.valueOf(dailyStatsService.getDate()))).append("\n\n");
        contents.append(bookLabel("Contents")).append("\n");
        contents.append("<click:change_page:'2'><color:" + BK_GREEN + ">» Personal</color></click>\n");
        contents.append("<click:change_page:'3'><color:" + BK_AMBER + ">» Streaks</color></click>\n");
        contents.append("<click:change_page:'4'><color:" + BK_BLUE + ">» Community</color></click>");
        if (staff) {
            contents.append(
                    "\n<click:change_page:'5'><color:" + BK_PURPLE + ">» Joined</color></click>");
        }
        pages.add(MINI.deserialize(contents.toString()));

        // Page 2: Personal
        pages.add(
                MINI.deserialize(
                        bookHeader(BK_GREEN, "⭐ Personal")
                                + "\n\n"
                                + bookLabel("Progress")
                                + "\n "
                                + bookValue(
                                        progressMinutes(session),
                                        "Your current progress towards earning your daily reward.")
                                + "\n\n"
                                + bookLabel("Earned reward")
                                + "\n "
                                + bookValue(
                                        session.isRewarded() ? "✅" : "❌",
                                        "Whether you have earned your daily reward today.")
                                + "\n\n"
                                + bookHome()));

        // Page 3: Streaks
        pages.add(
                MINI.deserialize(
                        bookHeader(BK_AMBER, "⭐ Streaks")
                                + "\n\n"
                                + bookLabel("Current")
                                + " "
                                + bookValue(
                                        streak.getCurrentStreak() + " days",
                                        "Consecutive days you have earned your daily reward.")
                                + "\n"
                                + bookLabel("Longest")
                                + " "
                                + bookValue(
                                        streak.getLongestStreak() + " days",
                                        "Your longest run of consecutive days.")
                                + "\n\n"
                                + bookLabel("Last earned")
                                + "\n "
                                + bookValue(
                                        streak.getLastEarnedDate() == null
                                                ? "N/A"
                                                : String.valueOf(streak.getLastEarnedDate()),
                                        "The last day you earned your daily reward.")
                                + "\n\n"
                                + bookHome()));

        // Page 4: Community ("sell hand" trimmed to keep the page within a book's lines)
        pages.add(
                MINI.deserialize(
                        bookHeader(BK_BLUE, "⭐ Community")
                                + "\n\n"
                                + bookLabel("Joined")
                                + " "
                                + bookValue(
                                        String.valueOf(dailyStatsService.getPlayersJoined()),
                                        "Players who have joined the server today.")
                                + "  "
                                + bookLabel("Earned")
                                + " "
                                + bookValue(
                                        String.valueOf(rewardCount),
                                        "Players who hit the daily playtime goal.")
                                + "\n\n"
                                + bookLabel("Active")
                                + "\n "
                                + bookValue(
                                        communityActiveBonus(rewardCount).replace(" sell hand", ""),
                                        "The community sell-hand bonus active for everyone now.")
                                + "\n"
                                + bookLabel("Ends in")
                                + " "
                                + bookValue(
                                        boostRemaining(rewardCount),
                                        "Active community bonuses clear at the daily reset"
                                                + " (midnight US Eastern).")
                                + "\n\n"
                                + bookLabel("Next")
                                + "\n "
                                + bookValue(
                                        communityNextBonus(rewardCount),
                                        "Progress towards unlocking the next community bonus"
                                                + " level.")
                                + "\n\n"
                                + bookHome()));

        // Page 5+: Joined roster (staff only), paginated; names carry detail on hover.
        if (staff) {
            pages.addAll(joinedPages(player));
        }

        return Book.book(
                MINI.deserialize("<bold><gradient:#1f8a4d:#0e5c30>Daily-Reward</gradient></bold>"),
                Component.text("The Oracle"),
                pages);
    }

    /** One or more roster pages, split when the count exceeds {@link #ROSTER_PER_PAGE}. */
    private List<Component> joinedPages(Player viewer) {
        List<Component> result = new ArrayList<>();
        List<Session> sessions = new ArrayList<>(sessionService.getSessions());

        if (sessions.isEmpty()) {
            result.add(
                    Component.text()
                            .append(MINI.deserialize(bookHeader(BK_PURPLE, "⭐ Joined")))
                            .append(Component.newline())
                            .append(Component.newline())
                            .append(
                                    Component.text("none yet")
                                            .color(TextColor.fromHexString(BK_LABEL)))
                            .append(Component.newline())
                            .append(Component.newline())
                            .append(MINI.deserialize(bookHome()))
                            .build());
            return result;
        }

        boolean includeAfk = viewer.hasPermission(Util.PERMISSION_ADMIN);
        TextColor nameColor = TextColor.fromHexString(BK_PURPLE);
        int pageCount = (sessions.size() + ROSTER_PER_PAGE - 1) / ROSTER_PER_PAGE;

        for (int p = 0; p < pageCount; p++) {
            String heading =
                    pageCount > 1 ? "⭐ Joined (" + (p + 1) + "/" + pageCount + ")" : "⭐ Joined";
            var page = Component.text().append(MINI.deserialize(bookHeader(BK_PURPLE, heading)));
            int end = Math.min((p + 1) * ROSTER_PER_PAGE, sessions.size());
            for (int i = p * ROSTER_PER_PAGE; i < end; i++) {
                Session session = sessions.get(i);
                page.append(Component.newline())
                        .append(
                                Component.text(session.getPlayerName())
                                        .color(nameColor)
                                        .hoverEvent(
                                                HoverEvent.showText(
                                                        Component.text(
                                                                detail(session, includeAfk)))));
            }
            page.append(Component.newline())
                    .append(Component.newline())
                    .append(MINI.deserialize(bookHome()));
            result.add(page.build());
        }
        return result;
    }

    private void showView(Player player, String label, String tab) {
        switch (sessionService.getSession(player.getUniqueId())) {
            case Ok<Session, Exception> ok -> {
                switch (streakService.getStreak(player.getUniqueId())) {
                    case Ok<Streak, Exception> okStreak -> player.sendMessage(
                            buildView(player, label, tab, ok.value(), okStreak.value())
                                    .toComponent());
                    case Err<Streak, Exception> errStreak -> player.sendMessage(
                            errStreak.error().getMessage());
                }
            }
            case Err<Session, Exception> err -> player.sendMessage(err.error().getMessage());
        }
    }

    private Menu buildView(
            Player player, String label, String requestedTab, Session session, Streak streak) {
        boolean canSeeRoster = canSeeRoster(player);
        String tab = normalizeTab(requestedTab, canSeeRoster);

        Menu.Builder builder =
                Menu.builder()
                        .themeColor(tabColor(tab))
                        .secondaryColor(TextColor.fromHexString(Util.COLOR_THREE))
                        .title(
                                Component.text(
                                        String.format(
                                                "Daily-Reward - %s  [%s]",
                                                dailyStatsService.getDate(), titleFor(tab))));

        switch (tab) {
            case "streaks" -> addStreaks(builder, streak);
            case "community" -> addCommunity(builder);
            case "joined" -> addJoined(builder, player);
            default -> addPersonal(builder, session);
        }

        builder.buttons(navButtons(label, tab, canSeeRoster));
        return builder.build();
    }

    private void addPersonal(Menu.Builder builder, Session session) {
        builder.entries("⭐ Personal Stats")
                .entries(
                        "   Progress:",
                        Menu.Entry.of(" " + progressMinutes(session))
                                .description(
                                        "Your current progress towards earning your daily reward."))
                .entries(
                        "   Earned Reward:",
                        Menu.Entry.of(session.isRewarded() ? " ✅" : " ❌")
                                .description("Whether you have earned your daily reward today."));
    }

    private void addStreaks(Menu.Builder builder, Streak streak) {
        builder.entries("⭐ Streaks")
                .entries(
                        "   Current Streak:",
                        Menu.Entry.of(String.format(" %d days", streak.getCurrentStreak()))
                                .description("Consecutive days you have earned your daily reward."))
                .entries(
                        "   Longest Streak:",
                        Menu.Entry.of(String.format(" %d days", streak.getLongestStreak()))
                                .description("Your longest run of consecutive days."))
                .entries(
                        "   Last Earned:",
                        Menu.Entry.of(
                                        " "
                                                + (streak.getLastEarnedDate() == null
                                                        ? "N/A"
                                                        : streak.getLastEarnedDate()))
                                .description("The last day you earned your daily reward."));
    }

    private void addCommunity(Menu.Builder builder) {
        int rewardCount = dailyStatsService.getRewardsEarned();
        builder.entries("⭐ Community")
                .entries(
                        "   Players Joined:",
                        Menu.Entry.of(String.format(" %s", dailyStatsService.getPlayersJoined()))
                                .description("Players who have joined the server today."))
                .entries(
                        "   Players Earned:",
                        Menu.Entry.of(String.format(" %s", rewardCount))
                                .description("Players who hit today's playtime goal."))
                .entries(
                        "   Active Bonus:",
                        Menu.Entry.of(" " + communityActiveBonus(rewardCount))
                                .description(
                                        "The community sell-hand bonus active for everyone now."))
                .entries(
                        "   Boost ends in:",
                        Menu.Entry.of(" " + boostRemaining(rewardCount))
                                .description(
                                        "Active community bonuses clear at the daily reset"
                                                + " (midnight US Eastern)."))
                .entries(
                        "   Next Bonus:",
                        Menu.Entry.of(" " + communityNextBonus(rewardCount))
                                .description(
                                        "Progress towards unlocking the next community bonus"
                                                + " level."));
    }

    private void addJoined(Menu.Builder builder, Player viewer) {
        // Standalone header + an indented names row, matching the other tabs'
        // layout (and giving the leading space the names were missing).
        builder.entries("⭐ Joined Today").entries("   ", roster(viewer));
    }

    private Menu.Cmd[] navButtons(String label, String active, boolean canSeeRoster) {
        List<Menu.Cmd> buttons = new ArrayList<>();
        buttons.add(navButton(label, "Personal", "personal", active));
        buttons.add(navButton(label, "Streaks", "streaks", active));
        buttons.add(navButton(label, "Community", "community", active));
        if (canSeeRoster) buttons.add(navButton(label, "Joined", "joined", active));
        return buttons.toArray(new Menu.Cmd[0]);
    }

    private Menu.Cmd navButton(String label, String text, String tab, String active) {
        // Reproduce the label the player actually used (/session or its /daily-reward
        // alias) so the buttons stay consistent with however the menu was opened.
        // Every button wears its tab's signature color; the active one gets a marker.
        Menu.Cmd button =
                Menu.Cmd.of("/" + label + " view " + tab).text(text).color(tabColor(tab));
        if (tab.equals(active)) button.icon(">");
        return button;
    }

    /**
     * Distinct accent color per tab so paging between them is visually obvious.
     * All four share one saturation/lightness (HSL ~S75 L66) so they read as a
     * cohesive family; only the hue moves, spread around the wheel to keep each
     * tab unmistakable.
     */
    private static TextColor tabColor(String tab) {
        return switch (tab) {
            case "streaks" -> TextColor.fromHexString("#E9C967"); // amber  (hue ~45)
            case "community" -> TextColor.fromHexString("#67A8E9"); // azure  (hue ~210)
            case "joined" -> TextColor.fromHexString("#D467E9"); // orchid (hue ~290)
            default -> TextColor.fromHexString("#67E9A8"); // personal -> mint (hue ~150)
        };
    }

    /** The roster of who joined today; each name carries that player's detail on hover. */
    private Menu.Entry[] roster(Player viewer) {
        Collection<Session> sessions = sessionService.getSessions();
        if (sessions.isEmpty()) {
            return new Menu.Entry[] {Menu.Entry.of("none yet")};
        }
        boolean includeAfk = viewer.hasPermission(Util.PERMISSION_ADMIN);
        return sessions.stream()
                .map(
                        session ->
                                Menu.Entry.of(session.getPlayerName())
                                        .description(detail(session, includeAfk)))
                .toArray(Menu.Entry[]::new);
    }

    /** Per-player hover detail (the old "check" view). AFK is admin-gated. */
    private String detail(Session session, boolean includeAfk) {
        String text = "Progress: " + progressMinutes(session);
        if (includeAfk) {
            text += "\nAFK: " + session.getAfkTime();
        }
        return text + "\nEarned reward: " + (session.isRewarded() ? "yes" : "no");
    }

    /** Session progress and threshold as decimal minutes, e.g. {@code "5.9/30.0 min"}. */
    private String progressMinutes(Session session) {
        double sessionMinutes = Math.floor(session.getSessionTime() / 6.0) / 10.0;
        double thresholdMinutes = configManager.getRewardThreshold() / 60.0;
        return sessionMinutes + "/" + thresholdMinutes + " min";
    }

    private boolean canSeeRoster(CommandSender sender) {
        return sender.hasPermission(Util.PERMISSION_ACTIVITY_COMMAND)
                || sender.hasPermission(Util.PERMISSION_ADMIN);
    }

    private String normalizeTab(String requestedTab, boolean canSeeRoster) {
        String tab = requestedTab == null ? "personal" : requestedTab.toLowerCase(Locale.ROOT);
        if (!TABS.contains(tab)) tab = "personal";
        if (tab.equals("joined") && !canSeeRoster) tab = "personal";
        return tab;
    }

    private static String titleFor(String tab) {
        return switch (tab) {
            case "streaks" -> "Streaks";
            case "community" -> "Community";
            case "joined" -> "Joined Today";
            default -> "Personal";
        };
    }

    private String communityActiveBonus(int rewardCount) {
        String active = "none unlocked yet today";
        switch (RewardTier.getNearestTier(rewardCount)) {
            case Ok<RewardTier, Exception> ok -> {
                RewardTier tier = ok.value();
                active = tier.getDisplayName() + " (+" + tier.getPercentage() + " sell hand)";
            }
            case Err<RewardTier, Exception> ignored -> {}
        }
        return active;
    }

    private String communityNextBonus(int rewardCount) {
        String next = "top level reached!";
        switch (RewardTier.getNextTier(rewardCount)) {
            case Ok<RewardTier, Exception> ok -> {
                RewardTier tier = ok.value();
                int needed = tier.getThreshold() - rewardCount;
                next =
                        needed
                                + " more at the goal to unlock "
                                + tier.getDisplayName()
                                + " (+"
                                + tier.getPercentage()
                                + ")";
            }
            case Err<RewardTier, Exception> ignored -> {}
        }
        return next;
    }

    /** "—" when no bonus is active, otherwise the time until that bonus clears at reset. */
    private String boostRemaining(int rewardCount) {
        String remaining = "—";
        switch (RewardTier.getNearestTier(rewardCount)) {
            case Ok<RewardTier, Exception> ok -> remaining = timeUntilReset();
            case Err<RewardTier, Exception> ignored -> {}
        }
        return remaining;
    }

    /**
     * Time until the daily reset (midnight in TheatriaTime's reset zone), when DayChange
     * clears the community bonuses. The zone is region-based, so it tracks DST automatically.
     */
    private String timeUntilReset() {
        ZonedDateTime now = ZonedDateTime.now(TimeUtils.timeZone);
        ZonedDateTime nextReset = now.toLocalDate().plusDays(1).atStartOfDay(TimeUtils.timeZone);
        Duration remaining = Duration.between(now, nextReset);
        return remaining.toHours() + "h " + (remaining.toMinutes() % 60) + "m";
    }
}
