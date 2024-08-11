package com.dre.brewery.model.items.old;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.filedata.BConfig;
import com.dre.brewery.model.items.types.CustomItemBase;
import com.dre.brewery.model.items.types.CustomMatchAnyItemBase;
import com.dre.brewery.model.items.types.SimpleItemBase;
import com.dre.brewery.recipe.BCauldronRecipe;
import com.dre.brewery.recipe.Ingredient;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface RecipeItem extends Cloneable{

	boolean matches(ItemStack itemStack);

	boolean matches(Ingredient ingredient);
	Ingredient toIngredient(ItemStack item);
	Ingredient toIngredientGeneric();
	boolean hasMaterials();
	List<Material> getMaterials();
	String getConfigId();
	int getAmount();
	void setAmount(int amount);
	void makeImmutable();
	RecipeItem getMutableCopy();

	/**
	 * Tries to find a matching RecipeItem for this item. It checks custom items and if it has found a unique custom item
	 * it will return that. If there are multiple matching custom items, a new CustomItem with all item info is returned.
	 * <br>If there is no matching CustomItem, it will return a SimpleItem with the items type
	 *
	 * @param item The Item for which to find a matching RecipeItem
	 * @param acceptAll If true it will accept any item and return a SimpleItem even if not on the accepted list
	 *                  <br>If false it will return null if the item is not acceptable by the Cauldron
	 * @return The Matched CustomItem, new CustomItem with all item info or SimpleItem
	 */
	@Nullable
	@Contract("_, true -> !null")
	public static BaseRecipeItem getMatchingRecipeItem(ItemStack item, boolean acceptAll) {
		BaseRecipeItem rItem = null;
		boolean multiMatch = false;
		for (BaseRecipeItem ri : BCauldronRecipe.acceptedCustom) {
			// If we already have a multi match, only check if there is a PluginItem that matches more strictly
			if (!multiMatch || (ri instanceof PluginItemBase)) {
				if (ri.matches(item)) {
					// If we match a plugin item, that's a very strict match, so immediately return it
					if (ri instanceof PluginItemBase) {
						return ri;
					}
					if (rItem == null) {
						rItem = ri;
					} else {
						multiMatch = true;
					}
				}
			}
		}
		if (multiMatch) {
			// We have multiple Custom Items matching, so just store all item info
			return new CustomItemBase(item);
		}
		if (rItem == null && (acceptAll || BCauldronRecipe.acceptedSimple.contains(item.getType()))) {
			// No Custom item found
			if (BreweryPlugin.getMCVersion().isOrLater(MinecraftVersion.V1_13)) {
				return new SimpleItemBase(item.getType());
			} else {
				@SuppressWarnings("deprecation")
				short durability = item.getDurability();
				return new SimpleItemBase(item.getType(), durability);
			}
		}
		return rItem;
	}

	@Nullable
	public static BaseRecipeItem fromConfigCustom(ConfigurationSection cfg, String id) {
		BaseRecipeItem rItem;
		if (cfg.getBoolean(id + ".matchAny", false)) {
			rItem = new CustomMatchAnyItemBase();
		} else {
			rItem = new CustomItemBase();
		}

		rItem.cfgId = id;
		rItem.immutable = true;

		List<Material> materials;
		List<String> names;
		List<String> lore;
		List<Integer> customModelDatas;

		List<String> load = BUtil.loadCfgStringList(cfg, id + ".material");
		if (load != null && !load.isEmpty()) {
			if ((materials = loadMaterials(load)) == null) {
				return null;
			}
		} else {
			materials = new ArrayList<>(0);
		}

		load = BUtil.loadCfgStringList(cfg, id + ".name");
		if (load != null && !load.isEmpty()) {
			names = load.stream().map(l -> BreweryPlugin.getInstance().color(l)).collect(Collectors.toList());
			if (BreweryPlugin.getMCVersion().isOrLater(MinecraftVersion.V1_13)) {
				// In 1.13 trailing Color white is removed from display names
				names = names.stream().map(l -> l.startsWith("§f") ? l.substring(2) : l).collect(Collectors.toList());
			}
		} else {
			names = new ArrayList<>(0);
		}

		load = BUtil.loadCfgStringList(cfg, id + ".lore");
		if (load != null && !load.isEmpty()) {
			lore = load.stream().map(l -> BreweryPlugin.getInstance().color(l)).collect(Collectors.toList());
		} else {
			lore = new ArrayList<>(0);
		}


		load = BUtil.loadCfgStringList(cfg, id + ".customModelData");
		if (load != null && !load.isEmpty()) {
			customModelDatas = load.stream().map(it -> BreweryPlugin.getInstance().parseInt(it)).toList();
		} else {
			customModelDatas = new ArrayList<>(0);
		}

		if (materials.isEmpty() && names.isEmpty() && lore.isEmpty() && customModelDatas.isEmpty()) {
			BreweryPlugin.getInstance().errorLog("No Config Entries found for Custom Item");
			return null;
		}

		if (rItem instanceof CustomItemBase cItem) {
			if (!materials.isEmpty()) {
				cItem.setMat(materials.get(0));
			}
			if (!names.isEmpty()) {
				cItem.setName(names.get(0));
			}
			cItem.setLore(lore);
			if (!customModelDatas.isEmpty()) {
				cItem.setCustomModelData(customModelDatas.get(0));
			}
		} else {
			CustomMatchAnyItemBase maItem = (CustomMatchAnyItemBase) rItem;
			maItem.setMaterials(materials);
			maItem.setNames(names);
			maItem.setLore(lore);
			maItem.setCustomModelDatas(customModelDatas);
		}

		return rItem;
	}

	@Nullable
	static List<Material> loadMaterials(List<String> ingredientsList) {
		List<Material> materials = new ArrayList<>(ingredientsList.size());
		for (String item : ingredientsList) {
			String[] ingredParts = item.split("/");
			if (ingredParts.length == 2) {
				BreweryPlugin.getInstance().errorLog("Item Amount can not be specified for Custom Items: " + item);
				return null;
			}
			Material mat = BUtil.getMaterialSafely(ingredParts[0]);

			if (mat == null && BreweryPlugin.getMCVersion().isOrEarlier(MinecraftVersion.V1_14) && ingredParts[0].equalsIgnoreCase("cornflower")) {
				// Using this in default custom-items, but will error on < 1.14
				materials.add(Material.BEDROCK);
				continue;
			}

			if (mat == null && BConfig.hasVault) {
				try {
					net.milkbowl.vault.item.ItemInfo vaultItem = net.milkbowl.vault.item.Items.itemByString(ingredParts[0]);
					if (vaultItem != null) {
						mat = vaultItem.getType();
					}
				} catch (Exception e) {
					BreweryPlugin.getInstance().errorLog("Could not check vault for Item Name");
					e.printStackTrace();
				}
			}
			if (mat != null) {
				materials.add(mat);
			} else {
				BreweryPlugin.getInstance().errorLog("Unknown Material: " + ingredParts[0]);
				return null;
			}
		}
		return materials;
	}



}
