package com.dre.brewery.config.addons;

import java.util.List;

public enum ConfigKey {
	AUTO_SAVE("autosave", Integer.class, 3),
	DEBUG("debug", Boolean.class, false),
	PUKE_ITEM("pukeItem", List.class, List.of("SOUL_SAND")),
	HANGOVER_DAYS("hangoverDays", Integer.class, 0),
	ENABLE_KICK_ON_OVERDRINK("enableKickOnOverdrink", Boolean.class, false),
	ENABLE_HOME("enableHome", Boolean.class, false),
	ENABLE_LOGIN_DISALLOW("enableLoginDisallow", Boolean.class, false),
	ENABLE_PUKE("enablePuke", Boolean.class, false),
	PUKE_DESPAWN_TIME("pukeDespawntime", Integer.class, 60),
	STUMBLE_PERCENT("stumblePercent", Integer.class, 100),
	SHOW_STATUS_ON_DRINK("showStatusOnDrink", Boolean.class, false),
	HOME_TYPE("homeType", String.class, null),
	ENABLE_WAKE("enableWake", Boolean.class, false),
	CRAFT_SEALING_TABLE("craftSealingTable", Boolean.class, false),
	ENABLE_SEALING_TABLE("enableSealingTable", Boolean.class, false),
	PLUGIN_PREFIX("pluginPrefix", String.class, "&2[Brewery]&f "),
	COLOR_IN_BARRELS("colorInBarrels", Boolean.class, false),
	COLOR_IN_BREWER("colorInBrewer", Boolean.class, false),
	ALWAYS_SHOW_QUALITY("alwaysShowQuality", Boolean.class, false),
	ALWAYS_SHOW_ALC("alwaysShowAlc", Boolean.class, false),
	SHOW_BREWER("showBrewer", Boolean.class, false),
	ENABLE_ENCODE("enableEncode", Boolean.class, false),
	OPEN_LARGE_BARREL_EVERYWHERE("openLargeBarrelEverywhere", Boolean.class, false),
	ENABLE_CAULDRON_PARTICLES("enableCauldronParticles", Boolean.class, false),
	MINIMAL_PARTICLES("minimalParticles", Boolean.class, false),
	USE_OFFHAND_FOR_CAULDRON("useOffhandForCauldron", Boolean.class, false),
	LOAD_DATA_ASYNC("loadDataAsync", Boolean.class, true),
	BREW_HOPPER_DUMP("brewHopperDump", Boolean.class, false),
	AGING_YEAR_DURATION("agingYearDuration", Integer.class, 20),
	REQUIRE_KEYWORD_ON_SIGNS("requireKeywordOnSigns", Boolean.class, true);

	private final String key;
	private final Class<?> type;
	private final Object defaultValue;

	ConfigKey(String key, Class<?> type, Object defaultValue) {
		this.key = key;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public Class<?> getType() {
		return type;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}
