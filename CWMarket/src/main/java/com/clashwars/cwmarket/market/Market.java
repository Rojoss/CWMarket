package com.clashwars.cwmarket.market;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.clashwars.cwmarket.CWMarket;
import com.clashwars.cwmarket.ItemMenu;
import com.clashwars.cwmarket.util.ItemUtils;
import com.clashwars.cwmarket.util.Utils;

public class Market {

	private CWMarket cwm;
	
	private TreeMap<String, ItemCategory> items = new TreeMap<String, ItemCategory>();
	
	private String name;
	private String title;
	
	private ItemMenu mainMenu;
	
	
	public Market(CWMarket cwm, String marketName, String title) {
		this.cwm = cwm;
		this.name = marketName;
		this.title = title;
	}
	
	public void update(String marketName, String title) {
		if (marketName != null) {
			this.name = marketName;
		}
		if (title != null) {
			this.title = title;
		}
	}

	public void loadItems() {
		//MarketItems database
		// 0:ID 1:Item 2:AcceptedItems 3:Data 4:Name 5:Market
		try {
			Statement statement = cwm.getSql().createStatement();
			ResultSet res = statement.executeQuery("SELECT * FROM MarketItems WHERE Market='" + name + "' ORDER BY Name ASC;");
			while (res.next()) {
				String name = res.getString("Name");
				Material mat = Material.getMaterial(res.getString("Item"));
				if (mat == null) {
					continue;
				}
				if (!items.containsKey(name)) {
					ItemCategory mi = new ItemCategory(cwm, this, mat, res.getInt("Data"), name, res.getString("AcceptedItems"));
					mi.loadItems();
					items.put(Utils.stripAllColour(name), mi);
				} else {
					items.get(Utils.stripAllColour(name)).update(mat, res.getInt("Data"), name, res.getString("AcceptedItems"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//items = new TreeMap<String, ItemCategory>(items);
	}
	
	public void createMenu() {
		//Main menu with all items
		mainMenu = new ItemMenu(name + "-main", 54, Utils.integrateColor(title));
		mainMenu.setTypeID(1);
		mainMenu.setData(name);
		setMainMenuBar();
		fillMenu();
	}
	
	//Fill menu with items from database.
	public void fillMenu() {
		//TODO: Pagination.
		int slot = 9;
		for (ItemCategory item : items.values()) {
			mainMenu.setSlot(item, slot, null);
			slot++;
		}
	}
	
	//Set default buttons for the menu like information etc.
	private void setMainMenuBar() {
		ItemStack item;
		
		item = ItemUtils.getItem(Material.BOOK, 1, (short)0, "&5&lInformation", new String[] {"&7Select one of the items below", "&7and you will see all available items of that type."});
		mainMenu.setSlot(item, 0, null);
		
		item = ItemUtils.getItem(175, 1, (short)0, "&6&lBalance: &e&l0 coins", new String[] {"&7The amount of coins you have."});
		mainMenu.setSlot(item, 4, null);
		
		item = ItemUtils.getItem(Material.REDSTONE_BLOCK, 1, (short)0, "&4&lClose", new String[] {"&cClose the trading interface."});
		mainMenu.setSlot(item, 8, null);
		
		
		int[] separators = new int[] {1,2,3,5,6,7};
		for (int sep : separators) {
			mainMenu.setSlot(ItemUtils.getSeperator(), sep, null);
		}
	}

	//Open a market for the given player.
	public void openForPlayer(UUID uuid) {
		if (mainMenu == null) {
			cwm.getServer().getPlayer(uuid).sendMessage(Utils.formatMsg("&cFailed to load the market."));
			return;
		}
		Player p = cwm.getServer().getPlayer(uuid);
		mainMenu.show(p);
		cwm.getMarketManager().updateBalance(mainMenu, p);
	}
	
	
	
	
	//Get the market name.
	public String getName() {
		return name;
	}
	
	//Get the market inventory title.
	public String getTitle() {
		return title;
	}
	
	//Get a map with all item categories 
	public TreeMap<String, ItemCategory> getItemCategories() {
		return items;
	}
	
	//Get the market main menu.
	public ItemMenu getMainMenu() {
		return mainMenu;
	}
}
