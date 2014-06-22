package com.clashwars.cwmarket.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import com.clashwars.cwmarket.CWMarket;

public class CWMarketPlugin extends JavaPlugin {
	private CWMarket cwm;

	public void onDisable() {
		cwm.onDisable();
	}

	public void onEnable() {
		cwm = new CWMarket(this);
		cwm.onEnable();
	}

	public CWMarket getInstance() {
		return cwm;
	}
}
