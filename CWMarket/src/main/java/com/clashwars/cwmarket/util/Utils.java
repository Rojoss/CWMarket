package com.clashwars.cwmarket.util;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;

public class Utils {
	
	//Format a message
	public static String formatMsg(String msg) {
		return integrateColor("&8[&4CW Market&8] &6" + msg);
	}
	
	
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
	
	public static String stripAllColour(String str) {
		return ChatColor.stripColor(integrateColor(str));
	}
	
	public static String removeColour(String str) {
		for (ChatColor c : ChatColor.values()) {
			str = str.replace(c.toString(), "&" + c.getChar());
		}

		return str;
	}
	
	
	//Trim first string from string array
	public static String[] trimFirst(String[] arr) {
		String[] ret = new String[arr.length - 1];

		for (int i = 1; i < arr.length; i++) {
			ret[i - 1] = arr[i];
		}

		return ret;
	}

	//Convert string to lore.
	public static List<String> loreFromString(String loreStr) {
		List<String> lore = null;
		loreStr = integrateColor(loreStr);
		String[] split = loreStr.split("\n");
		lore = Arrays.asList(split);
		return lore;
	}


	public static String integrateUnixColor(String name) {
		return name;
	}
	
}
