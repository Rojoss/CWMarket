package com.clashwars.cwmarket.market;

import java.sql.SQLException;
import java.sql.Statement;

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
	
	private int quantity;
	private int price;

	public BuySession(CWMarket cwm, Player player, ItemEntry itemEntry) {
		this.cwm = cwm;
		this.player = player;
		this.itemEntry = itemEntry;
		buyMenu = cwm.getMarketManager().getBuyMenu();
		setPrice(itemEntry.getPrice());
	}
	
	public void start() {
		if (!itemEntry.isInf()) {
			itemEntry.setReserved(true);
			itemEntry.getItemCat().fillMenus();
		} else {
			setQuantity(1);
		}
		buyMenu.show(player);
		buyMenu.setSlot(itemEntry.getItemStack(), 8, player);
	}
	
	public void stop() {
		itemEntry.setReserved(false);
		itemEntry.getItemCat().fillMenus();
		itemEntry.getItemCat().getMarketMenu(0).show(player);
		cwm.buySessions.remove(player.getName());
	}
	
	public void buyItem() {
		if (!itemEntry.isInf()) {
			//Remove item
			try {
				Statement statement = cwm.getSql().createStatement();
				if (statement.executeUpdate("DELETE FROM Items WHERE ID='" + itemEntry.getID() + "';") < 1) {
					player.sendMessage(Utils.formatMsg("&cError connecting to the databse. Item can't be bought right now."));
					stop();
					return;
				}
			} catch (SQLException e) {
				player.sendMessage(Utils.formatMsg("&cError connecting to the databse. Item can't be bought right now."));
				stop();
				e.printStackTrace();
				return;
			}
			
			itemEntry.getItemCat().items.remove(itemEntry.getID());
			itemEntry.getItemCat().fillMenus();
		}
		
		//Take money and give to seller
		if (!itemEntry.isInf()) {
			cwm.getEconomy().withdrawPlayer(player.getName(), itemEntry.getPrice());
			cwm.getEconomy().depositPlayer(cwm.getServer().getPlayer(itemEntry.getOwner()).getName(), itemEntry.getPrice());
		} else {
			cwm.getEconomy().withdrawPlayer(player.getName(), price);
		}
		
		//Give item to player and drop it if inv is full.
		ItemStack itemClone = itemEntry.getItemStack().clone();
		if (itemEntry.isInf()) {
			itemClone.setAmount(quantity);
		}
		if (player.getInventory().firstEmpty() >= 0) {
			player.getInventory().addItem(itemClone);
		} else {
			player.sendMessage(Utils.formatMsg("&cYour inventory is full! &4Item dropped on the ground."));
			player.getWorld().dropItem(player.getLocation(), itemClone);
		}
		
		//Send message to buyer and seller if he's online.
		if (!itemEntry.isInf()) {
			player.sendMessage(Utils.formatMsg("&6Sucesfully bought the item(s) for &e" + itemEntry.getPrice() + " coins&6!"));
			if (cwm.getServer().getPlayer(itemEntry.getOwner()) != null && cwm.getServer().getPlayer(itemEntry.getOwner()).isOnline()) {
				String itemName = itemEntry.getItemStack().getType().toString().toLowerCase().replace("_", " ");
				cwm.getServer().getPlayer(itemEntry.getOwner()).sendMessage(Utils.formatMsg("&6Your &5" + itemName + " &6was bought by &5" + player.getName() + " &6for &e" + itemEntry.getPrice() + " coins&6!"));
			}
		} else { 
			player.sendMessage(Utils.formatMsg("&6Sucesfully bought the item(s) for &e" + price + " coins&6!"));
		}
		cwm.buySessions.remove(player.getName());
		player.closeInventory();
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
	
	//Update Price icon.
	public void setPrice(int price) {
		ItemStack coinsItem = buyMenu.getItems()[4];
		ItemMeta coinsMeta = coinsItem.getItemMeta();
		coinsMeta.setDisplayName(Utils.integrateColor("&6&lPrice: &e&l" + price + " coins"));
		coinsItem.setItemMeta(coinsMeta);
		
		buyMenu.setSlot(coinsItem, 4, player);
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		ItemStack item = buyMenu.getItems()[8];
		item.setAmount(quantity);
		buyMenu.setSlot(item, 8, player);
		price = Math.round(itemEntry.getPrice() * quantity);
		setPrice(price);
		this.quantity = quantity;
	}
}
