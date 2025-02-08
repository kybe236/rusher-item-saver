package org.kybe;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;


public class ItemSaver extends Plugin {

	@Override
	public void onLoad() {
		final ItemSaverModule itemSaverModule = new ItemSaverModule();
		RusherHackAPI.getModuleManager().registerFeature(itemSaverModule);
	}

	@Override
	public void onUnload() {

	}

}