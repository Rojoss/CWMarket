package com.clashwars.cwmarket.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return cwm.onCommand(sender, cmd, label, args);
	}
}
