package com.clashwars.cwmarket;

import java.sql.Connection;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;

import com.clashwars.cwmarket.bukkit.CWMarketPlugin;
import com.clashwars.cwmarket.bukkit.events.MainEvents;
import com.clashwars.cwmarket.config.Config;
import com.clashwars.cwmarket.config.PluginConfig;
import com.clashwars.cwmarket.sql.MySql;
import com.clashwars.cwmarket.sql.SqlInfo;

public class CWMarket {

	private CWMarketPlugin cwm;
	private final Logger log = Logger.getLogger("Minecraft");

	private MySql sql = null;
	Connection c = null;

	private Config cfg;
	private PluginConfig pluginConfig;
	private Trader trader;

	

	public CWMarket(CWMarketPlugin cwm) {
		this.cwm = cwm;
	}

	public void log(Object msg) {
		log.info("[CWMarket " + getPlugin().getDescription().getVersion() + "]: " + msg.toString());
	}

	public void onDisable() {
		pluginConfig.save();
		log("Disabled.");
	}

	public void onEnable() {
		cfg = new Config();
		pluginConfig = new PluginConfig(cfg);
		pluginConfig.init();
		pluginConfig.load();
		
		SqlInfo sqli = cfg.getSqlInfo();
		sql = new MySql(this, sqli.getAddress(), sqli.getPort(), sqli.getDb(), sqli.getUser(), sqli.getPass());
		c = sql.openConnection();

		PluginManager pm = getPlugin().getServer().getPluginManager();
		registerEvents(pm);
		
		trader = new Trader(this);

		log("Enabled.");
	}

	private void registerEvents(PluginManager pm) {
		pm.registerEvents(new MainEvents(this), getPlugin());
	}

	public CWMarketPlugin getPlugin() {
		return cwm;
	}

	public Server getServer() {
		return getPlugin().getServer();
	}
	
	public Config getConfig() {
		return cfg;
	}
	
	public Trader getTrader() {
		return trader;
	}
}
