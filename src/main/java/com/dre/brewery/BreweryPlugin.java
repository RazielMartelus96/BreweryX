/**
 *
 *     Brewery Minecraft-Plugin for an alternate Brewing Process
 *     Copyright (C) 2021 Milan Albrecht
 *
 *     This file is part of Brewery.
 *
 *     Brewery is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Brewery is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Brewery.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.dre.brewery;

import com.dre.brewery.api.addons.AddonManager;
import com.dre.brewery.commands.CommandManager;
import com.dre.brewery.commands.CommandUtil;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.filedata.BData;
import com.dre.brewery.filedata.DataSave;
import com.dre.brewery.filedata.LanguageReader;
import com.dre.brewery.filedata.UpdateChecker;
import com.dre.brewery.integration.ChestShopListener;
import com.dre.brewery.integration.IntegrationListener;
import com.dre.brewery.integration.ShopKeepersListener;
import com.dre.brewery.integration.SlimefunListener;
import com.dre.brewery.integration.barrel.BlocklockerBarrel;
import com.dre.brewery.integration.barrel.LogBlockBarrel;
import com.dre.brewery.integration.papi.PlaceholderAPI;
import com.dre.brewery.listeners.BlockListener;
import com.dre.brewery.listeners.CauldronListener;
import com.dre.brewery.listeners.EntityListener;
import com.dre.brewery.listeners.InventoryListener;
import com.dre.brewery.listeners.PlayerListener;
import com.dre.brewery.listeners.WorldListener;
import com.dre.brewery.recipe.BCauldronRecipe;
import com.dre.brewery.recipe.BRecipe;
import com.dre.brewery.recipe.CustomItem;
import com.dre.brewery.recipe.Ingredient;
import com.dre.brewery.recipe.ItemLoader;
import com.dre.brewery.recipe.PluginItem;
import com.dre.brewery.recipe.SimpleItem;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.LegacyUtil;
import com.dre.brewery.utility.MinecraftVersion;
import com.dre.brewery.integration.bstats.Stats;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public class BreweryPlugin extends JavaPlugin {

	private static final int RESOURCE_ID = 114777;

	private static AddonManager addonManager;
	private static TaskScheduler scheduler;
	private static BreweryPlugin breweryPlugin;
	private static MinecraftVersion minecraftVersion;
	public static boolean debug;
	public static boolean useUUID;
	public static boolean useNBT;

	// Public Listeners
	public PlayerListener playerListener;

	// Registrations
	public Map<String, Function<ItemLoader, Ingredient>> ingredientLoaders = new HashMap<>();

	// Language
	public String language;
	public LanguageReader languageReader;

	// Metrics
	public Stats stats = new Stats();

	@Override // FIXME
	public void onLoad() {
		String path = BreweryPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String jarDir = new File(path).getParentFile().getAbsolutePath();

		File breweryFolder = new File(jarDir + File.separator + "Brewery");
		File breweryXFolder = new File(jarDir + File.separator + "BreweryX");
		if (breweryFolder.exists() && !breweryXFolder.exists()) {
			breweryFolder.renameTo(breweryXFolder);
		}
	}

	@Override
	public void onEnable() {
		breweryPlugin = this;
		scheduler = UniversalScheduler.getScheduler(this);

		// Version check
		minecraftVersion = MinecraftVersion.getIt();
		log("Minecraft Version: " + minecraftVersion.getVersion());
		if (minecraftVersion == MinecraftVersion.UNKNOWN) {
			warningLog("This version of Minecraft is not known to Brewery! Please be wary of bugs or other issues that may occur in this version.");
		}

		// Todo: find which version MC started using UUIDs
		String v = Bukkit.getBukkitVersion();
		useUUID = !v.matches("(^|.*[^.\\d])1\\.[0-6]([^\\d].*|$)") && !v.matches("(^|.*[^.\\d])1\\.7\\.[0-5]([^\\d].*|$)");

		// Load Addons
		addonManager = new AddonManager(this);
		addonManager.loadAddons();


		// MC 1.13 uses a different NBT API than the newer versions.
		// We decide here which to use, the new or the old or none at all
		if (LegacyUtil.initNbt()) {
			useNBT = true;
		}

		if (getMCVersion().isOrLater(MinecraftVersion.V1_14)) {
			// Campfires are weird
			// Initialize once now so it doesn't lag later when we check for campfires under Cauldrons
			getServer().createBlockData(Material.CAMPFIRE);
		}

		// load the Config
		try {
			FileConfiguration cfg = BConfig.loadConfigFile();
			if (cfg == null) {
				errorLog("Something went wrong when trying to load the config file! Please check your config.yml");
				return;
			}
			BConfig.readConfig(cfg);
		} catch (Exception e) {
			e.printStackTrace();
			errorLog("Something went wrong when trying to load the config file! Please check your config.yml");
			return;
		}

		// Register Item Loaders
		CustomItem.registerItemLoader(this);
		SimpleItem.registerItemLoader(this);
		PluginItem.registerItemLoader(this);

		// Read data files
		BData.readData();

		// Setup Metrics
		stats.setupBStats();


		getCommand("breweryx").setExecutor(new CommandManager());
		// Listeners
		playerListener = new PlayerListener();

		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		getServer().getPluginManager().registerEvents(new WorldListener(), this);
		getServer().getPluginManager().registerEvents(new IntegrationListener(), this);
		if (getMCVersion().isOrLater(MinecraftVersion.V1_9)) {
			getServer().getPluginManager().registerEvents(new CauldronListener(), this);
		}
		if (BConfig.hasChestShop && getMCVersion().isOrLater(MinecraftVersion.V1_13)) {
			getServer().getPluginManager().registerEvents(new ChestShopListener(), this);
		}
		if (BConfig.hasShopKeepers) {
			getServer().getPluginManager().registerEvents(new ShopKeepersListener(), this);
		}
		if (BConfig.hasSlimefun && getMCVersion().isOrLater(MinecraftVersion.V1_14)) {
			getServer().getPluginManager().registerEvents(new SlimefunListener(), this);
		}

		// Heartbeat
		BreweryPlugin.getScheduler().runTaskTimer(new BreweryRunnable(), 650, 1200);
		BreweryPlugin.getScheduler().runTaskTimer(new DrunkRunnable(), 120, 120);

		if (getMCVersion().isOrLater(MinecraftVersion.V1_9)) {
			BreweryPlugin.getScheduler().runTaskTimer(new CauldronParticles(), 1, 1);
		}


		if (BConfig.updateCheck) {
			new UpdateChecker(RESOURCE_ID).query(latestVersion -> {
				String currentVersion = getDescription().getVersion();

				if (UpdateChecker.parseVersion(latestVersion) > UpdateChecker.parseVersion(currentVersion)) {
					UpdateChecker.setUpdateAvailable(true);
					UpdateChecker.setLatestVersion(latestVersion);
					log(languageReader.get("Etc_UpdateAvailable", "v" + currentVersion, "v" + latestVersion));
				}
			});
		}

		// Register PlaceholderAPI Placeholders
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PlaceholderAPI().register();
		}

		log("Using scheduler: " + scheduler.getClass().getSimpleName());
		log(this.getDescription().getName() + " enabled!");
	}

	@Override
	public void onDisable() {
		addonManager.unloadAddons();

		// Disable listeners
		HandlerList.unregisterAll(this);

		// Stop schedulers
		BreweryPlugin.getScheduler().cancelTasks(this);

		if (breweryPlugin == null) {
			return;
		}

		// save Data to Disk
		DataSave.save(true);

		if (BConfig.sqlSync != null) {
			try {
				BConfig.sqlSync.closeConnection();
			} catch (SQLException ignored) {
			}
			BConfig.sqlSync = null;
		}

		// delete config data, in case this is a reload and to clear up some ram
		clearConfigData();

		this.log(this.getDescription().getName() + " disabled!");
	}

	public void reload(CommandSender sender) {
		if (sender != null && !sender.equals(getServer().getConsoleSender())) {
			BConfig.reloader = sender;
		}
		FileConfiguration cfg = BConfig.loadConfigFile();
		if (cfg == null) {
			// Could not read yml file, do not proceed, error was printed
			log("Something went wrong when trying to load the config file! Please check your config.yml");
			return;
		}

		// clear all existent config Data
		clearConfigData();

		// load the Config
		try {
			BConfig.readConfig(cfg);
		} catch (Exception e) {
			e.printStackTrace();
			log("Something went wrong when trying to load the config file! Please check your config.yml");
			return;
		}

		// Reload Cauldron Particle Recipes
		BCauldron.reload();

		// Clear Recipe completions
		CommandUtil.reloadTabCompleter();

		// Reload Recipes
		boolean successful = true;
		for (Brew brew : Brew.legacyPotions.values()) {
			if (!brew.reloadRecipe()) {
				successful = false;
			}
		}
		if (sender != null) {
			if (!successful) {
				msg(sender, breweryPlugin.languageReader.get("Error_Recipeload"));
			} else {
				breweryPlugin.msg(sender, breweryPlugin.languageReader.get("CMD_Reload"));
			}
		}
		BConfig.reloader = null;
	}

	public void clearConfigData() {
		BRecipe.getConfigRecipes().clear();
		BRecipe.numConfigRecipes = 0;
		BCauldronRecipe.acceptedMaterials.clear();
		BCauldronRecipe.acceptedCustom.clear();
		BCauldronRecipe.acceptedSimple.clear();
		BCauldronRecipe.getConfigRecipes().clear();
		BCauldronRecipe.numConfigRecipes = 0;
		BConfig.customItems.clear();
		BConfig.hasMMOItems = null;
		DistortChat.commands = null;
		BConfig.drainItems.clear();
		if (BConfig.useLB) {
			try {
				LogBlockBarrel.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * For loading ingredients from ItemMeta.
	 * <p>Register a Static function that takes an ItemLoader, containing a DataInputStream.
	 * <p>Using the Stream it constructs a corresponding Ingredient for the chosen SaveID
	 *
	 * @param saveID The SaveID should be a small identifier like "AB"
	 * @param loadFct The Static Function that loads the Item, i.e.
	 *                public static AItem loadFrom(ItemLoader loader)
	 */
	public void registerForItemLoader(String saveID, Function<ItemLoader, Ingredient> loadFct) {
		ingredientLoaders.put(saveID, loadFct);
	}

	/**
	 * Unregister the ItemLoader
	 *
	 * @param saveID the chosen SaveID
	 */
	public void unRegisterItemLoader(String saveID) {
		ingredientLoaders.remove(saveID);
	}

	public static BreweryPlugin getInstance() {
		return breweryPlugin;
	}

	public static TaskScheduler getScheduler() {
		return scheduler;
	}

	public static MinecraftVersion getMCVersion() {
		return minecraftVersion;
	}

	// Utility

	public void msg(CommandSender sender, String msg) {
		sender.sendMessage(color(BConfig.pluginPrefix + msg));
	}

	public void log(String msg) {
		Bukkit.getConsoleSender().sendMessage(color(BConfig.pluginPrefix + msg));
	}

	public void debugLog(String msg) {
		if (debug) {
			this.msg(Bukkit.getConsoleSender(), "&2[Debug] &f" + msg);
		}
	}

	public void warningLog(String msg) {
		Bukkit.getConsoleSender().sendMessage(color(BConfig.pluginPrefix + "&eWARNING: " + msg));
	}

	public void errorLog(String msg) {
		Bukkit.getConsoleSender().sendMessage(color(BConfig.pluginPrefix + "&cERROR: " + msg));
		if (BConfig.reloader != null) {
			BConfig.reloader.sendMessage(color(BConfig.pluginPrefix + "&cERROR: " + msg));
		}
	}

	public int parseInt(String string) {
		if (string == null) {
			return 0;
		}
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}

	public double parseDouble(String string) {
		if (string == null) {
			return 0;
		}
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}

	public float parseFloat(String string) {
		if (string == null) {
			return 0;
		}
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}


	public String color(String msg) {
		return BUtil.color(msg);
	}

	// Runnables

	public static class DrunkRunnable implements Runnable {
		@Override
		public void run() {
			if (!BPlayer.isEmpty()) {
				BPlayer.drunkenness();
			}
		}
	}

	public class BreweryRunnable implements Runnable {
		@Override
		public void run() {
			long t1 = System.nanoTime();
			BConfig.reloader = null;
            // runs every min to update cooking time
			Iterator<BCauldron> bCauldronsToRemove = BCauldron.bcauldrons.values().iterator();
			while (bCauldronsToRemove.hasNext()) {
				// runs every min to update cooking time
				BCauldron bCauldron = bCauldronsToRemove.next();
				BreweryPlugin.getScheduler().runTask(bCauldron.getBlock().getLocation(), () -> {
					if (!bCauldron.onUpdate()) {
						bCauldronsToRemove.remove();
					}
				});
			}
			long t2 = System.nanoTime();
			Barrel.onUpdate();// runs every min to check and update ageing time
			long t3 = System.nanoTime();
			if (getMCVersion().isOrLater(MinecraftVersion.V1_14)) MCBarrel.onUpdate();
			if (BConfig.useBlocklocker) BlocklockerBarrel.clearBarrelSign();
			long t4 = System.nanoTime();
			BPlayer.onUpdate();// updates players drunkenness

			long t5 = System.nanoTime();
			DataSave.autoSave();
			long t6 = System.nanoTime();

			debugLog("BreweryRunnable: " +
				"t1: " + (t2 - t1) / 1000000.0 + "ms" +
				" | t2: " + (t3 - t2) / 1000000.0 + "ms" +
				" | t3: " + (t4 - t3) / 1000000.0 + "ms" +
				" | t4: " + (t5 - t4) / 1000000.0 + "ms" +
				" | t5: " + (t6 - t5) / 1000000.0 + "ms" );
		}

	}

	public class CauldronParticles implements Runnable {
		@Override
		public void run() {
			if (!BConfig.enableCauldronParticles) return;
			if (BConfig.minimalParticles && BCauldron.particleRandom.nextFloat() > 0.5f) {
				return;
			}
			BCauldron.processCookEffects();
		}
	}

}
