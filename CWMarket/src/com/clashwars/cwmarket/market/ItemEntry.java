package com.clashwars.cwmarket.market;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class ItemEntry extends ItemStack {
	
	
	private ItemStack itemStack;
	private Market market;
	private ItemCategory itemCat;
	private int id;
	private UUID owner;
	private float pricePerItem;
	private int amount;
	private String inf;
	private boolean reserved;

	public ItemEntry(ItemStack itemStack, Market market, ItemCategory itemCat, int id, UUID owner, float pricePerItem, int amount, String inf) {
		this.setItemStack(itemStack);
		this.setMarket(market);
		this.setItemCat(itemCat);
		this.setID(id);
		this.setOwner(owner);
		this.setPricePerItem(pricePerItem);
		this.setAmount(amount);
		this.setInf(inf);
		setReserved(false);
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	
	public Market getMarket() {
		return market;
	}

	public void setMarket(Market market) {
		this.market = market;
	}

	
	public ItemCategory getItemCat() {
		return itemCat;
	}

	public void setItemCat(ItemCategory itemCat) {
		this.itemCat = itemCat;
	}


	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	
	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	
	public float getPricePerItem() {
		return pricePerItem;
	}

	public void setPricePerItem(float pricePerItem) {
		this.pricePerItem = pricePerItem;
	}
	
	
	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	
	public boolean isInf() {
		if (inf.equalsIgnoreCase("true")) {
			return true;
		} else {
			return false;
		}
	}
	

	public void setInf(String inf) {
		this.inf = inf;
	}

	public boolean isReserved() {
		return reserved;
	}

	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}
	
}
