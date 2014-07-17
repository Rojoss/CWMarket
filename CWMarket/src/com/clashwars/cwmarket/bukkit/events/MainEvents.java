package com.clashwars.cwmarket.bukkit.events;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.clashwars.cwmarket.CWMarket;
import com.clashwars.cwmarket.ItemMenu;
import com.clashwars.cwmarket.ItemMenu.ItemMenuClickEvent;
import com.clashwars.cwmarket.market.AcceptedItem;
import com.clashwars.cwmarket.market.BuySession;
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
        	if (cwm.getMarketManager().getMarket(Utils.stripAllColour(npc.getName()).toLowerCase()) != null) {
            	cwm.getMarketManager().getMarket(Utils.stripAllColour(npc.getName()).toLowerCase()).openForPlayer(p.getUniqueId());
            }
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
						
						
						//Click on a available item. (try buy it)
						if (i > 8 && i < 52) {
							int id;
							if ((id = ItemUtils.getIDFromLore(current)) >= 0) {
								ItemEntry itemEntry = itemCat.getItem(id);
								
								if (cwm.getEconomy().getBalance(player.getName()) >= itemEntry.getPrice()) {
									BuySession bs = new BuySession(cwm, player, itemEntry);
									cwm.buySessions.put(player.getUniqueId(), bs);
									bs.start();
								} else {
									player.sendMessage(Utils.formatMsg("&cNot enough coins to buy this."));
								}
								
								return;
							} else {
								player.sendMessage(Utils.formatMsg("&cCould not properly recognize this item."));
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
						
						if (i == 8 && bs.getItemEntry().isInf()) {
							if (event.getClick() == ClickType.LEFT) {
								if (bs.getQuantity() < current.getMaxStackSize()) {
									bs.setQuantity(current.getAmount() + 1);
								}
							} else if (event.getClick() == ClickType.RIGHT) {
								if (bs.getQuantity() > 1) {
									bs.setQuantity(current.getAmount() - 1);
								}
							}
						}
						
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
						int add = 0;
						
						if (i == 11) {
							add = 1;
						} else if (i == 12) {
							add = 10;
						} else if (i == 13) {
							add = 100;
						} else if (i == 14) {
							add = 1000;
						} else if (i == 15) {
							add = 10000;
						} else if (meta != null && Utils.stripAllColour(meta.getDisplayName()).equals("Sell Item!")) {
							ss.sellItem();
						} else if (meta != null && Utils.stripAllColour(meta.getDisplayName()).equals("Cancel!")) {
							ss.stop();
							return;
						}
						
						if (add > 0) {
							if (event.getClick() == ClickType.LEFT) {
								ss.setPrice(ss.getPrice() + add);
							} else if (event.getClick() == ClickType.RIGHT) {
								ss.setPrice(ss.getPrice() - add);
							}
							if (ss.getPrice() < 0) {
								ss.setPrice(0);
							}
						}
					}
					
					
					
					//UPDATE MENU
					
					
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
							SellSession ss = new SellSession(cwm, player, current, event.getSlot(), market, market.getItemCategories().get(Utils.stripAllColour(menu.getData2())));
							cwm.sellSessions.put(player.getUniqueId(), ss);
							ss.start();
							return;
						}
					}
				}
				player.sendMessage(Utils.formatMsg("&cThis item doesn't belong here."));
			}
			return;
		}
	}
	

}
