package com.clashwars.cwmarket.market;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.clashwars.cwmarket.CWMarket;
import com.clashwars.cwmarket.ItemMenu;
import com.clashwars.cwmarket.util.ItemUtils;
import com.clashwars.cwmarket.util.Utils;

public class EditSession {
	
	private CWMarket cwm;
	
	private ItemMenu editMenu;
	
	private Player player;
	private ItemEntry itemEntry;
	
	private float price;
	private int amount;

	public EditSession(CWMarket cwm, Player player, ItemEntry itemEntry) {
		this.cwm = cwm;
		this.player = player;
		this.itemEntry = itemEntry;
		editMenu = cwm.getMarketManager().getUpdateMenu();
	}
	
	public void start() {
		//reserve item and refill menu.
		itemEntry.setReserved(true);
		itemEntry.getItemCat().fillMenus();
		
		//Show menu and put player session in.
		editMenu.show(player);
		cwm.editSessions.put(player.getUniqueId(), this);
		
		//Set item in middle slot and update price/amount.
		ItemStack clone = itemEntry.getItemStack().clone();
		clone.setAmount(1);
		editMenu.setSlot(clone, 22, player);
		setPrice(itemEntry.getPricePerItem() * itemEntry.getAmount());
		setAmount(itemEntry.getAmount());
	}
	
	public void stop() {
		itemEntry.setReserved(false);
		itemEntry.getItemCat().fillMenus();
		cwm.editSessions.remove(player.getUniqueId());
		itemEntry.getItemCat().getMarketMenu(0).show(player);
	}
	
	public void editItem() {
		//Check if player has enough items if he tries to add more.
		if (amount > itemEntry.getAmount()) {
			int itemsFound = ItemUtils.getItemAmount(player.getInventory(), itemEntry.getItemStack());
			if ((amount - itemEntry.getAmount()) > itemsFound) {
				player.sendMessage(Utils.formatMsg("&cYou don't have enough items of these items."));
				return;
			}
			//Remove items from player his inventory.
			ItemUtils.removeItems(player, itemEntry.getItemStack(), amount - itemEntry.getAmount());
		}
		
		//Edit item in database.
		try {
			Statement statement = cwm.getSql().createStatement();
			if (statement.executeUpdate("UPDATE " + cwm.marketItemsTable + " SET Price=" + ((float) price / amount) + ", Amount=" + amount + " WHERE ID=" + itemEntry.getID() + ";") < 1) {
				player.sendMessage(Utils.formatMsg("&cError connecting to the databse. Item can't be edited right now."));
				stop();
				return;
			}
		} catch (SQLException e) {
			player.sendMessage(Utils.formatMsg("&cError connecting to the databse. Item can't be edited right now."));
			stop();
			e.printStackTrace();
			return;
		}
		
		//Amount reduced so give items back to player.
		if (amount < itemEntry.getAmount()) {
			ItemStack itemClone = itemEntry.getItemStack().clone();
			itemClone.setAmount(1);
			
			int itemsToGiveBack = itemEntry.getAmount() - amount;
			for (int i = 0; i < itemsToGiveBack; i++) {
				if (!player.getInventory().addItem(itemClone).isEmpty()) {
					player.getWorld().dropItem(player.getLocation(), itemClone);
				}
			}
		}
		
		//Update the item entry and refill menus
		itemEntry.setPricePerItem((float)price / amount);
		itemEntry.setAmount(amount);
		itemEntry.getItemCat().fillMenus();
		
		player.sendMessage(Utils.formatMsg("&6Item edited!"));
		
		stop();
	}
	
	public void removeItem() {
		//Delete in database.
		try {
			Statement statement = cwm.getSql().createStatement();
			if (statement.executeUpdate("DELETE FROM " + cwm.marketItemsTable + " WHERE ID='" + itemEntry.getID() + "';") < 1) {
				player.sendMessage(Utils.formatMsg("&cError connecting to the databse. Item can't be removed right now."));
				stop();
				return;
			}
		} catch (SQLException e) {
			player.sendMessage(Utils.formatMsg("&cError connecting to the databse. Item can't be removed right now."));
			stop();
			e.printStackTrace();
			return;
		}
		//Delete from map
		itemEntry.getItemCat().items.remove(itemEntry.getID());
		
		//Give items to player and drop it if inv is full.
		ItemStack itemClone = itemEntry.getItemStack().clone();
		itemClone.setAmount(1);
		for (int i = 0; i < amount; i++) {
			//Add item to inventory and if it can't be added drop it.
			if (!player.getInventory().addItem(itemClone).isEmpty()) {
				player.getWorld().dropItem(player.getLocation(), itemClone);
			}
		}
		
		player.sendMessage(Utils.formatMsg("&6Item removed and given back to you!"));
		
		stop();
	}

	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public ItemEntry getItemEntry() {
		return itemEntry;
	}
	
	public void setItemEntry(ItemEntry itemEntry) {
		this.itemEntry = itemEntry;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		if (price < 0) {
			price = 0;
		}
		
		this.price = price;
		
		//Update Price icon.
		ItemStack coinsItem = editMenu.getItems()[3];
		ItemMeta coinsMeta = coinsItem.getItemMeta();
		coinsMeta.setDisplayName(Utils.integrateColor("&6&lPrice: &e&l" + price + " coins"));
		
		List<String> lore = coinsMeta.getLore();
		float pricePerPiece = (float) price / amount;
		lore.set(0, Utils.integrateColor("&6&lPrice per piece: &e&l" + pricePerPiece + " coins"));
		coinsMeta.setLore(lore);
		
		coinsItem.setItemMeta(coinsMeta);
		
		editMenu.setSlot(coinsItem, 3, player);
	}
	
	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		if (amount < 1) {
			player.sendMessage(Utils.formatMsg("&cUse the trashcan (hopper) to remove your item from the market."));
			amount = 1;
		}
		if (amount > itemEntry.getAmount()) {
			int itemsFound = ItemUtils.getItemAmount(player.getInventory(), itemEntry.getItemStack());
			if ((amount - itemEntry.getAmount()) > itemsFound) {
				player.sendMessage(Utils.formatMsg("&cYou don't have more then " + itemsFound + " of this item to add."));
				return;
			}
		}
		
		this.amount = amount;
		
		//Update Amount icon.
		ItemStack amountItem = editMenu.getItems()[5];
		ItemMeta amountMeta = amountItem.getItemMeta();
		amountMeta.setDisplayName(Utils.integrateColor("&9&lAmount: &3&l" + amount));
		
		List<String> lore = amountMeta.getLore();
		lore.set(0, Utils.integrateColor("&9&lPrevious amount: &3&l" + itemEntry.getAmount() + " &7- &8&l" + (amount > itemEntry.getAmount() ? "&a&l+" : "&c&l") + (amount - itemEntry.getAmount())));
		amountMeta.setLore(lore);
		
		amountItem.setItemMeta(amountMeta);
		
		editMenu.setSlot(amountItem, 5, player);
		
		setPrice(price);
	}
}
