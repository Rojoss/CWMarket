package com.clashwars.cwmarket.market;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.clashwars.cwmarket.CWMarket;
import com.clashwars.cwmarket.ItemMenu;
import com.clashwars.cwmarket.util.Utils;

public class BankSession {
	
	private CWMarket cwm;
	
	private ItemMenu bankMenu;
	
	private Player player;
	
	private int value;
	
	private int blockValue = 810;
	private int ingotValue = 90;
	private int nuggetValue = 10;

	public BankSession(CWMarket cwm, Player player) {
		this.cwm = cwm;
		this.player = player;
		bankMenu = cwm.getMarketManager().getBankMenu();
	}
	
	public void start() {
		//Clear menu
		for (int s = 9; s < 18; s++) {
			bankMenu.setSlot(new ItemStack(Material.AIR), s, player);
		}
		
		bankMenu.show(player);
		
		setValue(0);
	}
	
	@SuppressWarnings("deprecation")
	public void stop() {
		for (int s = 9; s < 18; s++) {
			if (bankMenu.getItems()[s] == null || bankMenu.getItems()[s].getType() == Material.AIR) {
				continue;
			}
			player.getInventory().addItem(new ItemStack(bankMenu.getItems()[s].getType(), bankMenu.getItems()[s].getAmount()));
		}
		player.updateInventory();

		cwm.bankSessions.remove(player.getUniqueId());
		player.closeInventory();
	}
	
	public void depositGold() {
		cwm.getEconomy().depositPlayer(player.getName(), value);
		
		player.sendMessage(Utils.formatMsg("&6Gold deposited for &e" + value + " coins&6!"));

		for (int s = 9; s < 18; s++) {
			bankMenu.setSlot(new ItemStack(Material.AIR), s, player);
		}
		
		player.closeInventory();
		cwm.bankSessions.remove(player.getName());
	}
	
	
	@SuppressWarnings("deprecation")
	public boolean addGold(int slot, ClickType click) {
		ItemStack item = player.getInventory().getItem(slot);
		int amount = 0;
		if (click == ClickType.LEFT) {
			amount = 1;
		} else if (click == ClickType.SHIFT_LEFT) {
			amount = item.getAmount();
		}
		Material mat = item.getType();
		
		int amountLeft = amount;
		for (int s = 9; s < 18; s++) {
			//All items have been added.
			if (amountLeft <= 0) {
				break;
			}
			item = player.getInventory().getItem(slot);
			mat = item.getType();
			
			//Empty slot so just add the items at that slot.
			if (bankMenu.getItems()[s] == null || bankMenu.getItems()[s].getType() == Material.AIR) {
				bankMenu.setSlot(new ItemStack(mat, amountLeft), s, player);
				if (amountLeft == item.getAmount()) {
					player.getInventory().setItem(slot, new ItemStack(Material.AIR));
				} else {
					player.getInventory().setItem(slot, new ItemStack(mat, item.getAmount() - amountLeft));
				}
				amountLeft = 0;
				continue;
			}
			
			ItemStack bankItem = bankMenu.getItems()[s];
			if (bankItem.getType() == mat) {
				//Full itemstack so continue.
				if (bankItem.getAmount() >= 64) {
					continue;
				}
				
				//Not enough space left at current itemstack so add up to 64 and continue to add the rest at next available slot.
				if (bankItem.getAmount() + amountLeft > 64) {
					amountLeft -= 64 - bankItem.getAmount();
					player.getInventory().setItem(slot, new ItemStack(mat, item.getAmount() - (64 - bankItem.getAmount())));
					bankItem.setAmount(64);
					bankMenu.setSlot(bankItem, s, player);
					continue;
				//Enough space at current itemstack so just increase amount.
				} else {
					if (amountLeft == item.getAmount()) {
						player.getInventory().setItem(slot, new ItemStack(Material.AIR));
					} else {
						player.getInventory().setItem(slot, new ItemStack(mat, item.getAmount() - amountLeft));
					}
					bankItem.setAmount(bankItem.getAmount() + amountLeft);
					bankMenu.setSlot(bankItem, s, player);
					amountLeft = 0;
					continue;
				}
			}
		}
		//Update value.
		if (amountLeft < amount) {
			int deposited = amount - amountLeft;
			int itemValue = 0;
			if (mat == Material.GOLD_BLOCK) {
				itemValue = blockValue;
			}
			if (mat == Material.GOLD_INGOT) {
				itemValue = ingotValue;
			}
			if (mat == Material.GOLD_NUGGET) {
				itemValue = nuggetValue;
			}
			setValue(getValue() + deposited * itemValue);
		}
		
		player.updateInventory();
		
		//Send message when there is no space.
		if (amountLeft > 0) {
			player.sendMessage(Utils.formatMsg("&cNo more space to deposit this gold."));
			return false;
		}
		return true;
	}
	
	public void addAllGold() {
		for (int s = 0; s < 45; s++) {
			ItemStack item = player.getInventory().getItem(s);
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}
			if (item.getType() == Material.GOLD_BLOCK || item.getType() == Material.GOLD_INGOT || item.getType() == Material.GOLD_NUGGET) {
				if (addGold(s, ClickType.SHIFT_LEFT) == false) {
					return;
				}
			}
		}
	}
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
		
		//Update Price icon.
		ItemStack coinsItem = bankMenu.getItems()[4];
		ItemMeta coinsMeta = coinsItem.getItemMeta();
		coinsMeta.setDisplayName(Utils.integrateColor("&6&lValue: &e&l" + value + " coins"));
		coinsItem.setItemMeta(coinsMeta);
		
		bankMenu.setSlot(coinsItem, 4, player);
	}
}
