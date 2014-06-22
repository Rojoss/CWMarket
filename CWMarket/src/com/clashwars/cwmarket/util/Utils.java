package com.clashwars.cwmarket.util;

import org.bukkit.ChatColor;

public class Utils {
	//Integrate colors in a string
	public static String integrateColor(String str) {
		for (ChatColor c : ChatColor.values()) {
			str = str.replaceAll("&" + c.getChar() + "|&" + Character.toUpperCase(c.getChar()), c.toString());
		}
		return str;
	}
	
	public static String[] integrateColor(String[] str) {
		for (int i = 0; i < str.length; i++) {
			for (ChatColor c : ChatColor.values()) {
				str[i] = str[i].replaceAll("&" + c.getChar() + "|&" + Character.toUpperCase(c.getChar()), c.toString());
			}
		}
		return str;
	}
	
	
}
