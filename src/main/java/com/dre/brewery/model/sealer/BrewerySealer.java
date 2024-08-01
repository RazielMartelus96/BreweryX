package com.dre.brewery.model.sealer;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.utility.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Iterator;

/**
 * Interface representing the general functionality of an object able to seal {@linkplain com.dre.brewery.Brew Brews}
 * within the plugin, for the purposes of no longer being effected by {@linkplain
 * com.dre.brewery.Barrel Brewery Barrels} and having a common NBT, allowing for easier use in shop plugins.
 */
public interface BrewerySealer extends InventoryHolder {
	/**
	 * The tag key within the PDC of the sealing table block.
	 */
	NamespacedKey TAG_KEY = new NamespacedKey(BreweryPlugin.getInstance(), "SealingTable");

	/**
	 * The legacy tag, from previous versions of the plugin, for the key within the PDC of the sealing table block.
	 */
	NamespacedKey LEGACY_TAG_KEY = new NamespacedKey("brewery", "sealingtable"); // Do not capitalize

	/**
	 * Click within the inventory of the sealer.
	 */
	void clickInv();

	/**
	 * Close the inventory of the sealer.
	 */
	void closeInv();

	/**
	 * Static method used to determine if a block is a Sealer.
	 * @param block The block to check.
	 * @return True if the block is an instance of a sealer, False otherwise.
	 */
	static boolean isBSealer(Block block) {
		if (BreweryPlugin.getMCVersion().isOrLater(MinecraftVersion.V1_14) && block.getType() == Material.SMOKER) {
			Container smoker = (Container) block.getState();
			if (smoker.getCustomName() != null) {
				if (smoker.getCustomName().equals("§e" + BreweryPlugin.getInstance().languageReader.get("Etc_SealingTable"))) {
					return true;
				} else {
					return smoker.getPersistentDataContainer().has(TAG_KEY, PersistentDataType.BYTE) || smoker.getPersistentDataContainer().has(LEGACY_TAG_KEY, PersistentDataType.BYTE);
				}
			}
		}
		return false;
	}

	//TODO unsure on this block param , seems a bit messy?
	/**
	 * Place a block and initialise it as a sealer.
	 * @param item The potential sealer block.
	 * @param block
	 */
	static void blockPlace(ItemStack item, Block block) {
		if (item.getType() == Material.SMOKER && item.hasItemMeta()) {
			ItemMeta itemMeta = item.getItemMeta();
			assert itemMeta != null;
			if ((itemMeta.hasDisplayName() && itemMeta.getDisplayName().equals("§e" + BreweryPlugin.getInstance().languageReader.get("Etc_SealingTable"))) ||
				itemMeta.getPersistentDataContainer().has(TAG_KEY, PersistentDataType.BYTE)) {
				Container smoker = (Container) block.getState();
				// Rotate the Block 180°, so it doesn't look like a Smoker
				Directional dir = (Directional) smoker.getBlockData();
				dir.setFacing(dir.getFacing().getOppositeFace());
				smoker.setBlockData(dir);
				smoker.getPersistentDataContainer().set(TAG_KEY, PersistentDataType.BYTE, (byte)1);
				smoker.update();
			}
		}
	}

	/**
	 * Registers a crafting recipe for the sealer. Allowing players to craft it via the crafting table in-game.
	 */
	static void registerRecipe() {
		ItemStack sealingTableItem = new ItemStack(Material.SMOKER);
		ItemMeta meta = BreweryPlugin.getInstance().getServer().getItemFactory().getItemMeta(Material.SMOKER);
		if (meta == null) return;
		meta.setDisplayName("§e" + BreweryPlugin.getInstance().languageReader.get("Etc_SealingTable"));
		meta.getPersistentDataContainer().set(TAG_KEY, PersistentDataType.BYTE, (byte)1);
		sealingTableItem.setItemMeta(meta);

		ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(BreweryPlugin.getInstance(), "SealingTable"), sealingTableItem);
		recipe.shape("bb ",
			"ww ",
			"ww ");
		recipe.setIngredient('b', Material.GLASS_BOTTLE);
		recipe.setIngredient('w', new RecipeChoice.MaterialChoice(Tag.PLANKS));

		BreweryPlugin.getInstance().getServer().addRecipe(recipe);
	}

	/**
	 * Unregisters a crafting recipe for the sealer.
	 */
	static void unregisterRecipe() {
		//P.p.getServer().removeRecipe(new NamespacedKey(P.p, "SealingTable"));    1.15 Method
		Iterator<Recipe> recipeIterator = BreweryPlugin.getInstance().getServer().recipeIterator();
		while (recipeIterator.hasNext()) {
			Recipe next = recipeIterator.next();
			if (next instanceof ShapedRecipe && (((ShapedRecipe) next).getKey().equals(TAG_KEY) || ((ShapedRecipe) next).getKey().equals(LEGACY_TAG_KEY))) {
				recipeIterator.remove();
				return;
			}
		}
	}


}
