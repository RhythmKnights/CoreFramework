package io.rhythmknights.coreframework.component.core;

import io.rhythmknights.coreframework.CoreFramework;
import io.rhythmknights.coreframework.component.api.plugin.RegisteredPlugin;
import io.rhythmknights.coreframework.component.utility.TextUtility;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Controls the startup display and plugin initialization process
 */
public class CoreBootstrap {
    
    private final CoreFramework framework;
    private final YamlConfiguration config;
    private final YamlConfiguration lang;
    private final OutputCache outputCache;
    
    /**
     * Constructor for CoreBootstrap
     * 
     * @param framework The CoreFramework plugin instance
     * @param outputCache The output cache for delayed display
     */
    public CoreBootstrap(CoreFramework framework, OutputCache outputCache) {
        this.framework = framework;
        this.config = framework.getInternalConfig();
        this.lang = framework.getLanguageConfig();
        this.outputCache = outputCache;
    }
    
    /**
     * Original constructor for backward compatibility
     * 
     * @param framework The CoreFramework plugin instance
     */
    public CoreBootstrap(CoreFramework framework) {
        this.framework = framework;
        this.config = framework.getInternalConfig();
        this.lang = framework.getLanguageConfig();
        this.outputCache = null; // No output cache in legacy mode
    }
    
    /**
     * Prepare the startup header and basic info (caches without displaying)
     * This runs when CoreFramework enables but caches the output for later
     */
    public void prepareStartupHeaderOnly() {
        // Check if header is enabled
        boolean headerEnabled = config.getBoolean("header.enabled", true);
        if (!headerEnabled) {
            // Skip header entirely, just show basic init
            String initStart = lang.getString("startup.initialization_start", "");
            outputCache.addLine(initStart);
            prepareAPIInfo();
            prepareDetectedPlugins();
            return;
        }

        // Get number of header lines to display (1-8)
        int headerLines = config.getInt("header.lines", 3);

        // Clamp the value between 1 and 8
        headerLines = Math.max(1, Math.min(8, headerLines));

        // Add the specified number of header lines to the cache
        for (int i = 1; i <= headerLines; i++) {
            String headerLine = lang.getString("startup.header_line" + i, "");
            if (!headerLine.isEmpty()) {
                outputCache.addLine(headerLine);
            }
        }

        String initStart = lang.getString("startup.initialization_start", "");
        outputCache.addLine(initStart);

        prepareAPIInfo();
        prepareDetectedPlugins();

        String separator = lang.getString("startup.separator", "");
        outputCache.addLine(separator);
    }
    
    /**
     * Display the startup header and basic info immediately (for backward compatibility)
     * Using the original method signature from your latest version
     */
    public void displayStartupHeaderOnly() {
        // Check if we have an OutputCache
        if (outputCache != null) {
            // If we have a cache, prepare then flush
            prepareStartupHeaderOnly();
            outputCache.flush();
        } else {
            // Legacy behavior - direct output
            legacyDisplayStartupHeaderOnly();
        }
    }
    
    /**
     * Legacy implementation of displayStartupHeaderOnly for backward compatibility
     * Directly outputs to console without caching
     */
    private void legacyDisplayStartupHeaderOnly() {
        // Check if header is enabled
        boolean headerEnabled = config.getBoolean("header.enabled", true);
        if (!headerEnabled) {
            // Skip header display entirely, just show basic init
            String initStart = lang.getString("startup.initialization_start", "");
            TextUtility.sendConsoleMessage(initStart);
            legacyDisplayAPIInfo();
            legacyDisplayDetectedPlugins();
            return;
        }

        // Get number of header lines to display (1-8)
        int headerLines = config.getInt("header.lines", 3);

        // Clamp the value between 1 and 8
        headerLines = Math.max(1, Math.min(8, headerLines));

        // Display the specified number of header lines
        for (int i = 1; i <= headerLines; i++) {
            String headerLine = lang.getString("startup.header_line" + i, "");
            if (!headerLine.isEmpty()) {
                TextUtility.sendConsoleMessage(headerLine);
            }
        }

        String initStart = lang.getString("startup.initialization_start", "");
        TextUtility.sendConsoleMessage(initStart);

        legacyDisplayAPIInfo();
        legacyDisplayDetectedPlugins();

        String separator = lang.getString("startup.separator", "");
        TextUtility.sendConsoleMessage(separator);
    }
    
    /**
     * Prepare CoreAPI information (cache for later display)
     */
    private void prepareAPIInfo() {
        String detectingAPI = lang.getString("startup.detecting_api", "");
        outputCache.addLine(detectingAPI);

        String coreAPIVersion = config.getString("coreapi.version", "unknown");
        String apiFound = lang.getString("startup.api_found", "");
        apiFound = TextUtility.replaceVariables(apiFound, "version", coreAPIVersion);
        outputCache.addLine(apiFound);
    }
    
    /**
     * Legacy method to display API info directly
     */
    private void legacyDisplayAPIInfo() {
        String detectingAPI = lang.getString("startup.detecting_api", "");
        TextUtility.sendConsoleMessage(detectingAPI);

        String coreAPIVersion = config.getString("coreapi.version", "unknown");
        String apiFound = lang.getString("startup.api_found", "");
        apiFound = TextUtility.replaceVariables(apiFound, "version", coreAPIVersion);
        TextUtility.sendConsoleMessage(apiFound);
    }
    
    /**
     * Prepare detected core plugins info (cache for later display)
     */
    private void prepareDetectedPlugins() {
        String detectingPlugins = lang.getString("startup.detecting_plugins", "");
        outputCache.addLine(detectingPlugins);

        List<String> corePlugins = config.getStringList("detection.core_plugins");

        for (String pluginName : corePlugins) {
            if (Bukkit.getPluginManager().getPlugin(pluginName) != null) {
                String version = Bukkit.getPluginManager().getPlugin(pluginName).getDescription().getVersion();
                String pluginDetected = lang.getString("startup.plugin_detected", "");
                pluginDetected = TextUtility.replaceVariables(pluginDetected, 
                    "plugin", pluginName, 
                    "version", version);
                outputCache.addLine(pluginDetected);
            }
        }
    }
    
    /**
     * Legacy method to display detected plugins directly
     */
    private void legacyDisplayDetectedPlugins() {
        String detectingPlugins = lang.getString("startup.detecting_plugins", "");
        TextUtility.sendConsoleMessage(detectingPlugins);

        List<String> corePlugins = config.getStringList("detection.core_plugins");

        for (String pluginName : corePlugins) {
            if (Bukkit.getPluginManager().getPlugin(pluginName) != null) {
                String version = Bukkit.getPluginManager().getPlugin(pluginName).getDescription().getVersion();
                String pluginDetected = lang.getString("startup.plugin_detected", "");
                pluginDetected = TextUtility.replaceVariables(pluginDetected, 
                    "plugin", pluginName, 
                    "version", version);
                TextUtility.sendConsoleMessage(pluginDetected);
            }
        }
    }
    
    /**
     * Prepare hook status for a specific plugin (cache for later display)
     * 
     * @param registeredPlugin The registered plugin to prepare hook status for
     */
    public void preparePluginHookStatus(RegisteredPlugin registeredPlugin) {
        String separator = lang.getString("startup.separator", "");
        outputCache.addLine(separator);
        
        // Hooks header
        String hooksHeader = lang.getString("plugin_hooks.hooks_header", "");
        outputCache.addLine(hooksHeader);
        
        // Plugin header
        String pluginHeader = lang.getString("plugin_hooks.header", "");
        pluginHeader = TextUtility.replaceVariables(pluginHeader,
            "plugin", registeredPlugin.getName(),
            "version", registeredPlugin.getVersion(),
            "codename", registeredPlugin.getCodename());
        outputCache.addLine(pluginHeader);
        
        // Prepare required hooks
        boolean hasRequired = registeredPlugin.getHookRequirements().stream()
            .anyMatch(hr -> hr.isRequired());
        
        if (hasRequired) {
            String requiredHeader = lang.getString("plugin_hooks.required_header", "");
            outputCache.addLine(requiredHeader);
            
            registeredPlugin.getHookRequirements().stream()
                .filter(hr -> hr.isRequired())
                .forEach(this::prepareHookStatus);
        }
        
        // Prepare optional hooks
        boolean hasOptional = registeredPlugin.getHookRequirements().stream()
            .anyMatch(hr -> !hr.isRequired());
        
        if (hasOptional) {
            String optionalHeader = lang.getString("plugin_hooks.optional_header", "");
            outputCache.addLine(optionalHeader);
            
            registeredPlugin.getHookRequirements().stream()
                .filter(hr -> !hr.isRequired())
                .forEach(this::prepareHookStatus);
        }
    }
    
    /**
     * Display hook status for a specific plugin immediately (for backward compatibility)
     * Using the original method signature from your latest version
     * 
     * @param registeredPlugin The registered plugin to display hook status for
     */
    public void displayPluginHookStatus(RegisteredPlugin registeredPlugin) {
        // Check if we have an OutputCache
        if (outputCache != null) {
            // If we have a cache, prepare then flush
            preparePluginHookStatus(registeredPlugin);
            outputCache.flush();
        } else {
            // Legacy behavior - direct display
            legacyDisplayPluginHookStatus(registeredPlugin);
        }
    }
    
    /**
     * Legacy implementation of displayPluginHookStatus for backward compatibility
     * Directly outputs to console without caching
     */
    private void legacyDisplayPluginHookStatus(RegisteredPlugin registeredPlugin) {
        String separator = lang.getString("startup.separator", "");
        TextUtility.sendConsoleMessage(separator);
      
        // Hooks header
        String hooksHeader = lang.getString("plugin_hooks.hooks_header", "");
        TextUtility.sendConsoleMessage(hooksHeader);
        
        // Plugin header
        String pluginHeader = lang.getString("plugin_hooks.header", "");
        pluginHeader = TextUtility.replaceVariables(pluginHeader,
            "plugin", registeredPlugin.getName(),
            "version", registeredPlugin.getVersion(),
            "codename", registeredPlugin.getCodename());
        TextUtility.sendConsoleMessage(pluginHeader);
        
        // Display required hooks
        boolean hasRequired = registeredPlugin.getHookRequirements().stream()
            .anyMatch(hr -> hr.isRequired());
        
        if (hasRequired) {
            String requiredHeader = lang.getString("plugin_hooks.required_header", "");
            TextUtility.sendConsoleMessage(requiredHeader);
            
            registeredPlugin.getHookRequirements().stream()
                .filter(hr -> hr.isRequired())
                .forEach(this::legacyDisplayHookStatus);
        }
        
        // Display optional hooks
        boolean hasOptional = registeredPlugin.getHookRequirements().stream()
            .anyMatch(hr -> !hr.isRequired());
        
        if (hasOptional) {
            String optionalHeader = lang.getString("plugin_hooks.optional_header", "");
            TextUtility.sendConsoleMessage(optionalHeader);
            
            registeredPlugin.getHookRequirements().stream()
                .filter(hr -> !hr.isRequired())
                .forEach(this::legacyDisplayHookStatus);
        }
    }
    
    /**
     * Prepare individual hook status (cache for later display)
     */
    private void prepareHookStatus(io.rhythmknights.coreframework.component.api.hook.HookRequirement hookRequirement) {
        boolean isAvailable = Bukkit.getPluginManager().getPlugin(hookRequirement.getPluginName()) != null;
        
        String messageTemplate;
        if (isAvailable) {
            messageTemplate = lang.getString("plugin_hooks.hook_success", "");
        } else {
            messageTemplate = lang.getString("plugin_hooks.hook_failed", "");
        }
        
        String message = TextUtility.replaceVariables(messageTemplate, 
            "plugin", hookRequirement.getPluginName());
        outputCache.addLine(message);
    }
    
    /**
     * Legacy method to display hook status directly
     */
    private void legacyDisplayHookStatus(io.rhythmknights.coreframework.component.api.hook.HookRequirement hookRequirement) {
        boolean isAvailable = Bukkit.getPluginManager().getPlugin(hookRequirement.getPluginName()) != null;
        
        String messageTemplate;
        if (isAvailable) {
            messageTemplate = lang.getString("plugin_hooks.hook_success", "");
        } else {
            messageTemplate = lang.getString("plugin_hooks.hook_failed", "");
        }
        
        String message = TextUtility.replaceVariables(messageTemplate, 
            "plugin", hookRequirement.getPluginName());
        TextUtility.sendConsoleMessage(message);
    }
    
    /**
     * Prepare activation summary (cache for later display)
     */
    public void prepareActivationSummary(List<String> successfulPlugins, List<String> failedPlugins) {
        String separator = lang.getString("startup.separator", "");
        outputCache.addLine(separator);
        outputCache.addLine(separator);

        String activationHeader = lang.getString("activation.header", "");
        outputCache.addLine(activationHeader);
        
        if (!successfulPlugins.isEmpty()) {
            String successMessage = lang.getString("activation.success", "");
            outputCache.addLine(successMessage);
            
            String pluginList = String.join(" - ", successfulPlugins);
            String pluginListMessage = lang.getString("activation.plugin_list", "");
            pluginListMessage = TextUtility.replaceVariables(pluginListMessage, "plugins", pluginList);
            outputCache.addLine(pluginListMessage);
        }
        
        if (!failedPlugins.isEmpty()) {
            String failedMessage = lang.getString("activation.failed", "");
            outputCache.addLine(failedMessage);
            
            String failedPluginList = String.join(" - ", failedPlugins);
            String failedListMessage = lang.getString("activation.failed_plugin_list", "");
            failedListMessage = TextUtility.replaceVariables(failedListMessage, "plugins", failedPluginList);
            outputCache.addLine(failedListMessage);
        }
        
        outputCache.addLine(separator);
    }
    
    /**
     * Display activation summary immediately (for backward compatibility)
     * Using the original method signature from your latest version
     */
    public void displayActivationSummary(List<String> successfulPlugins, List<String> failedPlugins) {
        // Check if we have an OutputCache
        if (outputCache != null) {
            // If we have a cache, prepare then flush
            prepareActivationSummary(successfulPlugins, failedPlugins);
            outputCache.flush();
        } else {
            // Legacy behavior - direct display
            legacyDisplayActivationSummary(successfulPlugins, failedPlugins);
        }
    }
    
    /**
     * Legacy implementation of displayActivationSummary for backward compatibility
     * Directly outputs to console without caching
     */
    private void legacyDisplayActivationSummary(List<String> successfulPlugins, List<String> failedPlugins) {
        String separator = lang.getString("startup.separator", "");
        TextUtility.sendConsoleMessage(separator);

        String activationHeader = lang.getString("activation.header", "");
        TextUtility.sendConsoleMessage(activationHeader);
        
        if (!successfulPlugins.isEmpty()) {
            String successMessage = lang.getString("activation.success", "");
            TextUtility.sendConsoleMessage(successMessage);
            
            String pluginList = String.join(" - ", successfulPlugins);
            String pluginListMessage = lang.getString("activation.plugin_list", "");
            pluginListMessage = TextUtility.replaceVariables(pluginListMessage, "plugins", pluginList);
            TextUtility.sendConsoleMessage(pluginListMessage);
        }
        
        if (!failedPlugins.isEmpty()) {
            String failedMessage = lang.getString("activation.failed", "");
            TextUtility.sendConsoleMessage(failedMessage);
            
            String failedPluginList = String.join(" - ", failedPlugins);
            String failedListMessage = lang.getString("activation.failed_plugin_list", "");
            failedListMessage = TextUtility.replaceVariables(failedListMessage, "plugins", failedPluginList);
            TextUtility.sendConsoleMessage(failedListMessage);
        }
        
        TextUtility.sendConsoleMessage(separator);
    }
    
    /**
     * Display shutdown message
     */
    public void displayShutdownMessage() {
        String shutdownMessage = lang.getString("shutdown.message", "CoreFramework disabled.");
        TextUtility.sendConsoleMessage(shutdownMessage);
    }
}