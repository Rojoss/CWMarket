package com.clashwars.cwmarket;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.clashwars.cwmarket.bukkit.CWMarketPlugin;
import com.clashwars.cwmarket.bukkit.events.MainEvents;
import com.clashwars.cwmarket.commands.Commands;
import com.clashwars.cwmarket.config.Config;
import com.clashwars.cwmarket.config.PluginConfig;
import com.clashwars.cwmarket.market.BuySession;
import com.clashwars.cwmarket.market.MarketManager;
import com.clashwars.cwmarket.market.SellSession;
import com.clashwars.cwmarket.sql.MySql;
import com.clashwars.cwmarket.sql.SqlInfo;

public class CWMarket {

	private CWMarketPlugin cwm;
	private final Logger log = Logger.getLogger("Minecraft");

	private Economy econ;
	
	private MySql sql = null;
	private Connection c = null;

	private Commands cmds;
	
	private Config cfg;
	private PluginConfig pluginConfig;
	
	private MarketManager mm;
	public HashMap<UUID, SellSession> sellSessions = new HashMap<UUID, SellSession>();
	public HashMap<UUID, BuySession> buySessions = new HashMap<UUID, BuySession>();
	public ArrayList<UUID> infinite = new ArrayList<UUID>();

	
	
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
		if (!setupEconomy() ) {
			log("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(cwm);
            return;
        }
		
		cfg = new Config();
		pluginConfig = new PluginConfig(cfg);
		pluginConfig.init();
		pluginConfig.load();
		
		SqlInfo sqli = cfg.getSqlInfo();
		sql = new MySql(this, sqli.getAddress(), sqli.getPort(), sqli.getDb(), sqli.getUser(), sqli.getPass());
		c = sql.openConnection();

		PluginManager pm = getPlugin().getServer().getPluginManager();
		registerEvents(pm);
		
		cmds = new Commands(this);
		cmds.populateCommands();
		
		mm = new MarketManager(this);
		mm.populate();
		mm.createMenus();

		log("Enabled.");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return cmds.executeCommand(sender, label, args);
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

	private void registerEvents(PluginManager pm) {
		pm.registerEvents(new MainEvents(this), getPlugin());
		pm.registerEvents(new ItemMenu.Events(), getPlugin());
	}
	

	public CWMarketPlugin getPlugin() {
		return cwm;
	}

	public Server getServer() {
		return getPlugin().getServer();
	}
	
	public Economy getEconomy() {
		return econ;
	}
	
	public Connection getSql() {
		return c;
	}
	
	public Config getConfig() {
		return cfg;
	}
	
	public PluginConfig getPluginConfig() {
		return pluginConfig;
	}
	
	public MarketManager getMarketManager() {
		return mm;
	}
}
