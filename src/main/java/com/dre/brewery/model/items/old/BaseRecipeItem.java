package com.dre.brewery.model.items.old;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.recipe.*;
import com.dre.brewery.utility.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Item that can be used in a Recipe.
 * <p>They are not necessarily only loaded from config
 * <p>They are immutable if used in a recipe. If one implements Ingredient,
 * it can be used as mutable copy directly in a
 * BIngredients. Otherwise it needs to be converted to an Ingredient
 */
public abstract class BaseRecipeItem implements RecipeItem{

	private static final MinecraftVersion VERSION = BreweryPlugin.getMCVersion();

	String cfgId;
	private int amount;
	boolean immutable = false;


	/**
	 * Does this RecipeItem match the given ItemStack?
	 * <p>Used to determine if the given item corresponds to this recipeitem
	 *
	 * @param item The ItemStack for comparison
	 * @return True if the given item matches this recipeItem
	 */
	public abstract boolean matches(ItemStack item);

	/**
	 * Does this Item match the given Ingredient?
	 * <p>A RecipeItem matches an Ingredient if all required info of the RecipeItem are fulfilled on the Ingredient
	 * <br>This does not imply that the same holds the other way round, as the ingredient item might have more info than needed
	 *
	 *
	 * @param ingredient The ingredient that needs to fulfill the requirements
	 * @return True if the ingredient matches the required info of this
	 */
	public abstract boolean matches(Ingredient ingredient);

	/**
	 * Get the Corresponding Ingredient Item. For Items implementing Ingredient, just getMutableCopy()
	 * <p>This is called when this recipe item is added to a BIngredients
	 *
	 * @param forItem The ItemStack that has previously matched this RecipeItem. Used if the resulting Ingredient needs more info from the ItemStack
	 * @return The IngredientItem corresponding to this RecipeItem
	 */
	@NotNull
	public abstract Ingredient toIngredient(ItemStack forItem);

	/**
	 * Gets a Generic Ingredient for this recipe item
	 */
	@NotNull
	public abstract Ingredient toIngredientGeneric();

	/**
	 * @return True if this recipeItem has one or more materials that could classify an item. if true, getMaterials() is NotNull
	 */
	public abstract boolean hasMaterials();

	/**
	 * @return List of one or more Materials this recipeItem uses.
	 */
	@Nullable
	public abstract List<Material> getMaterials();

	/**
	 * @return The Id this Item uses in the config in the custom-items section
	 */
	@Nullable
	public String getConfigId() {
		return cfgId;
	}

	/**
	 * @return The Amount of this Item in a Recipe
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Set the Amount of this Item in a Recipe.
	 * <p>The amount can not be set on an existing item in a recipe or existing custom item.
	 * <br>To change amount you need to use getMutableCopy() and change the amount on the copy
	 *
	 * @param amount The new amount
	 */
	public void setAmount(int amount) {
		if (immutable) throw new IllegalStateException("Setting amount only possible on mutable copy");
		this.amount = amount;
	}

	/**
	 * Makes this Item immutable, for example when loaded from config. Used so if this is added to BIngredients,
	 * it needs to be cloned before changing anything like amount
	 */
	public void makeImmutable() {
		immutable = true;
	}

	/**
	 * Gets a shallow clone of this RecipeItem whose fields like amount can be changed.
	 *
	 * @return A mutable copy of this
	 */
	public BaseRecipeItem getMutableCopy() {
		try {
			BaseRecipeItem i = (BaseRecipeItem) super.clone();
			i.immutable = false;
			return i;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BaseRecipeItem that)) return false;
		return amount == that.amount &&
			immutable == that.immutable &&
			Objects.equals(cfgId, that.cfgId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(cfgId, amount, immutable);
	}

	@Override
	public String toString() {
		return "RecipeItem{(" + getClass().getSimpleName() + ") ID: " + getConfigId() + " Materials: " + (hasMaterials() ? getMaterials().size() : 0) + " Amount: " + getAmount();
	}
}
