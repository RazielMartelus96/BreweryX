package com.dre.brewery.model.sealer;

import com.dre.brewery.model.Factory;
import com.dre.brewery.model.sealer.old.BSealer;
import org.bukkit.entity.Player;

/**
 * Factory for creating {@link BrewerySealer} instances. Is great for ensuring an easy way to swap from one
 * implementation of the Brewery Sealer with another.
 */
public class SealerFactory implements Factory<BrewerySealer> {
	/**
	 * The player associated with this Brewery Sealer.
	 */
	private Player player;

	/**
	 * Constructor which initialises the player for which the sealer will be created.
	 * @param player The player requiring a sealer.
	 */
	public SealerFactory(Player player){
		this.player = player;
	}

	//TODO @Jsinco : Note how now, as all implementations of BSealer have been replaced with the Brewery Sealer
	//	   interface, the only thing you need to do now to completely change the implentation of sealer logic is
	//     create a new implementation of Brewery Sealer (such as the unpopulated BrewerySealerNewImplTest) and then
	//     replace it within this "create" method and , assuming the new implementation works, will not require editing
	//     of any other code in the plugin :)
	@Override
	public BrewerySealer create() {
		return new BSealer(player);
	}
}
