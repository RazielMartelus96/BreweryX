package com.dre.brewery.model.sealer.old;

import com.dre.brewery.Brew;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.model.sealer.BrewerySealer;
import com.dre.brewery.utility.MinecraftVersion;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

/**
 * The Sealing Inventory that is being checked for Brews and seals them after a second.
 * <p>Class doesn't load in mc 1.12 and lower (Can't find RecipeChoice, BlockData and NamespacedKey)
 */
public class BSealer implements BrewerySealer {
	public static final NamespacedKey TAG_KEY = new NamespacedKey(BreweryPlugin.getInstance(), "SealingTable");
	public static final NamespacedKey LEGACY_TAG_KEY = new NamespacedKey("brewery", "sealingtable"); // Do not capitalize
	public static boolean recipeRegistered = false;
	public static boolean inventoryHolderWorking = true;

	private final Inventory inventory;
	private final Player player;
	private final short[] slotTime = new short[9];
	private ItemStack[] contents = null;
	private MyScheduledTask task;

	//TODO @Jsinco, note how there is only one usage of this constructor now. Please refer to its usage for the next
	//	   note.
	public BSealer(Player player) {
		this.player = player;
		if (inventoryHolderWorking) {
			Inventory inv = BreweryPlugin.getInstance().getServer().createInventory(this, InventoryType.DISPENSER, BreweryPlugin.getInstance().languageReader.get("Etc_SealingTable"));
			// Inventory Holder (for DISPENSER, ...) is only passed in Paper, not in Spigot. Doing inventory.getHolder() will return null in spigot :/
			if (inv.getHolder() == this) {
				inventory = inv;
				return;
			} else {
				inventoryHolderWorking = false;
			}
		}
		inventory = BreweryPlugin.getInstance().getServer().createInventory(this, 9, BreweryPlugin.getInstance().languageReader.get("Etc_SealingTable"));
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inventory;
	}

	@Override
	public void clickInv() {
		contents = null;
		if (task == null) {
			task = BreweryPlugin.getScheduler().runTaskTimer(BreweryPlugin.getInstance(), this::itemChecking, 1, 1);
		}
	}

	@Override
	public void closeInv() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		contents = inventory.getContents();
		for (ItemStack item : contents) {
			if (item != null && item.getType() != Material.AIR) {
				player.getWorld().dropItemNaturally(player.getLocation(), item);
			}
		}
		contents = null;
		inventory.clear();
	}

	private void itemChecking() {
		if (contents == null) {
			contents = inventory.getContents();
			for (int i = 0; i < slotTime.length; i++) {
				if (contents[i] == null || contents[i].getType() != Material.POTION) {
					slotTime[i] = -1;
				} else if (slotTime[i] < 0) {
					slotTime[i] = 0;
				}
			}
		}
		boolean playerValid = player.isValid() && !player.isDead();
		for (int i = 0; i < slotTime.length; i++) {
			if (slotTime[i] > 20) {
				slotTime[i] = -1;
				Brew brew = Brew.get(contents[i]);
				if (brew != null && !brew.isStripped()) {
					brew.seal(contents[i]);
					if (playerValid && BreweryPlugin.getMCVersion().isOrLater(MinecraftVersion.V1_9)) {
						player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1, 1.5f + (float) (Math.random() * 0.2));
					}
				}
			} else if (slotTime[i] >= 0) {
				slotTime[i]++;
			}
		}
	}



}
