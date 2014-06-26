package com.clashwars.cwmarket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

public class ItemMenu implements Listener {
	private String					customName;
	private int						size;
	private String					title;
	private Entry[]					entries;
	private Map<String, Long>		cooldowns		= new HashMap<String, Long>();

	private Set<Inventory>			openInventories	= new HashSet<Inventory>();

	private final int				UUID;
	static Set<ItemMenu>	menus			= new HashSet<ItemMenu>();

	public ItemMenu(String customName, int size, String title) {
		this.customName = customName;
		this.size = size;
		this.title = title;
		this.entries = new Entry[size];
		this.UUID = new Random().nextInt(Integer.MAX_VALUE);

		menus.add(this);
	}

	public String getCustomName() {
		return customName;
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getUUID() {
		return UUID;
	}

	public Entry[] getEntries() {
		return entries;
	}

	public Map<String, Long> getCooldowns() {
		return cooldowns;
	}

	public Set<Inventory> getOpenInventories() {
		return openInventories;
	}

	public void setSlot(Entry entry, int slot) {
		this.entries[slot] = entry;

		for (Inventory inv : openInventories) {
			inv.setItem(slot, entry.getItem());
		}
	}

	public void setSlot(ItemStack stack, int cooldown, String executionScript, int slot) {
		setSlot(new Entry(stack, cooldown, executionScript), slot);
	}

	public void show(Player player) {
		player.closeInventory();

		Inventory inv = Bukkit.createInventory(player, size, title);
		inv.setMaxStackSize(UUID);

		for (int i = 0; i < entries.length; i++) {
			if (entries[i] != null) {
				inv.setItem(i, entries[i].getItem());
			}
		}

		player.openInventory(inv);
		openInventories.add(inv);
	}

	public static class Events implements Listener {
		@SuppressWarnings("deprecation")
		@EventHandler(priority = EventPriority.HIGHEST)
		public void click(InventoryClickEvent event) {
			Player player = (Player) event.getWhoClicked();
			Inventory inv = event.getInventory();

			for (ItemMenu menu : menus) {
				if (!inv.getTitle().equals(menu.getTitle()) || inv.getSize() != menu.getSize() || !inv.getHolder().equals(player)
						|| inv.getMaxStackSize() != menu.getUUID()) {
					continue;
				}

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

	public class Entry {
		private ItemStack	item;
		private int			cooldown;
		private String		executionScript;

		public Entry(ItemStack item, int cooldown, String executionScript) {
			this.item = item;
			this.cooldown = cooldown;
			this.executionScript = executionScript;
		}

		public ItemStack getItem() {
			return item;
		}

		public void setItem(ItemStack item) {
			this.item = item;
		}

		public int getCooldown() {
			return cooldown;
		}

		public void setCooldown(int cooldown) {
			this.cooldown = cooldown;
		}

		public String getExecutionScript() {
			return executionScript;
		}

		public void setExecutionScript(String executionScript) {
			this.executionScript = executionScript;
		}
	}

	public static Set<ItemMenu> getMenus() {
		return menus;
	}
}
