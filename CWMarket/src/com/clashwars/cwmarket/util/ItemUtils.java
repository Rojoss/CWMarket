package com.clashwars.cwmarket.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.craftbukkit.libs.com.google.gson.reflect.TypeToken;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {
	
	public static ItemStack getItem(int ID, int amount, short durability, String name, String[] alore){
	    @SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(ID, amount);
	    return getItem(item, durability, name, alore);
	}
	
	public static ItemStack getItem(Material mat, int amount, short durability, String name, String[] alore){
	    ItemStack item = new ItemStack(mat, amount);
	    return getItem(item, durability, name, alore);
	}
	
	public static ItemStack getItem(ItemStack item, short durability, String name, String[] alore) {
		if (alore != null) {
		    for (int i = 0; i < alore.length; i++) {
		    	alore[i] = Utils.integrateColor(alore[i]);
		    }
	    }
	    
	    ItemMeta meta = item.getItemMeta();
	    List<String> lore = new ArrayList<String>();
	    if (alore != null) {
	    	Collections.addAll(lore, alore);
	    }
	    meta.setLore(lore);
	    if (name != null) {
	    	meta.setDisplayName(Utils.integrateColor(name));
	    }
	    item.setItemMeta(meta);
	    
	    item.setDurability(durability);
	    return item;
	}

	public static ItemStack getSeperator() {
		return getItem(Material.STAINED_GLASS_PANE, 1, (short)15, "", null);
	}
	
	
	public static int getIDFromLore(ItemStack item) {
		if (!item.hasItemMeta()) {
			return -1;
		}
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasLore()) {
			return -1;
		}
		List<String> lore = meta.getLore();
		if (lore.isEmpty() || lore.size() < 1) {
			return -1;
		}
		try {
			return Integer.parseInt(Utils.stripAllColour(lore.get(0)));
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	
	
	
	
	
	public static String serializeItemStack(ItemStack itemStack) {
		Map<String, Object> serial = itemStack.serialize();
		
		
		//Check if item has meta
		if (serial.get("meta") != null && itemStack.getItemMeta() instanceof ConfigurationSerializable ) {
			ItemMeta itemMeta = itemStack.getItemMeta();
			Map<String, Object> meta = cloneMap(((ConfigurationSerializable) itemMeta).serialize());
			serial.put("meta", meta);
		}
		
		//Convert map to JSON string and return it.
		Gson gson = new Gson();
		return gson.toJson(serial);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static ItemStack deserializeItemStack(String itemString) {
		//Convert JSON string to item data map.
		Gson gson = new Gson();
	    Map<String, Object> itemData = gson.fromJson(itemString, new TypeToken<Map<String, Object>>() {}.getType());
	    
	    // Repair Gson thinking int == double
        if(itemData.get("amount") != null) {
            Double d = (Double) itemData.get("amount");
            Integer i = d.intValue();
            itemData.put("amount", i);
        }
	    
	    //Try create ItemStack.
	    ItemStack  item = ItemStack.deserialize(itemData);
	    if (item == null) {
            return null;
        }
	    
	    
		if (itemData.containsKey("meta")) {
			Map<String, Object> metaMap = (Map<String, Object>) itemData.get("meta");
			item.setItemMeta( (ItemMeta) ConfigurationSerialization.deserializeObject(metaMap, ConfigurationSerialization.getClassByAlias("ItemMeta")) );
			if (metaMap.containsKey("enchants")) {
				Map<String, Object> enchantsMap = (Map<String, Object>) metaMap.get("enchants");
				for (String key : enchantsMap.keySet()) {
					Double d = (Double) enchantsMap.get(key);
		            Integer i = d.intValue();
		            
					item.addUnsafeEnchantment(Enchantment.getByName(key), i);
				}
			}
			
			ItemMeta meta = item.getItemMeta();
			if (metaMap.containsKey("stored-enchants") && meta instanceof EnchantmentStorageMeta) {
				Map<String, Object> enchantsMap = (Map<String, Object>) metaMap.get("stored-enchants");
				for (String key : enchantsMap.keySet()) {
					Double d = (Double) enchantsMap.get(key);
		            Integer i = d.intValue();
		            
		            EnchantmentStorageMeta enchantsMeta = (EnchantmentStorageMeta) meta;
		            enchantsMeta.addStoredEnchant(Enchantment.getByName(key), i, true);
				}
			}
			item.setItemMeta(meta);
		}
		
		return item;
	}
	
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> cloneMap(Map<String, Object> map ) {
		Map<String, Object> newMap = new HashMap<String, Object>();
 
		if (!map.isEmpty()) {
			for (String x : map.keySet()) {
				Object value = map.get(x);
				if (value instanceof Map) {
					value = cloneMap((Map<String, Object>) value);
				}
				newMap.put(new String(x), value);
			}
		}
		return newMap;
	}
	
	
	
}
