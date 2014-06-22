package com.clashwars.cwmarket.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {
	
	public static ItemStack getItem(Material mat, int amount, short durability, String name, String[] alore){
	    ItemStack item = new ItemStack(mat, amount);
	    
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
	
}
