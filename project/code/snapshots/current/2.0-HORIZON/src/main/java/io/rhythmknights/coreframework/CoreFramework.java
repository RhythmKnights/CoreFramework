package io.rhythmknights.coreframework;

import io.rhythmknights.coreframework.component.api.FrameworkAPI;
import io.rhythmknights.coreframework.component.api.hook.HookRequirement;
import io.rhythmknights.coreframework.component.api.plugin.RegisteredPlugin;
import io.rhythmknights.coreframework.component.core.CoreRegistry;
import io.rhythmknights.coreframework.component.core.CoreBootstrap;
import io.rhythmknights.coreframework.component.core.OutputCache;
import io.rhythmknights.coreframework.component.utility.TextUtility;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * CoreFramework - Central management system for all RhythmKnights plugins
 * Handles plugin registration, hook management, and centralized messaging
 */
public class CoreFramework extends JavaPlugin implements Listener {
    
    private static CoreFramework instance;
    private static FrameworkAPI api;
    
    private YamlConfiguration internalConfig;
    private YamlConfiguration languageConfig;
    private CoreRegistry coreRegistry;
    private CoreBootstrap coreBootstrap;
    private OutputCache outputCache;
    
    // Delayed display system
    private boolean displayingStartup = false;
    private boolean delayedDisplayScheduled = false;
    
    /**
     * Default constructor for CoreFramework
     * Called by Bukkit when loading the plugin
     */
    public CoreFramework() {
        super();
        getLogger().info("CoreFramework constructor called");
    }
    
    @Override
    public void onLoad() {
        getLogger().info("CoreFramework onLoad() starting");
        instance = this;
        getLogger().info("CoreFramework instance set to: " + instance);
        
        // Load configurations
        loadInternalConfig();
        loadLanguageConfig();
        
        // Initialize output cache
        this.outputCache = new OutputCache();
        
        // Initialize core components (but NOT TextUtility yet)
        this.coreRegistry = new CoreRegistry(this);
        this.coreBootstrap = new CoreBootstrap(this, outputCache);
        
        // Initialize API
        api = new FrameworkAPI(this);
        getLogger().info("CoreFramework API initialized: " + api);
        
        getLogger().info("CoreFramework loaded - Ready for plugin registration");
        getLogger().info("CoreFramework onLoad() completed - instance: " + instance + ", api: " + api);
    }
    
    @Override
    public void onEnable() {
        //getLogger().info("CoreFramework onEnable() starting - instance: " + instance + ", api: " + api); - STARTUP LOGGER
        
        // NOW we can initialize TextUtility (during onEnable when event registration is allowed)
        TextUtility.initialize(this);
        
        // Register this class as an event listener
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Prepare the header information but don't display it yet
        coreBootstrap.prepareStartupHeaderOnly();
        
        // Schedule the delayed display check
        scheduleDelayedDisplay();
        
        // getLogger().info("CoreFramework enabled successfully - instance: " + instance + ", api: " + api); - STARTUP LOGGER
    }
    
    @Override
    public void onDisable() {
        getLogger().info("CoreFramework onDisable() called - instance: " + instance);
        
        // Display shutdown message
        if (coreBootstrap != null) {
            coreBootstrap.displayShutdownMessage();
        }
        
        // Cleanup
        TextUtility.close();
        
        // DON'T null the instance here - other plugins might still need it during shutdown
        // instance = null;
        // api = null;
        
        getLogger().info("CoreFramework disabled - instance: " + instance);
    }
    
    /**
     * Direct registration method for plugins to use when static access fails
     * This bypasses the need for static variable access across classloaders
     * 
     * @param plugin The plugin to register
     * @param version The plugin version
     * @param codename The version codename (e.g., "HORIZON")
     * @param hookRequirements List of hook requirements
     * @return The registered plugin instance, or null if registration failed
     */
    public RegisteredPlugin registerPluginDirect(JavaPlugin plugin, String version, String codename, List<HookRequirement> hookRequirements) {
        try {
            if (coreRegistry == null) {
                getLogger().severe("CoreRegistry is not initialized yet!");
                return null;
            }
            
            RegisteredPlugin registered = coreRegistry.registerPlugin(plugin, version, codename, hookRequirements);
            
            // Silent registration - just log quietly to server console (not our fancy display)
            // getLogger().info("Direct registration successful for: " + plugin.getName() + " v" + version + " [" + codename + "]"); - STARTUP LOGGER
            
            // Optionally trigger early display if all expected plugins are registered
            triggerDisplayIfReady();
            
            return registered;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register plugin " + plugin.getName() + " directly!", e);
            return null;
        }
    }
    
    /**
     * Listen for server fully loaded event to display plugin statuses
     * This ensures we display after all other plugins have finished their startup
     * 
     * @param event The server load event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(ServerLoadEvent event) {
        // Cancel any scheduled display task
        Bukkit.getScheduler().cancelTasks(this);
        
        // Display the plugin statuses now that server is fully loaded
        if (!displayingStartup) {
            displayAllPluginStatuses();
        }
    }
    
    /**
     * Schedule a delayed task to display all plugin hook statuses
     * This ensures all plugins have time to register before we display
     */
    private void scheduleDelayedDisplay() {
        if (delayedDisplayScheduled) return; // Prevent duplicate scheduling
        delayedDisplayScheduled = true;
        
        // Wait longer to ensure all plugins finish loading and their messages are done
        // This is a fallback in case ServerLoadEvent doesn't fire (older server versions)
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!displayingStartup) {
                displayAllPluginStatuses();
            }
        }, 100L); // 100 ticks = ~5 seconds delay as fallback
    }
    
    /**
     * Display all registered plugin hook statuses at once
     * This creates the uninterrupted display after all Bukkit messages
     */
    private void displayAllPluginStatuses() {
        if (displayingStartup) return; // Prevent duplicate calls
        displayingStartup = true;
        
        List<RegisteredPlugin> allRegistered = getCoreRegistry().getRegisteredPlugins();
        
        if (allRegistered.isEmpty()) {
            outputCache.addLine("No plugins registered with CoreFramework yet.");
            // Still add the closing separator
            String separator = getLanguageConfig().getString("startup.separator", "");
            outputCache.addLine(separator);
        } else {
            // Prepare each plugin's hook status
            for (RegisteredPlugin plugin : allRegistered) {
                coreBootstrap.preparePluginHookStatus(plugin);
            }
            
            // Collect successful and failed plugins
            List<String> successful = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            
            for (RegisteredPlugin plugin : allRegistered) {
                if (plugin.areAllRequiredHooksSuccessful()) {
                    successful.add(plugin.getName());
                } else {
                    failed.add(plugin.getName());
                }
            }
            
            // Prepare final activation summary
            coreBootstrap.prepareActivationSummary(successful, failed);
        }
        
        // Now that all output is prepared, display it at once
        outputCache.flush();
        
        getLogger().info("CoreFramework startup display completed.");
    }
    
    /**
     * Public method to trigger display if needed (called by API when plugins register)
     * This allows for dynamic triggering if all expected plugins have registered early
     */
    public void triggerDisplayIfReady() {
        // Optional: You could implement logic here to trigger early display
        // if all expected plugins from the config have already registered
        
        List<String> expectedPlugins = getInternalConfig().getStringList("detection.core_plugins");
        List<RegisteredPlugin> registered = getCoreRegistry().getRegisteredPlugins();
        
        // Check if all expected plugins are registered
        if (registered.size() >= expectedPlugins.size() && !displayingStartup) {
            // Cancel the scheduled task and display immediately
            Bukkit.getScheduler().cancelTasks(this);
            displayAllPluginStatuses();
        }
    }
    
    /**
     * Load the internal framework configuration
     */
    private void loadInternalConfig() {
        try {
            internalConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(getResource("framework.yml"))
            );
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load internal framework configuration!", e);
            throw new RuntimeException("Critical configuration error", e);
        }
    }
    
    /**
     * Load the language configuration
     */
    private void loadLanguageConfig() {
        try {
            languageConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(getResource("lang/global.yml"))
            );
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load language configuration!", e);
            throw new RuntimeException("Critical language configuration error", e);
        }
    }
    
    /**
     * Get the CoreFramework instance
     * @return The plugin instance
     */
    public static CoreFramework getInstance() {
        if (instance == null) {
            // Try to get from Bukkit plugin manager as fallback
            JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("CoreFramework");
            if (plugin instanceof CoreFramework) {
                instance = (CoreFramework) plugin;
                System.out.println("CoreFramework: Retrieved instance from Bukkit plugin manager: " + instance);
            } else {
                System.out.println("CoreFramework: Could not retrieve instance from Bukkit plugin manager");
            }
        }
        return instance;
    }
    
    /**
     * Get the Framework API
     * @return The API instance
     */
    public static FrameworkAPI getAPI() {
        if (api == null && getInstance() != null) {
            // Try to recreate API if instance exists but API is null
            api = new FrameworkAPI(getInstance());
            System.out.println("CoreFramework: Recreated API instance: " + api);
        }
        return api;
    }
    
    /**
     * Get the internal configuration
     * @return The internal configuration
     */
    public YamlConfiguration getInternalConfig() {
        return internalConfig;
    }
    
    /**
     * Get the language configuration
     * @return The language configuration
     */
    public YamlConfiguration getLanguageConfig() {
        return languageConfig;
    }
    
    /**
     * Get the core registry
     * @return The core registry
     */
    public CoreRegistry getCoreRegistry() {
        return coreRegistry;
    }
    
    /**
     * Get the startup controller
     * @return The startup controller
     */
    public CoreBootstrap getCoreBootstrap() {
        return coreBootstrap;
    }
    
    /**
     * Get the output cache
     * @return The output cache
     */
    public OutputCache getOutputCache() {
        return outputCache;
    }
}