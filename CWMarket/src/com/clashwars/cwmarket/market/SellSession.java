package com.clashwars.cwmarket.market;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.clashwars.cwmarket.CWMarket;
import com.clashwars.cwmarket.ItemMenu;
import com.clashwars.cwmarket.util.ItemUtils;
import com.clashwars.cwmarket.util.Utils;

public class SellSession {
	
	private CWMarket cwm;
	
	private ItemMenu sellMenu;
	
	private Player player;
	private ItemStack itemStack;
	private int itemSlot;
	private Market market;
	private ItemCategory itemCategory;
	
	private int price;

	public SellSession(CWMarket cwm, Player player, ItemStack itemStack, int itemSlot, Market market, ItemCategory itemCategory) {
		this.cwm = cwm;
		this.setPlayer(player);
		this.setItemStack(itemStack);
		this.setItemSlot(itemSlot);
		this.setMarket(market);
		this.setItemCategory(itemCategory);
		sellMenu = cwm.getMarketManager().getSellMenu();
		setPrice(0);
	}
	
	public void start() {
		sellMenu.show(player);
		sellMenu.setSlot(itemStack, 8, player);
	}
	
	public void stop() {
		itemCategory.getMarketMenu(0).show(player);
		cwm.sellSessions.remove(player.getName());
	}
	
	public void sellItem() {
		//Upload item to databse.
		boolean inf = cwm.infinite.contains(player.getUniqueId());
		try {
			Statement statement = cwm.getSql().createStatement();
			statement.executeUpdate("INSERT INTO Items (Market, ItemCategory, UUID, Owner, Price, Infinite, ItemStack) " +
					"VALUES ('" + market.getName() + "', '" + itemCategory.getName() + "', '" + UUID.randomUUID() + "', '" + player.getUniqueId().toString() +
					"', '" + price + "', '" + inf + "', '" + ItemUtils.serializeItemStack(itemStack) + "');");
		} catch (SQLException e) {
			player.sendMessage(Utils.formatMsg("&cError connecting to the databse. Item not sold."));
			e.printStackTrace();
			return;
		}
		//Clear item
		if (!inf) {
			player.getInventory().setItem(itemSlot, new ItemStack(Material.AIR));
			player.sendMessage(Utils.formatMsg("&6Item placed on the market."));
			player.sendMessage(Utils.formatMsg("&6You will receive your coins when someone buys it."));
		} else {
			player.sendMessage(Utils.formatMsg("&6Infinite item placed on the market."));
		}
		
		
		itemCategory.getMarketMenu(0).show(player);
		cwm.getMarketManager().updateBalance(itemCategory.getMarketMenu(0), player);
		
		cwm.getMarketManager().updateItemCategory(itemCategory);
		
		cwm.sellSessions.remove(player.getName());
	}

	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	
	public ItemStack getItemStack() {
		return itemStack;
	}

	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
	}
	
	
	public int getItemSlot() {
		return itemSlot;
	}

	public void setItemSlot(int itemSlot) {
		this.itemSlot = itemSlot;
	}
	
	
	public Market getMarket() {
		return market;
	}

	public void setMarket(Market market) {
		this.market = market;
	}

	
	public ItemCategory getItemCategory() {
		return itemCategory;
	}

	public void setItemCategory(ItemCategory itemCategory) {
		this.itemCategory = itemCategory;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
		
		//Update Price icon.
		ItemStack coinsItem = sellMenu.getItems()[4];
		ItemMeta coinsMeta = coinsItem.getItemMeta();
		coinsMeta.setDisplayName(Utils.integrateColor("&6&lPrice: &e&l" + price + " coins"));
		coinsItem.setItemMeta(coinsMeta);
		
		sellMenu.setSlot(coinsItem, 4, player);
	}

	

}
