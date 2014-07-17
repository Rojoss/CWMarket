package com.clashwars.cwmarket.market;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.clashwars.cwmarket.CWMarket;
import com.clashwars.cwmarket.ItemMenu;
import com.clashwars.cwmarket.util.ItemUtils;
import com.clashwars.cwmarket.util.Utils;

public class ItemCategory extends ItemStack {
	
	private CWMarket cwm;
	private Market market;
	private String name;
	
	private ArrayList<ItemMenu> marketMenus = new ArrayList<ItemMenu>();
	private ArrayList<AcceptedItem> acceptedItems = new ArrayList<AcceptedItem>();
	public TreeMap<Integer, ItemEntry> items = new TreeMap<Integer, ItemEntry>();
	
	
	public ItemCategory(CWMarket cwm, Market market, Material mat, int data, String name, String acceptedItemsString) {
		super(mat, 1, (short) data);
		this.cwm = cwm;
		this.setMarket(market);
		this.name = name;
		ItemMeta meta = this.getItemMeta();
		meta.setDisplayName(Utils.integrateColor(name));
		this.setItemMeta(meta);
		setAcceptedItems(acceptedItemsString);
	}

	public void update(Material mat, int data, String name,String acceptedItemsString) {
		this.setType(mat);
		this.setDurability((short) data);
		
		ItemMeta meta = this.getItemMeta();
		meta.setDisplayName(Utils.integrateColor(name));
		this.setItemMeta(meta);
		setAcceptedItems(acceptedItemsString);
	}
	
	public void loadItems() {
		//Items database
		// 0:ID 1:Market 2:ItemCategory 3:Owner 4:Price 5:Infinite 6:ItemStack
		try {
			Statement statement = cwm.getSql().createStatement();
			ResultSet res = statement.executeQuery("SELECT * FROM Items WHERE Market='" + market.getName() + "' AND ItemCategory='" + name + "';");
			TreeMap<Integer, ItemEntry> tempItems = new TreeMap<Integer, ItemEntry>();
			while (res.next()) {
				int id = res.getInt("ID");
				ItemEntry item = new ItemEntry(ItemUtils.deserializeItemStack(res.getString("ItemStack")), market, this, id, UUID.fromString(res.getString("Owner")), res.getInt("Price"), res.getString("Infinite"));
				tempItems.put(id, item);
			}
			setItems(tempItems);
			fillMenus();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void createMenus() {
		int pagesNeeded = (int) Math.ceil((double)items.size() / 44);
		if (pagesNeeded <= 0) {
			pagesNeeded = 1;
		}
		for (int page = 0; page < pagesNeeded; page++) {
			if (marketMenus.size() == page) {
				ItemMenu marketMenu = new ItemMenu(getName() + ":" + page, 54, Utils.integrateColor(getName()));
				marketMenu.setTypeID(2);
				marketMenu.setData(market.getName());
				marketMenu.setData2(getName());
				marketMenu.setPage(page);
				setMainMenuBar(marketMenu);
				marketMenus.add(marketMenu);
			}
		}
	}
	
	public void fillMenus() {
		createMenus();
		
		if (marketMenus.size() < 1 || marketMenus.get(0) == null) {
			return;
		}
		
		int page = 0;
		int slot = 9;
		boolean lastItem = false;
		
		//ArrayList<Integer> keys = new ArrayList<Integer>(items.keySet());
        
		
		//for(int i=keys.size()-1; i>=0;i--) {
		for (int itemID : items.keySet()) {
			lastItem = false;
			ItemMenu marketMenu = marketMenus.get(page);
			ItemEntry itemEntry = items.get(itemID);
			if (itemEntry == null || itemEntry.getItemStack() == null) {
				continue;
			}
			//Don't display reserved items.
			if (itemEntry.isReserved()) {
				continue;
			}

			ItemStack entryItem = itemEntry.getItemStack().clone();
			
			//Change meta to add information about item like seller and price etc.
			ItemMeta meta = entryItem.getItemMeta();
			List<String> lore = meta.getLore();
			if (lore == null) {
				lore = new ArrayList<String>();
			}
			lore.add(0, Utils.integrateColor("&0&o" + itemEntry.getID()));
			lore.add(1, Utils.integrateColor("&6&lPrice: &e" + itemEntry.getPrice() + " coins"));
			if (itemEntry.isInf()) {
				lore.add(2, Utils.integrateColor("&8&lSeller: &7Server &8[&aInfinite stock&8]"));
			} else {
				lore.add(2, Utils.integrateColor("&8&lSeller: &7" + cwm.getServer().getOfflinePlayer(itemEntry.getOwner()).getName()));
			}
			
			if (lore.size() > 3) {
				lore.add(3, Utils.integrateColor("&d&lItem lore:"));
			}
			meta.setLore(lore);
			entryItem.setItemMeta(meta);
			marketMenu.setSlot(entryItem, slot, null);
			if (slot < 52) {
				slot++;
			} else {
				lastItem = true;
				page++;
				slot = 9;
			}
		}
		if (!lastItem) {
			for (int i = slot; i < 53; i++) {
				marketMenus.get(page).setSlot(new ItemStack(Material.AIR), i, null);
			}
		}
	}
	
	//Set default buttons for the menu like information etc.
	private void setMainMenuBar(ItemMenu menu) {
		ItemStack item;
		
		item = ItemUtils.getItem(Material.LADDER, 1, (short)0, "&7Previous page", new String[] {"&8Switch to the previous page."});
		menu.setSlot(item, 0, null);
		
		item = ItemUtils.getItem(Material.LADDER, 1, (short)0, "&7Next page", new String[] {"&8Switch to the next page."});
		menu.setSlot(item, 53, null);
		
		item = ItemUtils.getItem(Material.BOOK, 1, (short)0, "&5&lInformation", new String[] {"&7Find a item you like to buy", "&7and left click it to start the transaction."});
		menu.setSlot(item, 1, null);
		
		item = ItemUtils.getItem(175, 1, (short)0, "&6&lBalance: &e&l0 coins", new String[] {"&7The amount of coins you have."});
		menu.setSlot(item, 4, null);
		
		item = ItemUtils.getItem(Material.CHEST, 1, (short)0, "&e&lMy items", new String[] {"&7Filter out your own items."});
		menu.setSlot(item, 6, null);
		
		item = ItemUtils.getItem(Material.REDSTONE_BLOCK, 1, (short)0, "&4&lBack", new String[] {"&cGo back to the " + market.getTitle()});
		menu.setSlot(item, 8, null);
		
		
		int[] separators = new int[] {2,3,5,7};
		for (int sep : separators) {
			menu.setSlot(ItemUtils.getSeperator(), sep, null);
		}
	}
	
	public ItemEntry getItem(int id) {
		if (items.containsKey(id)) {
			return items.get(id);
		}
		return null;
	}
	
	public void setAcceptedItems(String itemsStr) {
		acceptedItems.clear();
		if (itemsStr.contains(";")) {
			String[] itemArray = itemsStr.split(";");
			for (String i : itemArray) {
				String[] split = i.split(":");
				Material mat = Material.getMaterial(split[0]);
				Short durability = Short.parseShort(split[1].trim());
				String enchant = null;
				if (split.length > 2) {
					enchant = split[2];
				}
				acceptedItems.add(new AcceptedItem(mat, durability, enchant));
			}
		} else {
			String[] split = itemsStr.split(":");
			Material mat = Material.getMaterial(split[0]);
			Short durability = Short.parseShort(split[1]);
			String enchant = null;
			if (split.length > 2) {
				enchant = split[2];
			}
			
			acceptedItems.add(new AcceptedItem(mat, durability, enchant));
		}
	}
	
	public ArrayList<AcceptedItem> getAcceptedItems() {
		return acceptedItems;
	}
	
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return this.getItemMeta().getDisplayName();
	}
	
	public List<String> getLore() {
		return this.getItemMeta().getLore();
	}
	
	public Material getItem() {
		return this.getType();
	}
	
	public int getItemData() {
		return this.getDurability();
	}

	public Market getMarket() {
		return market;
	}

	public void setMarket(Market market) {
		this.market = market;
	}
	
	public ItemMenu getMarketMenu(int page) {
		return marketMenus.get(page);
	}
	
	public ArrayList<ItemMenu> getMarketMenus() {
		return marketMenus;
	}

	public TreeMap<Integer, ItemEntry> getItems() {
		return items;
	}

	public void setItems(TreeMap<Integer, ItemEntry> items) {
		this.items = items;
	}
	
}
