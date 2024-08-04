package com.dre.brewery.utility.logging;

import com.dre.brewery.config.BConfig;
import com.dre.brewery.utility.BUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class PluginLogger {
	private static PluginLogger instance;

	private boolean isEnabled;
	public static PluginLogger getInstance() {
		if (instance == null) {
			instance = new PluginLogger();
		}
		return instance;
	}

	public void setEnabled(boolean enabled){
		this.isEnabled = enabled;
	}
	public void msg(CommandSender sender, String msg) {
		sender.sendMessage(color(BConfig.pluginPrefix + msg));
	}

	public void log(String msg) {
		Bukkit.getConsoleSender().sendMessage(color(BConfig.pluginPrefix + msg));
	}

	public void debugLog(String msg) {
		if (isEnabled) {
			this.msg(Bukkit.getConsoleSender(), "&2[Debug] &f" + msg);
		}
	}

	public void warningLog(String msg) {
		Bukkit.getConsoleSender().sendMessage(color(BConfig.pluginPrefix + "&eWARNING: " + msg));
	}

	public void errorLog(String msg) {
		Bukkit.getConsoleSender().sendMessage(color(BConfig.pluginPrefix + "&cERROR: " + msg));
		if (BConfig.reloader != null) {
			BConfig.reloader.sendMessage(color(BConfig.pluginPrefix + "&cERROR: " + msg));
		}
	}
	private String color(String msg) {
		return BUtil.color(msg);
	}
}
