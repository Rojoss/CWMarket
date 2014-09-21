package com.clashwars.cwmarket.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigUtil {
	private YamlConfiguration	cfg;

	public ConfigUtil(YamlConfiguration cfg) {
		this.cfg = cfg;
	}

	public YamlConfiguration getConfig() {
		return cfg;
	}

	public String getString(String path, String def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, def);
			return def;
		}
		return cfg.getString(path, def);
	}

	public int getInt(String path, int def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, def);
			return def;
		}
		return cfg.getInt(path, def);
	}

	public Integer getObjectiveInt(String path, Integer def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, def);
			return def;
		}
		return cfg.getInt(path);
	}

	public float getFloat(String path, float def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, def);
			return def;
		}
		return (float) cfg.getDouble(path, (double) def);
	}

	public Float getObjectiveFloat(String path, Float def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, def);
			return def;
		}
		return (float) cfg.getDouble(path);
	}

	public boolean getBoolean(String path, boolean def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, def);
			return def;
		}
		return cfg.getBoolean(path, def);
	}

	public List<String> getStringList(String path, String... def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, (List<String>) new ArrayList<String>(Arrays.asList(def)));
			return new ArrayList<String>(Arrays.asList(def));
		}
		return cfg.getStringList(path);
	}
}
