package com.clashwars.cwmarket.market;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.clashwars.cwmarket.CWMarket;
import com.clashwars.cwmarket.ItemMenu;
import com.clashwars.cwmarket.util.ItemUtils;
import com.clashwars.cwmarket.util.Utils;

public class MarketManager {

	private CWMarket cwm;
	
	private ItemMenu sellMenu;
	private ItemMenu buyMenu;
	private ItemMenu updateMenu;
	
	public HashMap<String, Market> markets = new HashMap<String, Market>();
	
	
	public MarketManager(CWMarket cwm) {
		this.cwm = cwm;
	}
	
	//Populate markets with data from database.
	//If data is already populated it will update it.
	public void populate() {
		//Markets database
		// 0:ID 1:Name 2:Title
		try {
			Statement statement = cwm.getSql().createStatement();
			ResultSet res = statement.executeQuery("SELECT * FROM Markets;");
			while (res.next()) {
				String marketName = res.getString("Name").toLowerCase();
				if (!markets.containsKey(marketName)) {
					Market m = new Market(cwm, marketName, res.getString("Title"));
					m.loadItems();
					m.createMenu();
					markets.put(marketName, m);
				} else {
					markets.get(marketName).update(marketName, res.getString("Title"));
					markets.get(marketName).loadItems();
					markets.get(marketName).createMenu();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	//Get a market by name.
	public Market getMarket(String marketName) {
		if (markets.containsKey(marketName)) {
			return markets.get(marketName);
		}
		return null;
	}
	
	//Get item category by name.
	public ItemCategory getItemCategory(Market market, String itemCat) {
		if (market == null) {
			return null;
		}
		if (market.getItemCategories().containsKey(itemCat)) {
			return market.getItemCategories().get(itemCat);
		}
		return null;
	}
	
	public void updateMarket(Market market) {
		market.loadItems();
		market.fillMenu();
	}
	
	public void updateItemCategory(ItemCategory itemCat) {
		itemCat.loadItems();
		itemCat.fillMenus();
	}
	
	
	
	
	public void createMenus() {
		sellMenu = new ItemMenu("sell", 54, Utils.integrateColor("&5Sell this item"));
		sellMenu.setTypeID(4);
		fillSellMenu();
		buyMenu = new ItemMenu("buy", 54, Utils.integrateColor("&5Buy this item"));
		buyMenu.setTypeID(3);
		fillBuyMenu();
		updateMenu = new ItemMenu("update", 54, Utils.integrateColor("&5Update this item"));
		updateMenu.setTypeID(4);
		fillUpdateMenu();
	}
	
	private void fillBuyMenu() {
		ItemStack item;
		
		item = ItemUtils.getItem(Material.BOOK, 1, (short)0, "&5&lInformation", new String[] {"&7The item in the top right corner..", "&7is the item you are about to buy..",
			"&7for the price you see on the coin.", "&7Press the green button to buy it!"});
		buyMenu.setSlot(item, 0, null);
		
		item = ItemUtils.getItem(175, 1, (short)0, "&6&lPrice: &e&l0 coins", null);
		buyMenu.setSlot(item, 4, null);
		
		//Buy square.
		ItemStack buyGreen = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)5, "&2&lBuy Item!", new String[] {"&aYou will buy the item(s) in the corner."});
		ItemStack buyWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&2&lBuy Item!", new String[] {"&aYou will buy the item(s) in the corner."});
		buyMenu.setSlot(buyGreen, 19, null);
		buyMenu.setSlot(buyWhite, 20, null);
		buyMenu.setSlot(buyGreen, 21, null);
		buyMenu.setSlot(buyGreen, 28, null);
		buyMenu.setSlot(buyWhite, 29, null);
		buyMenu.setSlot(buyGreen, 30, null);
		buyMenu.setSlot(buyWhite, 37, null);
		buyMenu.setSlot(buyGreen, 38, null);
		buyMenu.setSlot(buyWhite, 39, null);
		
		//Cancel square.
		ItemStack cancelRed = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)14, "&4&lCancel!", new String[] {"&cYou wont buy the item(s) in the corner."});
		ItemStack cancelWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&4&lCancel!", new String[] {"&cYou wont buy the item(s) in the corner."});
		buyMenu.setSlot(cancelRed, 23, null);
		buyMenu.setSlot(cancelWhite, 24, null);
		buyMenu.setSlot(cancelRed, 25, null);
		buyMenu.setSlot(cancelWhite, 32, null);
		buyMenu.setSlot(cancelRed, 33, null);
		buyMenu.setSlot(cancelWhite, 34, null);
		buyMenu.setSlot(cancelRed, 41, null);
		buyMenu.setSlot(cancelWhite, 42, null);
		buyMenu.setSlot(cancelRed, 43, null);
		
		//Separators
		int[] separators = new int[] {1,2,3,5,6,7,9,10,11,12,13,14,15,16,17,18,22,26,27,31,35,36,40,44,45,46,47,48,49,50,51,52,53};
		for (int sep : separators) {
			buyMenu.setSlot(ItemUtils.getSeperator(), sep, null);
		}
	}
	
	private void fillSellMenu() {
		ItemStack item;
		item = ItemUtils.getItem(Material.BOOK, 1, (short)0, "&5&lInformation", new String[] {"&7The item in the top right corner..", "&7is the item you are about to sell.",
			"&7Set the sell price with the coin buttons.", "&7And then press the green button to sell it!"});
		sellMenu.setSlot(item, 0, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&lPrice: &e&l0 coins", new String[] {"&7You can change the price", "&7With the coin buttons below."});
		sellMenu.setSlot(item, 4, null);
		
		//Coin buttons.
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l1", new String[] {"&2Left click&7: &a+1 coin", "&4Right click&7: &c-1 coin"});
		sellMenu.setSlot(item, 11, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l10", new String[] {"&2Left click&7: &a+10 coin", "&4Right click&7: &c-10 coin"});
		sellMenu.setSlot(item, 12, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l100", new String[] {"&2Left click&7: &a+100 coin", "&4Right click&7: &c-100 coin"});
		sellMenu.setSlot(item, 13, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l1000", new String[] {"&2Left click&7: &a+1000 coin", "&4Right click&7: &c-1000 coin"});
		sellMenu.setSlot(item, 14, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l10000", new String[] {"&2Left click&7: &a+10000 coin", "&4Right click&7: &c-10000 coin"});
		sellMenu.setSlot(item, 15, null);
		
		//Sell square.
		ItemStack sellGreen = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)5, "&2&lSell Item!", new String[] {"&aYou will sell the item(s) in the corner.", "&aMake sure you specified the price!"});
		ItemStack sellWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&2&lSell Item!", new String[] {"&aYou will sell the item(s) in the corner.", "&aMake sure you specified the price!"});
		sellMenu.setSlot(sellGreen, 28, null);
		sellMenu.setSlot(sellWhite, 29, null);
		sellMenu.setSlot(sellGreen, 30, null);
		sellMenu.setSlot(sellGreen, 37, null);
		sellMenu.setSlot(sellWhite, 38, null);
		sellMenu.setSlot(sellGreen, 39, null);
		sellMenu.setSlot(sellWhite, 46, null);
		sellMenu.setSlot(sellGreen, 47, null);
		sellMenu.setSlot(sellWhite, 48, null);
		
		//Cancel square.
		ItemStack cancelRed = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)14, "&4&lCancel!", new String[] {"&cYou wont sell the item(s) in the corner."});
		ItemStack cancelWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&4&lCancel!", new String[] {"&cYou wont sell the item(s) in the corner."});
		sellMenu.setSlot(cancelRed, 32, null);
		sellMenu.setSlot(cancelWhite, 33, null);
		sellMenu.setSlot(cancelRed, 34, null);
		sellMenu.setSlot(cancelWhite, 41, null);
		sellMenu.setSlot(cancelRed, 42, null);
		sellMenu.setSlot(cancelWhite, 43, null);
		sellMenu.setSlot(cancelRed, 50, null);
		sellMenu.setSlot(cancelWhite, 51, null);
		sellMenu.setSlot(cancelRed, 52, null);
		
		//Separators
		int[] separators = new int[] {1,2,3,5,6,7,18,19,20,21,22,23,24,25,26,27,31,35,36,40,44,45,49,53};
		for (int sep : separators) {
			sellMenu.setSlot(ItemUtils.getSeperator(), sep, null);
		}
	}
	
	private void fillUpdateMenu() {
		int[] separators = new int[] {1,2,3,5,6,7,18,19,20,21,22,23,24,25,26,27,31,35,36,40,44,45,49,53};
		for (int sep : separators) {
			sellMenu.setSlot(ItemUtils.getSeperator(), sep, null);
		}
	}

	public ItemMenu getSellMenu() {
		return sellMenu;
	}
	
	public ItemMenu getBuyMenu() {
		return buyMenu;
	}
	
	public ItemMenu getUpdateMenu() {
		return updateMenu;
	}
	
	
	public void updateBalance(ItemMenu menu, Player player) {
		//Update Price icon.
		ItemStack coinsItem = menu.getItems()[4];
		ItemMeta coinsMeta = coinsItem.getItemMeta();
		coinsMeta.setDisplayName(Utils.integrateColor("&6&lBalance: &e&l" + (int) Math.round(cwm.getEconomy().getBalance(player.getName())) + " coins"));
		coinsItem.setItemMeta(coinsMeta);
		
		menu.setSlot(coinsItem, 4, player);
	}
	
	

}
