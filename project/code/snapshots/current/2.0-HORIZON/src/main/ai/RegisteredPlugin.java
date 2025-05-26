package io.rhythmknights.coreframework.component.api.plugin;

import io.rhythmknights.coreframework.component.api.hook.HookRequirement;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Represents a plugin registered with CoreFramework
 */
public class RegisteredPlugin {
    
    private final Plugin plugin;
    private final String version;
    private final String codename;
    private final List<HookRequirement> hookRequirements;
    private boolean initialized = false;
    private boolean allRequiredHooksSuccessful = false;
    
    /**
     * Create a new registered plugin
     * 
     * @param plugin The Bukkit plugin instance
     * @param version The plugin version
     * @param codename The version codename
     * @param hookRequirements List of hook requirements
     */
    public RegisteredPlugin(Plugin plugin, String version, String codename, List<HookRequirement> hookRequirements) {
        this.plugin = plugin;
        this.version = version;
        this.codename = codename;
        this.hookRequirements = hookRequirements;
    }
    
    /**
     * Get the plugin instance
     * @return The plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }
    
    /**
     * Get the plugin name
     * @return The plugin name
     */
    public String getName() {
        return plugin.getName();
    }
    
    /**
     * Get the plugin version
     * @return The version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Get the version codename
     * @return The codename
     */
    public String getCodename() {
        return codename;
    }
    
    /**
     * Get the hook requirements
     * @return List of hook requirements
     */
    public List<HookRequirement> getHookRequirements() {
        return hookRequirements;
    }
    
    /**
     * Check if the plugin has been initialized
     * @return True if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Set the initialization status
     * @param initialized The initialization status
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    
    /**
     * Check if all required hooks were successful
     * @return True if all required hooks succeeded
     */
    public boolean areAllRequiredHooksSuccessful() {
        return allRequiredHooksSuccessful;
    }
    
    /**
     * Set the required hooks status
     * @param allRequiredHooksSuccessful The status
     */
    public void setAllRequiredHooksSuccessful(boolean allRequiredHooksSuccessful) {
        this.allRequiredHooksSuccessful = allRequiredHooksSuccessful;
    }
}