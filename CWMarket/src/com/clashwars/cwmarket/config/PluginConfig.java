package com.clashwars.cwmarket.config;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

import com.clashwars.cwmarket.sql.SqlInfo;

public class PluginConfig extends Config {
	private YamlConfiguration cfgFile;
	private ConfigUtil cu;
	private Config cfg;
	private final File dir = new File("plugins/CWMarket/");
	private final File file = new File(dir + "/CWMarket.yml");

	public PluginConfig(Config cfg) {
		this.cfg = cfg;
	}

	public void init() {
		try {
			dir.mkdirs();
			file.createNewFile();
			cfgFile = new YamlConfiguration();
			cu = new ConfigUtil(cfgFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load() {
		try {
			cfgFile.load(file);

			// Sql
			String address = cu.getString("sql.address", "37.26.106.5");
			String port = cu.getString("sql.port", "3306");
			String username = cu.getString("sql.username", "clashwar_main");
			String password = cu.getString("sql.password", "pass");
			String database = cu.getString("sql.database", "clashwar_main");
			cfg.setSqlInfo(new SqlInfo(address, port, username, password, database));
			
			

			cfgFile.save(file);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			cfgFile.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
