package com.dre.brewery.config;

import com.dre.brewery.*;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.api.events.ConfigLoadEvent;
import com.dre.brewery.config.addons.ConfigKey;
import com.dre.brewery.filedata.ConfigUpdater;
import com.dre.brewery.filedata.LanguageReader;
import com.dre.brewery.config.addons.AddonType;
import com.dre.brewery.integration.barrel.BlocklockerBarrel;
import com.dre.brewery.integration.barrel.WGBarrel;
import com.dre.brewery.integration.barrel.WGBarrel5;
import com.dre.brewery.integration.barrel.WGBarrel6;
import com.dre.brewery.integration.barrel.WGBarrel7;
import com.dre.brewery.integration.item.BreweryPluginItem;
import com.dre.brewery.integration.item.MMOItemsPluginItem;
import com.dre.brewery.integration.item.SlimefunPluginItem;
import com.dre.brewery.model.sealer.BrewerySealer;
import com.dre.brewery.recipe.BCauldronRecipe;
import com.dre.brewery.recipe.BRecipe;
import com.dre.brewery.recipe.PluginItem;
import com.dre.brewery.recipe.RecipeItem;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.MinecraftVersion;
import com.dre.brewery.utility.SQLSync;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class BConfig {

	/**
	 * Instance of the ConfigHandler, as per the singleton design pattern.
	 */
	private static BConfig instance;

	/**
	 * Gets the singleton instance of the ConfigHandler.
	 * @return The ConfigHandler instance.
	 */
	public static BConfig getInstance() {
		if (instance == null) {
			instance = new BConfig();
		}
		return instance;
	}
	private final MinecraftVersion VERSION = BreweryPlugin.getMCVersion();

	public static final String configVersion = "3.1";
	public static boolean updateCheck;
	public static CommandSender reloader;
	private final Map<AddonType, Boolean> addonEnabledMap = new HashMap<>();
	private final Map<ConfigKey, Object> configValues = new HashMap<>();


	// Third Party Enabled
	public static WGBarrel wg;
	public static Boolean hasMMOItems = null; // MMOItems ; Null if not checked
	public static boolean hasChestShop;
	public static boolean hasShopKeepers;

	// Barrel
	public static boolean openEverywhere;
	public static boolean loadDataAsync;
	public static boolean virtualChestPerms;
	public static int agingYearDuration;
	public static boolean requireKeywordOnSigns;

	// Cauldron
	public static boolean useOffhandForCauldron;
	public static boolean enableCauldronParticles;
	public static boolean minimalParticles;

	//BPlayer
	public static Map<Material, Integer> drainItems = new HashMap<>();// DrainItem Material and Strength
	public static List<Material> pukeItem;
	public static boolean showStatusOnDrink;
	public static int pukeDespawntime;
	public static float stumbleModifier;
	public static int hangoverTime;
	public static boolean overdrinkKick;
	public static boolean enableHome;
	public static boolean enableLoginDisallow;
	public static boolean enablePuke;
	public static String homeType;
	public static boolean enableWake;

	//Brew
	public static boolean colorInBarrels; // color the Lore while in Barrels
	public static boolean colorInBrewer; // color the Lore while in Brewer
	public static boolean enableEncode;
	public static boolean alwaysShowQuality; // Always show quality stars
	public static boolean alwaysShowAlc; // Always show alc%
	public static boolean showBrewer;
	public static boolean brewHopperDump; // Allow Dumping of Brew liquid into Hoppers

	//Features
	public static boolean craftSealingTable; // Allow Crafting of Sealing Table
	public static boolean enableSealingTable; // Allow Usage of Sealing Table
	public static String pluginPrefix = "&2[Brewery]&f ";

	//Item
	public static List<RecipeItem> customItems = new ArrayList<>();

	//MySQL
	public static String sqlHost, sqlPort, sqlDB;
	public static SQLSync sqlSync;
	public static boolean sqlDrunkSync;

	public static boolean isSealerRegistered = false;

	public static BreweryPlugin breweryPlugin = BreweryPlugin.getInstance();

	private boolean checkConfigs() {
		File cfg = new File(breweryPlugin.getDataFolder(), "config.yml");
		if (!cfg.exists()) {
			breweryPlugin.log("§1§lNo config.yml found, creating default file! You may want to choose a config according to your language!");
			breweryPlugin.log("§1§lYou can find them in plugins/Brewery/configs/");
			breweryPlugin.log("§1§lJust copy the config for your language into the Brewery folder and /brew reload");
			InputStream defconf = breweryPlugin.getResource("config/" + (VERSION.isOrLater(MinecraftVersion.V1_13) ? "v13/" : "v12/") + "en/config.yml");
			if (defconf == null) {
				breweryPlugin.errorLog("default config file not found, your jarfile may be corrupt. Disabling Brewery!");
				return false;
			}
			try {
				BUtil.saveFile(defconf, breweryPlugin.getDataFolder(), "config.yml", false);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		if (!cfg.exists()) {
			breweryPlugin.errorLog("default config file could not be copied, your jarfile may be corrupt. Disabling Brewery!");
			return false;
		}

		copyDefaultConfigAndLangs(false);
		return true;
	}

	private void copyDefaultConfigAndLangs(boolean overwrite) {
		final File configs = new File(breweryPlugin.getDataFolder(), "configs");
		final File languages = new File(breweryPlugin.getDataFolder(), "languages");

		final List<String> configTypes =  new ArrayList<>(List.of("de", "en", "es", "fr", "it", "zh"));
		final List<String> langTypes = new ArrayList<>(List.of("de", "en", "es", "fr", "it", "ru", "tw", "zh"));
		if (VERSION.isOrEarlier(MinecraftVersion.V1_13)) { // not available for some versions according to original author, haven't looked. - Jsinco : 4/1
			configTypes.removeAll(List.of("es", "it", "zh"));
		}

		for (String l : configTypes) {
			try {
				BUtil.saveFile(breweryPlugin.getResource(
						"config/" + (VERSION.isOrLater(MinecraftVersion.V1_13) ? "v13/" : "v12/") + l + "/config.yml"), new File(configs, l), "config.yml", overwrite
				);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (String type : langTypes) {
			try {
				// Never overwrite languages, they get updated with their updater. - Original Author
				BUtil.saveFile(breweryPlugin.getResource("languages/" + type + ".yml"), languages, type + ".yml", false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}

	public FileConfiguration loadConfigFile() {
		File file = new File(BreweryPlugin.getInstance().getDataFolder(), "config.yml");
		if (!checkConfigs()) {
			return null;
		}

		try {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			if (cfg.contains("version") && cfg.contains("language")) {
				return cfg;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Failed to load
		if (breweryPlugin.languageReader != null) {
			BreweryPlugin.getInstance().errorLog(breweryPlugin.languageReader.get("Error_YmlRead"));
		} else {
			BreweryPlugin.getInstance().errorLog("Could not read file config.yml, please make sure the file is in valid yml format (correct spaces etc.)");
		}
		return null;
	}

	//TODO replace all uses of static values throughout the plugin.
	public boolean isAddonEnabled(AddonType type){
		return addonEnabledMap.get(type);
	}
	public void disableAddon(AddonType addonType){this.addonEnabledMap.put(addonType,false);}

	private void loadConfigValue(Configuration config, ConfigKey configKey) {
		String key = configKey.getKey();
		Class<?> type = configKey.getType();
		Object defaultValue = configKey.getDefaultValue();

		if (type == Integer.class) {
			configValues.put(configKey, config.getInt(key, (Integer) defaultValue));
		} else if (type == Boolean.class) {
			configValues.put(configKey, config.getBoolean(key, (Boolean) defaultValue));
		} else if (type == String.class) {
			configValues.put(configKey, config.getString(key, (String) defaultValue));
		} else if (type == List.class) {
			configValues.put(configKey, config.getStringList(key).isEmpty() ? defaultValue : config.getStringList(key));
		}
	}
	public void readConfig(FileConfiguration config) {
		// Set the Language
		breweryPlugin.language = config.getString("language", "en");

		// Load LanguageReader
		breweryPlugin.languageReader = new LanguageReader(new File(breweryPlugin.getDataFolder(), "languages/" + breweryPlugin.language + ".yml"), "languages/" + breweryPlugin.language + ".yml");

		// Has to config still got old materials
		boolean oldMat = config.getBoolean("oldMat", false);

		// Check if config is the newest version
		String version = config.getString("version", null);
		if (version != null) {
			if (!version.equals(configVersion) || (oldMat && VERSION.isOrLater(MinecraftVersion.V1_13))) {
				File file = new File(BreweryPlugin.getInstance().getDataFolder(), "config.yml");
				copyDefaultConfigAndLangs(true);
				new ConfigUpdater(file).update(version, oldMat, breweryPlugin.language, config);
				BreweryPlugin.getInstance().log("Config Updated to version: " + configVersion);
				config = YamlConfiguration.loadConfiguration(file);
			}
		}

		// If the Update Checker should be enabled
		updateCheck = config.getBoolean("updateCheck", false);

		PluginManager pluginManager = breweryPlugin.getServer().getPluginManager();

		initAddons(config, pluginManager);

		for (ConfigKey configKey : ConfigKey.values()) {
			loadConfigValue(config, configKey);
		}

		if (VERSION.isOrLater(MinecraftVersion.V1_14)) {
			MCBarrel.maxBrews = config.getInt("maxBrewsInMCBarrels", 6);
			MCBarrel.enableAging = config.getBoolean("ageInMCBarrels", true);
		}

		Brew.loadSeed(config, new File(BreweryPlugin.getInstance().getDataFolder(), "config.yml"));

		if (VERSION.isOrEarlier(MinecraftVersion.V1_13)) {
			// world.getBlockAt loads Chunks in 1.12 and lower. Can't load async
			loadDataAsync = false;
		}

		PluginItem.registerForConfig("brewery", BreweryPluginItem::new);
		PluginItem.registerForConfig("mmoitems", MMOItemsPluginItem::new);
		PluginItem.registerForConfig("slimefun", SlimefunPluginItem::new);
		PluginItem.registerForConfig("exoticgarden", SlimefunPluginItem::new);

		// Loading custom items
		ConfigurationSection configSection = config.getConfigurationSection("customItems");
		if (configSection != null) {
			for (String custId : configSection.getKeys(false)) {
				RecipeItem custom = RecipeItem.fromConfigCustom(configSection, custId);
				if (custom != null) {
					custom.makeImmutable();
					customItems.add(custom);
				} else {
					breweryPlugin.errorLog("Loading the Custom Item with id: '" + custId + "' failed!");
				}
			}
		}

		// loading recipes
		configSection = config.getConfigurationSection("recipes");
		if (configSection != null) {
			List<BRecipe> configRecipes = BRecipe.getConfigRecipes();
			for (String recipeId : configSection.getKeys(false)) {
				BRecipe recipe = BRecipe.fromConfig(configSection, recipeId);
				if (recipe != null && recipe.isValid()) {
					configRecipes.add(recipe);
				} else {
					breweryPlugin.errorLog("Loading the Recipe with id: '" + recipeId + "' failed!");
				}
			}
			BRecipe.numConfigRecipes = configRecipes.size();
		}

		// Loading Cauldron Recipes
		configSection = config.getConfigurationSection("cauldron");
		if (configSection != null) {
			List<BCauldronRecipe> configRecipes = BCauldronRecipe.getConfigRecipes();
			for (String id : configSection.getKeys(false)) {
				BCauldronRecipe recipe = BCauldronRecipe.fromConfig(configSection, id);
				if (recipe != null) {
					configRecipes.add(recipe);
				} else {
					breweryPlugin.errorLog("Loading the Cauldron-Recipe with id: '" + id + "' failed!");
				}
			}
			BCauldronRecipe.numConfigRecipes = configRecipes.size();
		}

		// Recalculating Cauldron-Accepted Items for non-config recipes
		for (BRecipe recipe : BRecipe.getAddedRecipes()) {
			recipe.updateAcceptedLists();
		}
		for (BCauldronRecipe recipe : BCauldronRecipe.getAddedRecipes()) {
			recipe.updateAcceptedLists();
		}

		// loading drainItems
		List<String> drainList = config.getStringList("drainItems");
		if (drainList != null) {
			for (String drainString : drainList) {
				String[] drainSplit = drainString.split("/");
				if (drainSplit.length > 1) {
					Material mat = BUtil.getMaterialSafely(drainSplit[0]);
					int strength = breweryPlugin.parseInt(drainSplit[1]);
					if (mat == null && isAddonEnabled(AddonType.VAULT) && strength > 0) {
						try {
							net.milkbowl.vault.item.ItemInfo vaultItem = net.milkbowl.vault.item.Items.itemByString(drainSplit[0]);
							if (vaultItem != null) {
								mat = vaultItem.getType();
							}
						} catch (Exception e) {
							BreweryPlugin.getInstance().errorLog("Could not check vault for Item Name");
							e.printStackTrace();
						}
					}
					if (mat != null && strength > 0) {
						drainItems.put(mat, strength);
					}
				}
			}
		}

		// Loading Words
		DistortChat.words = new ArrayList<>();
		DistortChat.ignoreText = new ArrayList<>();
		if (config.getBoolean("enableChatDistortion", false)) {
			for (Map<?, ?> map : config.getMapList("words")) {
				new DistortChat(map);
			}
			for (String bypass : config.getStringList("distortBypass")) {
				DistortChat.ignoreText.add(bypass.split(","));
			}
			DistortChat.commands = config.getStringList("distortCommands");
		}
		DistortChat.log = config.getBoolean("logRealChat", false);
		DistortChat.doSigns = config.getBoolean("distortSignText", false);

		// Register Sealing Table Recipe
		if (VERSION.isOrLater(MinecraftVersion.V1_14)) {
			if (craftSealingTable && !isSealerRegistered) {
				BrewerySealer.registerRecipe();
				isSealerRegistered = true;
			} else if (!craftSealingTable && isSealerRegistered) {
				BrewerySealer.unregisterRecipe();
				isSealerRegistered =false;
			}
		}

		//TODO jesus christ what even is this?!? The whole world guard impl needs fixing asap!
		if (isAddonEnabled(AddonType.WORLD_GUARD)) {
			Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
			if (plugin != null) {
				String wgv = plugin.getDescription().getVersion();
				if (wgv.startsWith("6.")) {
					wg = new WGBarrel6();
				} else if (wgv.startsWith("5.")) {
					wg = new WGBarrel5();
				} else {
					wg = new WGBarrel7();
				}
			}
			if (wg == null) {
				BreweryPlugin.getInstance().errorLog("Failed loading WorldGuard Integration! Opening Barrels will NOT work!");
				BreweryPlugin.getInstance().errorLog("Brewery was tested with version 5.8, 6.1 and 7.0 of WorldGuard!");
				BreweryPlugin.getInstance().errorLog("Disable the WorldGuard support in the config and do /brew reload");
			}
		}
		if (isAddonEnabled(AddonType.BLOCK_LOCKER)) {
			try {
				Class.forName("nl.rutgerkok.blocklocker.BlockLockerAPIv2");
				Class.forName("nl.rutgerkok.blocklocker.ProtectableBlocksSettings");
				BlocklockerBarrel.registerBarrelAsProtectable();
			} catch (ClassNotFoundException e) {
				addonEnabledMap.put(AddonType.BLOCK_LOCKER,false);
				BreweryPlugin.getInstance().log("Unsupported Version of 'BlockLocker', locking Brewery Barrels disabled");
			}
		}

		// init SQL
		if (sqlSync != null) {
			try {
				sqlSync.closeConnection();
			} catch (SQLException ignored) {
			}
			sqlSync = null;
		}
		sqlDrunkSync = false;

		ConfigurationSection sqlCfg = config.getConfigurationSection("multiServerDB");
		if (sqlCfg != null && sqlCfg.getBoolean("enabled")) {
			sqlDrunkSync = sqlCfg.getBoolean("syncDrunkeness");
			sqlHost = sqlCfg.getString("host", null);
			sqlPort = sqlCfg.getString("port", null);
			sqlDB = sqlCfg.getString("database", null);
			String sqlUser = sqlCfg.getString("user", null);
			String sqlPW = sqlCfg.getString("password", null);

			sqlSync = new SQLSync();
			if (!sqlSync.init(sqlUser, sqlPW)) {
				sqlSync = null;
			}
		}

		// The Config was reloaded, call Event
		ConfigLoadEvent event = new ConfigLoadEvent();
		BreweryPlugin.getInstance().getServer().getPluginManager().callEvent(event);


	}

	public Object getConfigValue(ConfigKey configKey) {
		return configValues.getOrDefault(configKey, configKey.getDefaultValue());
	}

	public <T> T getConfigValue(ConfigKey configKey, Class<T> type) {
		return type.cast(configValues.getOrDefault(configKey, configKey.getDefaultValue()));
	}

	private void initAddons(FileConfiguration config, PluginManager pluginManager){
		Arrays.stream(AddonType.values()).forEach(addon->{
			boolean isEnabled;
			boolean isUsed;
			if(addon.getName() != null){
				isEnabled = pluginManager.isPluginEnabled(addon.getName());
			}
			else{
				isEnabled = true;
			}
			if(addon.getKey() == null){
				isUsed = true;
			}
			else{isUsed = config
				.getBoolean(
					addon.getKey(),true);}

			this.addonEnabledMap.put(addon, isUsed && isEnabled);

		});
	}


}
