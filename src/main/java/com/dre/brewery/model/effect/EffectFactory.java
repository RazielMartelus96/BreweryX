package com.dre.brewery.model.effect;

import com.dre.brewery.model.Factory;
import com.dre.brewery.recipe.BEffect;

public class EffectFactory implements Factory<BrewEffect> {
	private String effectString;
	public EffectFactory(String effectString) {
		this.effectString = effectString;
	}
	@Override
	public BrewEffect create() {
		return new BEffect(this.effectString);
	}
}
