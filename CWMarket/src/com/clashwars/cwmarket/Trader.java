package com.clashwars.cwmarket;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.clashwars.cwmarket.util.Utils;

public class Trader {
	
	private CWMarket cwm;
	private Map<String, ItemMenu> menus = new HashMap<String, ItemMenu>();
	
	Map <Player, Player> requests = new HashMap<Player, Player>();
	
	
	public Trader(CWMarket cwm) {
		this.cwm = cwm;
	}


	public void openTrade(Player player, Player other) {
		ItemMenu im = null;
		String name = player.getName() + other.getName();
		if (menus.containsKey(name)) {
			im = menus.get(name);
		} else {
			im = new ItemMenu(name, cwm.getConfig().getTradeSize(), "Trade: " + player.getName() + "<~>" + other.getName());
			menus.put(name, im);
		}
		im.show(player);
		im.show(other);
	}
	
	public void test(Player player) {
		ItemMenu im = null;
		String name = player.getName();
		if (menus.containsKey(name)) {
			Bukkit.broadcastMessage("Reusing menu");
			im = menus.get(name);
		} else {
			Bukkit.broadcastMessage("Creating new menu");
			im = new TradeMenu(name, cwm.getConfig().getTradeSize(), "Trade: " + player.getName() + "<~>" + player.getName());
			menus.put(name, im);
		}
		im.show(player);
	}
	
	
	
	public void addRequest(Player other, Player player) {
		if (requests.containsKey(other)) {
			player.sendMessage(Utils.integrateColor("&8[&4CW&8] &4" + other.getName() + " &calready has a open trade request!"));
		} else {
			requests.put(other, player);
			player.sendMessage(Utils.integrateColor("&8[&4CW&8] &6Trade request send."));
			other.sendMessage(Utils.integrateColor("&8[&4CW&8] &5" + player.getName() + " &6would like to trade with you."));
			other.sendMessage(Utils.integrateColor("&8[&4CW&8] &6Crouch within 2 seconds to accept!"));
		}
	}

	public void removeRequest(Player other) {
		if (requests.containsKey(other)) {
			requests.remove(other);
			other.sendMessage(Utils.integrateColor("&8[&4CW&8] &cTrade request cancelled."));
		}
	}

	public void acceptRequest(Player other) {
		if (requests.containsKey(other)) {
			openTrade(requests.get(other), other);
			requests.remove(other);
		}
	}

}
