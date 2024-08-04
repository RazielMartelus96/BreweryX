package com.dre.brewery.runnables;

import com.dre.brewery.BPlayer;

public class DrunkRunnable implements Runnable {
	@Override
	public void run() {
		if (!BPlayer.isEmpty()) {
			BPlayer.drunkenness();
		}
	}
}
