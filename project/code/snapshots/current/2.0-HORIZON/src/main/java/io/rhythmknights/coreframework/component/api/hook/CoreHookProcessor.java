package io.rhythmknights.coreframework.component.api.hook;

import io.rhythmknights.coreframework.component.api.plugin.RegisteredPlugin;

/**
 * Interface for plugin hook processors
 * Individual plugins should implement this to handle their specific hook logic
 */
public interface CoreHookProcessor {
    
    /**
     * Process hooks for the plugin
     * @param registeredPlugin The registered plugin information
     * @return True if all required hooks succeeded
     */
    boolean processHooks(RegisteredPlugin registeredPlugin);
    
    /**
     * Get the hook results for reporting
     * @return Hook processing results
     */
    HookResult getHookResults();
}