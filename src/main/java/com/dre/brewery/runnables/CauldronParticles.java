package com.dre.brewery.runnables;

import com.dre.brewery.BCauldron;
import com.dre.brewery.config.BConfig;

public class CauldronParticles implements Runnable {
	@Override
	public void run() {
		if (!BConfig.enableCauldronParticles) return;
		if (BConfig.minimalParticles && BCauldron.particleRandom.nextFloat() > 0.5f) {
			return;
		}
		BCauldron.processCookEffects();
	}
}
