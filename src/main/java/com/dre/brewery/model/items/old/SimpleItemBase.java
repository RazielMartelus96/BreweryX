package com.dre.brewery.model.items.old;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.recipe.Ingredient;
import com.dre.brewery.recipe.ItemLoader;
import com.dre.brewery.utility.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Simple Minecraft Item with just Material
 */
public class SimpleItemBase extends BaseRecipeItem implements Ingredient {

	private static final MinecraftVersion VERSION = BreweryPlugin.getMCVersion();

	private Material mat;
	private short dur; // Old Mc


	public SimpleItemBase(Material mat) {
		this(mat, (short) 0);
	}

	public SimpleItemBase(Material mat, short dur) {
		this.mat = mat;
		this.dur = dur;
	}

	@Override
	public boolean hasMaterials() {
		return mat != null;
	}

	public Material getMaterial() {
		return mat;
	}

	@Override
	public List<Material> getMaterials() {
		List<Material> l = new ArrayList<>(1);
		l.add(mat);
		return l;
	}

	@NotNull
	@Override
	public Ingredient toIngredient(ItemStack forItem) {
		return ((SimpleItemBase) getMutableCopy());
	}

	@NotNull
	@Override
	public Ingredient toIngredientGeneric() {
		return ((SimpleItemBase) getMutableCopy());
	}

	@Override
	public boolean matches(ItemStack item) {
		if (!mat.equals(item.getType())) {
			return false;
		}
		//noinspection deprecation
		return VERSION.isOrLater(MinecraftVersion.V1_13) || dur == item.getDurability();
	}

	@Override
	public boolean matches(Ingredient ingredient) {
		if (isSimilar(ingredient)) {
			return true;
		}
		if (ingredient instanceof BaseRecipeItem) {
			if (!((BaseRecipeItem) ingredient).hasMaterials()) {
				return false;
			}
			if (ingredient instanceof CustomItemBase) {
				// Only match if the Custom Item also only defines material
				// If the custom item has more info like name and lore, it is not supposed to match a simple item
				CustomItemBase ci = (CustomItemBase) ingredient;
				return !ci.hasLore() && !ci.hasName() && mat == ci.getMaterial();
			}
		}
		return false;
	}

	@Override
	public boolean isSimilar(Ingredient item) {
		if (this == item) {
			return true;
		}
		if (item instanceof SimpleItemBase) {
			SimpleItemBase si = ((SimpleItemBase) item);
			return si.mat == mat && si.dur == dur;
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		SimpleItemBase item = (SimpleItemBase) o;
		return dur == item.dur &&
			mat == item.mat;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), mat, dur);
	}

	@Override
	public String toString() {
		return "SimpleItem{" +
			"mat=" + mat.name().toLowerCase() +
			" amount=" + getAmount() +
			'}';
	}

	@Override
	public void saveTo(DataOutputStream out) throws IOException {
		out.writeUTF("SI");
		out.writeUTF(mat.name());
		out.writeShort(dur);
	}

	public static SimpleItemBase loadFrom(ItemLoader loader) {
		try {
			DataInputStream in = loader.getInputStream();
			Material mat = Material.getMaterial(in.readUTF());
			short dur = in.readShort();
			if (mat != null) {
				SimpleItemBase item = new SimpleItemBase(mat, dur);
				return item;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Needs to be called at Server start
	public static void registerItemLoader(BreweryPlugin breweryPlugin) {
		breweryPlugin.registerForItemLoader("SI", SimpleItemBase::loadFrom);
	}

}

