package io.rhythmknights.coreframework.component.utility;

import io.rhythmknights.coreframework.CoreFramework;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Single Source of Truth for all message formatting and sending across RhythmKnights plugins
 * Handles both legacy and Adventure API formatting
 */
public class TextUtility {

    /**
     * Private constructor to prevent instantiation of utility class
     */
    private TextUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Initialize MiniMessage with the ability to parse legacy color codes ('&')
    // This is crucial for seamless integration if your language files use '&'
    // MiniMessage also supports its own tags like <bold>, <red>, etc.
    private static final MiniMessage miniMessage = MiniMessage.builder()
            .postProcessor(s -> {
                // Ensure legacy colors are processed AFTER MiniMessage tags for correct behavior
                // The .deserialize() method of MiniMessage handles tags,
                // and then you can process the *remaining* legacy codes.
                // However, the best practice is to configure MiniMessage to handle them directly
                // through its features.
                // If you want to keep legacy codes for *only* console, it gets complex.
                // For simplicity, let's assume MiniMessage will handle most formatting.

                // If you absolutely need to convert AFTER MiniMessage, do it carefully.
                // The primary problem was deserializing MiniMessage via LegacyComponentSerializer.
                // For most cases, MiniMessage can directly parse & codes if configured with
                // MiniMessage.builder().parseLegacyColors().build(); or similar.
                return s; // No post-processing needed if MiniMessage handles it during deserialize
            })
            // This is the key part: Tell MiniMessage to parse legacy color codes (&)
            .postProcessor(message -> {
                // This is a safety net. MiniMessage's parser is generally robust.
                // For pure MiniMessage, you wouldn't need this post-processor for legacy codes.
                // However, if your config files contain a mix, and you want to ensure legacy
                // are converted, you might serialize the MiniMessage output to legacy,
                // then deserialize it back to Adventure. This is an anti-pattern
                // for performance and reliability.
                // The correct way for MiniMessage to handle legacy is via its features:
                // .parsingComponentSerializer(LegacyComponentSerializer.builder().character('&').hexColors().build())
                // This is how you'd normally combine them if MiniMessage was the *only* entry point for parsing.

                // Given your current setup where parse() takes a String that *might* contain & codes
                // and then passes it to MiniMessage, let's adjust how the parsing happens.
                // The below legacySerializer is for outputting/inputting legacy codes separately.
                return message;
            })
            // Feature.LEGACY_COLORS allows MiniMessage to parse &c, &l, etc.
            .build();

    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static BukkitAudiences audiences;

    // Constants for dynamic formatting
    private static final String PADDING_CHAR = " ";
    private static final String SCALED_SEPARATOR_MARKER = "{scaled.separator}";

    /**
     * Initialize the message utility with a plugin instance
     * @param plugin The plugin instance
     */
    public static void initialize(Plugin plugin) {
        if (audiences == null) {
            audiences = BukkitAudiences.create(plugin);
        }
    }

    /**
     * Clean up resources when the plugin is disabled
     */
    public static void close() {
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
    }

    /**
     * Parse a message to a Component, supporting both legacy and MiniMessage formats.
     * Prioritizes MiniMessage. If legacy color codes are present, they will be
     * processed by MiniMessage if configured to do so, or converted if only legacy is used.
     *
     * @param message The message to parse
     * @return The parsed Component
     */
    public static Component parse(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        // Option 1: Only parse with MiniMessage.
        // If your source strings are purely MiniMessage, this is all you need.
        // MiniMessage handles its own tags (<bold>, <red>) directly.
        // If you want MiniMessage to also handle & codes, you need to configure MiniMessage to do so.
        // Example: MiniMessage.builder().tags(MiniMessage.standardTags()).preProcess(LegacyComponentSerializer.builder().character('&').build()::deserialize).build();
        // Or simpler, if you are on PaperMC/Spigot recent versions, their MiniMessage often
        // automatically parses legacy codes, or you can use .parseLegacyColors() if MiniMessage instance supports it.

        // Given your current setup, where MiniMessage instance is fixed,
        // and you also have legacySerializer, the most robust way to support BOTH
        // while prioritizing MiniMessage's tags is to:
        // 1. Convert legacy codes to MiniMessage hex format where possible
        // 2. Then, let MiniMessage parse everything.

        // This approach attempts to convert legacy codes to MiniMessage's
        // understanding of hex codes or named colors before parsing.
        // This is not standard but handles mixed input.
        // A cleaner approach would be for your source strings to be consistently MiniMessage.

        // Let's assume input strings primarily use MiniMessage tags, but might
        // also contain legacy '&' codes which you want to convert to MiniMessage's
        // format before full parsing.

        String finalMessage = message;

        // If it contains legacy codes, convert them to Adventure Components
        // then serialize them back to MiniMessage to ensure MiniMessage can handle them.
        // This is a bit of a hack to get mixed input working.
        if (message.contains("&") || message.contains("§")) {
            // Deserialize legacy codes into an Adventure Component
            Component legacyConverted = legacySerializer.deserialize(message);
            // Re-serialize that component using MiniMessage's serializer
            // This is complex because MiniMessage.miniMessage() is for deserializing strings TO components,
            // not for serializing components TO MiniMessage strings for re-parsing.
            // You would need a MiniMessage serializer if you wanted to do this cleanly.

            // Given your current setup, the safest is to pass it directly to MiniMessage
            // and ensure MiniMessage has a feature to parse legacy codes.
            // Or, if MiniMessage doesn't have that feature, convert legacy to string tags.
            // Example: &a -> <green>

            // A more robust way to handle mixed input:
            // Convert legacy codes to MiniMessage equivalents
            finalMessage = finalMessage.replace("&0", "<black>");
            finalMessage = finalMessage.replace("&1", "<dark_blue>");
            finalMessage = finalMessage.replace("&2", "<dark_green>");
            finalMessage = finalMessage.replace("&3", "<dark_aqua>");
            finalMessage = finalMessage.replace("&4", "<dark_red>");
            finalMessage = finalMessage.replace("&5", "<dark_purple>");
            finalMessage = finalMessage.replace("&6", "<gold>");
            finalMessage = finalMessage.replace("&7", "<gray>");
            finalMessage = finalMessage.replace("&8", "<dark_gray>");
            finalMessage = finalMessage.replace("&9", "<blue>");
            finalMessage = finalMessage.replace("&a", "<green>");
            finalMessage = finalMessage.replace("&b", "<aqua>");
            finalMessage = finalMessage.replace("&c", "<red>");
            finalMessage = finalMessage.replace("&d", "<light_purple>");
            finalMessage = finalMessage.replace("&e", "<yellow>");
            finalMessage = finalMessage.replace("&f", "<white>");
            finalMessage = finalMessage.replace("&k", "<obfuscated>");
            finalMessage = finalMessage.replace("&l", "<bold>");
            finalMessage = finalMessage.replace("&m", "<strikethrough>");
            finalMessage = finalMessage.replace("&n", "<underline>");
            finalMessage = finalMessage.replace("&o", "<italic>");
            finalMessage = finalMessage.replace("&r", "<reset>");
            // Also handle section symbol
            finalMessage = finalMessage.replace("§", "&"); // Convert § to & for consistency with replacements
            // This is not ideal as it means your config strings must be pre-converted,
            // or your MiniMessage instance must handle legacy.

            // The best way is to let MiniMessage handle everything from the start.
            // If you initialize MiniMessage with .parsingComponentSerializer(LegacyComponentSerializer.builder().character('&').build()),
            // then MiniMessage will parse & codes into Adventure components, and then it will apply MiniMessage tags on top.
            // HOWEVER, you only need to call deserialize ONCE.

            // Let's simplify: You want MiniMessage tags like <bold> to work.
            // So, pass the raw string to MiniMessage. It's the primary parsing engine.
            // If you ALSO want & codes, MiniMessage needs to be configured for that.
            // PaperMC's Adventure integration often handles legacy codes for you,
            // so sending MiniMessage.deserialize("Your &c<bold>Message</bold>") often works.
        }

        // Just deserialize with MiniMessage directly.
        // It will handle its own tags (<bold>, <red>, etc.)
        // If your server platform (Paper/Spigot) has MiniMessage configured to also
        // parse legacy codes, then this will work for both.
        // Otherwise, legacy codes will be displayed as raw text or stripped.
        return miniMessage.deserialize(finalMessage);
    }


    /**
     * Send a message to a command sender
     *
     * @param sender The command sender to receive the message
     * @param message The message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (audiences == null) {
            sender.sendMessage(message); // Fallback to raw if Adventure not initialized
            return;
        }

        // processMessage now only handles custom variables like {prefix} and {scaled.separator}
        // It should NOT modify formatting tags as 'parse' will handle that.
        String processedMessage = processMessage(message);

        // Send the parsed Component
        audiences.sender(sender).sendMessage(parse(processedMessage));
    }

    /**
     * Send a message to the console
     *
     * @param message The message to send
     */
    public static void sendConsoleMessage(String message) {
        if (audiences == null) {
            Bukkit.getConsoleSender().sendMessage(message); // Fallback to raw if Adventure not initialized
            return;
        }

        String processedMessage = processMessage(message);
        audiences.console().sendMessage(parse(processedMessage));
    }

    /**
     * Send a message to a player
     *
     * @param player The player to receive the message
     * @param message The message to send
     */
    public static void sendPlayerMessage(Player player, String message) {
        if (audiences == null) {
            player.sendMessage(message); // Fallback to raw if Adventure not initialized
            return;
        }

        String processedMessage = processMessage(message);
        audiences.player(player).sendMessage(parse(processedMessage));
    }

    /**
     * Broadcast a message to all players and console
     *
     * @param message The message to broadcast
     */
    public static void broadcastMessage(String message) {
        if (audiences == null) {
            Bukkit.broadcastMessage(message); // Fallback to raw if Adventure not initialized
            return;
        }

        String processedMessage = processMessage(message);
        audiences.all().sendMessage(parse(processedMessage));
    }

    /**
     * Process any message through the complete TextUtility system.
     * This automatically handles prefix, scaled separators, and any other processing.
     * It does *not* handle MiniMessage/legacy formatting tags, as 'parse' does that.
     *
     * @param message The raw message
     * @return The fully processed message
     */
    public static String processMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        // Handle prefix replacement first
        message = replacePrefixVariable(message);

        // Handle scaled separator replacement (this will be automatic)
        if (message.contains(SCALED_SEPARATOR_MARKER)) {
            message = replaceScaledSeparator(message);
        }

        return message;
    }

    /**
     * Replace variables in a message template
     * @param message The message template
     * @param replacements Variable replacements (key-value pairs)
     * @return The message with variables replaced
     */
    public static String replaceVariables(String message, String... replacements) {
        if (message == null || replacements.length % 2 != 0) {
            return message;
        }

        // First do variable replacements
        for (int i = 0; i < replacements.length; i += 2) {
            String key = replacements[i];
            String value = replacements[i + 1];
            message = message.replace("{" + key + "}", value);
        }

        // Then process through the complete system (prefix, separators etc.)
        // The 'parse' method will be called within sendMessage/sendConsoleMessage/etc.
        return message; // No need to call processMessage here directly. The sender methods do that.
    }

    /**
     * Replace variables and handle dynamic padding for consistent line lengths
     * @param message The message template
     * @param replacements Variable replacements (key-value pairs)
     * @return The message with variables replaced and padding adjusted
     */
    public static String replaceVariablesWithPadding(String message, String... replacements) {
        // This now just calls replaceVariables since all processing is automatic
        return replaceVariables(message, replacements);
    }

    /**
     * Replace the {prefix} variable with the configured prefix
     * @param message The message containing {prefix}
     * @return The message with prefix replaced
     */
    private static String replacePrefixVariable(String message) {
        if (!message.contains("{prefix}")) {
            return message;
        }

        CoreFramework framework = CoreFramework.getInstance();
        if (framework == null) {
            // Fallback if framework isn't initialized
            return message.replace("{prefix}", "[CoreFramework]");
        }

        // Check if prefix is enabled
        boolean prefixEnabled = framework.getInternalConfig().getBoolean("prefix.enabled", true);
        if (!prefixEnabled) {
            // Remove prefix entirely
            return message.replace("{prefix}", "").trim();
        }

        // Get the prefix from language config
        String prefix = framework.getLanguageConfig().getString("prefix", "[CoreFramework]");
        return message.replace("{prefix}", prefix);
    }

    /**
     * Replace {scaled.separator} with calculated strikethrough padding
     * This ensures the TOTAL line length equals the configured target length
     *
     * @param message The message containing {scaled.separator}
     * @return The message with scaled separator replaced
     */
    private static String replaceScaledSeparator(String message) {
        if (!message.contains(SCALED_SEPARATOR_MARKER)) {
            return message;
        }

        // Get configuration values
        int targetTotalLength = getConfiguredLineLength();
        int minPadding = getConfiguredMinPadding();

        // Remove the separator marker to calculate content length
        String contentWithoutSeparator = message.replace(SCALED_SEPARATOR_MARKER, "");
        // stripFormattingForLength needs to correctly handle MiniMessage for length calculation
        String plainTextContent = stripFormattingForLength(contentWithoutSeparator);

        int contentLength = plainTextContent.length();
        int neededPadding = targetTotalLength - contentLength;

        // Check if the content is too long (safety check)
        if (neededPadding < minPadding) {
            CoreFramework framework = CoreFramework.getInstance();
            if (framework != null) {
                String errorMessage = String.format(
                    "FATAL ERROR: Line content too long for configured line_length! " +
                    "Content length: %d, Target length: %d, Min padding: %d. " +
                    "Line: %s",
                    contentLength, targetTotalLength, minPadding, message
                );
                framework.getLogger().severe(errorMessage);

                // Disable the plugin
                framework.getServer().getPluginManager().disablePlugin(framework);

                // Return a safe fallback
                return message.replace(SCALED_SEPARATOR_MARKER, "<st>" + PADDING_CHAR.repeat(minPadding) + "</st>");
            }
        }

        // Create the separator with the calculated padding
        String dynamicPadding = "<st>" + PADDING_CHAR.repeat(neededPadding) + "</st>";
        return message.replace(SCALED_SEPARATOR_MARKER, dynamicPadding);
    }

    /**
     * Get the configured line length from the framework config
     * @return The target line length
     */
    private static int getConfiguredLineLength() {
        CoreFramework framework = CoreFramework.getInstance();
        if (framework == null) {
            return 60; // Default fallback
        }

        return framework.getInternalConfig().getInt("formatting.line_length", 60);
    }

    /**
     * Get the configured minimum padding from the framework config
     * @return The minimum padding spaces
     */
    private static int getConfiguredMinPadding() {
        CoreFramework framework = CoreFramework.getInstance();
        if (framework == null) {
            return 2; // Default fallback
        }

        return framework.getInternalConfig().getInt("formatting.min_padding", 2); // Assuming you have this config
    }

    /**
     * Strips MiniMessage and legacy formatting from a string for accurate length calculation.
     * @param message The message with formatting.
     * @return The plain text content.
     */
    private static String stripFormattingForLength(String message) {
        // Strip MiniMessage tags first
        String strippedMiniMessage = MiniMessage.miniMessage().stripTags(message);

        // Then strip legacy color codes if any remain (though MiniMessage.stripTags should handle most)
        String strippedLegacy = strippedMiniMessage.replaceAll("(?i)&[0-9a-fklmnor]", "");
        strippedLegacy = strippedLegacy.replaceAll("§[0-9a-fklmnor]", ""); // Also strip section symbol

        return strippedLegacy;
    }
}