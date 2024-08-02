package com.dre.brewery.model.effect;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Interface representing the general functionality of a potion effect that occurs due to the
 * consumption of a Brew.
 */
public interface BrewEffect {
	/**
	 * Return the effect, based on the given quality of the brew.
	 * @param quality The quality of the brew.
	 * @return The effect to be caused due to the drinking of the brew.
	 */
	PotionEffect generateEffect(int quality);

	/**
	 * Calculates the duration of a Brew Effect.
	 * @param quality The quality of the Brew.
	 * @return The duration of the Effect.
	 */
	int calcDuration(float quality);

	/**
	 * Calculate the level of the effect.
	 * @param quality The quality of the Brew.
	 * @return The effect level.
	 */
	int calcLvl(float quality);

	/**
	 * Writes the effect into the lore of a brew.
	 * @param meta Potion Metadata for the brew to add the lore to.
	 * @param quality The quality of the brew.
	 */
	void writeInto(PotionMeta meta, int quality);

	/**
	 * Checks if the Effect has all required params to be a valid effect within the
	 * plugin. Used to help ensure invalid config effects are added.
	 * @return True if the Effect is Valid, False otherwise.
	 */
	boolean isValid();

	/**
	 * Checks to see if the Effect has been set to be hidden from the Lore of the
	 * Brew.
	 * @return True if the Effect is hidden from the Lore, False otherwise.
	 */
	boolean isHidden();

	/**
	 * Get the type of Potion Effect Type of the Effect.
	 * @return The Effect's type.
	 */
	PotionEffectType getType();
}
