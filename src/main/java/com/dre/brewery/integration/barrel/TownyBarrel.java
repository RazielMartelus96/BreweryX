package com.dre.brewery.integration.barrel;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.api.events.barrel.BarrelAccessEvent;

import com.dre.brewery.utility.MinecraftVersion;
import org.bukkit.Location;
import org.bukkit.Material;

public class TownyBarrel {
	//TODO fix this mess
	public static boolean checkAccess(BarrelAccessEvent event) {
		return false;
	}

		/*
		Location barrelLoc = event.getSpigot().getLocation();
		Material mat = BreweryPlugin.getMCVersion().isOrLater(MinecraftVersion.V1_14) ? Material.BARREL : Material.CHEST;

		if (!TownySettings.isSwitchMaterial(mat, barrelLoc)) {
			return true;
		}
		return PlayerCacheUtil.getCachePermission(event.getPlayer(), barrelLoc, mat, TownyPermission.ActionType.SWITCH);
		*/
}

