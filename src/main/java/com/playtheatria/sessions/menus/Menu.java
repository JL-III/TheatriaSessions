package com.playtheatria.sessions.menus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Menu {
    private final Component title;
    private final Component description;
    private final TextColor themeColor;
    private final TextColor secondaryColor;
    private final Map<String, List<Entry>> entries;
    private final List<Cmd> buttons;

    private Menu(
            @NotNull Component title,
            @Nullable Component description,
            @NotNull TextColor themeColor,
            @NotNull TextColor secondaryColor,
            @NotNull Map<String, List<Entry>> entries,
            @NotNull List<Cmd> buttons) {
        this.title = title;
        this.description = description;
        this.themeColor = themeColor;
        this.secondaryColor = secondaryColor;
        this.entries = entries;
        this.buttons = buttons;
    }

    @NotNull public static Builder builder() {
        return new Builder();
    }

    @NotNull public TextComponent toComponent() {
        final TextComponent.Builder builder =
                Component.text()
                        .append(Component.newline())
                        .append(title.colorIfAbsent(themeColor).decorate(TextDecoration.BOLD));

        // Add description
        if (description != null) {
            builder.append(Component.newline()).append(description.colorIfAbsent(secondaryColor));
        }

        // Add attributions
        if (!entries.isEmpty()) {
            builder.append(Component.newline());
        }
        for (Map.Entry<String, List<Entry>> entry : entries.entrySet()) {
            builder.append(Component.newline())
                    .append(
                            Component.text(entry.getKey())
                                    .color(NamedTextColor.WHITE));

            final AtomicInteger entriesCount = new AtomicInteger();
            entry.getValue().stream()
                    .map(Entry::toComponent)
                    .forEach(
                            name -> {
                                if (entriesCount.getAndIncrement() > 0) {
                                    builder.append(Component.text(", "));
                                }
                                builder.append(name).color(secondaryColor);
                            });
        }

        // Add buttons
        if (!buttons.isEmpty()) {
            builder.append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("Navigation: ").color(secondaryColor));

            final AtomicInteger links = new AtomicInteger();
            buttons.stream()
                    .map(Cmd::toComponent)
                    .forEach(
                            link -> {
                                if (links.getAndIncrement() > 0) {
                                    builder.append(Component.text("   "));
                                }
                                builder.append(link);
                            });
        }

        return builder.build();
    }

    public static class Builder {
        private Component title;
        private Component description;
        private TextColor themeColor = TextColor.color(0x00FB9A);
        private TextColor secondaryColor = TextColor.color(0xAAAAAA);
        private final Map<String, List<Entry>> attributions = new LinkedHashMap<>();
        private final List<Cmd> buttons = new ArrayList<>();

        private Builder() {}

        /**
         * Set the title of the resource to display on the menu
         *
         * @param title The resource title
         * @return The {@link Builder}
         */
        @NotNull public Builder title(@NotNull Component title) {
            this.title = title;
            return this;
        }

        /**
         * Set the description of the resource to display on the menu
         *
         * @param description The resource description
         * @return The {@link Builder}
         */
        @NotNull public Builder description(@Nullable Component description) {
            this.description = description;
            return this;
        }

        /**
         * Set the theme color of the resource to display on the menu
         *
         * @param themeColor The resource theme color
         * @return The {@link Builder}
         */
        @NotNull public Builder themeColor(@NotNull TextColor themeColor) {
            this.themeColor = themeColor;
            return this;
        }

        /**
         * Set the secondary color of the resource to display on the menu
         *
         * @param secondaryColor The resource secondary color
         * @return The {@link Builder}
         */
        @NotNull public Builder secondaryColor(@NotNull TextColor secondaryColor) {
            this.secondaryColor = secondaryColor;
            return this;
        }

        /**
         * Add an attribution to the menu
         *
         * @param category The attribution category (e.g. {@code "Author"})
         * @param entries  {@link Entry}s to add
         * @return The {@link Builder}
         */
        @NotNull public Builder entries(@NotNull String category, @NotNull Entry... entries) {
            final List<Entry> entryList = new ArrayList<>(Arrays.asList(entries));
            attributions.putIfAbsent(category, new ArrayList<>());
            attributions.get(category).addAll(entryList);
            return this;
        }

        /**
         * Add linked buttons to the menu
         *
         * @param links {@link Link}s to add
         * @return The {@link Builder}
         */
        @NotNull public Builder buttons(@NotNull Cmd... buttons) {
            this.buttons.addAll(Arrays.asList(buttons));
            return this;
        }

        /**
         * Build the {@link AboutMenu}
         *
         * @return The {@link Builder}
         */
        @NotNull public Menu build() {
            if (title == null) {
                throw new IllegalStateException("Title must be set");
            }
            return new Menu(title, description, themeColor, secondaryColor, attributions, buttons);
        }
    }

    /**
     * Represents a command related to the resource
     */
    public static class Cmd {
        private String text = "Command";
        private TextColor color = TextColor.color(0x00fb9a);
        private final String cmd;
        private String icon;

        private Cmd(@NotNull String cmd) {
            this.cmd = cmd;
        }

        public static Cmd of(@NotNull String cmd) {
            return new Cmd(cmd);
        }

        public Cmd text(@NotNull String text) {
            this.text = text;
            return this;
        }

        public Cmd icon(@NotNull String icon) {
            this.icon = icon;
            return this;
        }

        public Cmd color(@NotNull TextColor color) {
            this.color = color;
            return this;
        }

        @NotNull public Component toComponent() {
            return Component.text("[" + (icon == null ? "" : icon) + " " + text + "]", color)
                    .clickEvent(ClickEvent.runCommand(cmd));
        }
    }

    /**
     * Represents information about someone who worked on the resource
     */
    public static class Entry {
        private final String name;
        @Nullable private String description;
        @Nullable private String url;
        private TextColor color = TextColor.color(0xAAAAAA);

        private Entry(@NotNull String name) {
            this.name = name;
        }

        /**
         * Create a entry for information
         *
         * @param name The name of the entry
         * @return The {@link Entry}
         */
        @NotNull public static Entry of(@NotNull String name) {
            return new Entry(name);
        }

        /**
         * Set the description of the entry; i.e. what they did
         *
         * @param description The description of the entry (what they did)
         * @return The {@link Entry}
         */
        @NotNull public Entry description(@Nullable String description) {
            this.description = description;
            return this;
        }

        /**
         * Set the URL of the entry; i.e. their website. Note this has no effect on the string representation of the menu
         *
         * @param url The URL of the entry (i.e. their website)
         * @return The {@link Entry}
         */
        @NotNull public Entry url(@Nullable String url) {
            this.url = url;
            return this;
        }

        /**
         * Set the color of the entry. Note this has no effect on the string representation of the menu
         *
         * @param color The color of the entry
         * @return The {@link Entry}
         */
        @NotNull public Entry color(@NotNull TextColor color) {
            this.color = color;
            return this;
        }

        @NotNull public Component toComponent() {
            final ComponentBuilder<TextComponent, TextComponent.Builder> builder =
                    Component.text().content(name);
            if (description != null) {
                builder.hoverEvent(HoverEvent.showText(Component.text(description, color)));
            }
            if (url != null) {
                builder.clickEvent(ClickEvent.openUrl(url));
            }
            return builder.color(color).build();
        }
    }
}
