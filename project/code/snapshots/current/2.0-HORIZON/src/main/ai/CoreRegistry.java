package io.rhythmknights.coreframework.component.core;

import io.rhythmknights.coreframework.CoreFramework;
import io.rhythmknights.coreframework.component.api.hook.HookRequirement;
import io.rhythmknights.coreframework.component.api.plugin.RegisteredPlugin;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages registered plugins and their information
 */
public class CoreRegistry {
    
    private final CoreFramework framework;
    private final ConcurrentHashMap<String, RegisteredPlugin> registeredPlugins;
    
    /**
     * Constructor for CoreRegistry
     * 
     * @param framework The CoreFramework plugin instance
     */
    public CoreRegistry(CoreFramework framework) {
        this.framework = framework;
        this.registeredPlugins = new ConcurrentHashMap<>();
    }
    
    /**
     * Register a plugin with the framework
     * 
     * @param plugin The plugin to register
     * @param version The plugin version
     * @param codename The version codename
     * @param hookRequirements List of hook requirements for the plugin
     * @return The registered plugin instance
     */
    public RegisteredPlugin registerPlugin(Plugin plugin, String version, String codename, List<HookRequirement> hookRequirements) {
        RegisteredPlugin registered = new RegisteredPlugin(plugin, version, codename, hookRequirements);
        registeredPlugins.put(plugin.getName(), registered);
        
        // framework.getLogger().info("Registered plugin: " + plugin.getName() + " v" + version + " [" + codename + "]"); - STARTUP LOGGER
        
        return registered;
    }
    
    /**
     * Get all registered plugins
     * 
     * @return List of all registered plugins
     */
    public List<RegisteredPlugin> getRegisteredPlugins() {
        return new ArrayList<>(registeredPlugins.values());
    }
    
    /**
     * Get a registered plugin by name
     * 
     * @param name The name of the plugin to retrieve
     * @return The registered plugin, or null if not found
     */
    public RegisteredPlugin getRegisteredPlugin(String name) {
        return registeredPlugins.get(name);
    }
    
    /**
     * Check if a plugin is registered
     * 
     * @param name The name of the plugin to check
     * @return True if the plugin is registered, false otherwise
     */
    public boolean isRegistered(String name) {
        return registeredPlugins.containsKey(name);
    }
}