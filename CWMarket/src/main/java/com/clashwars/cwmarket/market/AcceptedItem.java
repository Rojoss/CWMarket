package com.clashwars.cwmarket.market;

import org.bukkit.Material;

public class AcceptedItem {
	
	private Material material;
	private short durability;
	private String enchant;

	public AcceptedItem(Material material, short durability, String enchant) {
		this.setMaterial(material);
		this.setDurability(durability);
		this.setEnchant(enchant);
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public short getDurability() {
		return durability;
	}

	public void setDurability(short durability) {
		this.durability = durability;
	}

	public String getEnchant() {
		return enchant;
	}

	public void setEnchant(String enchant) {
		this.enchant = enchant;
	}
}
