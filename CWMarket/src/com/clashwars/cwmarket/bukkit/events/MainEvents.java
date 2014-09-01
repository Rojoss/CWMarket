package com.clashwars.cwmarket.bukkit.events;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.clashwars.cwmarket.CWMarket;
import com.clashwars.cwmarket.ItemMenu;
import com.clashwars.cwmarket.ItemMenu.ItemMenuClickEvent;
import com.clashwars.cwmarket.market.AcceptedItem;
import com.clashwars.cwmarket.market.BankSession;
import com.clashwars.cwmarket.market.BuySession;
import com.clashwars.cwmarket.market.EditSession;
import com.clashwars.cwmarket.market.ItemCategory;
import com.clashwars.cwmarket.market.ItemEntry;
import com.clashwars.cwmarket.market.Market;
import com.clashwars.cwmarket.market.SellSession;
import com.clashwars.cwmarket.util.ItemUtils;
import com.clashwars.cwmarket.util.Utils;

public class MainEvents implements Listener {

	private CWMarket cwm;
	
	public MainEvents(CWMarket cwm) {
		this.cwm = cwm;
	}
	
    @EventHandler
    public void npcInteract(PlayerInteractEntityEvent event) {
        Player p = event.getPlayer();
        if (event.getRightClicked() instanceof HumanEntity) {
        	HumanEntity npc = (HumanEntity) event.getRightClicked();
        	if (Utils.stripAllColour(npc.getName().toLowerCase()).equals("banker")) {
        		if (cwm.bankSessions.size() >= 1) {
        			p.sendMessage(Utils.integrateColor("&8[&6Banker&8] &cI'm busy with someone else right now."));
        		} else {
        			BankSession bs = new BankSession(cwm, p);
            		bs.start();
            		cwm.bankSessions.put(p.getUniqueId(), bs);
            		p.sendMessage(Utils.integrateColor("&8[&6Banker&8] &aPlease deposit your gold."));
        		}
        		return;
        	}
        	if (cwm.getMarketManager().getMarket(Utils.stripAllColour(npc.getName()).toLowerCase()) != null) {
            	cwm.getMarketManager().getMarket(Utils.stripAllColour(npc.getName()).toLowerCase()).openForPlayer(p.getUniqueId());
            }
        }
    }
    
    @EventHandler
	public void close(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		UUID uuid = player.getUniqueId();
		if (cwm.sellSessions.containsKey(uuid)) {
			cwm.sellSessions.get(uuid).stop();
		}
		if (cwm.buySessions.containsKey(uuid)) {
			cwm.buySessions.get(uuid).stop();
		}
		if (cwm.editSessions.containsKey(uuid)) {
			cwm.editSessions.get(uuid).stop();
		}
		if (cwm.bankSessions.containsKey(uuid)) {
			cwm.bankSessions.get(uuid).stop();
		}
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	private void menuClick(ItemMenuClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		ItemMenu menu = event.getItemMenu();
		
		//Get the clicked item.
		ItemStack current = event.getCurrentItem();
		if (current == null || current.getType() == Material.AIR) {
			return;
		}
		
		event.setCancelled(true);
		event.setResult(Result.DENY);
		event.setCursor(null);
		player.updateInventory();
		
		//Check if player clicked on top or bottom (market/inventory).
		if (event.getRawSlot() < menu.getSize()) {
			
			//Loop through all menu items.
			for (int i = 0; i < menu.getItems().length; i++) {
				ItemStack item = menu.getItems()[i];
				ItemMeta meta = null;

				//Check if the clicked item is the same as the current looped menu item.
				if (item != null && i == event.getRawSlot()) {
					if (item.hasItemMeta()) {
						meta = item.getItemMeta();
					}
					
					
					
					//MAIN MENU
					if (menu.getTypeID() == 1) {
						
						if (i == 8) {
							player.closeInventory();
							return;
						}
						
						//Get the market
						Market market = cwm.getMarketManager().getMarket(menu.getData());
						if (market == null) {
							continue;
						}

						//Click on a specific item (Open item market menu)
						String itemCatName = Utils.stripAllColour(item.getItemMeta().getDisplayName());
						if (market.getItemCategories().containsKey(itemCatName)) {
							ItemCategory itemCat = market.getItemCategories().get(itemCatName);
							itemCat.getMarketMenu(0).show(player);
							cwm.getMarketManager().updateBalance(itemCat.getMarketMenu(0), player);
						}
					}
					
					
					
					//ITEMS MENU
					if (menu.getTypeID() == 2) {
						Market market = cwm.getMarketManager().getMarket(menu.getData());
						if (market == null) {
							player.closeInventory();
							return;
						}
						ItemCategory itemCat = market.getItemCategories().get(Utils.stripAllColour(menu.getTitle()));
						if (itemCat == null) {
							player.closeInventory();
							return;
						}
						
						if (i == 8) {
							market.openForPlayer(player.getUniqueId());
							return;
						}
						
						//Previous page
						int page = menu.getPage() + 1;
						if (i == 0) {
							if (page > 1) {
								itemCat.getMarketMenu(page - 2).show(player);
								player.sendMessage(Utils.formatMsg("&6Switched to page &5" + (page - 1) + "&6."));
								return;
								//TODO: Update balance.
							} else {
								player.sendMessage(Utils.formatMsg("&cYou're already on the first page."));
								return;
							}
						}
						
						//Next page
						if (i == 53) {
							if (page < itemCat.getMarketMenus().size()) {
								itemCat.getMarketMenu(page).show(player);
								player.sendMessage(Utils.formatMsg("&6Switched to page &5" + (page + 1) + "&6."));
								return;
								//TODO: Update balance.
							} else {
								player.sendMessage(Utils.formatMsg("&cYou're already on the last page."));
								return;
							}
						}
						
						
						//Click on a available item. (try buy it or edit it)
						if (i > 8 && i < 52) {
							int id;
							if ((id = ItemUtils.getIDFromLore(current)) >= 0) {
								ItemEntry itemEntry = itemCat.getItem(id);
								
								if (itemEntry.getOwner().equals(player.getUniqueId())) {
									EditSession es = new EditSession(cwm, player, itemEntry);
									es.start();
									return;
								}
								
								BuySession bs = new BuySession(cwm, player, itemEntry);
								bs.start();
								
								return;
							} else {
								player.sendMessage(Utils.formatMsg("&cItem not available anymore."));
								return;
							}
						}
						
					}
					
					
					
					//BUY MENU
					if (menu.getTypeID() == 3) {
						if (!cwm.buySessions.containsKey(player.getUniqueId())) {
							player.sendMessage(Utils.formatMsg("&cCould not properly recognize your buy session please retry."));
							player.closeInventory();
							return;
						}
						BuySession bs = cwm.buySessions.get(player.getUniqueId());
						
						//Change amount.
						int amt = 0;
						if (i == 11) {
							amt = 1;
						} else if (i == 12) {
							amt = 8;
						} else if (i == 13) {
							amt = 16;
						} else if (i == 14) {
							amt = 32;
						} else if (i == 15) {
							amt = 64;
						}
						if (amt > 0) {
							if (event.getClick() == ClickType.LEFT) {
								bs.updateAmount(bs.getAmount() + amt);
							} else if (event.getClick() == ClickType.RIGHT) {
								bs.updateAmount(bs.getAmount() - amt);
							}
						}
						
						//Buy item or cancel.
						if (meta != null && Utils.stripAllColour(meta.getDisplayName()).equals("Buy Item!")) {
							bs.buyItem();
						} else if (meta != null && Utils.stripAllColour(meta.getDisplayName()).equals("Cancel!")) {
							bs.stop();
							return;
						}
					}
					
					
					
					
					//SELL MENU
					if (menu.getTypeID() == 4) {
						if (!cwm.sellSessions.containsKey(player.getUniqueId())) {
							player.sendMessage(Utils.formatMsg("&cCould not properly recognize your sell session please retry."));
							player.closeInventory();
							return;
						}
						SellSession ss = cwm.sellSessions.get(player.getUniqueId());
						
						//Change price
						int coins = 0;
						if (i == 9) {
							coins = 1;
						} else if (i == 10) {
							coins = 10;
						} else if (i == 11) {
							coins = 100;
						} else if (i == 12) {
							coins = 1000;
						}
						if (coins > 0) {
							if (event.getClick() == ClickType.LEFT) {
								ss.setPrice(ss.getPrice() + coins);
							} else if (event.getClick() == ClickType.RIGHT) {
								ss.setPrice(ss.getPrice() - coins);
							}
						}
						
						//Change amount
						int amt = 0;
						
						if (i == 14) {
							amt = 1;
						} else if (i == 15) {
							amt = 16;
						} else if (i == 16) {
							amt = 32;
						} else if (i == 17) {
							amt = 64;
						}
						if (amt > 0) {
							if (event.getClick() == ClickType.LEFT) {
								ss.setAmount(ss.getAmount() + amt);
							} else if (event.getClick() == ClickType.RIGHT) {
								ss.setAmount(ss.getAmount() - amt);
							}
						}
						
						//Sell item or cancel.
						if (meta != null && Utils.stripAllColour(meta.getDisplayName()).equals("Sell Item!")) {
							ss.sellItem();
						} else if (meta != null && Utils.stripAllColour(meta.getDisplayName()).equals("Cancel!")) {
							ss.stop();
							return;
						}
					}
					
					
					
					//EDIT MENU
					if (menu.getTypeID() == 5) {
						if (!cwm.editSessions.containsKey(player.getUniqueId())) {
							player.sendMessage(Utils.formatMsg("&cCould not properly recognize your edit session please retry."));
							player.closeInventory();
							return;
						}
						EditSession es = cwm.editSessions.get(player.getUniqueId());
						
						//Change price
						int coins = 0;
						if (i == 9) {
							coins = 1;
						} else if (i == 10) {
							coins = 10;
						} else if (i == 11) {
							coins = 100;
						} else if (i == 12) {
							coins = 1000;
						}
						if (coins > 0) {
							if (event.getClick() == ClickType.LEFT) {
								es.setPrice(es.getPrice() + coins);
							} else if (event.getClick() == ClickType.RIGHT) {
								es.setPrice(es.getPrice() - coins);
							}
						}
						
						//Change amount
						int amt = 0;
						
						if (i == 14) {
							amt = 1;
						} else if (i == 15) {
							amt = 16;
						} else if (i == 16) {
							amt = 32;
						} else if (i == 17) {
							amt = 64;
						}
						if (amt > 0) {
							if (event.getClick() == ClickType.LEFT) {
								es.setAmount(es.getAmount() + amt);
							} else if (event.getClick() == ClickType.RIGHT) {
								es.setAmount(es.getAmount() - amt);
							}
						}
						
						
						//Edit item or cancel.
						if (meta != null && Utils.stripAllColour(meta.getDisplayName()).equals("Edit Item!")) {
							es.editItem();
						} else if (meta != null && Utils.stripAllColour(meta.getDisplayName()).equals("Cancel!")) {
							es.stop();
							return;
						}
					}
					
					//BANK MENU
					if (menu.getTypeID() == 6) {
						if (!cwm.bankSessions.containsKey(player.getUniqueId())) {
							player.sendMessage(Utils.formatMsg("&cCould not properly recognize your bank session please retry."));
							player.closeInventory();
							return;
						}
						BankSession bs = cwm.bankSessions.get(player.getUniqueId());
						
						if (i == 8) {
							bs.addAllGold();
						} else if (meta != null && Utils.stripAllColour(meta.getDisplayName()).equals("Deposit gold!")) {
							bs.depositGold();
						} else if (meta != null && Utils.stripAllColour(meta.getDisplayName()).equals("Cancel!")) {
							bs.stop();
							return;
						}
					}
					
					return;
				}
			}
		} else {
			//ITEMS MENU (Sell items when in clicking on items in inv in ITEMS MENU)
			if (menu.getTypeID() == 2) {
				
				//Get the market
				Market market = cwm.getMarketManager().getMarket(menu.getData());
				if (market == null) {
					return;
				}
				
				ArrayList<AcceptedItem> acceptedItems = market.getItemCategories().get(Utils.stripAllColour(menu.getData2())).getAcceptedItems();
				for (AcceptedItem ai : acceptedItems) {
					//Check if item type is same as accepted item.
					if (ai.getMaterial() == current.getType()) {
						//Check if item durability(data) is same as accepted item.
						if (ai.getDurability() <= 0 || ai.getDurability() == current.getDurability()) {
							
							//Check if it's an enchanted book and that the accepted item has enchant string.
							if (ai.getEnchant() != null && current.getType() == Material.ENCHANTED_BOOK) {
								EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta)current.getItemMeta();
								//Accepting book with multiple enchants and getting book with 1.
								if (ai.getEnchant().equals("MIXED") && bookMeta.getStoredEnchants().size() <= 1) {
									continue;
								}
								//Accepting book with specified enchant and getting book with multiple enchants.
								if (!ai.getEnchant().equals("MIXED") && bookMeta.getStoredEnchants().size() > 1) {
									continue;
								}
								//Accepting book with specified enchant and enchant type doesn't match.
								if (!ai.getEnchant().equals("MIXED") && !bookMeta.getStoredEnchants().keySet().iterator().next().getName().trim().equals(ai.getEnchant().trim())) {
									continue;
								}
							}
							
							//Item match so start a sell session.
							SellSession ss = new SellSession(cwm, player, current, market, market.getItemCategories().get(Utils.stripAllColour(menu.getData2())));
							ss.start();
							return;
						}
					}
				}
				player.sendMessage(Utils.formatMsg("&cThis item doesn't belong here."));
			}
			
			//BANK MENU (Deposit gold)
			if (menu.getTypeID() == 6) {
				if (!cwm.bankSessions.containsKey(player.getUniqueId())) {
					player.sendMessage(Utils.formatMsg("&cCould not properly recognize your bank session please retry."));
					player.closeInventory();
					return;
				}
				BankSession bs = cwm.bankSessions.get(player.getUniqueId());
				if (current.getType() == Material.GOLD_BLOCK || current.getType() == Material.GOLD_INGOT || current.getType() == Material.GOLD_NUGGET) {
					bs.addGold(event.getSlot(), event.getClick());
				} else { 
					player.sendMessage(Utils.formatMsg("&cOnly gold can be deposited."));
				}
			}
			
			return;
		}
	}
	

}
