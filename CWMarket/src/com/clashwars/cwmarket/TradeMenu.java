package com.clashwars.cwmarket;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TradeMenu extends ItemMenu {

	public TradeMenu(String customName, int size, String title) {
		super(customName, size, title);
	}
	
	public static class Events implements Listener {
		@SuppressWarnings("deprecation")
		@EventHandler(priority = EventPriority.HIGHEST)
		public void click(InventoryClickEvent event) {
			Player player = (Player) event.getWhoClicked();
			Inventory inv = event.getInventory();
			Bukkit.broadcastMessage("Click inv!");

			for (ItemMenu menu : menus) {
				if (!inv.getTitle().equals(menu.getTitle()) || inv.getSize() != menu.getSize() || !inv.getHolder().equals(player)
						|| inv.getMaxStackSize() != menu.getUUID()) {
					continue;
				}
				Bukkit.broadcastMessage("Click trade screen!");

				int raw = event.getRawSlot();
				boolean top = raw <= menu.getSize();

				if (top) {
					ItemStack current = event.getCurrentItem();

					if (current == null || current.getTypeId() == 0) {
						return;
					}

					for (int i = 0; i < menu.getEntries().length; i++) {
						Entry entry = menu.getEntries()[i];

						if (entry != null && i == raw) {
							if (entry.getCooldown() <= -1 || !menu.getCooldowns().containsKey(player.getName())
									|| menu.getCooldowns().get(player.getName()) + entry.getCooldown() < System.currentTimeMillis()) {
								if (entry.getExecutionScript() != null && !entry.getExecutionScript().trim().isEmpty()) {
									Bukkit.dispatchCommand(Bukkit.getConsoleSender(), entry.getExecutionScript().replace("%PLAYER%", player.getName()));
								}

								if (entry.getCooldown() > -1) {
									menu.getCooldowns().put(player.getName(), System.currentTimeMillis());
								}
							}

							event.setCancelled(true);
							event.setResult(Result.DENY);
							event.setCursor(null);
							player.updateInventory();
							return;
						}
					}
				} else {
					event.setCancelled(true);
					event.setResult(Result.DENY);
					event.setCursor(null);
					player.updateInventory();
				}
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void close(InventoryCloseEvent event) {
			Player player = (Player) event.getPlayer();
			Inventory inv = event.getInventory();

			for (ItemMenu menu : menus) {
				if (!inv.getTitle().equals(menu.getTitle()) || inv.getSize() != menu.getSize() || !inv.getHolder().equals(player)
						|| inv.getMaxStackSize() != menu.getUUID()) {
					continue;
				}

				menu.getOpenInventories().remove(inv);
			}
		}
	}

}
