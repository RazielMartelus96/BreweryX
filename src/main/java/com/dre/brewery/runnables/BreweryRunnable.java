package com.dre.brewery.runnables;

import com.dre.brewery.*;
import com.dre.brewery.config.BConfig;
import com.dre.brewery.config.addons.AddonType;
import com.dre.brewery.filedata.DataSave;
import com.dre.brewery.integration.barrel.BlocklockerBarrel;
import com.dre.brewery.utility.MinecraftVersion;
import com.dre.brewery.utility.logging.PluginLogger;

import java.util.Iterator;

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
		if (BreweryPlugin.getMCVersion().isOrLater(MinecraftVersion.V1_14)) MCBarrel.onUpdate();
		if (BConfig.getInstance().isAddonEnabled(AddonType.BLOCK_LOCKER)) BlocklockerBarrel.clearBarrelSign();
		long t4 = System.nanoTime();
		BPlayer.onUpdate();// updates players drunkenness

		long t5 = System.nanoTime();
		DataSave.autoSave();
		long t6 = System.nanoTime();

		PluginLogger.getInstance().debugLog("BreweryRunnable: " +
			"t1: " + (t2 - t1) / 1000000.0 + "ms" +
			" | t2: " + (t3 - t2) / 1000000.0 + "ms" +
			" | t3: " + (t4 - t3) / 1000000.0 + "ms" +
			" | t4: " + (t5 - t4) / 1000000.0 + "ms" +
			" | t5: " + (t6 - t5) / 1000000.0 + "ms");
	}

}
