package com.dre.brewery.filedata.config.addons;

/**
 * Enum representing the type of third party addon plugin and its current state (enabled or not) within the Plugin, as
 * per settings determined within the {@linkplain com.dre.brewery.filedata.config.BConfig Config File}.
 */
public enum AddonType {

	WORLD_GUARD("WorldGuard","useWorldGuard", true),
	LWC("LWC","useLWC", true),
	TOWNY("Towny","useTowny", true),
	GRIEF_PREVENTION("GriefPrevention","useGriefPrevention", true),
	LOG_BLOCK("LogBlock","useLogBlock", false),
	GAME_MODE_INVENTORIES(null,"useGMInventories", false),
	CITADEL("Citadel","useCitadel", false),
	BLOCK_LOCKER("BlockLocker","useBlockLocker", false),
	VIRTUAL_CHEST_PERMS(null,"useVirtualChestPerms", false);

	private String name;
	private String key;
	private boolean defaultState;
	AddonType(String name,String key, boolean defaultState) {
		this.name = name;
		this.key = key;
		this.defaultState = defaultState;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

	public boolean isDefaultState() {
		return defaultState;
	}


}
