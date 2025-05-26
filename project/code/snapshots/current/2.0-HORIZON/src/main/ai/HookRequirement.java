package io.rhythmknights.coreframework.component.api.hook;

/**
 * Represents a hook requirement for a plugin
 */
public class HookRequirement {
    
    private final String pluginName;
    private final String minVersion;
    private final boolean required;
    
    /**
     * Create a new hook requirement
     * 
     * @param pluginName Name of the plugin to hook
     * @param minVersion Minimum version required (use "any" for any version)
     * @param required Whether this hook is required or optional
     */
    public HookRequirement(String pluginName, String minVersion, boolean required) {
        this.pluginName = pluginName;
        this.minVersion = minVersion;
        this.required = required;
    }
    
    /**
     * Get the plugin name
     * @return Plugin name
     */
    public String getPluginName() {
        return pluginName;
    }
    
    /**
     * Get the minimum version
     * @return Minimum version
     */
    public String getMinVersion() {
        return minVersion;
    }
    
    /**
     * Check if this hook is required
     * @return True if required
     */
    public boolean isRequired() {
        return required;
    }
    
    /**
     * Create a required hook
     * @param pluginName Plugin name
     * @param minVersion Minimum version
     * @return Hook requirement
     */
    public static HookRequirement required(String pluginName, String minVersion) {
        return new HookRequirement(pluginName, minVersion, true);
    }
    
    /**
     * Create an optional hook
     * @param pluginName Plugin name
     * @param minVersion Minimum version
     * @return Hook requirement
     */
    public static HookRequirement optional(String pluginName, String minVersion) {
        return new HookRequirement(pluginName, minVersion, false);
    }
}