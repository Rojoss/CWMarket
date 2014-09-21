package com.clashwars.cwmarket;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class ItemMenu implements Listener {
	private String					name; /* Menu name used to identify the menu */
	private int 					id; /* Used to set maxStackSize to identify the menu */
	private int						typeID; /* A custom ID which can be used to identify a menu */
	private String					data; /* Extra data */
	private String					data2; /* Extra data */
	private int						page; /* Page data */
	private int						size; /* Inventory size */
	private String					title; /* Inventory title */
	private ItemStack[]				items; /* List of menu items */

	private Set<Inventory>			openInventories	= new HashSet<Inventory>(); /* List of inventories with this menu */
	

	static Set<ItemMenu>			menus = new HashSet<ItemMenu>(); /* Static list with all menus. */

	//Create a new menu.
	public ItemMenu(String name, int size, String title) {
		this.name = name;
		this.size = size;
		this.title = title;
		this.items = new ItemStack[size];
		
		this.id = new Random().nextInt(Integer.MAX_VALUE - 64) + 64;
		
		menus.add(this);
	}

	//Get the menu name.
	public String getName() {
		return name;
	}

	//Set the menu name.
	public void setName(String name) {
		this.name = name;
	}
	
	//get the unique id
	public int getID() {
		return id;
	}
	
	//Get custom data.
	public String getData() {
		return data;
	}

	//Set extra menu data.
	public void setData(String data) {
		this.data = data;
	}
	
	//Get custom data.
	public String getData2() {
		return data2;
	}

	//Set extra menu data.
	public void setData2(String data2) {
		this.data2 = data2;
	}
	
	//Get the page number.
	public int getPage() {
		return page;
	}

	//Set the page number.
	public void setPage(int page) {
		this.page = page;
	}
	
	//Set the type ID.
	//This is only used if you want to identify a menu with the specified ID if a name is not enough.
	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}
	
	//Get the type ID.
	public int getTypeID() {
		return typeID;
	}

	//Get the inventory size.
	public int getSize() {
		return size;
	}

	//Set the inventory size.
	public void setSize(int size) {
		this.size = size;
	}

	//Get the inventory title.
	public String getTitle() {
		return title;
	}

	//Set the inventory title.
	public void setTitle(String title) {
		this.title = title;
	}

	//Get a list with all menu items.
	public ItemStack[] getItems() {
		return items;
	}

	//Get a list with all inventory's that have this menu.
	public Set<Inventory> getOpenInventories() {
		return openInventories;
	}

	//Set a menuitem at the specified slot.
	//Specify a player to only update the given item for the given player.
	//Player specific have to be set after showing the inventory.
	public void setSlot(ItemStack item, int slot, Player player) {
		this.items[slot] = item;

		for (Inventory inv : openInventories) {
			if (player == null) {
				inv.setItem(slot, item);
			} else {
				if (inv.getViewers().contains(player)) {
					inv.setItem(slot, item);
				}
			}
		}
	}

	//Show the menu to a player.
	public void show(Player player) {
		player.closeInventory();

		Inventory inv = Bukkit.createInventory(player, size, title);
		inv.setMaxStackSize(id);

		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				inv.setItem(i, items[i]);
			}
		}

		player.openInventory(inv);
		openInventories.add(inv);
	}

	
	//Listen for inventory actions.
	public static class Events implements Listener {
		
		//Click Event.
		@EventHandler(priority = EventPriority.HIGHEST)
		public void click(InventoryClickEvent event) {
			//Since ItemMenuClickEvent extends InventoryclickEvent need to make sure it's not the custom event.
			if (event instanceof ItemMenuClickEvent) {
				return;
			}
			
			Player player = (Player) event.getWhoClicked();
			Inventory inv = event.getInventory();
			
			//Loop through all menus.
			for (ItemMenu menu : menus) {
				//Check if the clicked inventory is the current menu.
				if (!inv.getTitle().equals(menu.getTitle()) || inv.getSize() != menu.getSize() || !inv.getHolder().equals(player) || inv.getMaxStackSize() != menu.getID()) {
					continue;
				}
				
				//Call custom ItemMenuclickEvent
				ItemMenuClickEvent e = new ItemMenuClickEvent(event.getView(), event.getSlotType(), event.getRawSlot(), event.getClick(), event.getAction(), menu);
				Bukkit.getServer().getPluginManager().callEvent(e);
				
				if (e.isCancelled()) {
					event.setCancelled(true);
				}
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void close(InventoryCloseEvent event) {
			Player player = (Player) event.getPlayer();
			Inventory inv = event.getInventory();

			for (ItemMenu menu : menus) {
				if (!inv.getTitle().equals(menu.getTitle()) || inv.getSize() != menu.getSize() || !inv.getHolder().equals(player)) {
					continue;
				}

				menu.getOpenInventories().remove(inv);
			}
		}
	}
	
	//Custom ItemMenuClickEvent.
	public static class ItemMenuClickEvent extends InventoryClickEvent implements Cancellable {
		private boolean cancelled;
		private ItemMenu menu;
		
	    public ItemMenuClickEvent(InventoryView view, SlotType type, int slot, ClickType click, InventoryAction action, ItemMenu menu) {
	    	super(view, type, slot, click, action);
	    	this.menu = menu;
	    }
	    
	    public ItemMenu getItemMenu() {
	    	return menu;
	    }
	    
	    public boolean isCancelled() {
	        return cancelled;
	    }
	 
	    public void setCancelled(boolean cancel) {
	        cancelled = cancel;
	    }
	}

	//Get a list with all item menus.
	public static Set<ItemMenu> getMenus() {
		return menus;
	}
}
