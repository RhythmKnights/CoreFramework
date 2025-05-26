package io.rhythmknights.coreframework.component.core;

import io.rhythmknights.coreframework.component.utility.TextUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Caches console output to be displayed all at once
 * This prevents other plugins from interrupting the formatted output display
 */
public class OutputCache {
    
    private final List<String> cachedLines;
    
    /**
     * Create a new output cache
     */
    public OutputCache() {
        this.cachedLines = new ArrayList<>();
    }
    
    /**
     * Add a line to the cache
     * 
     * @param line The line to add
     */
    public void addLine(String line) {
        cachedLines.add(line);
    }
    
    /**
     * Add multiple lines to the cache
     * 
     * @param lines The lines to add
     */
    public void addLines(List<String> lines) {
        cachedLines.addAll(lines);
    }
    
    /**
     * Clear the cache without displaying anything
     */
    public void clear() {
        cachedLines.clear();
    }
    
    /**
     * Get the current size of the cache
     * 
     * @return The number of cached lines
     */
    public int size() {
        return cachedLines.size();
    }
    
    /**
     * Check if the cache is empty
     * 
     * @return True if the cache is empty
     */
    public boolean isEmpty() {
        return cachedLines.isEmpty();
    }
    
    /**
     * Display all cached lines and clear the cache
     * This is the key method that outputs everything at once
     */
    public void flush() {
        // Create a visual separator before our output block to make it stand out
        System.out.println();
        
        // Display all cached lines
        for (String line : cachedLines) {
            TextUtility.sendConsoleMessage(line);
        }
        
        // Create a visual separator after our output block
        System.out.println();
        
        // Clear the cache
        clear();
    }
}