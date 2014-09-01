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
	private ItemMenu bankMenu;
	
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
		updateMenu = new ItemMenu("update", 54, Utils.integrateColor("&5Edit this item"));
		updateMenu.setTypeID(5);
		fillUpdateMenu();
		bankMenu = new ItemMenu("bank", 54, Utils.integrateColor("&6&lBanker"));
		bankMenu.setTypeID(6);
		fillBankMenu();
	}
	
	private void fillBuyMenu() {
		ItemStack item;
		
		item = ItemUtils.getItem(Material.BOOK, 1, (short)0, "&5&lInformation", new String[] {"&7The item in the middle is..", "&7the item you are about to buy..",
			"&7for the price you see on the &6coin&7.", "&7You can change the amount with the &fsugar buttons&7.", "&7Press the green button to buy it!"});
		buyMenu.setSlot(item, 0, null);
		
		item = ItemUtils.getItem(Material.SUGAR, 1, (short)0, "&9&lAmount: &3&l0", new String[] {"&7You can change the amount", "&7With the &fsugar buttons &7below.",
			"&4&lWARNING&4: &cUnstackable items get unstacked!", "&cSo don't buy like 50 potions at once."});
		buyMenu.setSlot(item, 4, null);
		
		item = ItemUtils.getItem(175, 1, (short)0, "&6&lPrice: &e&l0 coins", new String[] {"&6&lPrice per piece: &e&l0 coins", "&7The price will update as you change the amount."});
		buyMenu.setSlot(item, 8, null);
		
		
		//Amount buttons.
		item = ItemUtils.getItem(Material.SUGAR, 1, (short)0, "&9&l1", new String[] {"&2Left click&7: &a+1 item", "&4Right click&7: &c-1 item"});
		buyMenu.setSlot(item, 11, null);
		item = ItemUtils.getItem(Material.SUGAR, 8, (short)0, "&9&l8", new String[] {"&2Left click&7: &a+8 items", "&4Right click&7: &c-8 items"});
		buyMenu.setSlot(item, 12, null);
		item = ItemUtils.getItem(Material.SUGAR, 16, (short)0, "&9&l16", new String[] {"&2Left click&7: &a+16 items", "&4Right click&7: &c-16 items"});
		buyMenu.setSlot(item, 13, null);
		item = ItemUtils.getItem(Material.SUGAR, 32, (short)0, "&9&l32", new String[] {"&2Left click&7: &a+32 items", "&4Right click&7: &c-32 items"});
		buyMenu.setSlot(item, 14, null);
		item = ItemUtils.getItem(Material.SUGAR, 64, (short)0, "&9&l64", new String[] {"&2Left click&7: &a+64 items", "&4Right click&7: &c-64 items"});
		buyMenu.setSlot(item, 15, null);
		
		
		//Buy square.
		ItemStack buyGreen = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)5, "&2&lBuy Item!", new String[] {"&aYou will buy the item(s) in the middle."});
		ItemStack buyWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&2&lBuy Item!", new String[] {"&aYou will buy the item(s) in the middle."});
		buyMenu.setSlot(buyGreen, 28, null);
		buyMenu.setSlot(buyWhite, 29, null);
		buyMenu.setSlot(buyGreen, 30, null);
		buyMenu.setSlot(buyGreen, 37, null);
		buyMenu.setSlot(buyWhite, 38, null);
		buyMenu.setSlot(buyGreen, 39, null);
		buyMenu.setSlot(buyWhite, 46, null);
		buyMenu.setSlot(buyGreen, 47, null);
		buyMenu.setSlot(buyWhite, 48, null);
		
		//Cancel square.
		ItemStack cancelRed = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)14, "&4&lCancel!", new String[] {"&cYou wont buy the item(s) in the middle."});
		ItemStack cancelWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&4&lCancel!", new String[] {"&cYou wont buy the item(s) in the middle."});
		buyMenu.setSlot(cancelRed, 32, null);
		buyMenu.setSlot(cancelWhite, 33, null);
		buyMenu.setSlot(cancelRed, 34, null);
		buyMenu.setSlot(cancelWhite, 41, null);
		buyMenu.setSlot(cancelRed, 41, null);
		buyMenu.setSlot(cancelWhite, 43, null);
		buyMenu.setSlot(cancelRed, 50, null);
		buyMenu.setSlot(cancelWhite, 51, null);
		buyMenu.setSlot(cancelRed, 52, null);
		
		//Separators
		int[] separators = new int[] {1,2,3,5,6,7,18,19,20,21,23,24,25,26,27,31,35,36,40,44,45,49,53};
		for (int sep : separators) {
			buyMenu.setSlot(ItemUtils.getSeperator(), sep, null);
		}
	}
	
	private void fillSellMenu() {
		ItemStack item;
		item = ItemUtils.getItem(Material.BOOK, 1, (short)0, "&5&lInformation", new String[] {"&7The item in the middle is..", "&7 the item you are about to sell.",
			"&7Set the sell price with the &6coin buttons&7.", "&7You can also change the amount of items..", "&7you are about to sell with the &fsugar&7.", 
			"&7And then press the &agreen &7button to sell it!"});
		sellMenu.setSlot(item, 0, null);
		
		item = ItemUtils.getItem(175, 1, (short)0, "&6&lPrice: &e&l0 coins", new String[] {"&6&lPrice per piece: &e&l0 coins", "&7You can change the price", "&7With the &6coin buttons &7below."});
		sellMenu.setSlot(item, 3, null);
		item = ItemUtils.getItem(Material.SUGAR, 1, (short)0, "&9&lAmount: &3&l0", new String[] {"&7You can change the amount", "&7With the &fsugar buttons &7below.", 
			"&7You can even add more then a stack.", "&7After it's placed you can also add or remove items.", "&7And the buyer can specify how much he wants to buy."});
		sellMenu.setSlot(item, 5, null);
		
		//Coin buttons.
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l1", new String[] {"&2Left click&7: &a+1 coin", "&4Right click&7: &c-1 coin"});
		sellMenu.setSlot(item, 9, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l10", new String[] {"&2Left click&7: &a+10 coins", "&4Right click&7: &c-10 coins"});
		sellMenu.setSlot(item, 10, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l100", new String[] {"&2Left click&7: &a+100 coins", "&4Right click&7: &c-100 coins"});
		sellMenu.setSlot(item, 11, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l1000", new String[] {"&2Left click&7: &a+1000 coins", "&4Right click&7: &c-1000 coins"});
		sellMenu.setSlot(item, 12, null);
		
		//Amount buttons.
		item = ItemUtils.getItem(Material.SUGAR, 1, (short)0, "&9&l1", new String[] {"&2Left click&7: &a+1 item", "&4Right click&7: &c-1 item"});
		sellMenu.setSlot(item, 14, null);
		item = ItemUtils.getItem(Material.SUGAR, 8, (short)0, "&9&l8", new String[] {"&2Left click&7: &a+8 items", "&4Right click&7: &c-8 items"});
		sellMenu.setSlot(item, 15, null);
		item = ItemUtils.getItem(Material.SUGAR, 32, (short)0, "&9&l32", new String[] {"&2Left click&7: &a+32 items", "&4Right click&7: &c-32 items"});
		sellMenu.setSlot(item, 16, null);
		item = ItemUtils.getItem(Material.SUGAR, 64, (short)0, "&9&l64", new String[] {"&2Left click&7: &a+64 items", "&4Right click&7: &c-64 items"});
		sellMenu.setSlot(item, 17, null);
		
		
		//Sell square.
		ItemStack sellGreen = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)5, "&2&lSell Item!", new String[] {"&aYou will sell the item(s) in the middle.", 
			"&aMake sure you specified the right price and amount!"});
		ItemStack sellWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&2&lSell Item!", new String[] {"&aYou will sell the item(s) in the middle.", 
			"&aMake sure you specified the right price and amount!"});
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
		ItemStack cancelRed = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)14, "&4&lCancel!", new String[] {"&cYou wont sell the item(s) in the middle."});
		ItemStack cancelWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&4&lCancel!", new String[] {"&cYou wont sell the item(s) in the middle."});
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
		int[] separators = new int[] {1,2,4,6,7,8,13,18,19,20,21,23,24,25,26,27,31,35,36,40,44,45,49,53};
		for (int sep : separators) {
			sellMenu.setSlot(ItemUtils.getSeperator(), sep, null);
		}
	}
	
	private void fillUpdateMenu() {
		ItemStack item;
		item = ItemUtils.getItem(Material.BOOK, 1, (short)0, "&5&lInformation", new String[] {"&7The item in the middle is..", "&7the item you are editing.",
			"&7Change the price with the &6coin buttons &7below.", "&7Or specify the amount with the &fsugar buttons&7,", "&7to refill stock (add items) or remove items.", 
			"&7And then press the green button to confirm it!", "&7Or press the &ctrashcan&7 (hopper) to remove it."});
		updateMenu.setSlot(item, 0, null);
		
		item = ItemUtils.getItem(175, 1, (short)0, "&6&lPrice: &e&l0 coins", new String[] {"&6&lPrice per piece: &e&l0 coins", "&7You can change the price", "&7With the &6coin buttons &7below."});
		updateMenu.setSlot(item, 3, null);
		item = ItemUtils.getItem(Material.SUGAR, 1, (short)0, "&9&lAmount: &3&l0", new String[] {"&9&lPrevious amount: &3&l0 &7- &8&l0", "&7You can change the amount", "&7With the &fsugar buttons &7below.", 
			"&7You can restock with this by increasing the amount.", "&7Or remove items by decreasing the amount."});
		updateMenu.setSlot(item, 5, null);
		
		item = ItemUtils.getItem(Material.HOPPER, 1, (short)0, "&4&lTrashcan", new String[] {"&7Remove the item from the market.", "&7You will get the item back!"});
		updateMenu.setSlot(item, 8, null);
		
		//Coin buttons.
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l1", new String[] {"&2Left click&7: &a+1 coin", "&4Right click&7: &c-1 coin"});
		updateMenu.setSlot(item, 9, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l10", new String[] {"&2Left click&7: &a+10 coins", "&4Right click&7: &c-10 coins"});
		updateMenu.setSlot(item, 10, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l100", new String[] {"&2Left click&7: &a+100 coins", "&4Right click&7: &c-100 coins"});
		updateMenu.setSlot(item, 11, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&l1000", new String[] {"&2Left click&7: &a+1000 coins", "&4Right click&7: &c-1000 coins"});
		updateMenu.setSlot(item, 12, null);
		
		//Amount buttons.
		item = ItemUtils.getItem(Material.SUGAR, 1, (short)0, "&9&l1", new String[] {"&2Left click&7: &a+1 item", "&4Right click&7: &c-1 item"});
		updateMenu.setSlot(item, 14, null);
		item = ItemUtils.getItem(Material.SUGAR, 8, (short)0, "&9&l8", new String[] {"&2Left click&7: &a+8 items", "&4Right click&7: &c-8 items"});
		updateMenu.setSlot(item, 15, null);
		item = ItemUtils.getItem(Material.SUGAR, 32, (short)0, "&9&l32", new String[] {"&2Left click&7: &a+32 items", "&4Right click&7: &c-32 items"});
		updateMenu.setSlot(item, 16, null);
		item = ItemUtils.getItem(Material.SUGAR, 64, (short)0, "&9&l64", new String[] {"&2Left click&7: &a+64 items", "&4Right click&7: &c-64 items"});
		updateMenu.setSlot(item, 17, null);
		
		//Edit square.
		ItemStack editGreen = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)5, "&2&lEdit Item!", new String[] {"&aYou will update the item(s) in the corner."});
		ItemStack editWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&2&lEdit Item!", new String[] {"&aYou will update the item(s) in the corner."});
		updateMenu.setSlot(editGreen, 28, null);
		updateMenu.setSlot(editWhite, 29, null);
		updateMenu.setSlot(editGreen, 30, null);
		updateMenu.setSlot(editGreen, 37, null);
		updateMenu.setSlot(editWhite, 38, null);
		updateMenu.setSlot(editGreen, 39, null);
		updateMenu.setSlot(editWhite, 46, null);
		updateMenu.setSlot(editGreen, 47, null);
		updateMenu.setSlot(editWhite, 48, null);
		
		//Cancel square.
		ItemStack cancelRed = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)14, "&4&lCancel!", new String[] {"&cYou wont edit the item(s) in the corner."});
		ItemStack cancelWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&4&lCancel!", new String[] {"&cYou wont edit the item(s) in the corner."});
		updateMenu.setSlot(cancelRed, 32, null);
		updateMenu.setSlot(cancelWhite, 33, null);
		updateMenu.setSlot(cancelRed, 34, null);
		updateMenu.setSlot(cancelWhite, 41, null);
		updateMenu.setSlot(cancelRed, 42, null);
		updateMenu.setSlot(cancelWhite, 43, null);
		updateMenu.setSlot(cancelRed, 50, null);
		updateMenu.setSlot(cancelWhite, 51, null);
		updateMenu.setSlot(cancelRed, 52, null);
		
		int[] separators = new int[] {1,2,4,6,7,13,18,19,20,21,23,24,25,26,27,31,35,36,40,44,45,49,53};
		for (int sep : separators) {
			updateMenu.setSlot(ItemUtils.getSeperator(), sep, null);
		}
	}
	
	public void fillBankMenu() {
		ItemStack item;
		item = ItemUtils.getItem(Material.BOOK, 1, (short)0, "&5&lInformation", new String[] {"&7Click on gold in your inventory to desposit 1", "&7and shift click to deposit the whole stack.",
			"&7Hover on the coin to see the total value.", "&7Press the green button to deposit it!", "&7Press the hopper in the corner do despoit all your gold."});
		bankMenu.setSlot(item, 0, null);
		item = ItemUtils.getItem(175, 1, (short)0, "&6&lValue: &e&l0 coins", new String[] {"&7Total value of all gold on the second row.", "&7Gold values: &8Block: &e810 &7- &8Ingot: &e90 &7- &8Nugget: &e10"});
		bankMenu.setSlot(item, 4, null);
		item = ItemUtils.getItem(Material.HOPPER, 1, (short)0, "&6&lDeposit all!", new String[] {"&7Deposit all gold in your inventory."});
		bankMenu.setSlot(item, 8, null);
		
		
		//Deposit square.
		ItemStack depositGreen = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)5, "&2&lDeposit gold!", new String[] {"&aYou will deposit all gold on the second row."});
		ItemStack depositWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&2&lDeposit gold!", new String[] {"&aYou will deposit all gold on the second row."});
		bankMenu.setSlot(depositGreen, 28, null);
		bankMenu.setSlot(depositWhite, 29, null);
		bankMenu.setSlot(depositGreen, 30, null);
		bankMenu.setSlot(depositGreen, 37, null);
		bankMenu.setSlot(depositWhite, 38, null);
		bankMenu.setSlot(depositGreen, 39, null);
		bankMenu.setSlot(depositWhite, 46, null);
		bankMenu.setSlot(depositGreen, 47, null);
		bankMenu.setSlot(depositWhite, 48, null);
		
		//Cancel square.
		ItemStack cancelRed = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)14, "&4&lCancel!", new String[] {"&cYou wont deposit the gold on the second row."});
		ItemStack cancelWhite = ItemUtils.getItem(Material.STAINED_GLASS_PANE, 1, (short)0, "&4&lCancel!", new String[] {"&cYou wont deposit the gold on the second row."});
		bankMenu.setSlot(cancelRed, 32, null);
		bankMenu.setSlot(cancelWhite, 33, null);
		bankMenu.setSlot(cancelRed, 34, null);
		bankMenu.setSlot(cancelWhite, 41, null);
		bankMenu.setSlot(cancelRed, 42, null);
		bankMenu.setSlot(cancelWhite, 43, null);
		bankMenu.setSlot(cancelRed, 50, null);
		bankMenu.setSlot(cancelWhite, 51, null);
		bankMenu.setSlot(cancelRed, 52, null);
		
		//Separators
		int[] separators = new int[] {1,2,3,5,6,7,18,19,20,21,22,23,24,25,26,27,31,35,36,40,44,45,49,53};
		for (int sep : separators) {
			bankMenu.setSlot(ItemUtils.getSeperator(), sep, null);
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
	
	public ItemMenu getBankMenu() {
		return bankMenu;
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
