package com.clashwars.cwmarket.bukkit.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.clashwars.cwmarket.CWMarket;

public class MainEvents implements Listener {

	private CWMarket cwm;
	
	public MainEvents(CWMarket cwm) {
		this.cwm = cwm;
	}
	
	@EventHandler
	public void OnEntityInteract(PlayerInteractEntityEvent event) {
		Entity rC = event.getRightClicked();
		if (!(rC instanceof Player)) {
			return;
		}
		
		Player player = event.getPlayer();
		final Player other = (Player)event.getRightClicked();
		
		if (!player.isSneaking()) {
			return;
		}
		
		//TODO: Some way to limit requests so you can't spam ppl with it.
		
		//Send trade request if other isn't sneaking.
		if (!other.isSneaking()) {
			cwm.getTrader().addRequest(other, player);
			Bukkit.getScheduler().scheduleSyncDelayedTask(cwm.getPlugin(), new Runnable() {
				public void run() {
					cwm.getTrader().removeRequest(other);
				}
			}, 40);
			return;
		}
		
		//Both sneaking so start trade.
		cwm.getTrader().openTrade(player, other);
	}
	
	//Accept trade request by sneaking if player has a open request.
	@EventHandler
	public void OnPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		if (player.isSneaking()) {
			//cwm.getTrader().acceptRequest(player);
			cwm.getTrader().test(player);
		}
	}
	

}
