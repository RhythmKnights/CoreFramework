package io.rhythmknights.coreframework.component.api;

import io.rhythmknights.coreframework.CoreFramework;
import io.rhythmknights.coreframework.component.api.hook.HookRequirement;
import io.rhythmknights.coreframework.component.api.plugin.RegisteredPlugin;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Main API interface for CoreFramework
 * Provides methods for plugin registration and hook management
 */
public class FrameworkAPI {
    
    private final CoreFramework framework;
    
    /**
     * Constructor for FrameworkAPI
     * 
     * @param framework The CoreFramework plugin instance
     */
    public FrameworkAPI(CoreFramework framework) {
        this.framework = framework;
    }
    
    /**
     * Register a plugin with CoreFramework (silent registration)
     * The display will happen later in a batch for clean output
     * 
     * @param plugin The plugin to register
     * @param version The plugin version
     * @param codename The version codename (e.g., "HORIZON")
     * @param hookRequirements List of hook requirements
     * @return The registered plugin instance
     */
    public RegisteredPlugin registerPlugin(Plugin plugin, String version, String codename, List<HookRequirement> hookRequirements) {
        RegisteredPlugin registered = framework.getCoreRegistry().registerPlugin(plugin, version, codename, hookRequirements);
        
        // Silent registration - just log quietly to server console (not our fancy display)
        // framework.getLogger().info("Registered plugin: " + plugin.getName() + " v" + version + " [" + codename + "]"); - STARTUP LOGGER
        
        // Optionally trigger early display if all expected plugins are registered
        framework.triggerDisplayIfReady();
        
        return registered;
    }
    
    /**
     * Get the CoreAPI version
     * @return The bundled CoreAPI version
     */
    public String getCoreAPIVersion() {
        return framework.getInternalConfig().getString("coreapi.version", "unknown");
    }
    
    /**
     * Get the CoreFramework version
     * @return The framework version
     */
    public String getFrameworkVersion() {
        return framework.getDescription().getVersion();
    }
    
    /**
     * Check if a specific CoreAPI version is compatible
     * 
     * @param requiredVersion The required version
     * @return True if compatible
     */
    public boolean isAPIVersionCompatible(String requiredVersion) {
        // TODO: Implement version compatibility checking
        return true;
    }
}