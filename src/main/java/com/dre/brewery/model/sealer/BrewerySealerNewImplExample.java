package com.dre.brewery.model.sealer;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
//TODO @Jsinco: Note , as mentioned in the sealer factory TODO, the only thing required now to completely change the
//     BSealer code is fill in the implementation of the methods of this new class, and replace it within the SealerFactory
//     create method. :)
public class BrewerySealerNewImplExample implements BrewerySealer{
	@Override
	public void clickInv() {

	}

	@Override
	public void closeInv() {

	}

	@NotNull
	@Override
	public Inventory getInventory() {
		return null;
	}
}
