package io.rhythmknights.coreframework.component.api.hook;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the results of hook processing
 */
public class HookResult {
    
    private final Map<String, Boolean> hookResults;
    private final Map<String, String> hookMessages;
    private boolean allRequiredSuccessful;
    
    /**
     * Create a new hook processing result
     */
    public HookResult() {
        this.hookResults = new HashMap<>();
        this.hookMessages = new HashMap<>();
        this.allRequiredSuccessful = true;
    }
    
    /**
     * Add a hook result
     * @param pluginName The plugin name
     * @param success Whether the hook succeeded
     * @param message Optional result message
     */
    public void addResult(String pluginName, boolean success, String message) {
        hookResults.put(pluginName, success);
        if (message != null) {
            hookMessages.put(pluginName, message);
        }
    }
    
    /**
     * Set whether all required hooks were successful
     * @param allRequiredSuccessful The status
     */
    public void setAllRequiredSuccessful(boolean allRequiredSuccessful) {
        this.allRequiredSuccessful = allRequiredSuccessful;
    }
    
    /**
     * Check if all required hooks were successful
     * @return True if all required hooks succeeded
     */
    public boolean areAllRequiredSuccessful() {
        return allRequiredSuccessful;
    }
    
    /**
     * Get the hook results map
     * @return Map of plugin names to success status
     */
    public Map<String, Boolean> getHookResults() {
        return new HashMap<>(hookResults);
    }
    
    /**
     * Get the hook messages map
     * @return Map of plugin names to messages
     */
    public Map<String, String> getHookMessages() {
        return new HashMap<>(hookMessages);
    }
}