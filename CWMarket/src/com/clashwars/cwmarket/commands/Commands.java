package com.clashwars.cwmarket.commands;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.clashwars.cwmarket.CWMarket;
import com.clashwars.cwmarket.market.BuySession;
import com.clashwars.cwmarket.market.ItemCategory;
import com.clashwars.cwmarket.market.ItemEntry;
import com.clashwars.cwmarket.market.Market;
import com.clashwars.cwmarket.util.Utils;

public class Commands {
	private CWMarket			cwm;
	private final List<Method>	commands	= new ArrayList<Method>();

	public Commands(CWMarket cwm) {
		this.cwm = cwm;
	}

	/* Start of commands */

	@Command(permissions = {}, aliases = { "market", "ma", "shop" })
	public boolean market(CommandSender sender, String label, String argument, String... args) {
		
		if (args.length < 1) {
			sender.sendMessage(Utils.integrateColor("&8&l===== &4&lCommand help for &6&l/" + label + " &8&l====="));
			sender.sendMessage(Utils.integrateColor("&6/" + label + " open <market> [player] &7- &8Open a market."));
			sender.sendMessage(Utils.integrateColor("&6/" + label + " buy <ID> &7- &8Start a buy session for the specified item."));
			sender.sendMessage(Utils.integrateColor("&6/" + label + " infinite &7- &8Toggle infinite mode on/off."));
			sender.sendMessage(Utils.integrateColor("&6/" + label + " reload &7- &8Reload config."));
			return true;
		}
		
		UUID uuid = null;
		
		
		/*/market open <name> [player] */
		if (args.length >= 1 && args[0].equalsIgnoreCase("open")) {
			
			//Permission check
			if (!sender.hasPermission("cwmarket.*") && !sender.hasPermission("cwmarket.open") && !sender.isOp()) {
				sender.sendMessage(Utils.formatMsg("&cInsufficient permissions."));
				return true;
			}
			
			//Get the market
			Market market = null;
			if (args.length >= 1) {
				market = cwm.getMarketManager().getMarket(args[1].toLowerCase());
			}
			if (market == null) {
				sender.sendMessage(Utils.formatMsg("&cNo market found with the name &4" + args[1].toLowerCase() + "&c."));
				return true;
			}
			
			
			//Console check
			if (args.length < 3) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(Utils.formatMsg("&cSpecify a player or run as a player."));
					return true;
				}
				uuid = ((Player) sender).getUniqueId();
			} else {
				//Other player specified.
				if (!sender.hasPermission("cwmarket.*") && !sender.hasPermission("cwmarket.open.other") && !sender.isOp()) {
					sender.sendMessage(Utils.formatMsg("&cInsufficient permissions."));
					return true;
				}
				Player player = cwm.getServer().getPlayer(args[2]);
				uuid = (player != null ? player.getUniqueId() : UUID.fromString(args[1]));
			}
			
			if (uuid == null) {
				sender.sendMessage(Utils.formatMsg("&cInvalid player."));
				return true;
			}
			
			//Open the market.
			market.openForPlayer(uuid);
			return true;
		}
		
		
		/* /market infinite */
		if (args.length >= 1 && args[0].equalsIgnoreCase("infinite")) {
			
			//Permission check
			if (!sender.hasPermission("cwmarket.*") && !sender.hasPermission("cwmarket.infinite") && !sender.isOp()) {
				sender.sendMessage(Utils.formatMsg("&cInsufficient permissions."));
				return true;
			}
			
			//Console check
			if (!(sender instanceof Player)) {
				sender.sendMessage(Utils.formatMsg("&cPlayer command only."));
				return true;
			}
			uuid = ((Player) sender).getUniqueId();
			
			//Toggle mode.
			if (!cwm.infinite.contains(uuid)) {
				cwm.infinite.add(uuid);
				sender.sendMessage(Utils.formatMsg("&6Infinite mode toggled &aON&6."));
			} else {
				cwm.infinite.remove(uuid);
				sender.sendMessage(Utils.formatMsg("&6Infinite mode toggled &cOFF&6."));
			}
			
			return true;
		}
		
		
		
		
		/* /market buy <ID> */
		if (args.length >= 1 && args[0].equalsIgnoreCase("buy")) {
			
			//Console check
			if (!(sender instanceof Player)) {
				sender.sendMessage(Utils.formatMsg("&cPlayer command only."));
				return true;
			}
			Player player = (Player)sender;
			uuid = ((Player) sender).getUniqueId();
			
			if (args.length < 2) {
				sender.sendMessage(Utils.formatMsg("&cYou have to specify a itemID"));
			}
			
			int id = 0;
			try{
				id = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(Utils.formatMsg("&cInvalid ID."));
				return true;
			}
			int foundID = 0;
			String marketName = "";
			String itemCategoryName = "";
			try {
				Statement statement = cwm.getSql().createStatement();
				ResultSet res = statement.executeQuery("SELECT * FROM " + cwm.marketItemsTable + " WHERE ID=" + id + ";");
				res.next();
				foundID = res.getInt("ID");
				marketName = res.getString("Market");
				itemCategoryName = res.getString("ItemCategory");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			if (foundID <= 0) {
				sender.sendMessage(Utils.formatMsg("&cThis item is no longer available."));
				return true;
			}
			
			Market market = cwm.getMarketManager().getMarket(marketName);
			if (market == null) {
				sender.sendMessage(Utils.formatMsg("&cCould not properly load the market."));
				return true;
			}
			ItemCategory itemCat = cwm.getMarketManager().getItemCategory(market, Utils.stripAllColour(itemCategoryName));
			if (itemCat == null) {
				sender.sendMessage(Utils.formatMsg("&cCould not properly load the item category."));
				return true;
			}
			
			ItemEntry itemEntry = itemCat.getItem(foundID);
			if (itemEntry == null) {
				sender.sendMessage(Utils.formatMsg("&cCould not properly load the item entry."));
				return true;
			}
			
			if (itemEntry.isReserved()) {
				sender.sendMessage(Utils.formatMsg("&cThis item is reserved."));
			}
			
			BuySession bs = new BuySession(cwm, player, itemEntry);
			bs.start();
			return true;
		}
		
		
		
		/*/market reload */
		if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
			//Permission check
			if (!sender.hasPermission("cwmarket.*") && !sender.hasPermission("cwmarket.reload") && !sender.isOp()) {
				sender.sendMessage(Utils.formatMsg("&cInsufficient permissions."));
				return true;
			}
			cwm.getPluginConfig().load();
			sender.sendMessage(Utils.formatMsg("Reloaded."));
		}
		
		sender.sendMessage(Utils.formatMsg("&cInvalid command argument."));
		return false;
	}

	
	
	
	/* End of commands */
	
	
	
	public void populateCommands() {
		commands.clear();

		for (Method method : getClass().getMethods()) {
			if (method.isAnnotationPresent(Command.class) && method.getReturnType().equals(boolean.class)) {
				commands.add(method);
			}
		}
	}

	public boolean executeCommand(CommandSender sender, String lbl, String... args) {
		try {
			for (Method method : commands) {
				Command command = method.getAnnotation(Command.class);
				String[] permissions = command.permissions();
				String[] aliases = command.aliases();
				String[] saliases = command.secondaryAliases();

				for (String alias : aliases) {
					if (alias.equalsIgnoreCase(lbl)) {
						if ((saliases == null || saliases.length <= 0)) {
							check: if (!sender.isOp() && permissions != null && permissions.length > 0) {
								for (String p : permissions) {
									if (sender.hasPermission(p)) {
										break check;
									}
								}

								sender.sendMessage(Utils.formatMsg("Insufficient permissions."));
								return true;
							}

							return (Boolean) method.invoke(this, sender, lbl, null, args);
						}

						if (args.length <= 0) {
							continue;
						}

						for (String salias : saliases) {
							if (salias.equalsIgnoreCase(args[0])) {
								check: if (!sender.isOp() && permissions != null && permissions.length > 0) {
									for (String p : permissions) {
										if (sender.hasPermission(p)) {
											break check;
										}
									}

									sender.sendMessage(Utils.formatMsg("Insufficient permissions."));
									return true;
								}

								return (Boolean) method.invoke(this, sender, lbl, args[0], Utils.trimFirst(args));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
