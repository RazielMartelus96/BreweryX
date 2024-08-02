package com.dre.brewery.model.effect;

import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Interface representing the general functionality of a potion effect that occurs due to the
 * consumption of a Brew.
 */
public interface BrewEffect {
	/**
	 * Generates the effect, based on the given quality of the brew.
	 * @param quality The quality of the brew.
	 * @return The effect to be caused due to the drinking of the brew.
	 */
	PotionEffect generateEffect(int quality);
	void apply(int quality, Player player);
	int calcDuration(float quality);
	int calcLvl(float quality);
	void writeInto(PotionMeta meta, int quality);
	boolean isValid();
	boolean isHidden();
	PotionEffectType getType();
}
