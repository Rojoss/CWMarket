package com.clashwars.cwmarket.market;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.clashwars.cwmarket.CWMarket;
import com.clashwars.cwmarket.ItemMenu;
import com.clashwars.cwmarket.util.Utils;

public class BuySession {
	
	private CWMarket cwm;
	
	private ItemMenu buyMenu;
	
	private Player player;
	private ItemEntry itemEntry;
	
	private int amount;

	public BuySession(CWMarket cwm, Player player, ItemEntry itemEntry) {
		this.cwm = cwm;
		this.player = player;
		this.itemEntry = itemEntry;
		buyMenu = cwm.getMarketManager().getBuyMenu();
		updateAmount(itemEntry.getAmount());
	}
	
	public void start() {
		//Reserve item if it's not infinite update refill menu.
		if (!itemEntry.isInf()) {
			itemEntry.getItemCat().fillMenus();
		}
		
		//Show menu and add player to sessions
		buyMenu.show(player);
		cwm.buySessions.put(player.getUniqueId(), this);
		
		//Clone item and put it in the middle slot as preview
		ItemStack clone = itemEntry.getItemStack().clone();
		clone.setAmount(1);
		buyMenu.setSlot(clone, 22, player);
	}
	
	public void stop() {
		//Make item available again and refill menu.
		itemEntry.getItemCat().fillMenus();
		
		//remove player from session and show ItemCategory menu.
		cwm.buySessions.remove(player.getUniqueId());
		itemEntry.getItemCat().getMarketMenu(0).show(player);
	}
	
	public void buyItem() {
		//Calculate total price.
		float price = itemEntry.getPricePerItem() * amount;
		
		//Make sure player has money.
		if (cwm.getEconomy().getBalance(player.getName()) < price) {
			player.sendMessage(Utils.formatMsg("&cYou don't have enough money to buy this!"));
			return;
		}
		
		
		if (!itemEntry.isInf()) {
			//Get the amount of items that will be left after purchase.
			int amountDB = 0;
			try {
				Statement checkStatement = cwm.getSql().createStatement();
				ResultSet res = checkStatement.executeQuery("SELECT Amount FROM " + cwm.marketItemsTable + " WHERE Market='" + itemEntry.getMarket().getName() + "' AND ID=" + itemEntry.getID() + ";");
				res.next();
				amountDB = res.getInt("Amount");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			//Check if there are items left.
			int amountLeft = -1;
			if (amountDB > 0) {
				amountLeft = amountDB - amount;
			} else {
				player.sendMessage(Utils.formatMsg("&cThis item is sold out!"));
				stop();
				return;
			}
			if (amountLeft < 0) {
				player.sendMessage(Utils.formatMsg("&cThere are only " + amountDB + " items available."));
				stop();
				return;
			}
			
			if (amountLeft > 0) {
				//There are more items to be bought so update the amount.
				try {
					Statement statement = cwm.getSql().createStatement();
					//Try update amount and make sure it actually updated it if not stop session.
					if (statement.executeUpdate("UPDATE " + cwm.marketItemsTable + " SET Amount=" + amountLeft + " WHERE ID=" + itemEntry.getID() + ";") < 1) {
						player.sendMessage(Utils.formatMsg("&cThis item is no longer available."));
						stop();
						return;
					}
				} catch (SQLException e) {
					player.sendMessage(Utils.formatMsg("&cError connecting to the databse. Item can't be bought right now."));
					stop();
					e.printStackTrace();
					return;
				}
				
				//Update item in memory and refill menu.
				itemEntry.setAmount(amountLeft);
				itemEntry.getItemCat().fillMenus();
			} else {
				//There are no more items to be bought so remove the item.
				try {
					Statement statement = cwm.getSql().createStatement();
					//Try delete it and make sure it actually deleted it if not stop session.
					if (statement.executeUpdate("DELETE FROM " + cwm.marketItemsTable + " WHERE ID=" + itemEntry.getID() + ";") < 1) {
						player.sendMessage(Utils.formatMsg("&cThis item is no longer available."));
						stop();
						return;
					}
				} catch (SQLException e) {
					player.sendMessage(Utils.formatMsg("&cError connecting to the databse. Item can't be bought right now."));
					stop();
					e.printStackTrace();
					return;
				}
				
				//Remove item from memory and refill menu.
				itemEntry.getItemCat().items.remove(itemEntry.getID());
				itemEntry.getItemCat().fillMenus();
			}
		}
		
		//Take money and give to seller
		if (!itemEntry.isInf()) {
			cwm.getEconomy().withdrawPlayer(player.getName(), price);
			cwm.getServer().dispatchCommand(cwm.getServer().getConsoleSender(), "eco give " + cwm.getServer().getOfflinePlayer(itemEntry.getOwner()).getName() + " " + price);
		} else {
			cwm.getEconomy().withdrawPlayer(player.getName(), price);
		}
		
		//Give items to player and drop it if inv is full.
		ItemStack itemClone = itemEntry.getItemStack().clone();
		itemClone.setAmount(1);
		for (int i = 0; i < amount; i++) {
			//Add item to inventory and if it can't be added drop it.
			if (!player.getInventory().addItem(itemClone).isEmpty()) {
				player.getWorld().dropItem(player.getLocation(), itemClone);
			}
		}
		
		//Send message to buyer.
		player.sendMessage(Utils.formatMsg("&6Sucesfully bought " + amount + " item(s) for &e" + price + " coins&6!"));
		//Send message to seller if it's not infinite and he's online.
		if (!itemEntry.isInf()) {
			if (cwm.getServer().getPlayer(itemEntry.getOwner()) != null && cwm.getServer().getPlayer(itemEntry.getOwner()).isOnline()) {
				String itemName = itemEntry.getItemStack().getType().toString().toLowerCase().replace("_", " ");
				cwm.getServer().getPlayer(itemEntry.getOwner()).sendMessage(Utils.formatMsg("&5" + amount + " " + itemName + " &6was bought from you by &5" + player.getName() + " &6for &e" + price + " coins&6!"));
			}
		}
		
		//Remove session and close inventory.
		cwm.buySessions.remove(player.getUniqueId());
		player.closeInventory();
	}
	
	
	//Update price and amount
	public void updateAmount(int amount) {
		//Limit the amount minimal 1 and maximal the amount of items.
		if (amount <= 0) {
			amount = 1;
		}
		if (!itemEntry.isInf() && amount > itemEntry.getAmount()) {
			amount = itemEntry.getAmount();
		}
		this.amount = amount;
		
		//Update the coin item (price)
		ItemStack coinsItem = buyMenu.getItems()[8];
		ItemMeta coinsMeta = coinsItem.getItemMeta();
		coinsMeta.setDisplayName(Utils.integrateColor("&6&lPrice: &e&l" + (float)itemEntry.getPricePerItem() * amount + " coins"));
		
		List<String> lore = coinsMeta.getLore();
		lore.set(0, Utils.integrateColor("&6&lPrice per piece: &e&l" + itemEntry.getPricePerItem() + " coins"));
		coinsMeta.setLore(lore);
		
		coinsItem.setItemMeta(coinsMeta);
		buyMenu.setSlot(coinsItem, 8, player);
		
		//Update the sugar item (amount)
		ItemStack amountItem = buyMenu.getItems()[4];
		ItemMeta amountMeta = amountItem.getItemMeta();
		amountMeta.setDisplayName(Utils.integrateColor("&9&lAmount: &3&l" + amount));
		amountItem.setItemMeta(amountMeta);
		buyMenu.setSlot(amountItem, 4, player);
	}
	
	public int getAmount() {
		return amount;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public ItemEntry getItemEntry() {
		return itemEntry;
	}

	public void setItemEntry(ItemEntry itemEntry) {
		this.itemEntry = itemEntry;
	}
}
