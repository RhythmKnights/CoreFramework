package io.rhythmknights.coreframework.component.utility;

import io.rhythmknights.coreframework.CoreFramework;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Single Source of Truth for all message formatting and sending across RhythmKnights plugins
 * Handles both legacy and Adventure API formatting with proper defaults (white, non-italic)
 * Supports mixed formatting (legacy and MiniMessage in the same string)
 */
public class TextUtility {

    /**
     * Private constructor to prevent instantiation of utility class
     */
    private TextUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Initialize MiniMessage for parsing modern formatting
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Legacy serializer for handling & codes
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
     * Parse a message to a Component with proper defaults (white, non-italic).
     * Supports both legacy (&) and MiniMessage formats with hex colors, including mixed formats.
     *
     * @param message The message to parse
     * @return The parsed Component with proper defaults
     */
    public static Component parse(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        Component result;
        
        try {
            // Always process legacy codes first, since MiniMessage can handle the output
            // This allows for mixed formatting like: "&cHello <bold>world</bold> &#FF0000"
            
            // Step 1: Convert legacy codes (including hex) to a format MiniMessage can understand
            String processedMessage = message;
            
            // Handle legacy hex codes (&#RRGGBB) first
            processedMessage = processedMessage.replaceAll("&#([0-9A-Fa-f]{6})", "<#$1>");
            
            // Handle standard legacy codes (&a, &l, etc.) by converting to MiniMessage equivalents
            processedMessage = processedMessage
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");
            
            // Step 2: Parse with MiniMessage (which now handles everything)
            result = miniMessage.deserialize(processedMessage);
            
            // Step 3: Apply default formatting
            result = applyDefaults(result);
            
        } catch (Exception e) {
            // If MiniMessage fails, fall back to legacy-only parsing
            try {
                result = legacySerializer.deserialize(message);
                result = applyDefaults(result);
            } catch (Exception legacyException) {
                // Ultimate fallback to basic component with defaults
                result = Component.text(message);
                result = applyDefaults(result);
            }
        }

        return result;
    }

    /**
     * Apply default formatting (white, non-italic) to a component
     * @param component The component to apply defaults to
     * @return Component with defaults applied
     */
    private static Component applyDefaults(Component component) {
        // Apply defaults only if the component doesn't already have explicit formatting
        Component result = component;
        
        // Force non-italic for all components (Minecraft GUI default is italic)
        result = result.decoration(TextDecoration.ITALIC, false);
        
        // Apply white color if no color is set (check if it's the default/empty color)
        if (component.color() == null) {
            result = result.color(net.kyori.adventure.text.format.NamedTextColor.WHITE);
        }
        
        return result;
    }

    /**
     * Check if a message contains MiniMessage formatting tags
     * @param message The message to check
     * @return True if MiniMessage tags are detected
     */
    private static boolean containsMiniMessageTags(String message) {
        return message.contains("<") && message.contains(">") && 
               (message.contains("</") || message.matches(".*<[a-zA-Z_#][a-zA-Z0-9_#]*>.*"));
    }

    /**
     * Check if a message contains legacy color codes
     * @param message The message to check
     * @return True if legacy codes are detected
     */
    private static boolean containsLegacyCodes(String message) {
        // Check for standard legacy codes (&a, &f, etc.)
        boolean hasStandardLegacy = message.contains("&") || message.contains("§");
        
        // Check for hex color codes (&#RRGGBB format)
        boolean hasHexLegacy = message.matches(".*&#[0-9A-Fa-f]{6}.*");
        
        return hasStandardLegacy || hasHexLegacy;
    }

    /**
     * Check if a message contains mixed formatting (both legacy and MiniMessage)
     * @param message The message to check
     * @return True if mixed formatting is detected
     */
    private static boolean containsMixedFormatting(String message) {
        return containsLegacyCodes(message) && containsMiniMessageTags(message);
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

        // processMessage handles custom variables like {prefix} and {scaled.separator}
        String processedMessage = processMessage(message);

        // Send the parsed Component with proper defaults
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

        return message;
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
                return message.replace(SCALED_SEPARATOR_MARKER, "<strikethrough>" + PADDING_CHAR.repeat(minPadding) + "</strikethrough>");
            }
        }

        // Create the separator with the calculated padding using MiniMessage format
        String dynamicPadding = "<strikethrough>" + PADDING_CHAR.repeat(neededPadding) + "</strikethrough>";
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

        return framework.getInternalConfig().getInt("formatting.min_padding", 2);
    }

    /**
     * Strips formatting from a string for accurate length calculation.
     * @param message The message with formatting.
     * @return The plain text content.
     */
    private static String stripFormattingForLength(String message) {
        // Strip MiniMessage tags first
        String strippedMiniMessage = message.replaceAll("<[^>]+>", "");
        
        // Then strip legacy color codes
        String strippedLegacy = strippedMiniMessage.replaceAll("(?i)&[0-9a-fklmnor]", "");
        strippedLegacy = strippedLegacy.replaceAll("§[0-9a-fklmnor]", ""); // Also strip section symbol
        
        // Strip hex codes
        strippedLegacy = strippedLegacy.replaceAll("&#[0-9A-Fa-f]{6}", "");

        return strippedLegacy;
    }
}