package com.clashwars.cwmarket.market;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

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
	private Market market;
	private ItemCategory itemCategory;
	
	private int price;
	private int amount;

	public SellSession(CWMarket cwm, Player player, ItemStack itemStack, Market market, ItemCategory itemCategory) {
		this.cwm = cwm;
		this.setPlayer(player);
		this.setItemStack(itemStack);
		this.setMarket(market);
		this.setItemCategory(itemCategory);
		sellMenu = cwm.getMarketManager().getSellMenu();
		setPrice(0);
		setAmount(itemStack.getAmount());
	}
	
	public void start() {
		//Show sell menu and put player in sessions.
		sellMenu.show(player);
		cwm.sellSessions.put(player.getUniqueId(), this);
		
		//Set item to sell in middle slot.
		ItemStack clone = itemStack.clone();
		clone.setAmount(1);
		sellMenu.setSlot(clone, 22, player);
	}
	
	public void stop() {
		cwm.sellSessions.remove(player.getUniqueId());
		itemCategory.getMarketMenu(0).show(player);
	}
	
	public void sellItem() {
		//Check if player still has enough items.
		if (ItemUtils.getItemAmount(player.getInventory(), itemStack) < amount) {
			player.sendMessage(Utils.formatMsg("&cYou don't have " + amount + " of this item."));
			return;
		}
		
		//Upload item to databse.
		boolean inf = cwm.infinite.contains(player.getUniqueId());
		final UUID randomUUID = UUID.randomUUID();
		try {
			Statement statement = cwm.getSql().createStatement();
			statement.executeUpdate("INSERT INTO " + cwm.marketItemsTable + " (Market, ItemCategory, UUID, Owner, Price, Amount, Infinite, ItemStack) " +
					"VALUES ('" + market.getName() + "', '" + itemCategory.getName() + "', '" + randomUUID + "', '" + player.getUniqueId().toString() +
					"', '" + (float) price / amount + "', '" + amount + "', '" + inf + "', '" + ItemUtils.serializeItemStack(itemStack) + "');");
		} catch (SQLException e) {
			player.sendMessage(Utils.formatMsg("&cError connecting to the databse. Item not sold."));
			e.printStackTrace();
			return;
		}
		
		//Clear items
		if (!inf) {
			ItemUtils.removeItems(player, itemStack, amount);
			//player.getInventory().setItem(itemSlot, new ItemStack(Material.AIR));
			player.sendMessage(Utils.formatMsg("&6Item(s) placed on the market."));
			player.sendMessage(Utils.formatMsg("&6You will receive your coins when someone buys it."));
		} else {
			player.sendMessage(Utils.formatMsg("&6Infinite item placed on the market."));
		}
		
		itemCategory.getMarketMenu(0).show(player);
		cwm.getMarketManager().updateBalance(itemCategory.getMarketMenu(0), player);
		
		cwm.getMarketManager().updateItemCategory(itemCategory);
		
		cwm.sellSessions.remove(player.getUniqueId());
		
		cwm.getServer().getScheduler().scheduleSyncDelayedTask(cwm.getPlugin(), new Runnable() {
		  public void run() {
			  
				//Get Id of sold item.
				int id = 0;
				try {
					Statement statement = cwm.getSql().createStatement();
					ResultSet res = statement.executeQuery("SELECT ID FROM " + cwm.marketItemsTable + " WHERE Market='" + market.getName() + "' AND UUID='" + randomUUID + "';");
					res.next();
					id = res.getInt("ID");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if (id > 0) {
					//Send sell message.
					String rawMsg = getRawSellMessage(id);
					for (Player pl : cwm.getServer().getOnlinePlayers()) {
						cwm.getServer().dispatchCommand(cwm.getServer().getConsoleSender(), "tellraw " + pl.getName() + " " + rawMsg);
					}
				}
			  
		  }
		}, 10L);
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
		if (price < 0) {
			price = 0;
		}
		
		this.price = price;
		
		//Update Price icon.
		ItemStack coinsItem = sellMenu.getItems()[3];
		ItemMeta coinsMeta = coinsItem.getItemMeta();
		coinsMeta.setDisplayName(Utils.integrateColor("&6&lPrice: &e&l" + price + " coins"));
		
		List<String> lore = coinsMeta.getLore();
		float pricePerPiece = (float) price / amount;
		lore.set(0, Utils.integrateColor("&6&lPrice per piece: &e&l" + pricePerPiece + " coins"));
		coinsMeta.setLore(lore);
		
		coinsItem.setItemMeta(coinsMeta);
		
		sellMenu.setSlot(coinsItem, 3, player);
	}
	
	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		if (amount < 1) {
			amount = 1;
		}
		int itemsFound = ItemUtils.getItemAmount(player.getInventory(), itemStack);
		if (amount > itemsFound) {
			player.sendMessage(Utils.formatMsg("&cYou don't have more then " + itemsFound + " of this item."));
			amount = itemsFound; 
		}
		
		this.amount = amount;
		
		//Update Amount icon.
		ItemStack amountItem = sellMenu.getItems()[5];
		ItemMeta amountMeta = amountItem.getItemMeta();
		amountMeta.setDisplayName(Utils.integrateColor("&9&lAmount: &3&l" + amount));
		
		amountItem.setItemMeta(amountMeta);
		
		sellMenu.setSlot(amountItem, 5, player);
		
		setPrice(price);
	}

	@SuppressWarnings("deprecation")
	private String getRawSellMessage(int marketItemID) {
		float pricePerPiece = (float) price / amount;
		String itemName = itemStack.getType().toString().replace("_", " ").toLowerCase();
		if (itemStack.hasItemMeta()) {
			itemName = itemStack.getItemMeta().getDisplayName();
		}
		itemName = Utils.integrateUnixColor(itemName);
		String message = "" +
		"{" +
			"text: '\u00A78[\u00A74CWMarket\u00A78] \u00A76" + player.getName() + " put a item for sale: '," +
			"extra:[" +
				"{" +
					"text:'\u00A75" + itemName + "'," +
					"hoverEvent:" +
					"{" +
						"action: show_item," +
						"value: " +
						"'{" +
							"id:" + itemStack.getTypeId() + "," +
							"tag:" +
							"{" +
								"display:" +
								"{" +
									"Name:\u00A7r"+ itemName + "," +
									"Lore:[" +
										"\u00A76\u00A7lPrice \u00A7e\u00A7l" + price + " coins," +
										"\u00A76\u00A7lPrice per piece \u00A7e\u00A7l" + pricePerPiece + " coins," +
										"\u00A79\u00A7lAmount \u00A73\u00A7l" + amount + "," +
									"]" +
								"}" +
							"}" +
						"}'" +
					"}," +
					"clickEvent:" +
					"{" +
						"action: run_command," +
						"value: '/market buy " + marketItemID + "'" +
					"}" +
				"}" +
			"]" +
		"}";
		return message;
	}

}
